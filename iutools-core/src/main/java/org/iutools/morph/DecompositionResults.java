package org.iutools.morph;

/**
 * Outcome of calling decomposeWord() on a test word.
 * 
 * @author desilets
 *
 */
public class DecompositionResults {
	public String word = null;
	
	public boolean timedOut = false;
	public Decomposition[] decompositions = new Decomposition[0];
	public long totalMSecs = 0;
	
	
	public DecompositionResults(String word) {
		init_DecompositionResults(word);
	}
	
	private void init_DecompositionResults(String _word) {
		this.word = _word;		
	}

	public boolean includesDecomp(String decomp) {
		boolean includes = (null != decompRank(decomp));		
		return includes;
	}
	
	public Integer decompRank(String decomp) {
		Integer rank = null;
		for (int ii = 0; ii < decompositions.length; ii++) {
			if (decompositions[ii].toString().equals(decomp)) {
				rank = ii;
				break;
			}
		}
		
		return rank;
	}

	
	public String joinDecomps() {
		String joined = "";
		for (Decomposition aDecomp: decompositions) {
			joined += "  "+aDecomp.toString()+"\n";
		}
		return joined;
	}
}
