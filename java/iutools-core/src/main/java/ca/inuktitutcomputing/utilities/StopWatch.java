package ca.inuktitutcomputing.utilities;

import java.util.Calendar;
import java.util.concurrent.TimeoutException;

public class StopWatch {
	
	private long timeout; // milliseconds
	private long startTime;
	
	public StopWatch(long _timeout) {
		this.timeout = _timeout;
	}
	
	public void start() {
		this.startTime = Calendar.getInstance().getTimeInMillis();
	}
	
	public void check(String message) throws TimeoutException {
		if (Calendar.getInstance().getTimeInMillis() > this.startTime+timeout) {
			throw new TimeoutException(message);
		}
	}

}
