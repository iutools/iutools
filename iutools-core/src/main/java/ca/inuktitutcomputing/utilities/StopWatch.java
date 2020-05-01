package ca.inuktitutcomputing.utilities;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import ca.nrc.debug.Debug;

public class StopWatch {
	
	private long timeout; // milliseconds
	private long startTime;
	private boolean disactivated = false;
	
	private int clockNotForcedSince = 0;
	
	public StopWatch(long _timeout) {
		this.timeout = _timeout;
	}
	
	public void start() {
		this.startTime = now();
	}
	
	private long now() {
		long time = System.nanoTime() / 1000000;
		return time;
	}

	public void check(String message) throws TimeoutException {
		Logger tLogger = Logger.getLogger("ca.inuktitutcomputing.utilities.StopWatch.check");

		if (!disactivated) {
			forceClockUpdate();
		}

		Long elapsed = now() - startTime;
		
		if (tLogger.isTraceEnabled()) {
			String callstack = Debug.printCallStack();
			tLogger.trace("elapsed="+elapsed+"\nCall stack:\n"+callstack);
		}
		
		if (!disactivated && elapsed > timeout) {
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("TIMED OUT!! Raising TimeoutException");
			}
			throw new TimeoutException(message);
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
	 */	
	private void forceClockUpdate() {
		ClockUpdateStrategy updateStrat =
//				ForceUpdateStrategy.CALL_STACK;
//				ForceUpdateStrategy.WRITE_FILE;
				ClockUpdateStrategy.CHECK_FILE;
//				ClockUpdateStrategy.NONE;
		
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
