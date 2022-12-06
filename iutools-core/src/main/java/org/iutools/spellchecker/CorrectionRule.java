package org.iutools.spellchecker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;

import java.util.regex.Pattern;

import static org.iutools.script.TransCoder.Script;

/**
 * This class captures a rule for correcting certain types of "shallow" spelling
 * mistake.
 *
 * By "shallow rule", we mean a rule that does not require any knowledge about
 * morphology. Just knowlege about certain sequences of characters that are
 * ALWAYS wrong no matter the word.
 *
 * Many of those "mistakes" have to do with "old fashioned" spelling in certain
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

	/** Rule for the other script */
	private String _regexBad_otherScript = null;
	private String _regexFix_otherScript = null;

	/** If true, the rule applies to both ROMAN and SYLLABIC scripts*/
	protected boolean appliesToBothScripts = false;

	public CorrectionRule(String _regexBad, String _regexFix) throws SpellCheckerException {
		init__CorrectionRule(_regexBad, _regexFix, (Boolean)null);
	}

	public CorrectionRule(String _regexBad, String _regexFix,
		Boolean _appliesToBothScripts) throws SpellCheckerException {
		init__CorrectionRule(_regexBad, _regexFix, _appliesToBothScripts);
	}


	private void init__CorrectionRule(String _regexBad, String _regexFix,
		Boolean _appliesToBothScripts) throws SpellCheckerException {
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
		if (_appliesToBothScripts != null) {
			appliesToBothScripts = _appliesToBothScripts;
		}
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

	public String fixWord(String word) throws SpellCheckerException {
		Logger logger = LogManager.getLogger("org.iutools.spellchecker.CorrectionRule.fixWord");
		String fixed = word;
		logger.trace("rule is: "+this);
		if (regexFix != null) {
			fixed = word.replaceAll(regexBad, regexFix);
			if (fixed.equals(word)) {
				// The rule had no effect.
				// Try to apply the rule in the other script
				String before = fixed;
				fixed = word.replaceAll(regexBad_otherScript(), regexFix_otherScript());
				String result = "'"+before+"' ";
				if (before.equals(fixed)) {
					result += "UNCHANGED";
				} else {
					result += "-> '"+fixed+"'";
				}
				logger.trace(result);
				int x = 1;
			}
		}
		return fixed;
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
