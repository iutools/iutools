package ca.inuktitutcomputing.utilities;

public class EditDistanceCalculatorFactory {
	
	public static EditDistanceCalculator getEditDistanceCalculator(String name) throws EditDistanceCalculatorFactoryException {
		String nameLC = name.toLowerCase();
		EditDistanceCalculator edcalculator = null;
		if (nameLC.equals("levenshtein"))
			edcalculator = new Levenshtein();
		else if (nameLC.equals("dp5"))
			edcalculator =  new DP5();
		else if (nameLC.equals("lcs"))
			edcalculator =  new LCS();
		else if (nameLC.equals("jaro-winkler"))
			edcalculator =  new JaroWinkler();
		else
			throw new EditDistanceCalculatorFactoryException("Unknown edit distance method.");
		return edcalculator;
	}

}
