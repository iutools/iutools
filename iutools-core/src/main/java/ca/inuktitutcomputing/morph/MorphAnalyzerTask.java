package ca.inuktitutcomputing.morph;

import java.util.concurrent.Callable;

/**
 * Class for calling the morphological analyzer in a way that supports 
 * timeouts.
 * 
 * @author desilets
 *
 */
public class MorphAnalyzerTask implements Callable<Decomposition[]> {

	String word = null;
	boolean lenient = false;
	MorphologicalAnalyzerAbstract analyzer = null;
	
	public MorphAnalyzerTask(String _word, boolean _lenient, 
			MorphologicalAnalyzerAbstract morphologicalAnalyzerAbstract) {
		this.word = _word;
		this.lenient = _lenient;
		this.analyzer = morphologicalAnalyzerAbstract;
	}
	
	@Override
	public Decomposition[] call() throws Exception {
		Decomposition[] decomps = analyzer.doDecompose(word);
		return decomps;
	}
	
}
