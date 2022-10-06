package org.iutools.datastructure.trie;

import java.util.concurrent.TimeoutException;

import org.iutools.linguisticdata.LinguisticDataException;

/****************************************
 * This StringSegmenter always returns a null segmentation.
 * This does NOT mean that the word does not segment. It just
 * means that the decompositions were not computed.
 * 
 * @author desilets
 *
 */
public class StringSegmenter_AlwaysNull extends StringSegmenter {

	@Override
	public String[] segment(String string, Boolean fullAnalysis)
			throws TimeoutException, StringSegmenterException {
		return null;
	}

	@Override
	public String[][] possibleSegmentations(String string, Boolean fullAnalysis)
			throws TimeoutException, StringSegmenterException {
		return null;
	}

	@Override
	public void disactivateTimeout() {
	}

}
