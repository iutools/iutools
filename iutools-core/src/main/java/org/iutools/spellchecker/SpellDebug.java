package org.iutools.spellchecker;

import java.util.*;


import ca.nrc.datastructure.Pair;
import ca.nrc.dtrc.stats.FrequencyHistogram;
import ca.nrc.string.diff.DiffResult;

import org.apache.log4j.Logger;

public class SpellDebug {

	// Set to true to deactivate all traces
	private static final boolean debugActive;

	static {
		try {
			debugActive =
				Logger.getLogger("org.iutools.spellchecker.SpellDebug")
				.isTraceEnabled();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// List of SpellChecker methods that need to be traced
	// If null, trace them all.
	// If empty, trace none
	//
	private static Set<String> methodsToTrace = new HashSet<String>();
	static {
//		methodsToTrace = null;
//		methodsToTrace = new HashSet<String>();
		methodsToTrace.add("SpellChecker.correctWord");
		methodsToTrace.add("SpellChecker.firstPassCandidates_TFIDF");
		methodsToTrace.add("SpellChecker.candidatesWithBestNGramsMatch");
		methodsToTrace.add("SpellChecker.computeCandidateSimilarity");
		methodsToTrace.add("SpellChecker.computeCandidateSimilarity");
		methodsToTrace.add("IUDiffCosting.cost");
		methodsToTrace.add("IUDiffCosting.costFirstMorphemeChange");
		methodsToTrace.add("IUSpellingDistance.distance");
	}

    // If this is not-null, then when a trace does not provide the word
    // being corrected, we assume it is the value of assumeBadWordIs
    //
//    private static String assumeBadWordIs = null;
//	private static String assumeBadWordIs = "kiinaujatigut";

    // - Keys are the misspelled words to trace
	// - Values are the ordered list of suggested corrections that you expect
	//   to get for those words
	//
	// If left at null, then all misspelled words will be traced, but
	// we won't be able to trace whether or not the correct spelling was found
	// at a given stage.
	//
	private static Map<String,String[]> badWordsToTrace = null;
	static {
		badWordsToTrace = new HashMap<String,String[]>();
		badWordsToTrace
			.put("nunavuumit",
				new String[] {"nunavummit"});
	}

	// List of candidate spellings to be traced.
	// If null, then trace all of them
	// If empty, trace none
	//
	private static Set<String> candidatesToTrace = null;
	static {
		candidatesToTrace = new HashSet<String>();
		candidatesToTrace.add("nunavuumit");
		candidatesToTrace.add("nunavummit");
	}

	// List of ngrams to be traced.
	// If nulll, then trace all of them.
	// If empty, trace none
	//
	private static Set<String> ngramsToTrace = null;
	static {
		ngramsToTrace = new HashSet<String>();
		ngramsToTrace.add("laut");
	}

	private static Map<String,String[]> badWordsToTraceNormalized = null;

	private static Map<String,String[]> getBadWordsToTrace() {
		if (badWordsToTraceNormalized == null) {
			if (badWordsToTrace != null) {
				badWordsToTraceNormalized = new HashMap<String, String[]>();
				for (Map.Entry<String, String[]> anEntry : badWordsToTrace.entrySet()) {
					String[] suggestions = anEntry.getValue();
					for (int ii = 0; ii < suggestions.length; ii++) {
						suggestions[ii] = normalizeNumerical(suggestions[ii]);
						badWordsToTraceNormalized.put(anEntry.getKey(), suggestions);
					}
				}
			}
		}
		return badWordsToTraceNormalized;
	}

	private static Pair<Boolean,String> traceStatus(String method, String badWord,
			String candidate, String ngram) {

		if (!debugActive) {
			return Pair.of(false, "None");
		}

		Boolean traceIsOn = null;
		String traceID = null;

		// Should this method be traced?
		//
		boolean methodShouldBeTraced  =
			(methodsToTrace == null ||
				methodsToTrace.contains(method));

		// Should this badWord be traced?
		//
		boolean badWordShouldBeTraced = true;
		String badWordTraceID = "*";
		if (badWord != null) {
			badWordShouldBeTraced =
				(getBadWordsToTrace() == null ||
					getBadWordsToTrace().containsKey(badWord));
			badWordTraceID = badWord;
		}

		// Should this candidate be traced?
		//
		boolean candShouldBeTraced = true;
		String candTraceID = "*";
		if (candidate != null) {
			candShouldBeTraced =
				(candidatesToTrace == null ||
						candidatesToTrace.contains(candidate));
			candTraceID = candidate;
		}

		// Should this ngram be traced?
		//
		boolean ngramShouldBeTraced = true;
		String ngramTraceID = "*";
		if (ngram != null) {
			ngramShouldBeTraced =
				(ngramsToTrace == null ||
					ngramsToTrace.contains(ngram));
			ngramTraceID = ngram;
		}

		traceIsOn =
			(methodShouldBeTraced && badWordShouldBeTraced &&
				candShouldBeTraced && ngramShouldBeTraced);
		traceID = "badWord="+badWordTraceID+"/candidate="+candTraceID+"/ngram="+ngramTraceID;

		return Pair.of(traceIsOn, traceID);
	}

	public static void trace(String who, String mess,
			String badWord, String candidate) {
		trace(who, mess, badWord, candidate, null);
	}

	public static void trace(String who, String mess, String badWord,
		 String candidate, String ngram) {
		Pair<Boolean,String> status = traceStatus(who, badWord, candidate, ngram);
		if (status.getFirst()) {
			System.out.println("-- "+who+"("+status.getSecond()+"):\n   "+mess);
		}
	}

	public static void trace(String who, String mess, DiffResult diff) {
		Pair<Boolean,String> status = traceStatus(who, diff.origStr(), diff.revStr(), null);
		if (status.getFirst()) {
			System.out.println("-- "+who+"("+status.getSecond()+"):\n   "+mess);			
		}
	}

	public static void containsCorrection(String who, String what,
		String badWord, Set<String> scoredSpellings) {
		containsCorrection(who, what, badWord, (String)null,
				scoredSpellings);
	}


	public static void
	containsCorrection(String who, String what,
			String badWord, String ngram,
			Set<String> possibleSpellings) {
		Pair<Boolean,String> status = traceStatus(who, badWord, (String) null, ngram);
		if (status.getFirst()) {
			String[] correctSpellings =
                correctSpellingsFor(badWord);

			if (correctSpellings != null) {
				Set<String> missingSuggestions = new HashSet<String>();
				Collections.addAll(missingSuggestions, correctSpellings);

				for (String spelling : possibleSpellings) {
					if (missingSuggestions.contains(spelling)) {
						missingSuggestions.remove(spelling);
					}
					if (missingSuggestions.isEmpty()) {
						break;
					}
				}

				String missing_or_not = " was NOT missing ANY suggested corrections";
				if (!missingSuggestions.isEmpty()) {
					missing_or_not =
						" WAS missing the following suggested corrections:\n"+
						"   "+String.join(", ", missingSuggestions)+"\n"+
						"   Total candidates in the list = "+possibleSpellings.size()
					;
				}

				System.out.println("-- " + who +
						"(" + status.getSecond() +
						"): \n   " + what + missing_or_not + ".");
			}
		}
	}

	public static void containsCorrection(String who, String what,
		String badWord, Collection<ScoredSpelling> possibleSpellings) {
		containsCorrection(who, what, badWord, (String)null, possibleSpellings);
	}

	public static void containsCorrection(String who, String what,
		  String badWord, String ngram,
		  Collection<ScoredSpelling> possibleSpellings) {
		String normalizedBadWord = normalizeNumerical(badWord);
		Pair<Boolean,String> status = traceStatus(who, badWord, (String) null, ngram);
		if (status.getFirst()) {
			String[] correctSpellings =
                correctSpellingsFor(badWord);

			if (correctSpellings != null) {
				Set<String> missingSuggestions = new HashSet<String>();
				Collections.addAll(missingSuggestions, correctSpellings);

				for (ScoredSpelling scoredSpelling : possibleSpellings) {
					if (missingSuggestions.contains(scoredSpelling.spelling)) {
						missingSuggestions.remove(scoredSpelling.spelling);
					}
					if (missingSuggestions.isEmpty()) {
						break;
					}
				}

				String missing_or_not = " was NOT missing ANY suggested corrections";
				if (!missingSuggestions.isEmpty()) {
					missing_or_not =
							" WAS missing the following suggested corrections:\n" +
									"   " + String.join(", ", missingSuggestions)
					;
				}

				System.out.println("-- " + who +
						"(" + status.getSecond() +
						"): \n   " + what + missing_or_not + ".");
			}
		}
	}

	public static String[] correctSpellingsFor(String badWord) {
		String[] correctSpellings = null;
//		if (badWord == null && assumeBadWordIs != null) {
		if (badWord == null && badWordsToTrace.size() == 1) {
			badWord = badWordsToTrace.keySet().iterator().next();
		}
		if (badWord != null) {
            correctSpellings = getBadWordsToTrace().get(badWord);
		}

		return correctSpellings;
	}

    public static void containsNgramsToTrace(String who, String what,
             String badWord, String ngram, Pair<String, Double>[] ngramFreqs) {

        if (ngramsToTrace != null) {
            Pair<Boolean, String> status = traceStatus(who, badWord, (String) null, ngram);
            if (status.getFirst()) {
                Set<String> missingNgrams = new HashSet<String>();
                missingNgrams.addAll(ngramsToTrace);

                Set<String> gotNgrams = new HashSet<String>();
                for (Pair<String,Double> aNgramFreq: ngramFreqs) {
                    gotNgrams.add(aNgramFreq.getFirst());
                }

                for (String aNgram : ngramsToTrace) {
                    if (gotNgrams.contains(aNgram)) {
                        missingNgrams.remove(aNgram);
                    }
                    if (missingNgrams.isEmpty()) {
                        break;
                    }
                }

                String missing_or_not = " was NOT missing ANY of the ngrams";
                if (!missingNgrams.isEmpty()) {
                    missing_or_not =
                            " WAS missing the following ngrams:\n" +
                                    "   " + String.join(", ", missingNgrams)
                    ;
                }

                System.out.println("-- " + who +
                        "(" + status.getSecond() +
                        "): \n   " + what + missing_or_not + ".");
            }
        }
    }

    public static void traceNgrams(String who, String mess, String badWord,
       String ngram, Pair<String, Double>[] ngramsIDF) {

        Pair<Boolean, String> status = traceStatus(who, badWord, (String) null, ngram);
        if (traceIsActive(who, badWord)) {
            String ngrams = "";
            for (Pair<String, Double> aNgramIDF: ngramsIDF) {
                if (!ngrams.equals("")) {
					ngrams += "\n      ";
				}
                ngrams += aNgramIDF.getFirst()+": "+aNgramIDF.getSecond();
            }

            trace(who,
                mess+"\n   Ngrams are:\n"+ngrams,
                badWord, ngram);
        }

    }


	public static boolean traceIsActive(String who, String badWord) {
		return traceIsActive(who, badWord, null);
	}

	public static boolean traceIsActive(String who, String badWord, String candidate) {
		boolean isActive = traceStatus(who, badWord, candidate, null).getFirst();
		
		return isActive;
	}

	protected static String normalizeNumerical(String orig) {

		String normalized = null;
		if (orig != null) {
			normalized = orig.replaceAll("(\\d+-*)", "0000");
		}
		return normalized;
	}

	public static void containsDuplicates(
		String who, String what, String badWord,
		Collection<ScoredSpelling> candidates) {
		Pair<Boolean, String> status =
			traceStatus(who, badWord, (String) null, (String)null);
		if (status.getFirst()) {
			FrequencyHistogram<String> histogram =
				new FrequencyHistogram<String>();
			for (ScoredSpelling aCandidate: candidates) {
				String word = aCandidate.spelling;
				histogram.updateFreq(word);
			}
			int numDups = 0;
			String mess = "The following duplicates were found in "+what+": ";
			for (String word: histogram.allValues()) {
				if (histogram.frequency(word) > 1) {
					numDups++;
					mess += word+", ";
				}
			}
			if (numDups > 0) {
				mess += "\nTotal dups = " + numDups;
				System.out.println("-- " + who + "(" + status.getSecond() + "):\n   " + mess);
			}
		}
	}
}
