package org.iutools.corpus;

import ca.nrc.debug.Debug;
import ca.nrc.dtrc.elasticsearch.ElasticSearchException;

public class BadESRecordException extends CompiledCorpusException {
	public BadESRecordException(String mess, ElasticSearchException e) {
		super(mess, e);
	}
	public BadESRecordException(String mess) {
		super(mess);
	}
	public BadESRecordException(ElasticSearchException e) {
		super(e);
	}

	public static boolean includedInStackOf(Exception e) {
		String stack = Debug.printCallStack(e);
		boolean included = (stack.contains(BadESRecordException.class.toString()));
		return included;
	}
}
