package ca.pirurvik.iutools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertHelpers;

import org.junit.*;

public class MorphemeExtractorTest {
	
	private MorphemeExtractor morphemeExtractor = new MorphemeExtractor();
	private MockCompiledCorpus mockCompiledCorpus;
	
	@Before
	public void setUp() throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		HashMap<String,String> dictionary = new HashMap<String,String>();
		dictionary.put("inuit", "{root1/idr1} {affix1/idaff1}");
		dictionary.put("nunami", "{root2/idr2} {affix21/idaff21}");
		dictionary.put("iglumik", "{root3/idr3} {affix21/idaff21} {affix32/idaff31}");
		dictionary.put("inuksuk", "{root1/idr1} {affix4/idaff4}");
		mockCompiledCorpus = new MockCompiledCorpus();
		// The MockCompiledCorpus's segmenter will use this dictionary instead of calling 
		// the morphological analyzer.
		mockCompiledCorpus.setDictionary(dictionary);
		String[] stringsOfWords = new String[] {
				"inuit nunami iglumik inuksuk"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        try {
        	mockCompiledCorpus.compileCorpusFromScratch(corpusDirPathname);
        } catch(Exception e) {
        	System.err.println("Exiting from compiler");
        	System.err.println("because: "+e.getMessage());
        	System.exit(1);
        }
        morphemeExtractor.useCorpus(mockCompiledCorpus);
	}

	@Test
	public void test__MorphemeExtractor__Synopsis() throws Exception {
		//
		MorphemeExtractor morphemeExtractor = new MorphemeExtractor();

		// The morpheme extractor uses the word base of a compiled corpus
		// 
		// For example
		//
		CompiledCorpus corpus = mockCompiledCorpus;
		morphemeExtractor.useCorpus(corpus);
		
		//
		// Once you have built its dictionary, you can look for words that
		// contain a given morpheme.
		String morpheme = "nunami";
		List<MorphemeExtractor.Words> wordsForMorphemes = morphemeExtractor.wordsContainingMorpheme(morpheme);
	}
	
	@Test(expected=Exception.class)
	public void test__MorphemeExtractor__Synopsis__No_corpus_defined() throws Exception {
		MorphemeExtractor morphemeExtractor = new MorphemeExtractor();
		String morpheme = "nunami";
		List<MorphemeExtractor.Words> wordsForMorphemes = morphemeExtractor.wordsContainingMorpheme(morpheme);
	}
		
	/**********************************
	 * VERIFICATION TESTS
	 * @throws Exception 
	 **********************************/
	
	@Test
	public void test__wordsContainingMorpheme__root() throws Exception {
		String morpheme = "root1";
		List<MorphemeExtractor.Words> wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		List<Pair<String, Long>> words = wordsForMorphemes.get(0).words;	
		List<Pair<String, Long>> expected = new ArrayList<Pair<String, Long>>();
		expected.add(new Pair<String,Long>("inuit",new Long(1))); 
		expected.add(new Pair<String,Long>("inuksuk",new Long(1)));
		Assert.assertEquals(2, words.size());
		AssertHelpers.assertDeepEquals("",expected,words);
		
		morpheme = "root2";
		wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		words = wordsForMorphemes.get(0).words;
		expected = new ArrayList<Pair<String, Long>>();
		expected.add(new Pair<String,Long>("nunami",new Long(1)));
		Assert.assertEquals(1, words.size());
		AssertHelpers.assertDeepEquals("",expected,words);
		
		HashMap<String,String> dictionary = new HashMap<String,String>();
		dictionary.put("inuit", "{root1/idr1} {affix1/idaff1}");
		dictionary.put("nunami", "{root2/idr2} {affix21/idaff21}");
		dictionary.put("iglumik", "{root3/idr3} {affix21/idaff21} {affix32/idaff31}");
		dictionary.put("inuksuk", "{root1/idr1} {affix41/idaff411}");
		dictionary.put("iglumut", "{root3/idr3} {affix41/idaff411}");
		dictionary.put("nunatsiaq", "{root2/idr2} {affix41/idaff412} {affix42/idaff42}");
		MockCompiledCorpus mockCompiledCorpus = new MockCompiledCorpus();
		mockCompiledCorpus.setDictionary(dictionary);
		String[] stringsOfWords = new String[] {
				"inuit nunami iglumik inuksuk iglumut nunatsiaq"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        try {
        	mockCompiledCorpus.compileCorpusFromScratch(corpusDirPathname);
        } catch(Exception e) {
        	System.err.println("Exiting from compiler");
        }
        morphemeExtractor.useCorpus(mockCompiledCorpus);
		morpheme = "affix41";
		wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==2);
		
		if (wordsForMorphemes.get(0).morphemeWithId.equals("affix41/idaff411")) {
			words = wordsForMorphemes.get(0).words;
			expected = new ArrayList<Pair<String, Long>>();
			expected.add(new Pair<String, Long>("inuksuk", new Long(1))); 
			expected.add(new Pair<String, Long>("iglumut", new Long(1)));
			Assert.assertEquals(2, words.size());
			AssertHelpers.assertDeepEquals("",expected,words);
		
			words = wordsForMorphemes.get(1).words;
			expected = new ArrayList<Pair<String, Long>>();
			expected.add(new Pair<String, Long>("nunatsiaq",new Long(1)));
			Assert.assertEquals(1, words.size());
			AssertHelpers.assertDeepEquals("",expected,words);
		} else {
			words = wordsForMorphemes.get(1).words;
			expected = new ArrayList<Pair<String, Long>>();
			expected.add(new Pair<String, Long>("inuksuk", new Long(1))); 
			expected.add(new Pair<String, Long>("iglumut", new Long(1)));
			Assert.assertEquals(2, words.size());
			AssertHelpers.assertDeepEquals("",expected,words);
		
			words = wordsForMorphemes.get(0).words;
			expected = new ArrayList<Pair<String, Long>>();
			expected.add(new Pair<String, Long>("nunatsiaq", new Long(1)));
			Assert.assertEquals(1, words.size());
			AssertHelpers.assertDeepEquals("",expected,words);
		}
	}
	
	public void test__wordsContainingMorpheme__infix() throws Exception {
		String morpheme = "affix21";
		List<MorphemeExtractor.Words> wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		List<Pair<String, Long>> words = wordsForMorphemes.get(0).words;	
		List<String> expected = new ArrayList<String>();
		expected.add("word2"); expected.add("word3");
		Assert.assertEquals(2, words.size());
		Assert.assertTrue(words.contains(expected.get(0)));
		Assert.assertTrue(words.contains(expected.get(1)));
	}
	
	public void test__wordsContainingMorpheme__ending() throws Exception {
		String morpheme = "affix4";
		List<MorphemeExtractor.Words> wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		List<Pair<String, Long>> words = wordsForMorphemes.get(0).words;	
		List<String> expected = new ArrayList<String>();
		expected.add("word4");
		Assert.assertEquals(1, words.size());
		Assert.assertTrue(words.contains(expected.get(0)));
	}
	

	/**********************************
	 * TEST HELPERS
	 **********************************/
	
    private String createTemporaryCorpusDirectory(String[] stringOfWords) throws IOException {
        File corpusDirectory = Files.createTempDirectory("").toFile();
        corpusDirectory.deleteOnExit();
        String corpusDirPath = corpusDirectory.getAbsolutePath();
        for (int i=0; i<stringOfWords.length; i++) {
        	File wordFile = new File(corpusDirPath+"/contents"+(i+1)+".txt");
        	BufferedWriter bw = new BufferedWriter(new FileWriter(wordFile));
        	bw.write(stringOfWords[i]);
        	bw.close();
        }
        return corpusDirPath;
	}


}
