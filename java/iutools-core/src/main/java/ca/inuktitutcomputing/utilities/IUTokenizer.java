package ca.inuktitutcomputing.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class IUTokenizer {
	
	static Pattern pParan = Pattern.compile("(\\((.*?)\\))");
	
	private List<String> words;
	
	public IUTokenizer() {
	}
	
	
	public List<String> run(String text) {
		words = new ArrayList<String>();
		String[] tokens = text.split("\\s+");

		for (int itoken = 0; itoken < tokens.length; itoken++) {
			__processToken(tokens[itoken]);
		}

		return words;
	}
	
	
	public void __processToken(String token) {
		Logger logger = Logger.getLogger("IUTokenizer.__processToken");
		logger.debug("token= " + token);
		Matcher m = pParan.matcher(token);
		boolean parPatternFound = false;

		int pos = 0;
		while (m.find()) {
			logger.debug("found () pattern in " + token + " at position " + m.start(1));
			parPatternFound = true;
			String textFound = m.group(2);
			__processToken(token.substring(pos,m.start(1)));
			__processToken(textFound);
			pos = m.end(1);
		}

		if (!parPatternFound) {
			Pattern pp = Pattern.compile("^\\p{Punct}*(.+?)\\p{Punct}*$");
			Matcher mp = pp.matcher(token);
			if (mp.matches()) {
				Pattern pacr = Pattern.compile("^([^\\.]\\.)+$");
				Matcher macr = pacr.matcher(token);
				if (macr.matches())
					words.add(token);
				else
					words.add(mp.group(1));
			}
		}

	}

}
