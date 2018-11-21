package ca.nrc.datastructure.trie;

public class StringSegmenter_Word extends StringSegmenter {

	public String[] segment(String string) {
		return string.split("\\W+");
	}

}
