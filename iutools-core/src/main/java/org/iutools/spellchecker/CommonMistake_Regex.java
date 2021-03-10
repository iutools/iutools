package org.iutools.spellchecker;

/**
 * This class captures a pattern for a common type of spelling mistake
 */
public class CommonMistake_Regex {
	/** Regex that catches misspelled characters (and ONLY that) */
	private String regexBad = null;

	/** Regex that fixes the misspelled characters (and ONLY that) */
	private String regexFix = null;

	public CommonMistake_Regex(String _regexBad, String _regexFix) {
		this.regexBad = _regexBad;
		this.regexFix = _regexFix;
	}

	public String fixWord(String word) {
		String fixed = word;
		return fixed;
	}
}
