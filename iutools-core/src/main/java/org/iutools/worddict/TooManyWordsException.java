package org.iutools.worddict;

public class TooManyWordsException extends MultilingualDictException {
	public TooManyWordsException(long totalWords) {
		super("Too many words (totalWords="+totalWords+")");
	}
}
