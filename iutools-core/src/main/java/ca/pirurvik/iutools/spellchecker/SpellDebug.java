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
	// - Values are the correct spelling for those words
	//
	// If left at null, then all misspelled words will be traced, but
	// we won't be able to trace whether or not the correct spelling was found
	// at a given stage.
	//
	private static Map<String,String> badWordsToTrace = null;
	static {
		badWordsToTrace = new HashMap<String,String>();
		badWordsToTrace.put("ninavut", "nunavut");
	}

	private static Map<String,String> getBadWordsToTrace() {
		if (badWordsToTrace == null) {
			badWordsToTrace = new HashMap<String,String>();
		}

		return badWordsToTrace;
	}

	private static Set<String> candidatesToTrace = null;
	static {
//		candidatesToTrace = new HashSet<String>();
//		candidatesToTrace.add("nunavut");
	}

	private static Set<String> getCandidatesToTrace() {
		if (candidatesToTrace == null) {
			candidatesToTrace = new HashSet<String>();
		}
		return candidatesToTrace;
	}

	private static Set<String> ngramsToTrace = null;
	static {
//		ngramsToTrace = new HashSet<String>();
//		ngramsToTrace.add("navu");
	}

	private static Set<String> getNgramsToTrace() {
		if (ngramsToTrace == null) {
			ngramsToTrace = new HashSet<String>();
		}
		return ngramsToTrace;
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
						getCandidatesToTrace().contains(candidate));
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
			String correctSpelling =
				badWordsToTrace == null ? null: badWordsToTrace.get(badWord);
			if (correctSpelling != null) {
				boolean found = false;
				for (String spelling : possibleSpellings) {
					if (spelling.equals(correctSpelling)) {
						found = true;
						break;
					}
				}

				String contain_or_not = " did NOT contain";
				if (found) {
					contain_or_not = " DID contain";
				}
				System.out.println("-- " + who +
						"(" + status.getSecond() +
						"): \n   " + what + contain_or_not + " candidate spelling '" +
						correctSpelling + "'.");
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
			String correctSpelling =
					badWordsToTrace == null ? null: badWordsToTrace.get(badWord);
			if (correctSpelling != null) {
				boolean found = false;
				for (ScoredSpelling scoredSpelling : possibleSpellings) {
					;
					if (scoredSpelling.spelling.equals(correctSpelling)) {
						found = true;
						break;
					}
				}

				String contain_or_not = " did NOT contain";
				if (found) {
					contain_or_not = " DID contain";
				}
				System.out.println("-- " + who +
						"(" + status.getSecond() +
						"): \n   " + what + contain_or_not + " candidate spelling '" +
						correctSpelling + "'.");
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
