package org.iutools.datastructure.trie;

import java.util.concurrent.TimeoutException;

import org.iutools.linguisticdata.LinguisticDataException;

public abstract class StringSegmenter {
	
	public abstract String[] segment(String string, Boolean fullAnalysis) throws TimeoutException, StringSegmenterException;
	
	public abstract String[][] possibleSegmentations(
			String string, Boolean fullAnalysis)
			throws TimeoutException, StringSegmenterException;
	
	public abstract void disactivateTimeout();

	public String[] segment(String string) 
			throws TimeoutException, StringSegmenterException {
		return segment(string, (Boolean)null);
	}
	
	public String[][] possibleSegmentations(
		String string) throws TimeoutException, StringSegmenterException {
		return possibleSegmentations(string, false);
	}

}
