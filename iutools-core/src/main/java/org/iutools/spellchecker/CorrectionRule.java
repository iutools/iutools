package org.iutools.spellchecker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.text.IUWord;
import org.iutools.text.WordException;

import java.util.regex.Pattern;

import static org.iutools.script.TransCoder.Script;

/**
 * This class captures a "shallow" rule for correcting certain types of spelling
 * mistake.
 *
 * By "shallow rule", we mean a rule that looks for invalid sequences of characters
 * that can NEVER occur in a valid Inuktitut word. In particular, these rules
 *
 * Many of those rules address "mistakes" that are in fact "old fashioned" spelling in certain
 * dialects, and the rule transforms the spelling to the more modern, standardized
 * spelling.
 */
public class CorrectionRule {
	/** Regex that catches misspelled characters (and ONLY that) */
	public String regexBad = null;

	/** Pattern for regexBad **/
	private Pattern _pattBad = null;

	/** Regex that fixes the misspelled characters (and ONLY that) */
	public String regexFix = null;

	/** Script in which the rule is written */
	public Script ruleScript = null;

	/** Rule for the other script */
	private String _regexBad_otherScript = null;
	private String _regexFix_otherScript = null;

	/** Used to spot special regex expressions that might look like non-syllabic chars */
	private static Pattern pattRegexExpressions = Pattern.compile("(\\\\[wb]|\\(\\?<[a-zA-Z\\d]*>|\\$\\{[a-zA-Z\\d]*)");

	public CorrectionRule(String _regexBad, String _regexFix) throws SpellCheckerException {
		init__CorrectionRule(_regexBad, _regexFix);
	}

	private void init__CorrectionRule(String _regexBad, String _regexFix) throws SpellCheckerException {
		if (_regexFix == null) {
			// If no rule has been provided to actually fix the faulty characters,
			// use a regexp that hihglights them with a pair of brackets []
			_regexFix = "[$0]";
		}

		assertNoInvalidRegexpChars(_regexBad, _regexFix);

		Script script1 = regexScript(_regexBad);
		if (script1 == Script.MIXED) {
			throw new SpellCheckerException("Left-hand side of the rule was written in a 'mixed' script: "+this.toString());
		}
		this.regexBad = _regexBad;
		if (_regexFix != null) {
			this.regexFix = _regexFix;
			Script script2 = regexScript(regexFix);

			if (script2 == Script.MIXED) {
				throw new SpellCheckerException("Right-hand side of the rule was written in a 'mixed' script: " + this.toString());
			}
			if (script1 != script2) {
				throw new SpellCheckerException("The two sides of the rule were written in different scripts: " + this.toString());
			}
		}
		this.ruleScript = script1;
	}

	private void assertNoInvalidRegexpChars(String _regexBad, String _regexFix) throws SpellCheckerException {
		String ruleStr = toString(_regexBad, _regexFix);
		if (_regexBad.matches(".*\\\\b.*")) {
			throw new SpellCheckerException(ruleStr+": \\b is not allowed in left-hand regexp. Use ^ or $ depending on whether you are looking at the start or end of a word.");
		}
		if (_regexBad.matches(".*\\\\W.*")) {
			throw new SpellCheckerException(ruleStr+": \\W is not allowed in left-hand regexp. Use ^ or $ depending on whether you are looking at the start or end of a word.");
		}
		if (_regexBad.matches(".*\\\\w.*")) {
			throw new SpellCheckerException(ruleStr+": \\w is not allowed in left-hand regexp. Use . instead.");
		}
	}

	protected Script regexScript(String regex) {
		// Remove regex special chars because they may look like Roman chars in a Syll regex
//		regex = regex.replaceAll("\\\\[wb]", "");
		regex = pattRegexExpressions.matcher(regex).replaceAll("");

		// Some Syllabics rules may contain H in the middle of a regexp
		// Note: It may look like we are including the same character twice, but in fact, the
		// second character is the syllabic ᕼ, which looks just like the roman H.
		regex = regex.replaceAll("[Hᕼ]", "");

		Script script = TransCoder.textScript(regex);
		return script;
	}

	@Override
	public String toString() {
		return toString(regexBad, regexFix);
	}

	public String toString(String _regexBad, String _regexFix) {
		String toS = "'"+_regexBad+"' --> '"+_regexFix+"'";
		return toS;
	}

	public Pattern pattBad() {
		if (_pattBad == null) {
			_pattBad = Pattern.compile(regexBad, Pattern.UNICODE_CHARACTER_CLASS);
		}
		return _pattBad;
	}

	public IUWord fixWord(IUWord origWord) throws SpellCheckerException {
		Logger logger = LogManager.getLogger("org.iutools.spellchecker.CorrectionRule.fixWord");
		String origStr = origWord.inScript(ruleScript);
		String fixedStr = pattBad().matcher(origStr).replaceAll(regexFix);
		if (logger.isTraceEnabled()) {
			logger.trace("For origWord="+origWord+", fixedStr="+fixedStr);
		}
		String result = "'"+origStr+"' " +
			(fixedStr.equals(origStr) ? "UNCHANGED": "-> '"+fixedStr+"'");

		traceRulesThatFired(origWord, origStr, fixedStr);

		fixedStr = removeInnerBadCharMarkers(fixedStr);

		IUWord fixedWord = null;
		try {
			fixedWord = new IUWord(fixedStr, ruleScript);
			if (logger.isTraceEnabled()) {
				logger.trace("returning fixedWord="+fixedWord+", created from fixedStr="+fixedStr+", ruleScript="+ruleScript);
			}
		} catch (WordException e) {
			throw new SpellCheckerException(e);
		}
		return fixedWord;
	}

	/** If we have nested bad char markers (ex: 'aa[bb[c]dd]'), remove all but the outermost
	 *  markers (ex: 'aa[bb[c]dd]' --> 'aa[bbcdd]') */
	private String removeInnerBadCharMarkers(String fixedStr) {
		StringBuilder noNested = new StringBuilder();
		int nestingLevel = 0;
		for (char ch: fixedStr.toCharArray()) {
			boolean skipChar = false;
			if (ch == '[') {
				nestingLevel++;
				if (nestingLevel > 1) {
					skipChar = true;
				}
			} else if (ch == ']') {
				nestingLevel--;
				if (nestingLevel > 0) {
					skipChar = true;
				}
			}
			if (!skipChar) {
				noNested.append(ch);
			}
		}

//		System.out.println("--** fixNestedBadCharMarkers: for fixedStr="+fixedStr+", returning noNested="+noNested);
		return noNested.toString();
	}

	private void traceRulesThatFired(IUWord origWord, String origStr, String fixedStr) {
		Logger logger = LogManager.getLogger("org.iutools.spellchecker.CorrectionRule.traceRulesThatFired");
		if (!origStr.equals(fixedStr) && logger.isTraceEnabled()) {
			logger.trace(origWord + ": rule fired\n  Rule: " + this.toString() + "\n  Result: --> " + fixedStr);
		}
	}

	private String regexBad_otherScript() throws SpellCheckerException {
		if (_regexBad_otherScript == null) {
			try {
				_regexBad_otherScript = TransCoder.inOtherScript(regexBad);
			} catch (TransCoderException e) {
				throw new SpellCheckerException(e);
			}
		}
		return _regexBad_otherScript;
	}

	private String regexFix_otherScript() throws SpellCheckerException {
		if (_regexFix_otherScript == null) {
			try {
				_regexFix_otherScript = TransCoder.inOtherScript(regexFix);
			} catch (TransCoderException e) {
				throw new SpellCheckerException(e);
			}
		}
		return _regexFix_otherScript;
	}
}
