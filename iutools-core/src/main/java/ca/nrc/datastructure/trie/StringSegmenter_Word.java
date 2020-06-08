package ca.nrc.datastructure.trie;

public class StringSegmenter_Word extends StringSegmenter {

	public String[] segment(String string) {
		return string.split("\\W+");
	}
	public String[] segment(String string, boolean fullAnalysis) {
		return string.split("\\W+");
	}
	@Override
	public void disactivateTimeout() {
		// TODO Auto-generated method stub
		
	}

}
