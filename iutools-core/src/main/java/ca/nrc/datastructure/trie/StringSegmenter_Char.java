package ca.nrc.datastructure.trie;

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

}
