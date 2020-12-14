package org.iutools.corpus;

public class CompiledCorpusException extends Exception {

	public CompiledCorpusException(Exception e) { super(e); }

	public CompiledCorpusException(String mess) { super (mess); }

	public CompiledCorpusException(String mess, Exception e) { super(mess, e); }

}
