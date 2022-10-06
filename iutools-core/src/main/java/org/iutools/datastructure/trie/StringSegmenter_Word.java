package org.iutools.datastructure.trie;

import java.util.concurrent.TimeoutException;

public class StringSegmenter_Word extends StringSegmenter {

	public String[] segment(String string, Boolean fullAnalysis) {
		return string.split("\\W+");
	}
	@Override
	public void disactivateTimeout() {
		// TODO Auto-generated method stub
	}

	@Override
	public String[][] possibleSegmentations(String string, Boolean fullAnalysis)
			throws TimeoutException, StringSegmenterException {
		return new String[][] { segment(string, fullAnalysis) };
	}

}
