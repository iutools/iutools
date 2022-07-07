package org.iutools.morph;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iutools.utilities.StopWatch;

/**
 * Class for calling the morphological analyzer in a way that supports 
 * timeouts.
 * 
 * @author desilets
 *
 */
public class MorphAnalyzerTask implements Callable<Decomposition[]> {

	StopWatch stopWatch = null;
	String word = null;
	boolean lenient = true;
	MorphologicalAnalyzer analyzer = null;
	
	public MorphAnalyzerTask(String _word, Boolean _lenient, 
			MorphologicalAnalyzer morphologicalAnalyzerAbstract) {
		this.word = _word;
		if (_lenient != null) {
			this.lenient = _lenient;
		}
		this.analyzer = morphologicalAnalyzerAbstract;
	}
	
	@Override
	public Decomposition[] call() throws Exception {
		Logger mLogger = LogManager.getLogger("ca.inuktitutcomputing.morph.MorphAnalyzerTask.call");
		mLogger.trace("Calling on word="+word);
		long start = System.currentTimeMillis();
		Decomposition[] decomps = new Decomposition[0];
		try {
			decomps = analyzer.doDecompose(word, lenient);
		} catch (TimeoutException e) {
			mLogger.trace("For word="+word+", caught TimeoutException e="+e.getMessage());
			throw e;
		}
		long elapsed = System.currentTimeMillis() - start;
		
		mLogger.trace("exiting for word="+word);

		return decomps;
	}
}
