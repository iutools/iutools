package ca.nrc.datastructure.trie;

public abstract class StringSegmenter {

	public abstract String[] segment(String string) throws Exception;
	public abstract String[] segment(String string, boolean fullAnalysis) throws Exception;

}
