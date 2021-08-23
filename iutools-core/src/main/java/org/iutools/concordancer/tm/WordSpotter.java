package org.iutools.concordancer.tm;

import ca.nrc.json.PrettyPrinter;
import org.apache.log4j.Logger;
import org.iutools.concordancer.SentencePair;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Given a SentencePair that has word-level alignments, this class can spot
 * a word or expression in one language as well as its equivalent in the other
 * language.
 *
 * For more details on how to use this class, see the DOCUMENTATION TESTS section
 * of WordSpotterTest.
 *
 */
public class WordSpotter {

	SentencePair pair = null;
	Map<String,List<String>> tokpunct4lang = new HashMap<String,List<String>>();

	// The WordSpotter is still experimental and there may be many error
	// conditions that have not been tested.
	//
	// If you set neverRaiseExceptions=true, then the WordSpotter will NEVER fail.
	// In case of exception, it will simply return "empty" results.
	//
	protected final static boolean neverRaiseException = true;

	public WordSpotter(SentencePair _pair) throws WordSpotterException {
		pair = _pair;
		for (String lang: pair.langs()) {
			tokpunct4lang.put(lang, splitText(lang));
		}
	}

	private List<String> splitText(String lang) throws WordSpotterException {
		String text = pair.langText.get(lang);
		String[] tokens = pair.tokens4langLowercased.get(lang);
		List<String> splitElts = splitText(text, tokens);
		return splitElts;
	}

	static List<String> splitText(String text, String[] tokens) throws WordSpotterException {
		Logger tLogger = Logger.getLogger("org.iutools.concordancer.tm.WordSpotter.splitText");

		List<String> splitElts = new ArrayList<String>();
		String remainingText = text;
		for (String token: tokens) {
			Pattern patt = tokenPattern(token);
			Matcher matcher = patt.matcher(remainingText);
			if (!matcher.find()) {
				String errMess = "Could not find token='"+token+"' in text '"+text+"'";
				tLogger.trace(errMess);
				throw new WordSpotterException(errMess);
			} else {
				if (matcher.start() > 0) {
					splitElts.add(remainingText.substring(0, matcher.start()));
				}
				splitElts.add(remainingText.substring(matcher.start(), matcher.end()));
				remainingText = remainingText.substring(matcher.end(), remainingText.length());
			}
		}

		return splitElts;
	}

	private static Pattern tokenPattern(String token) {
		Logger tLogger = Logger.getLogger("org.iutools.concordancer.SentencePair.tokenPattern");
		tLogger.trace("token='"+token+"'");
		token = SentencePair.escapeRegexpSpecialChars(token);
		String tokenRegex =
			token.replaceAll("^@@", "[^\\p{Punct}\\s]*?");
		tokenRegex = tokenRegex.replaceAll("@@$", "");
		Pattern patt =
			Pattern.compile(tokenRegex, Pattern.CASE_INSENSITIVE);

		return patt;
	}

	public Map<String,String> spot(String l1, String l1Word) throws WordSpotterException {
		Map<String,String> spottings = new HashMap<String,String>();
		String tagName = "strong";
		Map<String, String> highlighted = highlight(l1, l1Word, tagName);
		for (String lang: new String[] {l1, pair.otherLangThan(l1)}) {
			spottings.put(lang, spotHighlight(tagName, highlighted.get(lang)));
		}

		return spottings;
	}

	public static String spotHighlight(String tagName, String text) {
		return spotHighlight(tagName, text, (Boolean)null);
	}

	public static String spotHighlight(String tagName, 
		String text, Boolean ignoreRepetitions) {
		
		if (ignoreRepetitions == null) {
			ignoreRepetitions = false;
		}
		Logger tLogger = Logger.getLogger("org.iutools.concordancer.tm.WordSpotter.spotHighlight");
		tLogger.trace("text="+text);
		Matcher matcher =
			Pattern.compile("<"+tagName+">([^<]*)"+"</"+tagName+">")
				.matcher(text);
		String spotted = null;
		Set<String> alreadySeen = new HashSet<String>();
		while (matcher.find()) {
			String aHighglight = matcher.group(1);
			if (ignoreRepetitions && alreadySeen.contains(aHighglight)) {
				continue;
			}
			alreadySeen.add(aHighglight);
			if (spotted != null) {
				spotted += " ... ";
			} else {
				spotted = "";
			}
			spotted += aHighglight;
		}

		tLogger.trace("returning spotted='"+spotted+"'");

		return spotted;
	}

	private WordSpotterException wrapException(Exception e) {
		WordSpotterException wrapped = null;
		if (e instanceof WordSpotterException) {
			wrapped = (WordSpotterException)e;
		} else {
			wrapped = new WordSpotterException(e);
		}
		return wrapped;
	}

	public Map<String, String> highlight(String l1, String l1Expr,
 		String tagName) throws WordSpotterException {
		return highlight(l1, l1Expr, tagName, (Boolean)null);
	}


	public Map<String, String> highlight(String l1, String l1Expr,
		String tagName, Boolean higlightInPlace) throws WordSpotterException {
		Logger tLogger = Logger.getLogger("org.iutools.concordancer.tm.WordSpotter.highlight");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("l1=" + l1 + ", l1Expr=" + l1Expr + "pair=, =" + PrettyPrinter.print(pair));
		}
		Map<String,String> highglighted = new HashMap<String,String>();
		if (higlightInPlace == null) {
			higlightInPlace = false;
		}
		String l2 = pair.otherLangThan(l1);

		// Init the highlights map with text that is highlighted on the l1 side,
		// and text that is NOT higlighted on the other language side. That way
		// if an exception is raised during token alignment and
		// neverRaiseException=true, we will be
		// returning a map that contains the original text without highlights.
		{
			highglighted.put(l1,
				highlightExpressionDirectly(l1, l1Expr, pair.getText(l1), tagName));
			highglighted.put(l2, pair.getText(l2));
		}
		if (pair.hasWordLevel()) {
			int[] l1Tokens = tokensMatchingText(l1, l1Expr);

			try {
				int[] l2Tokens = pair.otherLangTokens(l1, l1Tokens);
				String l2Highlight = highlightTokens(l2, l2Tokens, tagName);
				l2Highlight = ensureTagsAreNotInMiddleOfWord(l2Highlight, tagName);
				highglighted.put(l2, l2Highlight);
			} catch (Exception e) {
				if (!neverRaiseException) {
					throw wrapException(e);
				}
			}
		}

		tLogger.trace("Highlighted text"+
			"\n   "+l1+": "+highglighted.get(l1)+
			"\n   "+l2+": "+highglighted.get(l2));

		if (higlightInPlace) {
			String l1HighlightedText = highglighted.get(l1);
			pair.langText.put(l1, l1HighlightedText);
			String l2HighlightedText = highglighted.get(l2);
			pair.langText.put(l2, l2HighlightedText);
		}

		return highglighted;
	}

	protected static String ensureTagsAreNotInMiddleOfWord(String text, String tagName) {
		Logger tLogger = Logger.getLogger("org.iutools.concordancer.tm.WordSpotter.ensureTagsAreNotInMiddleOfWord");
		tLogger.trace("text="+text);
		String openTag = "<"+tagName+">";
		String closeTag = "</"+tagName+">";
		String regex =
			"(?<=(^|\\s|\\p{Punct}))([^\\s\\p{Punct}]*?)"+openTag+"([^<]*)"+closeTag+"([^\\s\\p{Punct}]*?)(?=($|\\s|\\p{Punct}))";

		if (tLogger.isTraceEnabled()) {
			Matcher matcher = Pattern.compile(regex).matcher(text);
			matcher.find();
			for (int grp = 1; grp <= matcher.groupCount(); grp++) {
				tLogger.trace("Group #" + grp + ": '" + matcher.group(grp) + "'");
			}
		}

		text = text.replaceAll(regex, openTag+"$2$3$4"+closeTag);

		return text;
	}

	private String highlightExpressionDirectly(
		String lang, String expr, String inText, String tagName) throws WordSpotterException {
		if (lang.equals("iu")) {
			// Make sure expression to highlight is in same script as the
			// text we are highlighting
			try {
				expr = TransCoder.ensureSameScriptAsSecond(expr, inText, true);
			} catch (TransCoderException e) {
				throw new WordSpotterException(e);
			}
		}
		String regex = "(?i)("+SentencePair.escapeRegexpSpecialChars(expr)+")";
		inText = inText.replaceAll(regex, "<"+tagName+">$1</"+tagName+">");
		return inText;
	}

	private String highlightTokens__NEW(String lang,
	 	int[] tokensToHighlight, String tagName) throws WordSpotterException {

		Logger tLogger = Logger.getLogger("org.iutools.concordancer.tm.WordSpotter.highlightTokens");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("lang="+lang+", tokensToHighlight="+ PrettyPrinter.print(tokensToHighlight));
		}
		String[] allTokens = pair.tokens4lang.get(lang);
		Pattern pattToHighlight = pattTokensToHighlight(tokensToHighlight, allTokens);
		String startTag = "<"+tagName+">";
		String endTag = "</"+tagName+">";
		List<String> textTokens = this.tokpunct4lang.get(lang);
		String highlightedText = "";
		for (String token: textTokens) {
			String highlightedTok = token;
			if (pattToHighlight.matcher(token).matches()) {
				highlightedTok = startTag+highlightedTok+endTag;
			}
			highlightedText += highlightedTok;
		}

		highlightedText = expandHighlights(highlightedText, tagName);

		tLogger.trace("Returning highlightedText="+highlightedText);
		return highlightedText;
	}

	private String expandHighlights(String highlightedText, String tagName) {
		String startTag = "<"+tagName+">";
		String endTag = "</"+tagName+">";
		String regex = endTag+"([\\s\\p{Punct}]*)"+startTag;
		highlightedText = highlightedText.replaceAll(regex, "$1");
		return highlightedText;
	}

	private Pattern pattTokensToHighlight(
		int[] tokensToHighlight, String[] allTokens) {
		String regex = "";
		for (int tokenNum: tokensToHighlight) {
			if (!regex.isEmpty()) {
				regex += "|";
			}
//			regex += SentencePair.escapeRegexpSpecialChars(allTokens[tokenNum]);
			regex += tokenPattern(allTokens[tokenNum]);
		}
		regex = "("+regex+")";
		return Pattern.compile(regex);
	}

	private String highlightTokens(String lang,
	 	int[] tokensToHighlight, String tagName) throws WordSpotterException {

		Logger tLogger = Logger.getLogger("org.iutools.concordancer.tm.WordSpotter.highlightTokens");

		if (1+1 == 2) return highlightTokens__NEW(lang, tokensToHighlight, tagName);
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("lang="+lang+", tokensToHighlight="+ PrettyPrinter.print(tokensToHighlight));
		}
		String highlighted = "";
		String startTag = "<"+tagName+">";
		String endTag = "</"+tagName+">";
		String remainingText = pair.langText.get(lang);
		String wholeText = remainingText;
		String[] allTokens = pair.tokens4lang.get(lang);
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("allTokens="+ PrettyPrinter.print(allTokens));
		}
		for (int tokenNum: tokensToHighlight) {
			String token = allTokens[tokenNum];
			tLogger.trace("highlighting token #"+tokenNum+"="+token);
			Pattern pattToken = SentencePair.stemmedTokensPattern(token);
			Matcher matcher = pattToken.matcher(remainingText);
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("matcher="+matcher.pattern());
			}
			if (!matcher.find()) {
				tLogger.trace("Could not find token '"+token+"' in text: \'"+wholeText);
				throw new WordSpotterException(
					"Could not find token '"+token+"' in text: \'"+wholeText );
			} else {
				highlighted += remainingText.substring(0, matcher.start());
				highlighted += startTag;
				highlighted += matcher.group(1);
				highlighted += endTag;
				highlighted += matcher.group(2);
				remainingText = remainingText.substring(matcher.end());
			}
			tLogger.trace("at this point, highlighted="+highlighted);
		}

		highlighted += remainingText;
		highlighted = highlighted.replaceAll(endTag+"(\\s*)"+startTag, "$1");

		tLogger.trace("Returning highlighted="+highlighted);

		return highlighted;
	}

	public int[] tokensMatchingText(String sourceLang, String text) {
		Logger tLogger = Logger.getLogger("org.iutools.concordancer.tm.WordSpotter.tokensMatchingText");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("sourceLang="+sourceLang+", text="+text);
			tLogger.trace("this.pair="+PrettyPrinter.print(this.pair));
		}

		int[] tokens = new int[0];
		if (sourceLang != null && text != null) {
			text = SentencePair.canonizeText(text);
			tLogger.trace("After canonisation, text="+text);
			String[] canonicalTokens = pair.canonicalTokenText(sourceLang);
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("canonicalTokens=" + PrettyPrinter.print(canonicalTokens));
			}
			for (int start = 0; start < canonicalTokens.length; start++) {
				for (int end = start+1; end < canonicalTokens.length; end++) {
					String[] spannedTokens = Arrays.copyOfRange(canonicalTokens, start, end);
					String spannedText = String.join(" ", spannedTokens);
					tLogger.trace("Looking at start="+start+", end="+end);
					tLogger.trace("spannedText="+spannedText);
					tLogger.trace("Calling stemmedTokensPattern with '"+spannedText+"'");
					Pattern pattSpannedText = SentencePair.stemmedTokensPattern(spannedText);
					if (tLogger.isTraceEnabled()) {
						tLogger.trace("pattSpannedText="+PrettyPrinter.print(pattSpannedText.pattern()));
					}
					Matcher matcher =
						SentencePair.stemmedTokensPattern(spannedText, true)
						.matcher(text);
					if (matcher.find()) {
						tLogger.trace("spanned text MATCHES");
						tokens = new int[end - start];
						for (int ii = 0; ii < tokens.length; ii++) {
							tokens[ii] = start + ii;
						}
					}
				}
			}
		}

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("returning tokens="+PrettyPrinter.print(tokens));
		}
		return tokens;
	}

	protected int[] otherLangTokens(String lang, String langExpr) {
		int[] otherTokens = new int[0];
		String otherLang = pair.otherLangThan(lang);

		int[] langTokens = tokensMatchingText(lang, langExpr);
		String langText = pair.joinTokens(lang, langTokens);

		int[] otherLangTokens = pair.otherLangTokens(lang, langTokens);

		return otherTokens;
	}
}
