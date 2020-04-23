package ca.pirurvik.iutools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumericExpression {
	
	public String numericFrontPart;
	public String morphemicEndPart;
	public String separator;
	
	private static String[] listOfSeparators = new String[] {
			"-",
			"\u2013", // n-dash
			"\u2014", // m-dash
			"\u2212", // minus sign
	};
	
	public NumericExpression(String _numericFrontPart, String _separator, String _morphemicEndPart) {
		this.numericFrontPart = _numericFrontPart;
		this.morphemicEndPart = _morphemicEndPart;
		this.separator = _separator;
	}
	
	public static NumericExpression tokenIsNumberWithSuffix(String token) {
		Pattern p = Pattern.compile("^(-?\\$?\\d+(?:[.,:]\\d+)?(?:[.,:]\\d+)?\\%?)((["
				+Pattern.quote(String.join("",listOfSeparators))
				+"])?([agijklmnpqrstuv&]+))?$");
		Matcher mp = p.matcher(token);
		if (mp.matches())
			if (mp.group(2)==null)
				return new NumericExpression(mp.group(1),null,null);
			else
				return new NumericExpression(mp.group(1),mp.group(3),mp.group(4));
		else
			return null;
	}



}
