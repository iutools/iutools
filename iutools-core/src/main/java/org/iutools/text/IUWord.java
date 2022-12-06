package org.iutools.text;

import org.iutools.script.TransCoder;
import static org.iutools.script.TransCoder.Script;

public class IUWord extends Word {
	private Script _origScript = null;
	String wordInRoman = null;
	String wordInSyll = null;

	public IUWord(String __word) throws WordException {
		super(__word, "iu");
		this._origScript = TransCoder.textScript(__word);
		if (_origScript == Script.MIXED) {
			throw new WordException("IUWord is a mix of SYLLABIC and ROMAN scripts: "+ this._word);
		}
	}

	public Script origScript() {
		return _origScript;
	}

	public String inRoman() {
		if (wordInRoman == null) {
			wordInRoman = TransCoder.ensureRoman(_word);
		}
		return wordInRoman;
	}

	public String inSyll() {
		if (wordInSyll == null) {
			wordInSyll = TransCoder.ensureSyllabic(_word);
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

	@Override
	public String toString() {
		String str = "[roman: '"+this.inRoman()+"'; syll: '"+inSyll()+"']";
		return str;
	}
}
