package ca.nrc.datastructure.trie;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringSegmenter_IUMorphemeTest {
	
	/******************************************
	 * DOCUMENTATION TESTS
	 ******************************************/
	
	@Test
	public void test__segment() {
		StringSegmenter segmenter = new StringSegmenter_IUMorpheme();
		String[] segments = segmenter.segment("takujuq");
		assertEquals("The number of segments should be 2",2,segments.length);
	}


}
