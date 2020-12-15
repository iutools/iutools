package org.iutools.datastructure.trie;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringSegmenter_IUMorphemeTest {
	
	/******************************************
	 * DOCUMENTATION TESTS
	 ******************************************/
	
	@Test
	public void test__segment__short_analysis() throws Exception {
		StringSegmenter segmenter = new StringSegmenter_IUMorpheme();
		String[] segments;
		try {
			segments = segmenter.segment("inuit");
			assertEquals("The number of segments should be 2",2,segments.length);
			String[] expected = new String[]{"{inuk/1n}","{it/tn-nom-p}"};
			assertArrayEquals("The segments are not correct.",expected,segments);
		} catch (Exception e) {
			assertFalse("The number of segments should be 2 but the segmenter returned with an error",true);
		}
	}

	@Test
	public void test__segment__short_analysis_syllabique()  throws Exception {
		StringSegmenter segmenter = new StringSegmenter_IUMorpheme();
		String[] segments;
		try {
			segments = segmenter.segment("ᐃᓄᐃᑦ");
			assertEquals("The number of segments should be 2",2,segments.length);
			String[] expected = new String[]{"{inuk/1n}","{it/tn-nom-p}"};
			assertArrayEquals("The segments are not correct.",expected,segments);
		} catch (Exception e) {
			assertFalse("The number of segments should be 2 but the segmenter returned with an error",true);
		}
	}

	@Test
	public void test__segment__full_analysis()  throws Exception  {
		StringSegmenter segmenter = new StringSegmenter_IUMorpheme();
		String[] segments;
		try {
			segments = segmenter.segment("inuit",true);
			assertEquals("The number of segments should be 2",2,segments.length);
			String[] expected = new String[]{"{inu:inuk/1n}","{it:it/tn-nom-p}"};
			assertArrayEquals("The segments are not correct.",expected,segments);
		} catch (Exception e) {
			assertFalse("The number of segments should be 2 but the segmenter returned with an error",true);
		}
	}


}
