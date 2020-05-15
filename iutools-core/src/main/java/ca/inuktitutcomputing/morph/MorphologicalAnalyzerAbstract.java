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
	
	/**
	 * Strategy used to monitor execution of the analyzer and timing it out 
	 * after a set time.
	 * 
	 * - EXECUTOR: Use the Java Executor+Future classes
	 * - STOPWATCH: Use the homegrown StopWatch class
	 * - BOTH: Use the EXECUTOR approach in tandem with STOPWATCH 
	 * 
	 * @author desilets
	 *
	 */
	public static enum TimeoutStrategy {
		EXECUTOR, STOPWATCH, BOTH}
	
//	protected TimeoutStrategy timeoutStrategy = TimeoutStrategy.STOPWATCH;
//	protected TimeoutStrategy timeoutStrategy = TimeoutStrategy.BOTH;
	protected TimeoutStrategy timeoutStrategy = TimeoutStrategy.EXECUTOR;
    protected Long millisTimeout = new Long(10*1000);
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
    	
    	long start = System.currentTimeMillis();
    	if (timeoutStrategy != TimeoutStrategy.EXECUTOR) {
    		this.stpw = new StopWatch(millisTimeout);
    	}
    	
    	Logger tLogger = Logger.getLogger("ca.inuktitutcomputing.morph.decomposeWord");
    	tLogger.trace("word="+word+", lenient="+lenient);
    	
    	Decomposition[] decomps = null;
    	
    	MorphAnalyzerTask task = new MorphAnalyzerTask(word, lenient, this);
    	
		String mess = "Decomp of word="+word+"; STARTS at "+start+"msecs";
		tLogger.trace(mess);
    	
    	if (timeoutStrategy == TimeoutStrategy.STOPWATCH) {
    		decomps = invokeDirectly(word, lenient);
    	} else {
    		decomps = invokeThroughExecutor(task);
    	}
    	
		long elapsed = System.currentTimeMillis() - start;
		mess = "Decompositio of word="+word+"; ENDS with elapsed="+elapsed+"msecs";
		tLogger.trace(mess);
		
    	return decomps;
    }

    private Decomposition[] invokeThroughExecutor(MorphAnalyzerTask task) 
    		throws TimeoutException, MorphologicalAnalyzerException {
    	Logger tLogger = Logger.getLogger("ca.inuktitutcomputing.morph.invokeThroughExecutor");
    	
    	String word = task.word;
		
        ExecutorService executor = Executors.newCachedThreadPool();
		Future<Decomposition[]> future = executor.submit(task);
		Decomposition[] decomps = null;	
    	long start = System.currentTimeMillis();
		
		try {
			if (timeoutActive) {
				decomps = (Decomposition[])future.get(millisTimeout, TimeUnit.MILLISECONDS);
			} else {
				decomps = (Decomposition[])future.get();
			}
		} catch (InterruptedException e) {
			tLogger.trace("Caught InterruptedException");
		} catch (ExecutionException e) {
			tLogger.trace("Caught ExecutionException e.getClass()="+e.getClass()+", e.getCause()="+e.getCause()+", e="+Debug.printCallStack(e));
			Throwable cause =  e.getCause();
			if (cause instanceof TimeoutException) {
				throw (TimeoutException) cause;
			} else if (cause instanceof Exception){
				throw new MorphologicalAnalyzerException((Exception)cause);
			} else {
				throw new MorphologicalAnalyzerException(e.getLocalizedMessage());
			}
		} finally {
			checkElapsedTime(word, start);
			future.cancel(true); // may or may not desire this
			executor.shutdown();
		}	
				
		return decomps;
	}

	private Decomposition[] invokeDirectly(String word, boolean lenient) 
			throws TimeoutException, MorphologicalAnalyzerException {
    	Logger tLogger = Logger.getLogger("ca.inuktitutcomputing.morph.invokeDirectly");
    	
    	long start = System.currentTimeMillis();
    	Decomposition[] decomps = null;    	
		try {
			decomps = doDecompose(word, lenient);
		} catch (TimeoutException e) {
			throw(e);
		} catch (Exception e) {
			tLogger.trace("Caught Exception e.getClass()="+e.getClass()+", e.getCause()="+e.getCause()+", e="+Debug.printCallStack(e));
			Exception cause = (Exception) e.getCause();
			if (cause instanceof TimeoutException) {
				throw (TimeoutException) cause;
			} else {
				throw new MorphologicalAnalyzerException(cause);
			}
		} finally {
			checkElapsedTime(word, start);
		}	
				
		return decomps;
	}

	private void checkElapsedTime(String word, long start) {
    	Logger tLogger = Logger.getLogger("ca.inuktitutcomputing.morph.MorphologicalAnalyzerAbstract.checkElapsedTime");
    	
    	if (timeoutActive) {
	    	long elapsedMSecs = System.currentTimeMillis() - start;
	    	if (elapsedMSecs > 1.1 * millisTimeout) {
				tLogger.trace(
					"word="+word+
					"; Elapsed time was significantly greater than the specified timeout value.\n"+
					"  Elapsed: "+elapsedMSecs/1000+"secs\n"+
					"  Timeout: "+millisTimeout/1000+"secs");
	    	}
			
			long excess = elapsedMSecs - millisTimeout;
			
			if (excess  > 2*1000) {
				String mess = 
					"Word "+word+" exceeded millisTimeout="+millisTimeout+
					" by "+excess+"msecs (elapsedMSecs="+
					elapsedMSecs+"msecs)\ncallStack="+Debug.printCallStack();
				tLogger.trace(mess);
			} 
    	}
    	
    	if (tLogger.isTraceEnabled()) {
    		tLogger.trace("Upon exit, number of threads = "+Thread.activeCount());
    	}
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
    	return setTimeout(new Long(val));
    }

    public MorphologicalAnalyzerAbstract setTimeout(Long val) {
    	if (val != null) {
    		millisTimeout = val;
    	}
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
    
    public MorphologicalAnalyzerAbstract setTimeoutStrategy(TimeoutStrategy _strat) {
    	this.timeoutStrategy = _strat;
    	return this;
    }
}
