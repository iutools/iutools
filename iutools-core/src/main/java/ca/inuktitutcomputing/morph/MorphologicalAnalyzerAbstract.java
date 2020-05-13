package ca.inuktitutcomputing.morph;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.utilities.StopWatch;
import ca.nrc.debug.Debug;

public abstract class MorphologicalAnalyzerAbstract {
	
    protected Long millisTimeout = new Long(10000);
    protected boolean timeoutActive = true;
    protected StopWatch stpw;
    
    /** 
     * DÉCOMPOSITION DU MOT.
     * Les décompositions résultantes sont ordonnées selon certaines règles.
     * L'analyse tient compte de certaines habitudes : l'omission de la consonne finale,
     * et l'usage de 'n' au lieu de 't' en finale.
     * @param word String word to be decomposed, in syllabics or roman alphabet
     * @return Decomposition[] array of decompositions
     * @throws LinguisticDataException 
     * @throws MorphologicalAnalyzerException 
     */
     public Decomposition[] decomposeWord(String word) 
    	throws TimeoutException, MorphologicalAnalyzerException {
    	 return decomposeWord(word, null);
     }
     
    /** 
     * DÉCOMPOSITION DU MOT.
     * Les décompositions résultantes sont ordonnées selon certaines règles.
     * @param word String word to be decomposed, in syllabics or roman alphabet
     * @param extendedAnalysis boolean if true, check also for possible missing consonant at the end of the word
     * @return Decomposition[] array of decompositions
     * @throws LinguisticDataException 
     * @throws MorphologicalAnalyzerException 
     */
    public Decomposition[] decomposeWord(String word, Boolean lenient) 
    		throws TimeoutException, MorphologicalAnalyzerException {
    	if (lenient == null) {
    		lenient = true;
    	}
    	
    	Logger tLogger = Logger.getLogger("ca.inuktitutcomputing.morph.decomposeWord");
    	tLogger.trace("word="+word+", lenient="+lenient);
    	
    	Decomposition[] decomps = null;
    	
    	MorphAnalyzerTask task = new MorphAnalyzerTask(word, lenient, this);
    	
		ExecutorService executor = Executors.newCachedThreadPool();
		Future<Decomposition[]> future = executor.submit(task);
		try {
			if (timeoutActive) {
				decomps = (Decomposition[])future.get(millisTimeout, TimeUnit.MILLISECONDS);
			} else {
				decomps = (Decomposition[])future.get();
			}
			System.out.println("   Completed successfully with decomps.length="+decomps.length);
		} catch (InterruptedException e) {
			tLogger.trace("Caught InterruptedException");
		} catch (ExecutionException e) {
			tLogger.trace("Caught ExecutionException e.getClass()="+e.getClass()+", e.getCause()="+e.getCause()+", e="+Debug.printCallStack(e));
			Exception cause = (Exception) e.getCause();
			if (cause instanceof TimeoutException) {
				throw (TimeoutException) cause;
			} else {
				throw new MorphologicalAnalyzerException(cause);
			}
		} finally {
		   future.cancel(true); // may or may not desire this
		}	
    	
    	return decomps;
    }

    protected Decomposition[] doDecompose(String word) 
			throws MorphologicalAnalyzerException, TimeoutException {
		return doDecompose(word, null);
	}
    
	protected abstract Decomposition[] doDecompose(String word, Boolean lenient) 
		throws MorphologicalAnalyzerException, TimeoutException;
    
    public MorphologicalAnalyzerAbstract() throws LinguisticDataException {
    	
    }
    
    public MorphologicalAnalyzerAbstract setTimeout(long val) {
    	millisTimeout = val;
    	return this;
    }
    
    public MorphologicalAnalyzerAbstract disactivateTimeout() {
    	timeoutActive = false;
    	return this;
    }
    
    public MorphologicalAnalyzerAbstract activateTimeout() {
    	timeoutActive = true;
    	return this;
    }
}
