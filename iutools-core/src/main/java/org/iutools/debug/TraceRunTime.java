package org.iutools.debug;

import org.apache.logging.log4j.Logger;
import org.iutools.utilities.StopWatch;
import org.iutools.utilities.StopWatchException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TraceRunTime {
	public static void traceTotalTime(String mess, Logger logger, StopWatch sw) {
		if (logger.isTraceEnabled()) {
			try {
				logger.trace(mess+": took "+sw.totalTime(MILLISECONDS)+" msecs");
			} catch (StopWatchException e) {
				e.printStackTrace();
			}
		}
	}

	public static void traceLapTime(String mess, Logger logger, StopWatch sw) {
		if (logger.isTraceEnabled()) {
			try {
				logger.trace(mess+": took "+sw.lapTime(MILLISECONDS)+" msecs");
			} catch (StopWatchException e) {
				e.printStackTrace();
			}
		}
	}

}
