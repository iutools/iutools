package ca.pirurvik.iutools.spellchecker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.nrc.datastructure.Pair;

public class SpellDebug {
	
	
	private static String[][] pairsToTraceArr = new String[][] {
//		new String[] {"angijuqqaaqaqtutik", "angajuqqaaqaqtutik"},
//		new String[] {"angijuqqaaqaqtutik", "*"},
//		new String[] {"maliklugu","maligluglu"},
//		new String[] {"maliklugu","*"},
//		new String[] {"nakuqmi","nakurmiik"}
//		new String[] {"tamaini", "tamainni"},
//		new String[] {"tamainni", "tamaini"}
//		new String[] {"*", "tamaini"},
//		new String[] {"tamainni", "*"},
	};
	
	private static Set<String> pairsToTrace = null;
		private static Set<String> getPairsToTrace() {
			if (pairsToTrace == null) {
				pairsToTrace = new HashSet<String>();
				for (String[] aPair: pairsToTraceArr) {
					pairsToTrace.add(aPair[0]+"/"+aPair[1]);
				}
			}
			
			return pairsToTrace;
		}
	
	private static Set<String> badWordsToTrace = null;
		private static Set<String> getBadWordsToTrace() {
			if (badWordsToTrace == null) {
				badWordsToTrace = new HashSet<String>();
				for (String[] aPair: pairsToTraceArr) {
					badWordsToTrace.add(aPair[0]);
				}
			}
			return badWordsToTrace;
		}
		
	private static Pair<Boolean,String> traceStatus(String badWord, 
			String candidate) {
		Boolean traceIsOn = null;
		String traceID = null;
		
		if (candidate == null) {
			traceIsOn = getBadWordsToTrace().contains(badWord);
			traceID = badWord + "/";
		}
		
		if (traceIsOn == null) {
			traceID = badWord + "/" + candidate;
			String key2 = "*/" + candidate;
			String key3 = badWord + "/*";
			if (getPairsToTrace().contains(traceID) ||
					getPairsToTrace().contains(key2) ||
					getPairsToTrace().contains(key3)) {
				traceIsOn = true;
			}			
		}	
		
		if (traceIsOn == null) {
			traceIsOn = false;
		}
		
		return Pair.of(traceIsOn, traceID);
	}
	
	public static void trace(String who, String mess, 
			String badWord, String candidate) {
		Pair<Boolean,String> status = traceStatus(badWord, candidate);
		if (status.getFirst()) {
			System.out.println("-- "+who+"("+status.getSecond()+"): "+mess);			
		}
	}

	public static void containsCorrection(String who, String what, 
			String badWord, String candidate,
			List<ScoredSpelling> scoredSpellings) {
		Pair<Boolean,String> status = traceStatus(badWord, candidate);
		if (status.getFirst()) {
			boolean found = false;
			for (ScoredSpelling spelling: scoredSpellings) {
				if (spelling.spelling.equals(candidate)) {
					found = true;
					break;
				}
			}
			
			String contain_or_not = " did NOT contain";
			if (found) {
				contain_or_not = " DID contain";
			}
			System.out.println("-- "+who+
					"("+status.getSecond()+
					"): "+what+contain_or_not+" candidate spelling '"+
					candidate+"'.");			
		}
	}

	public static void containsCorrection(String who, String what, 
			String badWord, String candidate,
			Set<String> scoredSpellings) {
		Pair<Boolean,String> status = traceStatus(badWord, candidate);
		if (status.getFirst()) {
			boolean found = false;
			for (String spelling: scoredSpellings) {
				if (spelling.equals(candidate)) {
					found = true;
					break;
				}
			}
			
			String contain_or_not = " did NOT contain";
			if (found) {
				contain_or_not = " DID contain";
			}
			System.out.println("-- "+who+
					"("+status.getSecond()+
					"): "+what+contain_or_not+" candidate spelling '"+
					candidate+"'.");			
		}
	}

	public static boolean traceIsActive(String badWord) {
		return traceIsActive(badWord, null);
	}

	public static boolean traceIsActive(String badWord, String candidate) {
		if (candidate == null) {
			candidate = "*";
		}
		boolean isActive = traceStatus(badWord, candidate).getFirst();
		
		return isActive;
	}

}
