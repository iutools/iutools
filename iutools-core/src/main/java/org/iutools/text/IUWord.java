package org.iutools.text;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.script.TransCoder;
import static org.iutools.script.TransCoder.Script;

public class IUWord extends Word {
	private Script _origScript = null;
	String wordInRoman = null;
	String wordInSyll = null;


	public IUWord(String __word) throws WordException {
		super(__word, "iu");
		init__IUWord(__word, (Script)null);
	}

	public IUWord(String __word, Script __inScript) throws WordException {
		super(__word, "iu");
		init__IUWord(__word, __inScript);
	}

	private void init__IUWord(String __word, Script __origScript) throws WordException {
		int x = 0;
		if (__origScript == null) {
			__origScript = TransCoder.textScript(__word);
		}
		this._origScript = __origScript;
		if (_origScript == Script.MIXED) {
			throw new WordException("IUWord is a mix of SYLLABIC and ROMAN scripts: "+ this._word);
		}
		if (_origScript == Script.ROMAN) {
			wordInRoman = __word;
		} else {
			wordInSyll = __word;
		}
	}

	public Script origScript() {
		return _origScript;
	}

	public String inRoman() {
		if (wordInRoman == null) {
			wordInRoman = TransCoder.ensureRoman(_word, true);
		}
		return wordInRoman;
	}

	public String inSyll() {
		if (wordInSyll == null) {
			wordInSyll = TransCoder.ensureSyllabic(_word, true);
		}
		return wordInSyll;
	}

	public String inScript(TransCoder.Script script) {
		String word = null;
		if (script == TransCoder.Script.ROMAN) {
			word = inRoman();
		} else {
			word = inSyll();
		}
		return word;
	}

	public Script otherScript() {
		Script script = Script.SYLLABIC;
		if (origScript() == Script.SYLLABIC) {
			script = Script.ROMAN;
		}
		return script;
	}

	public String inOtherScript() {
		return inScript(otherScript());
	}

	@Override
	public String toString() {
		String str = "[roman: '"+this.inRoman()+"'; syll: '"+inSyll()+"']";
		return str;
	}

	@Override
	public boolean equals(Object other) {
		Boolean answer = null;
		// First, check that other is an IUWord
		if (! (other instanceof IUWord)) {
			answer = false;
		}
		// Next, check if they are written the same way in Roman
		if (answer == null) {
			IUWord otherWord = (IUWord) other;
			String thisStr = this.word();
			String otherStr = otherWord.inScript(this.origScript());
			answer = (thisStr.equals(otherStr));
		}

		return answer;
	}
}
