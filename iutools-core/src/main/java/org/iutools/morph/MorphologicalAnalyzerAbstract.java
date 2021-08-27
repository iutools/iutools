package org.iutools.morph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.utilities.StopWatch;
import ca.nrc.debug.Debug;
import ca.nrc.string.StringUtils;

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
	
	protected TimeoutStrategy timeoutStrategy = TimeoutStrategy.STOPWATCH;
//	protected TimeoutStrategy timeoutStrategy = TimeoutStrategy.BOTH;
//	protected TimeoutStrategy timeoutStrategy = TimeoutStrategy.EXECUTOR;
    protected Long millisTimeout = new Long(10*1000);
    protected boolean timeoutActive = true;
    protected StopWatch stpw;
    protected boolean decomposeCompositeRoot = true;


	protected static ExecutorService executor = null;
    public static Map<String,Future<Decomposition[]>> taskFutures =
    	new HashMap<String,Future<Decomposition[]>>();

    public void setDecomposeCompositeRoot(boolean value) {
		decomposeCompositeRoot = value;
	}
    
    public void close() throws Exception {
    	System.out.println("--** MorphologicalAnalyzerAbstract.close: INVOKED");
    }
    
    // TODO: Make that synchronized?
    public static void shutdownExecutorPool() {
    	Logger mLogger = Logger.getLogger("ca.inuktitutcomputing.morph.MorphologicalAnalyzerAbstract.shutdownExecutorPool");
    	
    	traceTasks(mLogger, "Before shutting down executor pool");
    	
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
    	traceTasks(mLogger, "AFTER shutting down executor pool");
    	executor = null;
    	mLogger.trace("Upon exit, #threads="+Thread.activeCount());
    }
    
    private static void traceTasks(Logger logger) {
    	traceTasks(logger, null);
    }

    
    private static void traceTasks(Logger logger, String mess) {
    	if (mess == null) {
    		mess = "";
    	}
		if (logger.isTraceEnabled()) {
			int numDone = 0;
			int numCancelled = 0;
			int numPending = 0;
			Set<String> pendingWords = new HashSet<String>();
			Set<String> cancelledWords = new HashSet<String>();
			for (String word: taskFutures.keySet()) {
				Future<Decomposition[]> aFuture = taskFutures.get(word);
				if (aFuture.isCancelled()) {
					cancelledWords.add(word);
					numCancelled++;
				} else if (aFuture.isDone()) {
					numDone++;
				} else {
					pendingWords.add(word);
					numPending++;
				}
			}
			mess += 
				"\nState of all decomposition tasks\n"+
				"  #Done      : "+numDone+"\n"+
				"  #Cancelled : "+numCancelled+"\n"+
				"  #Pending   : "+numPending+"\n"+
				"\n"+
				"Cancelled words:\n  "+
				StringUtils.join(cancelledWords.iterator(), "\n  ")+"\n"+
				"\n"+
				"Pending words:\n  "+
				StringUtils.join(pendingWords.iterator(), "\n  ")
				;
			
			logger.trace(mess);			
		}
	}

	  public DecompositionSimple[] decomposeWord_NEW(String word)
	  throws TimeoutException, MorphologicalAnalyzerException {
    	 return decomposeWord_NEW(word, (Boolean)null);
     }

	public DecompositionSimple[] decomposeWord_NEW(String word, Boolean lenient)
	throws TimeoutException, MorphologicalAnalyzerException {
		DecompositionSimple[] decomps = null;
		try {
			Decomposition[] decompStates = decomposeWord(word, lenient);
			decomps = new DecompositionSimple[decompStates.length];
			for (int ii = 0; ii < decompStates.length; ii++) {
				decomps[ii] = decompStates[ii].toSimpleDecomposition();
			}
		} catch (DecompositionExcepion e) {
			throw new MorphologicalAnalyzerException(e);
		}
		return decomps;
	}

	/**
	 * DecompositionSimple of an Inuktitut word.
     * DÉCOMPOSITION DU MOT.
     * Les décompositions résultantes sont ordonnées selon certaines règles.
     * L'analyse tient compte de certaines habitudes : l'omission de la consonne finale,
     * et l'usage de 'n' au lieu de 't' en finale.
     * @param word String word to be decomposed, in syllabics or roman alphabet
     * @return DecompositionSimple[] array of decompositions
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
     * @param lenient boolean if true, check also for possible missing consonant at the end of the word
     * @return DecompositionSimple[] array of decompositions
     * @throws LinguisticDataException 
     * @throws MorphologicalAnalyzerException 
     */
    public Decomposition[] decomposeWord(String word, Boolean lenient)
    		throws TimeoutException, MorphologicalAnalyzerException {
    	if (lenient == null) {
    		lenient = true;
    	}
    	
    	long start = System.currentTimeMillis();
    	if (timeoutStrategy != TimeoutStrategy.EXECUTOR && timeoutActive) {
    		this.stpw = new StopWatch(millisTimeout, "Decomposing word="+word);
    	}
    	
    	Logger tLogger = Logger.getLogger("ca.inuktitutcomputing.morph.decomposeWord");
    	tLogger.trace("word="+word+", lenient="+lenient);
    	
    	Decomposition[] decomps = null;
    	
    	MorphAnalyzerTask task = new MorphAnalyzerTask(word, lenient, this);
    	
		String mess = "Decomp of word="+word+"; STARTS at "+start+"msecs";
		tLogger.trace(mess);

		tLogger.trace("timeoutStrategy: "+timeoutStrategy);
    	if (timeoutStrategy == TimeoutStrategy.STOPWATCH) {
    		tLogger.trace("invoke directly");
    		decomps = invokeDirectly(word, lenient);
    	} else {
    		try {
    			tLogger.trace("invoke through task");
				decomps = invokeThroughExecutor(task);
			} catch (InterruptedException e) {
				tLogger.trace("InterruptedException caught: "+e.getMessage());				
			}
    	}
    	
		long elapsed = System.currentTimeMillis() - start;
		mess = "DecompositionSimple of word="+word+"; ENDS with elapsed="+elapsed+"msecs";
		tLogger.trace(mess);
		
    	return decomps;
    }

    private Decomposition[] invokeThroughExecutor(MorphAnalyzerTask task)
    		throws TimeoutException, MorphologicalAnalyzerException, InterruptedException {
    	Logger tLogger = Logger.getLogger("ca.inuktitutcomputing.morph.invokeThroughExecutor");
    	
    	String word = task.word;
    	traceTasks(tLogger, "processing word="+word);
    	if (executor == null) {
    		// TODO: Make the creation of the executor synchronized
    		//   ... as in https://dzone.com/articles/prevent-breaking-a-singleton-class-pattern
    		// make it a call to a method getExecutor()...
    		// The method itself will not be synchronized, but the bit of code that actually
    		// creates the executor will be.
    		//
    		traceTasks(tLogger, "Before creating the executor for word="+word+", #threads="+Thread.activeCount());
    		executor = Executors.newCachedThreadPool();
    		traceTasks(tLogger, "AFTER creating the executor for word="+word+", #threads="+Thread.activeCount());
    	}

		
    	traceTasks(tLogger, "Before submitting the future for word="+word+", #threads="+Thread.activeCount());
		Future<Decomposition[]> future = executor.submit(task);
		taskFutures.put(word, future);
		traceTasks(tLogger, "After submitting the future for word="+word+", #threads="+Thread.activeCount());
		Decomposition[] decomps = null;
    	long start = System.currentTimeMillis();
		
    	traceTasks(tLogger, "Before getting the future for word="+word+", #threads="+Thread.activeCount());
		try {
			if (timeoutActive) {
				decomps = (Decomposition[])future.get(millisTimeout, TimeUnit.MILLISECONDS);
			} else {
				decomps = (Decomposition[])future.get();
			}
			traceTasks(tLogger, "Future for word="+word+" has completed normally, #threads="+Thread.activeCount());
		} catch (InterruptedException e) {
			traceTasks(tLogger, "Future for word="+task.word+" caught InterruptedException; Rethrowing it...");
			throw e;
		} catch (ExecutionException e) {
			traceTasks(tLogger, "Future for word="+task.word+
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
			traceTasks(tLogger, "'finally' clause for word="+word+"; at beginning, #threads="+Thread.activeCount());
			checkElapsedTime(word, start);
//			future.cancel(true); // may or may not desire this
			tLogger.trace("'finally' clause for word="+word+"; at END, #threads="+Thread.activeCount());
//			executor.shutdown();
		}	
				
		traceTasks(tLogger, "Upon exit for word="+word+", #threads="+Thread.activeCount());
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
			tLogger.trace("TimeoutException");
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
    
    public MorphologicalAnalyzerAbstract() {}
    
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
