package org.iutools.worddict;

public class TooManyWordsException extends IUWordDictException {
	public TooManyWordsException(long totalWords) {
		super("Too many words (totalWords="+totalWords+")");
	}
}
