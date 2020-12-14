package org.iutools.edit_distance;

public class EditDistanceCalculatorFactory {
	
	public enum DistanceMethod {LEVENSHTEIN, DP5, LCS, JARO_WINKLER, IU_DIFF};
	
	private static final DistanceMethod defaultDistanceMethod = DistanceMethod.IU_DIFF;
	
	public static EditDistanceCalculator getEditDistanceCalculator() {
		return getEditDistanceCalculator(defaultDistanceMethod);
	}
	
	public static EditDistanceCalculator getEditDistanceCalculator(DistanceMethod method) {
		EditDistanceCalculator edcalculator = null;
		if (method == DistanceMethod.LEVENSHTEIN)
			edcalculator = new Levenshtein();
		else if (method == DistanceMethod.DP5)
			edcalculator =  new DP5();
		else if (method == DistanceMethod.LCS)
			edcalculator =  new LCS();
		else if (method == DistanceMethod.JARO_WINKLER)
			edcalculator =  new JaroWinkler();
		else if (method == DistanceMethod.IU_DIFF) {
			edcalculator = new IUSpellingDistance();
		}
		
		return edcalculator;
	}

}
