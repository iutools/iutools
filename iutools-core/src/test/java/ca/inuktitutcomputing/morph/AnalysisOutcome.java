package ca.inuktitutcomputing.morph;

/**
 * Outcome of calling decomposeWord() on a test word.
 * 
 * @author desilets
 *
 */
public class AnalysisOutcome {
	public boolean timedOut = false;
	public Decomposition[] decompositions = new Decomposition[0];
	
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
