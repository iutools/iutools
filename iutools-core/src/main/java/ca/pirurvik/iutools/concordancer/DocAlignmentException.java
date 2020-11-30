package ca.pirurvik.iutools.concordancer;

public class DocAlignmentException extends Exception {
	public DocAlignmentException(String mess, Exception e) {
		super(mess, e);
	}
	public DocAlignmentException(String mess) {
		super(mess);
	}
	public DocAlignmentException(Exception e) {
		super(e);
	}
}
