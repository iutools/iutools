package org.iutools.corpus.sql;

import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.text.segmentation.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Lengthens short words (N<5) in an inuktitut text so that they won't be
 * treated as stop words by the SQL MyISAM FULLTEXT parser,
 */
public class IUWordLengthener {

	static IUTokenizer tokenizer = new IUTokenizer();
	public static final int MIN_LEN = 5;
	public static  String EXTRA_CHARS = "";
	static {
		for (int ii=0; ii < MIN_LEN; ii++) {
			EXTRA_CHARS += "Z";
		}
	}

	public static String lengthen(String text) {
		return lengthen(text, (Boolean)null);
	}

	public static String lengthen(String text, Boolean asSingleToken) {
		if (asSingleToken == null) {
			asSingleToken = false;
		}
		String lengthenedText = null;
		if (text != null) {
			if (text.isEmpty()) {
				lengthenedText = "";
			} else {
				lengthenedText = "";
				List<Token> tokens = new ArrayList<Token>();
				if (asSingleToken) {
					tokens.add(new Token(text, true));
				} else {
					tokenizer.tokenize(text);
					tokens = tokenizer.getAllTokens();
				}

				for (Token aToken : tokens) {
					lengthenedText += lengthenToken(aToken);
				}
			}
		}
		return lengthenedText;
	}

	private static String lengthenToken(Token token) {
		String tokenText = token.text;
		if (token.isWord && tokenText.length() < MIN_LEN) {
			tokenText += EXTRA_CHARS;
		}
		return tokenText;
	}

	public static String restoreLengths(String text) {
		String restoredText = null;
		if (text != null) {
			tokenizer.tokenize(text);
			List<Token> tokens = tokenizer.getAllTokens();
			restoredText = "";
			for (Token aToken: tokens) {
				String tokText = aToken.text;
				if (aToken.isWord && tokText.endsWith(EXTRA_CHARS)) {
					tokText = tokText.replaceAll(EXTRA_CHARS+"*$", "");
				}
				restoredText += tokText;
			}
		}
		return restoredText;
	}
}
