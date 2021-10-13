package org.iutools.loganalysis;

import java.io.FileNotFoundException;

public class LogAnalyzerException extends Exception {
	public LogAnalyzerException(String mess, Exception e) {
		super(mess, e);
	}
	public LogAnalyzerException(Exception e) {
		super(e);
	}
	public LogAnalyzerException(String mess) {
		super(mess);
	}
}
