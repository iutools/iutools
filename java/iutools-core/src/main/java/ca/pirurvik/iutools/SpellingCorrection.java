package ca.pirurvik.iutools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpellingCorrection {
	public String orig;
	public Boolean wasMispelled = false;
	private List<String> possibleSpellings = new ArrayList<String>();

	public SpellingCorrection(String _orig, String[] _corrections, Boolean _wasMispelled) {
		List<String> correctionsList = Arrays.asList(_corrections);
		initialize(_orig, correctionsList, _wasMispelled);
	}

	
	public SpellingCorrection(String _orig, List<String> _corrections) {
		initialize(_orig, _corrections, true);
	}
	
	public SpellingCorrection(String _orig) {
		initialize(_orig, null, null);
	}

	public SpellingCorrection(String word, boolean _wasMispelled) {
		initialize(word, null, _wasMispelled);
	}


	private void initialize(String _orig, List<String> _corrections, Boolean _wasMispelled) {
		this.orig = _orig;
		if (_corrections != null) this.possibleSpellings = _corrections;
		if (_wasMispelled != null) this.wasMispelled = _wasMispelled;
	}
	
	public SpellingCorrection setPossibleSpellings(List<String> possSpellings) {
		
		this.possibleSpellings = possSpellings;
		
		if (possSpellings != null && possSpellings.size() > 0 && possSpellings.get(0).equals(orig)) {
			this.possibleSpellings.remove(0);
		}
		return this;
	}

	public List<String> getPossibleSpellings() {
		return this.possibleSpellings;
	}


}
