package org.iutools.cli;

public class ConsoleException extends Exception {
	
	public ConsoleException(String mess, Exception e) {
		super(mess, e);
	}

	public ConsoleException(String mess) {
		super(mess);
	}

	public ConsoleException(Exception e) {
		super(e);
	}
}
