package org.iutools.spellchecker;

/**
 * This class captures a pattern for an 'absolute' spelling mistake.
 * By absolute, we mean that any word that fits the left side of the
 * pattern is necessarily misspelled.
 */
public class AbsoluteMistake_Regex {
	/** Regex that catches misspelled characters (and ONLY that) */
	private String regexBad = null;

	/** Regex that fixes the misspelled characters (and ONLY that) */
	private String regexFix = null;

	public AbsoluteMistake_Regex(String _regexBad, String _regexFix) {
		this.regexBad = _regexBad;
		this.regexFix = _regexFix;
	}

	public String fixWord(String word) {
		String fixed = word.replaceAll(regexBad, regexFix);
		return fixed;
	}
}
