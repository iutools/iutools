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

public abstract class MorphologicalAnalyzerAbstract implements AutoCloseable {
	
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
	protected TimeoutStrategy timeoutStrategy = TimeoutStrategy.BOTH;
//	protected TimeoutStrategy timeoutStrategy = TimeoutStrategy.EXECUTOR;
    protected Long millisTimeout = new Long(10*1000);
    protected boolean timeoutActive = true;
    protected StopWatch stpw;
    
    protected static ExecutorService executor = null;
    
    
    public void close() throws Exception {
    	System.out.println("--** MorphologicalAnalyzerAbstract.close: INVOKED");
    }
    
    // TODO: Make that synchronized?
    public static void shutdownExecutorPool() {
    	Logger mLogger = Logger.getLogger("ca.inuktitutcomputing.morph.MorphologicalAnalyzerAbstract.shutdownExecutorPool");
    	if (executor != null) {
    		int secs = 0;
        	mLogger.trace("Shutting down morph executor pool, #threads="+Thread.activeCount());
        	try {
            	mLogger.trace("** Sleeping for "+secs+" secs before shutdown");
				Thread.sleep(secs*1000);
			} catch (InterruptedException e) {
				mLogger.trace("InterruptedException caught: "+e.getMessage());
			}
    		executor.shutdownNow();
    	}
    	executor = null;
    	mLogger.trace("Upon exit, #threads="+Thread.activeCount());
    }
    
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
    		try {
				decomps = invokeThroughExecutor(task);
			} catch (InterruptedException e) {
				tLogger.trace("InterruptedException caught: "+e.getMessage());				
			}
    	}
    	
		long elapsed = System.currentTimeMillis() - start;
		mess = "Decompositio of word="+word+"; ENDS with elapsed="+elapsed+"msecs";
		tLogger.trace(mess);
		
    	return decomps;
    }

    private Decomposition[] invokeThroughExecutor(MorphAnalyzerTask task) 
    		throws TimeoutException, MorphologicalAnalyzerException, InterruptedException {
    	Logger tLogger = Logger.getLogger("ca.inuktitutcomputing.morph.invokeThroughExecutor");
    	
    	String word = task.word;
    	tLogger.trace("processing word="+word);
    	if (executor == null) {
    		// TODO: Make the creation of the executor synchronized
    		//   ... as in https://dzone.com/articles/prevent-breaking-a-singleton-class-pattern
    		// make it a call to a method getExecutor()...
    		// The method itself will not be synchronized, but the bit of code that actually
    		// creates the executor will be.
    		//
    		tLogger.trace("Before creating the executor, #threads="+Thread.activeCount());
    		executor = Executors.newCachedThreadPool();
    		tLogger.trace("AFTER creating the executor, #threads="+Thread.activeCount());
    	}

		
		tLogger.trace("Before submitting the future, #threads="+Thread.activeCount());
		Future<Decomposition[]> future = executor.submit(task);
		tLogger.trace("After submitting the future, #threads="+Thread.activeCount());
		Decomposition[] decomps = null;	
    	long start = System.currentTimeMillis();
		
		tLogger.trace("Before getting the future, #threads="+Thread.activeCount());
		try {
			if (timeoutActive) {
				decomps = (Decomposition[])future.get(millisTimeout, TimeUnit.MILLISECONDS);
			} else {
				decomps = (Decomposition[])future.get();
			}
	    	tLogger.trace("Future  has completed normally, #threads="+Thread.activeCount());
//		} catch (InterruptedException e) {
//			tLogger.trace("Future for word="+task.word+" caught InterruptedException");
		} catch (ExecutionException e) {
			tLogger.trace("Future for word="+task.word+
				" caught ExecutionException e.getClass()="+e.getClass()+
				", e.getCause()="+e.getCause()+", e="+Debug.printCallStack(e)+
				"\n#threads="+Thread.activeCount());
			Throwable cause =  e.getCause();
			if (cause instanceof TimeoutException) {
				throw (TimeoutException) cause;
			} else if (cause instanceof Exception){
				throw new MorphologicalAnalyzerException((Exception)cause);
			} else {
				throw new MorphologicalAnalyzerException(e.getLocalizedMessage());
			}
		} finally {
			
			tLogger.trace("'finally' clause; Before cancelling the future, #threads="+Thread.activeCount());
			checkElapsedTime(word, start);
			future.cancel(true); // may or may not desire this
			tLogger.trace("'finally' clause; After cancelling the future, #threads="+Thread.activeCount());
//			executor.shutdown();
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
