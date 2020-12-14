package org.iutools.text.segmentation;

import ca.nrc.string.SimpleTokenizer;

public class Segmenter_Generic extends Segmenter {

	@Override
	protected String[] tokenize(String text) {
		String[] tokens = new SimpleTokenizer().tokenize(text, true);
		return tokens;
	}
	
}
