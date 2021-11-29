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

import org.iutools.utilities.StopWatch;
import ca.nrc.debug.Debug;
import ca.nrc.string.StringUtils;

public abstract class MorphologicalAnalyzer implements AutoCloseable {
	
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
    	System.out.println("--** MorphologicalAnalyzer.close: INVOKED");
    }
    
    // TODO: Make that synchronized?
    public static void shutdownExecutorPool() {
    	Logger mLogger = Logger.getLogger("ca.inuktitutcomputing.morph.MorphologicalAnalyzer.shutdownExecutorPool");
    	
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

	public boolean isDecomposable(String word) throws MorphologicalAnalyzerException{
    	boolean answer = false;
    	try {
    		Decomposition[] decomps = decomposeWord(word);
    		if (decomps != null && decomps.length > 0) {
    			answer = true;
			}
		} catch (TimeoutException e) {
			// If analysis times out, consider that the word is NOT decomposable
		}

		return answer;
	}

	public Decomposition[] decomposeWord(String word)
	   throws TimeoutException, MorphologicalAnalyzerException {
	   return decomposeWord(word, (Boolean)null);
	}

	public Decomposition[] decomposeWord(String word, Boolean lenient)
      throws TimeoutException, MorphologicalAnalyzerException {
		Decomposition[] decompsSimple = null;
		if (lenient == null) {
			lenient = true;
		}

		long start = System.currentTimeMillis();
		if (timeoutStrategy != TimeoutStrategy.EXECUTOR && timeoutActive) {
			this.stpw = new StopWatch(millisTimeout, "Decomposing word="+word);
		}

		Logger tLogger = Logger.getLogger("ca.inuktitutcomputing.morph.decomposeWord");
		tLogger.trace("word="+word+", lenient="+lenient);

		Decomposition[] decompositions = null;

		MorphAnalyzerTask task = new MorphAnalyzerTask(word, lenient, this);

		String mess = "Decomp of word="+word+"; STARTS at "+start+"msecs";
		tLogger.trace(mess);

		tLogger.trace("timeoutStrategy: "+timeoutStrategy);
		if (timeoutStrategy == TimeoutStrategy.STOPWATCH) {
			tLogger.trace("invoke directly");
			decompositions = invokeDirectly(word, lenient);
		} else {
			tLogger.trace("invoke through task");
			try {
				decompositions = invokeThroughExecutor(task);
			} catch (InterruptedException e) {
				throw new MorphologicalAnalyzerException("Analysis interrupted", e);
			}
		}

		long elapsed = System.currentTimeMillis() - start;
		mess = "Decomposition of word="+word+"; ENDS with elapsed="+elapsed+"msecs";
		tLogger.trace(mess);

		return decompositions;
	}

    private Decomposition[] invokeThroughExecutor(MorphAnalyzerTask task) throws InterruptedException, TimeoutException, MorphologicalAnalyzerException {
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
		} catch (InterruptedException | TimeoutException e) {
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
			if (cause == null) {
				cause = e;
			}
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
    	Logger tLogger = Logger.getLogger("ca.inuktitutcomputing.morph.MorphologicalAnalyzer.checkElapsedTime");
    	
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
    
    public MorphologicalAnalyzer() {}
    
    public MorphologicalAnalyzer setTimeout(long val) {
    	return setTimeout(new Long(val));
    }

    public MorphologicalAnalyzer setTimeout(Long val) {
    	if (val != null) {
    		millisTimeout = val;
    	}
    	return this;
    }
    
    public MorphologicalAnalyzer disactivateTimeout() {
    	timeoutActive = false;
    	return this;
    }
    
    public MorphologicalAnalyzer activateTimeout() {
    	timeoutActive = true;
    	return this;
    }
    
    public MorphologicalAnalyzer setTimeoutStrategy(TimeoutStrategy _strat) {
    	this.timeoutStrategy = _strat;
    	return this;
    }
}
