package org.iutools.utilities;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.nrc.debug.Debug;

/**
 * This class can used to monitor the running time of long operation, and raise 
 * a TimeoutException if it exceeds a particular threshold.
 * 
 * It can be used in one of three modes:
 * - Executor mode
 * - Standalone  mode
 * 
 * Executor mode
 *    In this mode, the long task is run through an ExecutorService which will 
 *    interrupt the task when the run time is exceeded. In this mode, the role 
 *    of the StopWatch is simply to ensure that the task is responsive to thread 
 *    interruption, and will that its thread will terminate if and when the 
 *    it is terminated by the ExecutorService (after the allocated time).
 *    
 * Standalone mode
 *    In this mode, the long task is run directly, without going through an
 *    ExecutorService. In this mode, the StopWatch monitors the running time 
 *    (as opposed to letting the ExecutorService do that) and raises a 
 *    TimeoutException if and when it exceeds the allocated time.
 * 
 * @author desilets
 *
 */
public class StopWatch {
	
	private String taskName = "";
	public long timeoutMSecs;	
	private boolean deactivated = false;
	private final int updateClockEveryNTimes = 100;
	
	private long startTime = -1;
	private int clockNotForcedSince = 0;
	private long checksSoFar = 0;
	
	public StopWatch(long _timeoutMSecs, String taskName) {
		initStopWatch(_timeoutMSecs, taskName);
	}
	
	public StopWatch(long _timeoutMSecs) {
		initStopWatch(_timeoutMSecs, "");
	}
	
	private void initStopWatch(long _timeoutMSecs, String _taskName) {
		this.timeoutMSecs = _timeoutMSecs;
		this.taskName = _taskName;
	}
	
	public void reset() {
		this.startTime = nowMSecs();
	}
	
	public void check(String message) throws TimeoutException {
		Logger mLogger = LogManager.getLogger("ca.inuktitutcomputing.utilities.StopWatch.check");
		
		if (deactivated) {
			return;
		}
		if (startTime == -1) {	
			this.startTime = nowMSecs();
		}

		checksSoFar++;
		
		// We only print traces every 10K calls...
		boolean traceThisCall = (checksSoFar % 10000) == 0;
		
		if (traceThisCall) {
			mLogger.trace("Checking for task="+taskName+" (#checks="+checksSoFar+")");
		}
		
		checkForInterruption();
		forceClockUpdate();
		checkElapsedTime(traceThisCall);
	}
	
	private void checkElapsedTime(boolean traceThisCall) throws TimeoutException {
		Logger mLogger = LogManager.getLogger("ca.inuktitutcomputing.utilities.StopWatch.checkElapsedTime");
		Long elapsed = nowMSecs() - startTime;
		if (traceThisCall) {
			mLogger.trace("Task "+taskName+" elapsed = "+elapsed/1000+" secs (max: "+timeoutMSecs/1000+" secs)");
		}
		
		if (elapsed > timeoutMSecs) {
			mLogger.trace("Task "+taskName+" exceeded its allocated time.\nThrowing a TimeoutException");
			throw new TimeoutException("Task "+taskName+"\nTimed out after "+elapsed+"msecs");
		}
	}

	private void checkForInterruption() throws TimeoutException {
		Logger mLogger = LogManager.getLogger("ca.inuktitutcomputing.utilities.StopWatch.checkForInterruption");
		
		if (Thread.interrupted()) {
			mLogger.trace("Task "+taskName+" was interrupted.\nRaising TimeoutException");
			// Note: The call to interrupted() sets the thread's interrupted 
			//  status to false. So we invoke interrupt() to reset it true in 
			//  someone above us depends on that.  
			Thread.currentThread().interrupt(); 
			throw new TimeoutException("Analyzer task was interrupted");
		}
	}

	public static long nowMSecs() {
		long time = System.nanoTime() / 1000000;
		return time;
	}
	
	static enum ClockUpdateStrategy {
		NONE, CALL_STACK, WRITE_FILE, CHECK_FILE};
	
	/**
	 * For some reason or another, the StopWatch does not always seem to keep 
	 * the time or check for interruption correctly unless it does some 
	 * operation there is some sort of operation that produces or responds to 
	 * interrupts.
	 * 
	 * Operations that we have found to work are:
	 * - Checking existence of a file
	 * - Writing to a file
	 * - Getting the stack trace
	 * 
	 * Not sure what kind of "dark magic" is at play here, but this hack seems 
	 * to do the trick.
	 */	
	private void forceClockUpdate() throws MorphTimeoutException {
		Logger mLogger = LogManager.getLogger("ca.inuktitutcomputing.utilities.StopWatch.forceClockUpdate");
		
		ClockUpdateStrategy updateStrat =
				ClockUpdateStrategy.CHECK_FILE;
		
		clockNotForcedSince++;
		// Don't force at each iteration as it will slow things down
		if (clockNotForcedSince > updateClockEveryNTimes) {
			mLogger.trace("Forcing clock updated for Task "+taskName);			
			clockNotForcedSince = 0;
		
			if (updateStrat == ClockUpdateStrategy.CALL_STACK) {
				Debug.printCallStack();
			} else if (updateStrat == ClockUpdateStrategy.WRITE_FILE) {
				File file = new File("/tmp/stopwatch.txt");
				try {
					FileWriter fr = new FileWriter(file);
					fr.write("nevermind");
					fr.close();	
				} catch (Exception e) {
					e.printStackTrace();
				}	
			} else if (updateStrat == ClockUpdateStrategy.CHECK_FILE) {
				File file = new File("/tmp/stopwatch.txt");
				file.exists();
			}
		}
	}

	public void disactivate() {
		deactivated = true;
	}

	public static long elapsedMsecsSince(long start) {
		long end = nowMSecs();
		long elapsed = end - start;
		return elapsed;
	}

	public static long now(TimeUnit unit) throws StopWatchException {
		long time = System.nanoTime();
		long nanosPerUnit = 1;
		if (unit == TimeUnit.MILLISECONDS) {
			nanosPerUnit = 1000000;
		} else if (unit == TimeUnit.SECONDS) {
			nanosPerUnit = 1000000000;
		} else {
			throw new StopWatchException("Unsupported time unit "+unit);
		}
		
		time = time / nanosPerUnit;
		
		return time;
	}
	
	public static long elapsedSince(long start, TimeUnit unit) throws StopWatchException {
		long end = now(unit);
		long elapsed = end - start;
		return elapsed;
	}
	
}
