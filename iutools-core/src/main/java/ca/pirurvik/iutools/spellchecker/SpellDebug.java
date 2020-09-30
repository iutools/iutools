package ca.pirurvik.iutools.spellchecker;

import java.util.*;

import ca.nrc.datastructure.Pair;
import ca.nrc.string.diff.DiffResult;

public class SpellDebug {

	// List of SpellChecker methods that need to be traced
	//
	private static Set<String> methodsToTrace = null;
	static {
		methodsToTrace = new HashSet<String>();
		methodsToTrace.add("SpellChecker.correctWord");
	}


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
			.put("inuktigtut",
				new String[] {
					"inuktitut",
					"inuktut",
					"inukttut",
					"inukutt",
					"inuk"
				});
	}

	private static Set<String> candidatesToTrace = null;
	static {
//		candidatesToTrace = new HashSet<String>();
//		candidatesToTrace.add("nunavut");
	}

	private static Set<String> ngramsToTrace = null;
	static {
//		ngramsToTrace = new HashSet<String>();
//		ngramsToTrace.add("navu");
	}

	private static Pair<Boolean,String> traceStatus(String method, String badWord,
			String candidate, String ngram) {

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
				(badWordsToTrace == null ||
					badWordsToTrace.containsKey(badWord));
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

		// Should this candidate be traced?
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


	public static void containsCorrection(String who, String what,
			String badWord, String ngram,
			Set<String> possibleSpellings) {
		Pair<Boolean,String> status = traceStatus(who, badWord, (String) null, ngram);
		if (status.getFirst()) {
			String[] suggestedCorrections =
				badWordsToTrace == null ? null: badWordsToTrace.get(badWord);

			if (suggestedCorrections != null) {
				Set<String> missingSuggestions = new HashSet<String>();
				Collections.addAll(missingSuggestions, suggestedCorrections);

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
						"   "+String.join(", ", missingSuggestions)
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
		Pair<Boolean,String> status = traceStatus(who, badWord, (String) null, ngram);
		if (status.getFirst()) {
			String[] suggestedCorrections =
					badWordsToTrace == null ? null : badWordsToTrace.get(badWord);

			if (suggestedCorrections != null) {
				Set<String> missingSuggestions = new HashSet<String>();
				Collections.addAll(missingSuggestions, suggestedCorrections);

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

	public static boolean traceIsActive(String who, String badWord) {
		return traceIsActive(who, badWord, null);
	}

	public static boolean traceIsActive(String who, String badWord, String candidate) {
		if (candidate == null) {
			candidate = "*";
		}
		boolean isActive = traceStatus(who, badWord, candidate, null).getFirst();
		
		return isActive;
	}
}
