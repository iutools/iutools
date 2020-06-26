package ca.inuktitutcomputing.utilities;

public class StopWatchException extends Exception {
	public StopWatchException(String mess, Exception e) {
		super(mess, e);
	}
	
	public StopWatchException(String mess) {
		super(mess);
	}

	public StopWatchException(Exception e) {
		super(e);
	}
}
