package ca.pirurvik.iutools.text.segmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ca.nrc.datastructure.Pair;
import ca.pirurvik.iutools.NumericExpression;

public class IUTokenizer {

	static String[] punctuationRegexpElements = new String[] { "\\p{Punct}", // ascii punctuation
			"\2013", "\u2014", "\u2212", // minus sign and similar
			"«", "\u2018", "\u201B", "\u201C", "\u201F", // opening quote signs
			"»", "\u2019", "\u201D", };
	static Pattern pParan = Pattern.compile("(\\((.*?)\\))");
	// TODO: add other signs that might be used in the stead of "dash"
	static Pattern pPunct = Pattern.compile("((\\p{Punct}|\u2212|\u2013)+)");
	static Pattern pAcronym = Pattern.compile("^([^\\.]\\.)+$");

	public List<String> tokens = new ArrayList<String>();
	public List<Pair<String, Boolean>> allTokensPunctuation = new ArrayList<Pair<String, Boolean>>();
	private String text;

	public IUTokenizer() {
	}

	public List<String> tokenize(String text) {
		this.text = text;
		tokens = new ArrayList<String>();
		allTokensPunctuation = new ArrayList<Pair<String, Boolean>>();
		Pattern pSpace = Pattern.compile("(\\s+)");
		Matcher mSpace = pSpace.matcher(text);

		int pos = 0;
		while (mSpace.find()) {
			String token = text.substring(pos, mSpace.start(1));
			if (token.length() != 0)
				__processToken(token);
			allTokensPunctuation.add(new Pair<>(mSpace.group(1), false));
			pos = mSpace.end(1);
		}
		if (text.substring(pos).length() != 0)
			__processToken(text.substring(pos));

		return onlyWords();
	}

	public List<Pair<String, Boolean>> getAllTokens() {
		return allTokensPunctuation;
	}
	public List<Pair<String, Boolean>> getTokens() {
		return allTokensPunctuation;
	}


	/*
	 * Each token is a sequence of non space characters.
	 */
	public void __processToken(String token) {
		Logger logger = Logger.getLogger("IUTokenizer.__processToken");
		logger.debug("token= " + token);
//		token = token.replaceAll("\\s","");
		String tokenAfterInitialPunctuation = __processInitialPunctuation(token);
		if (tokenAfterInitialPunctuation.equals(""))
			return;
		String tokenAfterAcronym = __processAcronym(tokenAfterInitialPunctuation);
		if (tokenAfterAcronym.contentEquals(""))
			return;
		String[] tokenBeforeFinalPunctuationsAndFinalPunctuation = __processBeforePossibleFinalPunctuation(tokenAfterAcronym);
		String tokenBeforeFinalPunctuation = tokenBeforeFinalPunctuationsAndFinalPunctuation[0];
		String finalPunctuation = tokenBeforeFinalPunctuationsAndFinalPunctuation[1];
		if ( !__processNumericExpression(tokenBeforeFinalPunctuation) ) {
			__processMainToken(tokenBeforeFinalPunctuation);
		}
		__processFinalPunctuation(finalPunctuation);
	}

	private boolean __processNumericExpression(String token) {
		boolean res = false;
		if (wordIsNumberWithSuffix(token)!= null) {
			allTokensPunctuation.add(new Pair<>(token,true));
			res = true;
		}
		
		return res;
	}

	protected void __processFinalPunctuation(String finalPunctuation) {
		if ( !finalPunctuation.equals("") ) {
			allTokensPunctuation.add(new Pair<>(finalPunctuation, false));
		}
	}

	protected void __processMainToken(String token) {
		Logger logger = Logger.getLogger("IUTokenizer.__processMainToken");
		Matcher mpunct = pPunct.matcher(token);
		int pos = 0;
		while (mpunct.find()) {
			String punctuationMark = mpunct.group(1);
			logger.debug("found punctuation pattern in " + token + " at position " + mpunct.start(1));
			if (punctuationMark.equals("&") && mpunct.start(1) != 0)
				continue;
			if (pos != mpunct.start(1))
				allTokensPunctuation.add(new Pair<>(token.substring(pos, mpunct.start(1)), true));
			allTokensPunctuation.add(new Pair<>(punctuationMark, false));
			pos = mpunct.end(1);
		}
		if (pos != token.length())
			allTokensPunctuation.add(new Pair<>(token.substring(pos), true));
	}

	protected String[] __processBeforePossibleFinalPunctuation(String token) {
		if (token=="")
			return new String[] {};
		String punctuationRegexp = String.join("", punctuationRegexpElements);
		Pattern pFinalPunct = Pattern.compile("^(.+[^"+punctuationRegexp+"])"
				+"(["+punctuationRegexp+"]+)"+"$");
		Matcher mFinalPunct = pFinalPunct.matcher(token);
		if (mFinalPunct.matches()) {
			token = mFinalPunct.group(1);
			return new String[] { mFinalPunct.group(1), mFinalPunct.group(2)};
		} else
			return new String[] {token, ""};
	}

	/*
	 * Acronyms are sequences of single characters each followed by a period, eg.
	 * N.R.C.
	 */
	protected String __processAcronym(String token) {
		Pattern pAcronym = Pattern.compile("^([^\\.]\\.){2,}$");
		Matcher macr = pAcronym.matcher(token);
		if (macr.matches()) {
			tokens.add(token);
			allTokensPunctuation.add(new Pair<>(token, true));
			return "";
		} else
			return token;
	}

	protected String __processInitialPunctuation(String token) {
		String punctuationRegexp = String.join("", punctuationRegexpElements);
		String regexp = "[" + punctuationRegexp + "]";
		Pattern p = Pattern.compile("^" + "(" + regexp + "+" + ")" + "(.*)$");
		Matcher mp = p.matcher(token);
		if (mp.matches()) {
			tokens.add(mp.group(1));
			allTokensPunctuation.add(new Pair<>(mp.group(1), false));
			return mp.group(2);
		} else
			return token;
	}

	protected NumericExpression wordIsNumberWithSuffix(String token) {
		NumericExpression numexp = NumericExpression.tokenIsNumberWithSuffix(token);
		return numexp;
	}

	public List<String> onlyWords() {
		List<String> onlyWords = new ArrayList<String>();
		for (int iToken = 0; iToken < allTokensPunctuation.size(); iToken++) {
			Pair<String, Boolean> token = allTokensPunctuation.get(iToken);
			if (token.getSecond())
				onlyWords.add(token.getFirst());
		}

		return onlyWords;
	}

	public String reconstruct() {
		String str = "";
		for (int iTok = 0; iTok < allTokensPunctuation.size(); iTok++) {
			Pair<String, Boolean> tok = allTokensPunctuation.get(iTok);
			str += tok.getFirst();
		}

		return str;
	}

	public static boolean isDash(String character) {
		return character.equals("-") || character.equals("\u2212") || character.equals("\u2013");
	}

}
