package org.iutools.cli;

public class CLIException extends Exception {
	
	public CLIException(String mess, Exception e) {
		super(mess, e);
	}

	public CLIException(String mess) {
		super(mess);
	}

	public CLIException(Exception e) {
		super(e);
	}
}
