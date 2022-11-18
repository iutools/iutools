package org.iutools.worddict;

public class TooManyWordsException extends MachineGeneratedDictException {
	public TooManyWordsException(long totalWords) {
		super("Too many words (totalWords="+totalWords+")");
	}
}
