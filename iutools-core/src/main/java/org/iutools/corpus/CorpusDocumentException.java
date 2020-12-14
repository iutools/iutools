package org.iutools.corpus;

public class CorpusDocumentException extends Exception {
	public CorpusDocumentException(String mess, Exception e) {
		super(mess, e);
	}
	public CorpusDocumentException(String mess) {
		super(mess);
	}
	public CorpusDocumentException(Exception e) {
		super(e);
	}
}
