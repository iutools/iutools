package ca.inuktitutcomputing.utilities;

public class EditDistanceCalculatorFactory {
	
	public enum DistanceMethod {LEVENSTHEIN, DP5, LCS, JARO_WINKLER};
	
	private static final DistanceMethod defaultDistanceMethod = DistanceMethod.LEVENSTHEIN;
	
	public static EditDistanceCalculator getEditDistanceCalculator() {
		return getEditDistanceCalculator(defaultDistanceMethod);
	}
	
	public static EditDistanceCalculator getEditDistanceCalculator(DistanceMethod method) {
		EditDistanceCalculator edcalculator = null;
		if (method == DistanceMethod.LEVENSTHEIN)
			edcalculator = new Levenshtein();
		else if (method == DistanceMethod.DP5)
			edcalculator =  new DP5();
		else if (method == DistanceMethod.LCS)
			edcalculator =  new LCS();
		else if (method == DistanceMethod.JARO_WINKLER)
			edcalculator =  new JaroWinkler();
		
		return edcalculator;
	}

}
