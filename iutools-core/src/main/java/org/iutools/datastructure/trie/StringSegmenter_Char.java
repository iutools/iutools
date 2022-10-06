package org.iutools.datastructure.trie;

import java.util.concurrent.TimeoutException;

public class StringSegmenter_Char extends StringSegmenter {
	
	public String[] segment(String string, Boolean fullAnalysis) {
		if (fullAnalysis == null) {
			fullAnalysis = false;
		}
		String[] segments = string.split("");
		for (int ii=0; ii<segments.length; ii++) {
			segments[ii] = segments[ii]+":"+segments[ii];
		}
		return segments;
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
