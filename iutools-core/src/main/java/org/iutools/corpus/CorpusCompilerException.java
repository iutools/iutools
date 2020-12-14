package org.iutools.corpus;

public class CorpusCompilerException extends Exception {
	public CorpusCompilerException(String mess, Exception e) {
		super(mess, e);
	}

	public CorpusCompilerException(String mess) {
		super(mess);
	}

	public CorpusCompilerException(Exception e) {
		super(e);
	}
	
}
