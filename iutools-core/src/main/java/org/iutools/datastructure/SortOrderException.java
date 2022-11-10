package org.iutools.datastructure;

public class SortOrderException extends Exception {
	public SortOrderException(String mess, Exception e) {
		super(mess, e);
	}
	public SortOrderException(Exception e) {
		super(e);
	}
	public SortOrderException(String mess) {
		super(mess);
	}
}
