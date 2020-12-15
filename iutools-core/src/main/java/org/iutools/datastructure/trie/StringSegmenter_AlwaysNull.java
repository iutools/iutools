package org.iutools.datastructure.trie;

import java.util.concurrent.TimeoutException;

import ca.inuktitutcomputing.data.LinguisticDataException;

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
	public String[] segment(String string, boolean fullAnalysis)
			throws TimeoutException, StringSegmenterException, LinguisticDataException {
		return null;
	}

	@Override
	public String[][] possibleSegmentations(String string, boolean fullAnalysis)
			throws TimeoutException, StringSegmenterException {
		return null;
	}

	@Override
	public void disactivateTimeout() {
	}

}
