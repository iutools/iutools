package ca.pirurvik.iutools;

public class QueryExpanderException extends Exception {

	public QueryExpanderException(String mess, Exception e) {
		super(mess, e);
	}

	public QueryExpanderException(Exception e) {
		super(e);
	}

}
