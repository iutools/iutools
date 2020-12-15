package org.iutools.datastructure.trie;

import java.util.concurrent.TimeoutException;

public class StringSegmenter_Char extends StringSegmenter {
	
	public String[] segment(String string) {
		return string.split("");
	}
	
	public String[] segment(String string, boolean fullAnalysis) {
		return string.split("");
	}

	@Override
	public void disactivateTimeout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[][] possibleSegmentations(String string, boolean fullAnalysis)
			throws TimeoutException, StringSegmenterException {
		return new String[][] { segment(string, fullAnalysis) };
	}

}
