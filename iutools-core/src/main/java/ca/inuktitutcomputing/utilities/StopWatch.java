package ca.inuktitutcomputing.utilities;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import ca.nrc.debug.Debug;

/**
 * This class can used to monitor the running time of long operation, and raise 
 * a TimeoutException if it exceeds a particular threshold.
 * 
 * WARNING: This class may be in a "solution in search of a problem" as Java is 
 * supposed to provide native ways of doing this (ex: Executer, Future). But we 
 * have been experiencing problems with those native solutions and are therefore 
 * trying some homegrown approaches.
 * 
 * @author desilets
 *
 */
public class StopWatch {
	
	private long timeoutMSecs; // milliseconds
	private long startTime;
	private boolean disactivated = false;
	
	private int clockNotForcedSince = 0;
	
	public StopWatch(long _timeoutMSecs) {
		initStopWatch(_timeoutMSecs);
	}
	
	private void initStopWatch(long _timeoutMSecs) {
		this.timeoutMSecs = _timeoutMSecs;
	}

	public void start() {
		this.startTime = nowMSecs();
	}
	
	private long nowMSecs() {
		long time = System.nanoTime() / 1000000;
		return time;
	}

	// TODO: Can we replace StopWatch by a class TimeoutChecker 
	//   with a single static method
	//
	// public static void check() {
	//    if (Thread.interrupted()) {
	//       throw new TimeoutException();
	//    }
	//  }
	//
	public void check(String message) throws TimeoutException {
		boolean deactivated = false;
		boolean justUpdateClock = false;
		boolean justPrintTimeout = false;
		
		if (deactivated) { 
			return; 
		}
		
		Logger tLogger = Logger.getLogger("ca.inuktitutcomputing.utilities.StopWatch.check");
		
		if (!disactivated) {
			forceClockUpdate();
		}
		
		if (justUpdateClock) {
			return;
		}

		Long elapsed = nowMSecs() - startTime;
		
		if (!disactivated && elapsed > timeoutMSecs) {
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("TIMED OUT!! Raising TimeoutException");
			}
			if (!justPrintTimeout) {
				throw new MorphTimeoutException(message+"\nTimed out after "+elapsed+"msecs");
			}
		}
	}
	
	static enum ClockUpdateStrategy {
		NONE, CALL_STACK, WRITE_FILE, CHECK_FILE};
	
	/**
	 * For some reason or another, the StopWatch does not always seem to keep 
	 * the time correctly unless there is some sort of operation that forces 
	 * the system to increment the clock.
	 * 
	 * Operations that we have found to work are:
	 * - Writing to a file
	 * - Checking existence of a file
	 * - Getting the stack trace
	 * 
	 * Not sure what kind of "dark magic" is at play here, but this hack seems 
	 * to do the trick.
	 * @throws MorphTimeoutException 
	 */	
	private void forceClockUpdate() throws MorphTimeoutException {
		Logger mLogger = Logger.getLogger("ca.inuktitutcomputing.utilities.StopWatch.forceClockUpdate");

		ClockUpdateStrategy updateStrat =
//				ForceUpdateStrategy.CALL_STACK;
//				ForceUpdateStrategy.WRITE_FILE;
				ClockUpdateStrategy.CHECK_FILE;
//				ClockUpdateStrategy.NONE;
		
		if (Thread.interrupted()) {
			mLogger.trace("Thread was interrupted");
			throw new MorphTimeoutException("Analyzer task was interreputed");
		}
		
		clockNotForcedSince++;
		// Don't force at each iteration as it will slow things down
		if (clockNotForcedSince > 100) {
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
		disactivated = true;
	}
}
