package ca.inuktitutcomputing.utilities;

import java.util.concurrent.TimeoutException;

public class MorphTimeoutException extends TimeoutException {
	public MorphTimeoutException(String mess) {
		super(mess);
	}
}
