package ca.nrc.datastructure.trie;

import java.util.concurrent.TimeoutException;

import ca.inuktitutcomputing.data.LinguisticDataException;

public abstract class StringSegmenter {

	public abstract String[] segment(String string) throws TimeoutException, StringSegmenterException, LinguisticDataException;
	public abstract String[] segment(String string, boolean fullAnalysis) throws TimeoutException, StringSegmenterException, LinguisticDataException;

}
