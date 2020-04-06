package ca.inuktitutcomputing.script;

public class TransCoderException extends Exception {
	
	public TransCoderException(String mess, Exception e) {
		super(mess, e);
	}

	public TransCoderException(String mess) {
		super(mess);
	}

	public TransCoderException(Exception e) {
		super(e);
	}
}
