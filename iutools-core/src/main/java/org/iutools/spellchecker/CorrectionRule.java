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

	public CorrectionRule(String _regexBad, String _regexFix) throws SpellCheckerException {
		init__CorrectionRule(_regexBad, _regexFix);
	}


	private void init__CorrectionRule(String _regexBad, String _regexFix) throws SpellCheckerException {

		if (_regexFix == null) {
			// If no rule has been provided to actually fix the faulty characters,
			// use a regexp that hihglights them with a pair of brackets []
			_regexFix = "[$0]";
		}
		this.regexBad = _regexBad;
		Script script1 = TransCoder.textScript(regexBad);
		if (script1 == Script.MIXED) {
			throw new SpellCheckerException("Left-hand side of the rule was written in a 'mixed' script: "+this.toString());
		}
		if (_regexFix != null) {
			this.regexFix = _regexFix;
			Script script2 = TransCoder.textScript(regexFix);

			if (script2 == Script.MIXED) {
				throw new SpellCheckerException("Right-hand side of the rule was written in a 'mixed' script: " + this.toString());
			}
			if (script1 != script2) {
				throw new SpellCheckerException("The two sides of the rule were written in different scripts: " + this.toString());
			}
		}
		this.ruleScript = script1;
	}

	@Override
	public String toString() {
		String toS = "'"+regexBad+"' --> '"+regexFix+"'";
		return toS;
	}

	public Pattern pattBad() {
		if (_pattBad == null) {
			_pattBad = Pattern.compile(regexBad);
		}
		return _pattBad;
	}

	public IUWord fixWord(IUWord origWord) throws SpellCheckerException {
		Logger logger = LogManager.getLogger("org.iutools.spellchecker.CorrectionRule.fixWord");

		String origStr = origWord.inScript(ruleScript);
		String fixedStr = pattBad().matcher(origStr).replaceAll(regexFix);
		String result = "'"+origStr+"' " +
			(fixedStr.equals(origStr) ? "UNCHANGED": "-> '"+fixedStr+"'");
		if (!origStr.equals(fixedStr) && logger.isTraceEnabled()) {
			logger.trace(origWord + ": rule fired\n  Rule: " + this.toString() + "\n  Result: --> " + fixedStr);
		}

		IUWord fixedWord = null;
		try {
			fixedWord = new IUWord(fixedStr, ruleScript);
		} catch (WordException e) {
			throw new SpellCheckerException(e);
		}
		return fixedWord;
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
