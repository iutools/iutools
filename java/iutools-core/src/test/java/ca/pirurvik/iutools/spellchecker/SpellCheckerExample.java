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
	public Integer maxRankAssumingInDict = null;
	public Integer maxRankNOTAssumingInDict = null;

	public SpellCheckerExample(String _wordToCheck) {
		super(_wordToCheck);
		this.init_SpellCheckerExample(_wordToCheck, -1, null, 
				new String[] {});
	}	

	public SpellCheckerExample(String _wordToCheck, Integer _expMaxRank, 
			String... _acceptableCorrections) {
		super(_wordToCheck);
		this.init_SpellCheckerExample(_wordToCheck, _expMaxRank, null,
				_acceptableCorrections);
	}

	public SpellCheckerExample(String _wordToCheck, 
			Integer _maxRankAssumingInDic, Integer _maxRankNOTAssumingInDic,
			String... _acceptableCorrections) {
		super(_wordToCheck);
		this.init_SpellCheckerExample(_wordToCheck, 
				_maxRankAssumingInDic, _maxRankNOTAssumingInDic, 
				_acceptableCorrections);
	}

	
	private void init_SpellCheckerExample(String _wordToCheck, 
			Integer _maxRankAssumingInDict, 
			Integer _maxRankNOTAssumingInDict,
			String[] _acceptableCorrections) {
		this.wordToCheck = _wordToCheck;
		this.maxRankAssumingInDict = _maxRankAssumingInDict;
		this.maxRankNOTAssumingInDict = _maxRankNOTAssumingInDict;
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
				"   exp max rank: "+maxRankAssumingInDict
				;
		return str;
	}
}
