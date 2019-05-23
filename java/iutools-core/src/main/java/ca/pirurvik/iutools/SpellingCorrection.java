package ca.pirurvik.iutools;

import java.util.ArrayList;
import java.util.List;

public class SpellingCorrection {
	public String orig;
	public List<String> corrections;
	
	public SpellingCorrection(String _orig, List<String> _corrections) {
		initialize(_orig, _corrections);
	}
	
	public SpellingCorrection(String _orig) {
		initialize(_orig, null);
	}

	private void initialize(String _orig, List<String> _corrections) {
		this.orig = _orig;
		
		if (_corrections == null) _corrections = new ArrayList<String>();
		this.corrections = _corrections;
	}

	public boolean wasMispelled() {
		return corrections.size() > 0;
	}

}
