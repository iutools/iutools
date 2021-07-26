package org.iutools.concordancer.tm;

import org.iutools.concordancer.SentencePair;

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

	// The WordSpotter is still experimental and there may be many error
	// conditions that have not been tested.
	//
	// If you set neverRaiseExceptions=true, then the WordSpotter will NEVER fail.
	// In case of exception, it will simply return "empty" results.
	//
	protected final static boolean neverRaiseException = true;

	public WordSpotter(SentencePair _pair) {
		pair = _pair;
	}

	public Map<String,String> spot(String lang, String langWord) throws WordSpotterException {
		Map<String,String> spottings = new HashMap<String,String>();
		try {
			String otherLang = pair.otherLangThan(lang);

			int[] langTokens = tokensMatchingText(lang, langWord);
			String langText = pair.joinTokens(lang, langTokens);

			int[] otherLangTokens = pair.otherLangTokens(lang, langTokens);
			String otherLangText =
			pair.joinTokens(pair.otherLangThan(lang), otherLangTokens);

			spottings.put(lang, langText);
			spottings.put(otherLang, otherLangText);
		} catch (Exception e) {
			if (!neverRaiseException) {
				throw this.wrapException(e);
			}
		}

		return spottings;
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

	public Map<String, String> higlight(String l1, String l1Expr,
		String tagName) throws WordSpotterException {
		Map<String,String> highglighted = new HashMap<String,String>();
		String l2 = pair.otherLangThan(l1);

		// Init the highlights map with un-highligted text. That way if
		// an exception is raised and neverRaiseException=true, we will be
		// returning a map that contains the original text without highlights.
		{
			highglighted.put(l1, pair.getText(l1));
			highglighted.put(l2, pair.getText(l2));
		}
		int[] l1Tokens = tokensMatchingText(l1, l1Expr);
		try {
			String l1Highlight = highlightTokens(l1, l1Tokens, tagName);
			highglighted.put(l1, l1Highlight);
		} catch (Exception e) {
			if (!neverRaiseException) {
				throw wrapException(e);
			}
		}


		try {
			int[] l2Tokens = pair.otherLangTokens(l1, l1Tokens);
			String l2Highlight = highlightTokens(l2, l2Tokens, tagName);
			highglighted.put(l2, l2Highlight);
		} catch (Exception e) {
			if (!neverRaiseException) {
				throw wrapException(e);
			}
		}

		return highglighted;
	}

	private String highlightTokens(String lang,
	 	int[] tokensToHighlight, String tagName) throws WordSpotterException {
		String highlighted = "";
		String startTag = "<"+tagName+">";
		String endTag = "</"+tagName+">";
		String remainingText = pair.langText.get(lang);
		String wholeText = remainingText;
		String[] allTokens = pair.tokens4lang.get(lang);
		for (int tokenNum: tokensToHighlight) {
			String token = allTokens[tokenNum];
			token = token.replaceAll("@@$", ".*?");
			Matcher matcher = Pattern.compile(token, Pattern.CASE_INSENSITIVE).matcher(remainingText);
			if (!matcher.find()) {
				throw new WordSpotterException(
					"Could not find token '"+token+"' in text: \'"+wholeText );
			} else {
				highlighted += remainingText.substring(0, matcher.start());
				highlighted += startTag;
				highlighted += matcher.group();
				highlighted += endTag;
				remainingText = remainingText.substring(matcher.end());
			}
		}

		highlighted += remainingText;
		highlighted = highlighted.replaceAll(endTag+"(\\s*)"+startTag, "$1");

		return highlighted;
	}

	public int[] tokensMatchingText(String sourceLang, String text) {
		int[] tokens = new int[0];
		if (sourceLang != null && text != null) {
			text = SentencePair.canonizeText(text);
			String[] canonicalTokens = pair.canonicalTokenText(sourceLang);
			for (int start = 0; start < canonicalTokens.length; start++) {
				for (int end = start; end < canonicalTokens.length; end++) {
					String[] spannedTokens = Arrays.copyOfRange(canonicalTokens, start, end);
					String spannedText = String.join(" ", spannedTokens);
					if (spannedText.equals(text)) {
						tokens = new int[end - start];
						for (int ii = 0; ii < tokens.length; ii++) {
							tokens[ii] = start + ii;
						}
					}
				}
			}
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
