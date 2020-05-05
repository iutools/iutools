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
		boolean includes = false;
		for (Decomposition aDecomp: decompositions) {
			if (aDecomp.toString().equals(decomp)) {
				includes = true;
				break;
			}
		}
		
		return includes;
	}
	
	public String joinDecomps() {
		String joined = "";
		for (Decomposition aDecomp: decompositions) {
			joined += "  "+aDecomp.toString()+"\n";
		}
		return joined;
	}
}
