package ca.nrc.datastructure.trie;

import java.util.concurrent.TimeoutException;

public abstract class StringSegmenter {

	public abstract String[] segment(String string) throws TimeoutException, StringSegmenterException;
	public abstract String[] segment(String string, boolean fullAnalysis) throws TimeoutException, StringSegmenterException;

}
