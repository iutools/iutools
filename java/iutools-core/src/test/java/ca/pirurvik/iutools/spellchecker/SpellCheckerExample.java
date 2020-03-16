package ca.pirurvik.iutools.spellchecker;

import java.util.HashSet;
import java.util.Set;

/** 
 * This class represents an example used for evaluating
 * the SpellChecker.
 * 
 * @author desilets
 *
 */
public class SpellCheckerExample extends BinaryClassifierExample {
	
	String wordToCheck = null;
	Set<String> acceptableCorrections = null;
	public int expMaxRank;

	public SpellCheckerExample(String _wordToCheck) {
		super(_wordToCheck);
		this.init_SpellCheckerExample(_wordToCheck, -1, new String[] {});
	}
	

	public SpellCheckerExample(String _wordToCheck, int _expMaxRank, String... _acceptableCorrections) {
		super(_wordToCheck);
		this.init_SpellCheckerExample(_wordToCheck, _expMaxRank, _acceptableCorrections);
	}

	private void init_SpellCheckerExample(String _wordToCheck, 
			int _expMaxRank, String[] _acceptableCorrections) {
		this.wordToCheck = _wordToCheck;
		this.expMaxRank = _expMaxRank;
		this.acceptableCorrections = new HashSet<String>();
		for (String anAcceptable: _acceptableCorrections) {
			this.acceptableCorrections.add(anAcceptable);
		}
		
		if (_acceptableCorrections == null && _acceptableCorrections.length == 0) {
			this.corectCategory = "ok";
		} else {
			this.corectCategory = "misspelled";
		}
	}
	
	public String key() {
		return wordToCheck;
	}
	
	public String toString() {
		String str = 
				wordToCheck+
				" -->\n"+
				"   ["+String.join(",", acceptableCorrections)+"]\n"+
				"   exp max rank: "+expMaxRank
				;
		return str;
	}
}
