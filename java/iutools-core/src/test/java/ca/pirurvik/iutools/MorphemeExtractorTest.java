package ca.pirurvik.iutools;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.SpellChecker;

import org.junit.*;

public class MorphemeExtractorTest {
	
	private MorphemeExtractor morphemeExtractor = new MorphemeExtractor();
	
	@Before
	public void setUp() {
		String dictionary = ",,"
				+"word1:{root1/idr1}{affix1/idaff1},,"
				+"word2:{root2/idr2}{affix21/idaff21},,"
				+"word3:{root3/idr3}{affix21/idaff21}{affix32/idaff31},,"
				+"word4:{root1/idr1}{affix4/idaff4},,"
			;
		this.morphemeExtractor.setDictionary(dictionary);
	}

	@Test (expected=IOException.class)
	public void test__SpellChecker__Synopsis() throws IOException {
		//
		MorphemeExtractor morphemeExtractor = new MorphemeExtractor();

		// Before you can use a morpheme extractor, you must first build its
		// dictioanary of decompositions.
		// 
		// For example
		//
		File file = new File("/path/to/words/decompositions/file");
		morphemeExtractor.useDictionary(file);
		
		//
		// Once you have built its dictionary, you can look for words that
		// contain a given morpheme.
		String morpheme = "lauqsima";
		List<MorphemeExtractor.Words> wordsForMorphemes = morphemeExtractor.wordsContainingMorpheme(morpheme);
	}
	
	/**********************************
	 * VERIFICATION TESTS
	 **********************************/
	
	@Test
	public void test__wordsContainingMorpheme__root() {
		String morpheme = "root1";
		List<MorphemeExtractor.Words> wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		List<String> words = wordsForMorphemes.get(0).words;	
		List<String> expected = new ArrayList<String>();
		expected.add("word1"); expected.add("word4");
		Assert.assertEquals(2, words.size());
		Assert.assertTrue(words.contains(expected.get(0)));
		Assert.assertTrue(words.contains(expected.get(1)));
		
		morpheme = "root2";
		wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		words = wordsForMorphemes.get(0).words;
		expected = new ArrayList<String>();
		expected.add("word2");
		Assert.assertEquals(1, words.size());
		Assert.assertTrue(words.contains(expected.get(0)));
		
		String dictionary = ",,"
				+"word1:{root1/idr1}{affix1/idaff1},,"
				+"word2:{root2/idr2}{affix21/idaff21},,"
				+"word3:{root3/idr3}{affix21/idaff21}{affix32/idaff31},,"
				+"word4:{root1/idr1}{affix41/idaff411},,"
				+"word5:{root3/idr3}{affix41/idaff411},,"
				+"word6:{root2/idr2}{affix41/idaff412}{affix42/idaff42}"
			;
		this.morphemeExtractor.setDictionary(dictionary);
		morpheme = "affix41";
		wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==2);
		
		if (wordsForMorphemes.get(0).morphemeWithId.equals("affix41/idaff411")) {
			words = wordsForMorphemes.get(0).words;
			expected = new ArrayList<String>();
			expected.add("word4"); expected.add("word5");
			Assert.assertEquals(2, words.size());
			Assert.assertTrue(words.contains(expected.get(0)));
			Assert.assertTrue(words.contains(expected.get(1)));
		
			words = wordsForMorphemes.get(1).words;
			expected = new ArrayList<String>();
			expected.add("word6");
			Assert.assertEquals(1, words.size());
			Assert.assertTrue(words.contains(expected.get(0)));
		} else {
			words = wordsForMorphemes.get(1).words;
			expected = new ArrayList<String>();
			expected.add("word4"); expected.add("word5");
			Assert.assertEquals(2, words.size());
			Assert.assertTrue(words.contains(expected.get(0)));
			Assert.assertTrue(words.contains(expected.get(1)));
		
			words = wordsForMorphemes.get(0).words;
			expected = new ArrayList<String>();
			expected.add("word6");
			Assert.assertEquals(1, words.size());
			Assert.assertTrue(words.contains(expected.get(0)));
		}
	}
	
	public void test__wordsContainingMorpheme__infix() {
		String morpheme = "affix21";
		List<MorphemeExtractor.Words> wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		List<String> words = wordsForMorphemes.get(0).words;	
		List<String> expected = new ArrayList<String>();
		expected.add("word2"); expected.add("word3");
		Assert.assertEquals(2, words.size());
		Assert.assertTrue(words.contains(expected.get(0)));
		Assert.assertTrue(words.contains(expected.get(1)));
	}
	
	public void test__wordsContainingMorpheme__ending() {
		String morpheme = "affix4";
		List<MorphemeExtractor.Words> wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		List<String> words = wordsForMorphemes.get(0).words;	
		List<String> expected = new ArrayList<String>();
		expected.add("word4");
		Assert.assertEquals(1, words.size());
		Assert.assertTrue(words.contains(expected.get(0)));
	}
	

	/**********************************
	 * TEST HELPERS
	 **********************************/
	
}
