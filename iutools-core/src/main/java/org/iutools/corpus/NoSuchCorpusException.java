package org.iutools.corpus;

public class NoSuchCorpusException extends CompiledCorpusException {
	public NoSuchCorpusException(String mess, Exception exc) {
		super(mess, exc);
	}
	public NoSuchCorpusException(String mess) {
		super(mess);
	}
	public NoSuchCorpusException(Exception exc) {
		super(exc);
	}

}
