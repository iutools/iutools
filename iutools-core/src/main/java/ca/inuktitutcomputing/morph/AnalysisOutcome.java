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
	public Exception raisedException = null;
}
