package ca.pirurvik.iutools.corpus;

import static org.junit.Assert.assertArrayEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie_InMemory;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.datastructure.trie.Trie_InMemory;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertHelpers;
import ca.nrc.testing.AssertObject;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import ca.pirurvik.iutools.corpus.WordInfo;
import ca.pirurvik.iutools.corpus.CompiledCorpus.WordWithMorpheme;
import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class CompiledCorpusTest extends CompiledCorpus_BaseTest
{

	@Override
	protected CompiledCorpus_Base makeCorpusUnderTest() {
		return new CompiledCorpus();
	}

	
	private File corpusDirectory = null;
	
	@After
    public void tearDown() throws Exception {
        if (corpusDirectory != null) {
        	File[] listOfFiles = corpusDirectory.listFiles();
        	for (File file : listOfFiles)
        		file.delete();
        }
        corpusDirectory = null;
    }

	///////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////
	
	@Test
	public void test__CompiledCorpus__Synopsis() throws Exception {
		//
		// Use a CompiledCorpus to trie-compile a corpus and compute statistics.
		//
		//
		CompiledCorpus compiledCorpus = new CompiledCorpus();
		
		// 
		// By default, the compiler segments words on a character by character basis.
		// 
		// But you can also pass it a different segmenter. For example, the following
		// compiler will segment words by inuktitut morphemes.
		//
		compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());

		compiledCorpus.setVerbose(false); // set verbose to false for tests only

		// Identify the full path of the corpus directory to be compiled
		//
		String corpusDirectoryPathname = "path/to/corpus/directory";
		
		// Compile the corpus given in argument as directory pathname from scratch
		try {
			compiledCorpus.compileCorpusFromScratch(corpusDirectoryPathname);
		} catch(CompiledCorpusException | StringSegmenterException e) {
			// do something
		}
		
		// Compile the corpus given in argument as directory pathname (will resume where it was left after the last run)
		try {
			compiledCorpus.compileCorpus(corpusDirectoryPathname);
		} catch(CompiledCorpusException | StringSegmenterException e) {
			// do something
		}
		
		// Eventually, when a corpus has been compiled, one will want to save it
		// for later use:
		String trieCompilationFilePathname = "path/to/file";
		try {
			compiledCorpus.saveCompilerInJSONFile(trieCompilationFilePathname);
		} catch (CompiledCorpusException e) {
			// do something
		}
		
		// Once a corpus has been compiled, you can loop throug all the words
		// that were seen in it, and get information about those words.
		//
		Iterator<String> iter = compiledCorpus.allWords();
		while (iter.hasNext()) {
			String word = iter.next();
			WordInfo wInfo  = compiledCorpus.info4word(word);
			if (wInfo == null) {
				// Means the corpus does not know about this word
				//
				// Note: Should not happen in this case, because we obtained 
				// 'word' through the allWords() iterator (so it know that this
				// word was seen in the corpus).
				//
			} else {
				// Total number of morphological decompositions for this word, 
				// as well as a short list of the first few decompositions 
				// found.
				// 
				// If those two values are 'null', it means that the decomps 
				// have not been computed.
				// It does NOT mean that no decomps can be computed for this 
				// word.
				//
				Integer numDecomps = wInfo.totalDecompositions;
				String[] decomps = wInfo.topDecompositions;
			}			
		}
		
		// You can ask for information about the various ngrams 
		// that were seen in the corpus.
		//
		// This returns all the words that START with "nuna"
		//
		Set<String> wordsWithNgram = 
				compiledCorpus.wordsContainingNgram("^nuna");
		
		// Words that END with "vut"
		//
		wordsWithNgram = 
				compiledCorpus.wordsContainingNgram("vut$");

		// Words that have "nav" in the middle
		//
		wordsWithNgram = 
				compiledCorpus.wordsContainingNgram("nav");
		
		// The most common way to populate a corpus is to compile it from a file 
		// or series of text files.
		//
		// But you can also manually add some words to it.
		// 
		// You can add the word by itself, WITH or WITHOUT decompositions.
		//
		String word = "inukshuk";
		compiledCorpus.addWord(word); // WITHOUT decompositions
		word = "inuktut";
		Decomposition[] decomps = new MorphologicalAnalyzer().decomposeWord(word);
		String[] decompsStr = new String[decomps.length];
		for (int ii=0; ii < decomps.length; ii++) {
			decompsStr[ii] = decomps[ii].toStr2();
		}
		compiledCorpus.addWord(word, decompsStr); // WITH decompositions
		
		// You can override the decompositions of a word that is already in the 
		// corpus
		//
		compiledCorpus.info4word(word)
			.setDecompositions(new String[0]);
		
		// Attempting to add a word that is already registered raises a 
		// WordAlreadinInCorpusException exception.
		// 
		// So you should check for the existence of 
		// the word before adding it.
		//
		if (compiledCorpus.info4word(word) == null) {
			compiledCorpus.addWord(word);
		}
				
		// When you encounter an occurence of a word, you can tell 
		// the corpus about it as follows.
		//
		compiledCorpus.incrementWordFreq(word);
	}	
	
	////////////////////////
	// VERIFICATION TESTS
    ////////////////////////
	
	@Test
	public void test__addWord__HappyPath() throws Exception {
		CompiledCorpus corpus = new CompiledCorpus();
		final String[] noWords = new String[] {};
		String nunavut = "nunavut";
		String nunavik = "nunavik";

		String ngram_nuna = "^nuna";
		String ngram_navik = "navik$";

		// Check that the corpus is initially empty and does not know about any 
		// of the words, nor their ngrams
		//
		{
			for (String aWord: new String[] {nunavut, nunavik}) {
				Assert.assertFalse(
						"Initially, corpus should not have known about word ",
						corpus.containsWord(aWord));
			}
	
			for (String ngram: new String[] {ngram_nuna, ngram_navik }) {
				AssertObject.assertDeepEquals(
					"Initially, no words should have been associated to ngram "+ngram,
					noWords, corpus.wordsContainingNgram(ngram));
			}
		}
		
		// Add word 'nunavut' and check that the corpus now knows about it and 
		// about its ngrams. But it should still not know about 'nunavik'.
		//
		{
			corpus.addWord(nunavut);
			Long gotKey = corpus.key4word(nunavut);
			Assert.assertEquals("After addition of the word, corpus should have known about "+nunavut, 
					gotKey, new Long(0));
			Assert.assertFalse("After addition of word "+nunavut+
					", corpus should NOT have known about word "+nunavik, 
					corpus.containsWord(nunavik));
			
			String[] expWords = new String[] {nunavut};
			AssertObject.assertDeepEquals(
				"After addition of word "+nunavut+" that word should have been associated to ngram "+ngram_nuna,
				expWords, corpus.wordsContainingNgram(ngram_nuna));
			AssertObject.assertDeepEquals(
				"After addition of word "+nunavut+" that word should NOT have been associated to ngram "+ngram_navik,
				expWords, corpus.wordsContainingNgram(ngram_nuna));
		}
		
		// Add word 'nunavik' and check that the corpus now knows about it AND 
		// about 'nunavut'.
		//
		{
			corpus.addWord(nunavik);
			
			Long gotKey = corpus.key4word(nunavik);
			Assert.assertEquals("After addition of "+nunavik+
					", the corpus should know about that word (and the word key shold be key(nunavut)+1", 
					gotKey, new Long(1));
			gotKey = corpus.key4word(nunavut);
			Assert.assertEquals("After addition of "+nunavik+
					", the corpus should still know about "+nunavut, 
					gotKey, new Long(0));
			
			Set<String> expWords = new HashSet<String>();
			{
				expWords.add(nunavut); expWords.add(nunavik);
			}
			AssertObject.assertDeepEquals(
				"After addition of 2nd word "+nunavik+
				" corpus should ngram of both words",
				expWords, corpus.wordsContainingNgram(ngram_nuna));
			expWords = new HashSet<String>();
			{
				expWords.add(nunavik);
			}
			AssertObject.assertDeepEquals(
					"After addition of 2nd word "+nunavik+
					" corpus should ngram of both words",
				expWords, corpus.wordsContainingNgram(ngram_navik));
		}
	}
	

	@Test
    public void test__resume_compilation_of_corpus_after_crash_or_abortion__1_file_in_corpus_directory() 
    		throws Exception  
    {
    	// The corpus directory contains 1 file with 8 words :
    	// nunavut inuit
    	// takujuq
    	// amma
    	// kanaujaq
    	// iglumik takulaaqtuq
    	// nunait

		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq amma kanaujaq iglumik takulaaqtuq nunait"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.saveFrequency = 3;
        compiledCorpus.stopAfter = 7; // should stop after takulaaqtuq
        try {
        	compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
        } catch(CompiledCorpusException | StringSegmenterException e) {
        }
			
        CompiledCorpus retrievedCompiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        retrievedCompiledCorpus.setVerbose(false);
        retrievedCompiledCorpus.__resumeCompilation(corpusDirPathname);
        
		Trie_InMemory trie = retrievedCompiledCorpus.trie;
		long expectedCurrentFileWordCounter = 6;
		Assert.assertEquals("The value of the 'current file word counter' is wrong.", expectedCurrentFileWordCounter,
				retrievedCompiledCorpus.currentFileWordCounter);
		HashMap<String, String[]> segmentsCache = retrievedCompiledCorpus.getSegmentsCache();
		String[] expected_takulaaqtuq_segments = null;
		assertArrayEquals("The cache should not contain the segments of 'takulaaqtuq'", expected_takulaaqtuq_segments,
				segmentsCache.get("takulaaqtuq"));
		String[] expected_nunait_segments = null;
		assertArrayEquals("The cache should not contain the segments of 'nunait'", expected_nunait_segments,
				segmentsCache.get("nunait"));
		String[] expected_iglumik_segments = new String[] { "{iglu/1n}", "{mik/tn-acc-s}" };
		assertArrayEquals("The cache should contain the segments of 'iglumik'", expected_iglumik_segments,
				segmentsCache.get("iglumik"));
		TrieNode taku_juq_node = trie.getNode(new String[] { "{taku/1v}", "{juq/1vn}" });
		String expectedText = "{taku/1v} {juq/1vn}";
		Assert.assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.keysAsString());
		TrieNode nuna_it_node = trie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
	Assert.assertTrue("The trie should not contain the node for 'nunait'.", nuna_it_node == null);

		// resume compilation
		retrievedCompiledCorpus.stopAfter = -1; // do not stop anymore; let compilation continue til the end
		retrievedCompiledCorpus.compileCorpus(corpusDirPathname);

		expectedCurrentFileWordCounter = 8;
		Assert.assertEquals("The value of the 'current file word counter' is wrong.", expectedCurrentFileWordCounter,
				retrievedCompiledCorpus.currentFileWordCounter);
		segmentsCache = retrievedCompiledCorpus.getSegmentsCache();
		expected_takulaaqtuq_segments = new String[] { "{taku/1v}", "{laaq/2vv}", "{juq/1vn}" };
		assertArrayEquals("The cache should contain the segments of 'takulaaqtuq'", expected_takulaaqtuq_segments,
				segmentsCache.get("takulaaqtuq"));
		expected_nunait_segments = new String[] { "{nuna/1n}", "{it/tn-nom-p}" };
		assertArrayEquals("The cache should contain the segments of 'nunait'", expected_nunait_segments,
				segmentsCache.get("nunait"));
		expected_iglumik_segments = new String[] { "{iglu/1n}", "{mik/tn-acc-s}" };
		assertArrayEquals("The cache should contain the segments of 'iglumik'", expected_iglumik_segments,
				segmentsCache.get("iglumik"));
		taku_juq_node = retrievedCompiledCorpus.trie.getNode(new String[] { "{taku/1v}", "{juq/1vn}" });
		expectedText = "{taku/1v} {juq/1vn}";
		Assert.assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.keysAsString());
		nuna_it_node = retrievedCompiledCorpus.trie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
	Assert.assertTrue("The trie should contain the node for 'nunait'.", nuna_it_node != null);
		expectedText = "{nuna/1n} {it/tn-nom-p}";
	Assert.assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, nuna_it_node.keysAsString());
    }
        
    
	@Test
    public void test__resume_compilation_of_corpus_after_crash_or_abortion__2_files_in_corpus_directory() throws Exception 
    {
    	// contains 2 files: 
    	// 1 with 8 words:      		   1 with 3 words:
    	// nunavut inuit                   umialiuqti
    	// takujuq                         iglumut
    	// amma                            sanalauqsimajuq
    	// kanaujaq
    	// iglumik takulaaqtuq
    	// nunait

		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq amma kanaujaq iglumik takulaaqtuq nunait",
				"umialiuqti iglumut sanalauqsimajuq"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.saveFrequency = 3;
        compiledCorpus.stopAfter = 10; // should stop after iglumut
        try {
        	compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
        } catch(CompiledCorpusException | StringSegmenterException e) {
        }
			
        CompiledCorpus retrievedCompiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        retrievedCompiledCorpus.setVerbose(false);
        retrievedCompiledCorpus.__resumeCompilation(corpusDirPathname);

        Trie_InMemory trie = retrievedCompiledCorpus.trie;
		long expectedCurrentFileWordCounter = 1;
	Assert.assertEquals("The value of the 'current file word counter' is wrong.", expectedCurrentFileWordCounter,
				retrievedCompiledCorpus.currentFileWordCounter);
		HashMap<String, String[]> segmentsCache = retrievedCompiledCorpus.getSegmentsCache();

		String[] expected_iglumut_segments = null;
		assertArrayEquals("The cache should not contain the segments of 'iglumut'", expected_iglumut_segments,
				segmentsCache.get("iglumut"));
		String[] expected_nunait_segments = new String[] { "{nuna/1n}", "{it/tn-nom-p}" };
		assertArrayEquals("The cache should contain the segments of 'nunait'", expected_nunait_segments,
				segmentsCache.get("nunait"));
		String[] expected_iglumik_segments = new String[] { "{iglu/1n}", "{mik/tn-acc-s}" };
		assertArrayEquals("The cache should contain the segments of 'iglumik'", expected_iglumik_segments,
				segmentsCache.get("iglumik"));
		TrieNode taku_juq_node = trie.getNode(new String[] { "{taku/1v}", "{juq/1vn}" });
		String expectedText = "{taku/1v} {juq/1vn}";
	Assert.assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.keysAsString());
		TrieNode nuna_it_node = trie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
	Assert.assertTrue("The trie should not contain the node for 'nunait'.", nuna_it_node != null);

		// resume compilation
		retrievedCompiledCorpus.stopAfter = -1; // do not stop anymore; let compilation continue til the end
		retrievedCompiledCorpus.compileCorpus(corpusDirPathname);

        Trie_InMemory completeTrie = retrievedCompiledCorpus.trie;
		expectedCurrentFileWordCounter = 3;
	Assert.assertEquals("The value of the 'current file word counter' is wrong.", expectedCurrentFileWordCounter,
				retrievedCompiledCorpus.currentFileWordCounter);
		segmentsCache = retrievedCompiledCorpus.getSegmentsCache();
		String[] expected_takulaaqtuq_segments = new String[] { "{taku/1v}", "{laaq/2vv}", "{juq/1vn}" };
		assertArrayEquals("The cache should contain the segments of 'takulaaqtuq'", expected_takulaaqtuq_segments,
				segmentsCache.get("takulaaqtuq"));
		expected_nunait_segments = new String[] { "{nuna/1n}", "{it/tn-nom-p}" };
		assertArrayEquals("The cache should contain the segments of 'nunait'", expected_nunait_segments,
				segmentsCache.get("nunait"));
		expected_iglumik_segments = new String[] { "{iglu/1n}", "{mik/tn-acc-s}" };
		assertArrayEquals("The cache should contain the segments of 'iglumik'", expected_iglumik_segments,
				segmentsCache.get("iglumik"));
		taku_juq_node = completeTrie.getNode(new String[] { "{taku/1v}", "{juq/1vn}" });
		expectedText = "{taku/1v} {juq/1vn}";
	Assert.assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.keysAsString());
		nuna_it_node = completeTrie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
	Assert.assertTrue("The trie should contain the node for 'nunait'.", nuna_it_node != null);
		expectedText = "{nuna/1n} {it/tn-nom-p}";
	Assert.assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, nuna_it_node.keysAsString());

		TrieNode iglu_mut_node = completeTrie.getNode(new String[] { "{iglu/1n}", "{mut/tn-dat-s}" });
	Assert.assertTrue("The trie should contain the node for 'iglumut'.", iglu_mut_node != null);
		expectedText = "{iglu/1n} {mut/tn-dat-s}";
	Assert.assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, iglu_mut_node.keysAsString());

		TrieNode sana_lauqsima_juq_node = completeTrie
				.getNode(new String[] { "{sana/1v}", "{lauqsima/1vv}", "{juq/1vn}" });
	Assert.assertTrue("The trie should contain the node for 'sanalauqsimajuq'.", sana_lauqsima_juq_node != null);
		expectedText = "{sana/1v} {lauqsima/1vv} {juq/1vn}";
	Assert.assertEquals("The text of the node should be '" + expectedText + "'.", expectedText,
				sana_lauqsima_juq_node.keysAsString());
		
		// 11 words, but one did not analyze.
	Assert.assertEquals("The frequency of the word sanalauqsimajuq should be 1.",1,sana_lauqsima_juq_node.getFrequency());
	Assert.assertEquals("The size of the trie should be 10.",10,completeTrie.getSize());
		
		
    }
    
    @Test
    public void test__compile__3_files_in_corpus_directory() throws Exception
    {
    	// contains 3 files: 
    	// 1 with 8 words:         1 with 3 words:      1 with 3 words:
    	// nunavut inuit           umialiuqti           uqaqti
    	// takujuq                 iglumut              isumajunga
    	// amma                    sanalauqsimajuq      qikiqtait
    	// kanaujaq
    	// iglumik takulaaqtuq
    	// nunait

		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq amma kanaujaq iglumik takulaaqtuq nunait",
				"umialiuqti iglumut sanalauqsimajuq",
				"uqaqti isumajunga qikiqtait"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.saveFrequency = 3;
        try {
        	compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
        } catch(CompiledCorpusException | StringSegmenterException e) {
        }
			
        CompiledCorpus retrievedCompiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        retrievedCompiledCorpus.setVerbose(false);
        retrievedCompiledCorpus.__resumeCompilation(corpusDirPathname);

		Trie_InMemory trie = retrievedCompiledCorpus.trie;

		TrieNode iglu_mut_node = trie.getNode(new String[] { "{iglu/1n}", "{mut/tn-dat-s}" });
	Assert.assertTrue("The trie should contain the node for 'iglumut'.", iglu_mut_node != null);
		String expectedText = "{iglu/1n} {mut/tn-dat-s}";
	Assert.assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, iglu_mut_node.keysAsString());

		TrieNode sana_lauqsima_juq_node = trie.getNode(new String[] { "{sana/1v}", "{lauqsima/1vv}", "{juq/1vn}" });
	Assert.assertTrue("The trie should contain the node for 'sanalauqsimajuq'.", sana_lauqsima_juq_node != null);
		expectedText = "{sana/1v} {lauqsima/1vv} {juq/1vn}";
	Assert.assertEquals("The text of the node should be '" + expectedText + "'.", expectedText,
				sana_lauqsima_juq_node.keysAsString());
				
    }
    
    
    @Test
    public void test__compile_2_subdirectories() throws Exception {
		String[] stringsOfWords11 = new String[] {
				"nunavut", "takujuq", "iglumik", "plugak", "takujuq", "iijuq"
				};
		String[] stringsOfWords12 = new String[] {
				"iglumi", "takulauqtuq", "nanurmik"
				};
		String[] stringsOfWords21 = new String[] {
				"umiaq", "siniktuq", "kuummi"
				};
    	String[][][] subdirs = new String[][][] {
    		{ stringsOfWords11, stringsOfWords12 },
    		{ stringsOfWords21 }
    	};

		String corpusDirPathname = createTemporaryCorpusDirectoryWithSubdirectories(subdirs);
        File corpusSaveFile = new File(corpusDirPathname+"/trie_compilation.json");
        corpusSaveFile.delete();
		
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        try {
        	compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
        	Trie_InMemory trie = compiledCorpus.getTrie();
        	TrieNode terminals[] = trie.getAllTerminals();
        Assert.assertEquals("The number of terminals in the trie is incorrect.",10,terminals.length);
        } catch(CompiledCorpusException | StringSegmenterException e) {
        }
        
       Assert.assertTrue("The compilation file is not in the corpus directory.",corpusSaveFile.exists());

    }
    
    
    
    @Test
    public void test__verify_wordSegmentations_and_other_data_after_compilation() throws Exception
    {
		String[] stringsOfWords = new String[] {
				"nunavut takujuq iglumik plugak takujuq iijuq"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
       try {
        	compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
        } catch(CompiledCorpusException | StringSegmenterException e) {
        }
		
       // takujuq should add something only once into wordSegmentations and decomposedWordsSuite
       String expected = ",,nunavut:{nunavut/1n},,takujuq:{taku/1v}{juq/1vn},,iglumik:{iglu/1n}{mik/tn-acc-s},,iijuq:{ii/1v}{juq/1vn},,";
       String wordSegmentations = compiledCorpus.getWordSegmentations();
	  Assert.assertEquals("The word segmentations string is not correct.", expected, wordSegmentations);
		
       String expectedSuite = ",,nunavut,,takujuq,,iglumik,,iijuq,,";
       String decomposedWordsSuite = compiledCorpus.getDecomposedWordsSuite();
	  Assert.assertEquals("The decomposed words suite string is not correct.", expectedSuite, decomposedWordsSuite);
	   
	   Map<String,Long> expectedNgramStats = new HashMap<String,Long>();
	   expectedNgramStats.put("nun", new Long(1));
	   expectedNgramStats.put("nuna", new Long(1));
	   expectedNgramStats.put("nunav", new Long(1));
	   expectedNgramStats.put("nunavu", new Long(1));
	   expectedNgramStats.put("nunavut", new Long(1));
	   expectedNgramStats.put("una", new Long(1));
	   expectedNgramStats.put("unav", new Long(1));
	   expectedNgramStats.put("unavu", new Long(1));
	   expectedNgramStats.put("unavut", new Long(1));
	   expectedNgramStats.put("nav", new Long(1));
	   expectedNgramStats.put("navu", new Long(1));
	   expectedNgramStats.put("navut", new Long(1));
	   expectedNgramStats.put("avu", new Long(1));
	   expectedNgramStats.put("avut", new Long(1));
	   expectedNgramStats.put("vut", new Long(1));
	   
	   expectedNgramStats.put("tak", new Long(1));
	   expectedNgramStats.put("taku", new Long(1));
	   expectedNgramStats.put("takuj", new Long(1));
	   expectedNgramStats.put("takuju", new Long(1));
	   expectedNgramStats.put("takujuq", new Long(1));
	   expectedNgramStats.put("aku", new Long(1));
	   expectedNgramStats.put("akuj", new Long(1));
	   expectedNgramStats.put("akuju", new Long(1));
	   expectedNgramStats.put("akujuq", new Long(1));
	   expectedNgramStats.put("kuj", new Long(1));
	   expectedNgramStats.put("kuju", new Long(1));
	   expectedNgramStats.put("kujuq", new Long(1));
	   expectedNgramStats.put("uju", new Long(1));
	   expectedNgramStats.put("ujuq", new Long(1));
	   expectedNgramStats.put("juq", new Long(2));
	   
	   expectedNgramStats.put("igl", new Long(1));
	   expectedNgramStats.put("iglu", new Long(1));
	   expectedNgramStats.put("iglum", new Long(1));
	   expectedNgramStats.put("iglumi", new Long(1));
	   expectedNgramStats.put("iglumik", new Long(1));
	   expectedNgramStats.put("glu", new Long(1));
	   expectedNgramStats.put("glum", new Long(1));
	   expectedNgramStats.put("glumi", new Long(1));
	   expectedNgramStats.put("glumik", new Long(1));
	   expectedNgramStats.put("lum", new Long(1));
	   expectedNgramStats.put("lumi", new Long(1));
	   expectedNgramStats.put("lumik", new Long(1));
	   expectedNgramStats.put("umi", new Long(1));
	   expectedNgramStats.put("umik", new Long(1));
	   expectedNgramStats.put("mik", new Long(1));
	   
	   expectedNgramStats.put("iij", new Long(1));
	   expectedNgramStats.put("iiju", new Long(1));
	   expectedNgramStats.put("iijuq", new Long(1));
	   expectedNgramStats.put("iju", new Long(1));
	   expectedNgramStats.put("ijuq", new Long(1));

	   Map<String,Long> ngramStats = compiledCorpus.getNgramStats();
	   AssertHelpers.assertDeepEquals("", expectedNgramStats, ngramStats);
    }
    
    @Test
    public void test__canBeResumed() throws ConfigException, IOException {
		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq amma kanaujaq iglumik takulaaqtuq nunait"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        boolean canBeResumed = compiledCorpus.canBeResumed(corpusDirPathname);
       Assert.assertFalse("The compiler should not be able to resume; there is no JSON compilation backup.",canBeResumed);

        File jsonFile = new File(corpusDirPathname+"/"+CompiledCorpus.JSON_COMPILATION_FILE_NAME);
        jsonFile.createNewFile();
        canBeResumed = compiledCorpus.canBeResumed(corpusDirPathname);
       Assert.assertTrue("The compiler should be able to resume; there is a JSON compilation backup.",canBeResumed);
}
    
	@Test
	public void test__processDocumentContents__happy_path() throws Exception {
		String documentContents = "inuit takujuq nunavut takujuq takulaaqtuq";
		BufferedReader br = new BufferedReader(new StringReader(documentContents));
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.processDocumentContents(br,null);
		
		String[] inuit_segments = new String[]{"{inuk/1n}","{it/tn-nom-p}", "\\"};
		String[] taku_segments = new String[]{"{taku/1v}"};
		String[] takujuq_segments = new String[]{"{taku/1v}", "{juq/1vn}", "\\"};
		
		assertContains(compiledCorpus, inuit_segments, 1, inuit_segments);
		assertContains(compiledCorpus, taku_segments, 3, takujuq_segments);
		assertContains(compiledCorpus, takujuq_segments, 2, takujuq_segments);
	}
	
	@Test
	public void test__readFromJson() throws Exception {
		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq amma kanaujaq iglumik takulaaqtuq nunait"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.saveFrequency = 3;
        compiledCorpus.stopAfter = 7; 
        // Compilation should stop after takulaaqtuq. This is to simulate
        // an exception raised during the segmentation of takulaaqtuq, which
        // should result in takulaaqtuq not being compiled.
        try {
        	compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
        } catch(CompiledCorpusException | StringSegmenterException e) {
        }
        
        CompiledCorpus retrievedCompiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        retrievedCompiledCorpus.__resumeCompilation(corpusDirPathname);
        retrievedCompiledCorpus.setVerbose(false);
        //FileUtils.deleteDirectory(dir);

		Trie_InMemory trie = retrievedCompiledCorpus.trie;
		long trieSize = trie.getSize();
		long expectedSize = 5;
	Assert.assertEquals("The number of terminals in the retrieved trie is faulty.",expectedSize,trieSize);

		long expectedCurrentFileWordCounter = 6;
	Assert.assertEquals("The value of the 'current file word counter' is wrong.", expectedCurrentFileWordCounter,
				retrievedCompiledCorpus.currentFileWordCounter);
		HashMap<String, String[]> segmentsCache = retrievedCompiledCorpus.getSegmentsCache();
		String[] expected_takulaaqtuq_segments = null;
		assertArrayEquals("The cache should not contain the segments of 'takulaaqtuq'", expected_takulaaqtuq_segments,
				segmentsCache.get("takulaaqtuq"));
		String[] expected_nunait_segments = null;
		assertArrayEquals("The cache should not contain the segments of 'nunait'", expected_nunait_segments,
				segmentsCache.get("nunait"));
		String[] expected_iglumik_segments = new String[] { "{iglu/1n}", "{mik/tn-acc-s}" };
		assertArrayEquals("The cache should contain the segments of 'iglumik'", expected_iglumik_segments,
				segmentsCache.get("iglumik"));
		TrieNode taku_juq_node = trie.getNode(new String[] { "{taku/1v}", "{juq/1vn}" });
		String expectedText = "{taku/1v} {juq/1vn}";
	Assert.assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.keysAsString());
		TrieNode nuna_it_node = trie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
	Assert.assertTrue("The trie should not contain the node for 'nunait'.", nuna_it_node == null);
		
}
	
	
	@Test
	public void test__mostFrequentWordWithRadical() throws Exception {
		CompiledCorpus compiledCorpus = new CompiledCorpus();
        compiledCorpus.setVerbose(false);
        Trie_InMemory charTrie = new Trie_InMemory();
		try {
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helicopter");
		compiledCorpus.trie = charTrie;
		} catch (Exception e) {
		Assert.assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode mostFrequent = compiledCorpus.getMostFrequentTerminal("hel".split(""));
	Assert.assertEquals("The frequency of the most frequent found is wrong.",2,mostFrequent.getFrequency());
	Assert.assertEquals("The text of the the most frequent found is wrong.","h e l i c o p t e r \\",mostFrequent.keysAsString());
	}

	@Test
	public void test__getTerminalsSumFreq() throws Exception {
		CompiledCorpus compiledCorpus = new CompiledCorpus();
        compiledCorpus.setVerbose(false);
        Trie_InMemory charTrie = new Trie_InMemory();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helicopter");
		compiledCorpus.trie = charTrie;
		long nbCompiledOccurrences = compiledCorpus.getNumberOfCompiledOccurrences();
	Assert.assertEquals("The sum of the frequencies of all terminals is incorrect.",5,nbCompiledOccurrences);
		
	}
	
	/*
	 * 
	 */

	
    private String createTemporaryCorpusDirectory(String[] stringOfWords) throws IOException {
       	Logger logger = Logger.getLogger("CompiledCorpusTest.createTemporaryCorpusDirectory");
        corpusDirectory = Files.createTempDirectory("").toFile();
        corpusDirectory.deleteOnExit();
        String corpusDirPath = corpusDirectory.getAbsolutePath();
        for (int i=0; i<stringOfWords.length; i++) {
        	File wordFile = new File(corpusDirPath+"/contents"+(i+1)+".txt");
        	BufferedWriter bw = new BufferedWriter(new FileWriter(wordFile));
        	bw.write(stringOfWords[i]);
        	bw.close();
        	logger.debug("wordFile= "+wordFile.getAbsolutePath());
        	logger.debug("contents= "+wordFile.length());
        }
        return corpusDirPath;
	}
    
    private String createTemporaryCorpusDirectoryWithSubdirectories(String[][][] subdirs) throws IOException {
        Path corpusDirectory = Files.createTempDirectory("corpus_");
        corpusDirectory.toFile().deleteOnExit();
        for (int isubdir=0; isubdir<subdirs.length; isubdir++) {
        	Path subDirectory = Files.createTempDirectory(corpusDirectory,"sub_");
        	subDirectory.toFile().deleteOnExit();
        	String [][] subdirFiles = subdirs[isubdir];
        	for (int ifile=0; ifile<subdirFiles.length; ifile++) {
        		String[] words = subdirFiles[ifile];
        		Path filepath = Files.createTempFile(subDirectory, "file_", ".txt");
        		filepath.toFile().deleteOnExit();
        		File file = filepath.toFile();
            	BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            	String lineOfWords = String.join(" ", words);
            	bw.write(lineOfWords);
            	bw.close();
        	}
        }
        return corpusDirectory.toFile().getAbsolutePath();
    }


	@Test
	public void test__getNbFailedSegmentations() throws Exception {
		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq amma kanaujaq iglumik takulaaqtuq nunait"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
       Assert.assertEquals("The number of words that failed segmentation is wrong.",1,
        		compiledCorpus.getNbWordsThatFailedSegmentations());
       Assert.assertEquals("The number of occurrences that failed segmentation is wrong.",1,
        		compiledCorpus.getNbOccurrencesThatFailedSegmentations());
	}
	

	@Test
	public void test_recompileWordsWhoFailedAnalysis() throws Exception {
		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq amma kanaujaq iglumik takulaaqtuq nunait"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        String completeCompilationFilePathname = corpusDirPathname+"/compiled_corpus.json";
//        compiledCorpus.setCompleteCompilationFilePath(completeCompilationFilePathname);
        compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
       Assert.assertEquals("The number of words that failed segmentation is wrong.",1,
        		compiledCorpus.getNbWordsThatFailedSegmentations());
       Assert.assertEquals("The number of occurrences that failed segmentation is wrong.",1,
        		compiledCorpus.getNbOccurrencesThatFailedSegmentations());

        String wordThatFailed = "saqkilauqtuq";
        String[] keysOfFailedWord = new String[] {"{saqqik/1v}","{lauq/1vv}","{juq/1vn}"};
        TrieNode trieNode1 = compiledCorpus.getTrie().getNode(keysOfFailedWord);
        Assert.assertFalse("The node should not exist in the trie.",trieNode1 != null);
        compiledCorpus.getWordsFailedSegmentation().add(wordThatFailed);
        compiledCorpus.wordsFailedSegmentationWithFreqs.put(wordThatFailed, new Long(4));
        compiledCorpus.getSegmentsCache().put(wordThatFailed, new String[] {});
        Assert.assertEquals("The number of words that failed segmentation is wrong.",2,
        		compiledCorpus.getNbWordsThatFailedSegmentations());
        Assert.assertEquals("The number of occurrences that failed segmentation is wrong.",5,
        		compiledCorpus.getNbOccurrencesThatFailedSegmentations());
        
        compiledCorpus.recompileWordsThatFailedAnalysis(corpusDirPathname);
        
       Assert.assertEquals("The number of words that failed segmentation is wrong.",1,
        		compiledCorpus.getNbWordsThatFailedSegmentations());
       Assert.assertEquals("The number of occurrences that failed segmentation is wrong.",1,
        		compiledCorpus.getNbOccurrencesThatFailedSegmentations());
       Assert.assertEquals("","{saqqik/1v} {lauq/1vv} {juq/1vn}",
        		String.join(" ", compiledCorpus.getSegmentsCache().get(wordThatFailed)));
        TrieNode trieNode2 = compiledCorpus.getTrie().getNode(compiledCorpus.getSegmentsCache().get(wordThatFailed));
       Assert.assertTrue("The node should exist in the trie.",trieNode2 != null);
       Assert.assertEquals("The frequency of that node is not right.",4,trieNode2.getFrequency());
        // compilation file should be updated
        String compiledCorpusFilename = corpusDirPathname+"/"+CompiledCorpus.JSON_COMPILATION_FILE_NAME;
        CompiledCorpus newCompiledCorpus = CompiledCorpus.createFromJson(compiledCorpusFilename);
       Assert.assertEquals("(3) The number of words that failed segmentation is wrong.",1,
        		newCompiledCorpus.getNbWordsThatFailedSegmentations());
       Assert.assertEquals("(3) The number of occurrences that failed segmentation is wrong.",1,
        		newCompiledCorpus.getNbOccurrencesThatFailedSegmentations());
       Assert.assertEquals("(3)","{saqqik/1v} {lauq/1vv} {juq/1vn}",
        		String.join(" ", newCompiledCorpus.getSegmentsCache().get(wordThatFailed)));
        
        compiledCorpus.saveCompilerInJSONFile(completeCompilationFilePathname);
        TrieNode trieNode3 = CompiledCorpus.createFromJson(completeCompilationFilePathname).getTrie().getNode(compiledCorpus.getSegmentsCache().get(wordThatFailed));
       Assert.assertTrue("The node should exist in the trie.",trieNode3 != null);
       Assert.assertEquals("The frequency of that node is not right.",4,trieNode3.getFrequency());
	}
	
	@Test
	public void test__saveCompilerInJSONFile() throws IOException, CompiledCorpusException, StringSegmenterException, CompiledCorpusRegistryException {
		String[] stringsOfWords = new String[] {
				"nunavut inuit"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
		File tempFile = File.createTempFile("compiled_corpus", ".json");
		compiledCorpus.saveCompilerInJSONFile(tempFile.getAbsolutePath());
		CompiledCorpusRegistry.registerCorpus("compiled_corpus", tempFile);
		
		CompiledCorpus savedCompiledCorpus = CompiledCorpusRegistry.getCorpus("compiled_corpus");
	Assert.assertEquals("",",,nunavut,,inuit,,",savedCompiledCorpus.decomposedWordsSuite);
	}
	
	
	@Test
	public void test__isWordInCorpus() throws IOException, CompiledCorpusException, StringSegmenterException {
		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq uvlimik"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
		String word;
		Boolean result;
		
		word = "iben";
		result = compiledCorpus.isWordInCorpus(word);
	Assert.assertTrue("The word "+word+" is not in the corpus; result should be null",result==null);
		
		word = "takujuq";
		result = compiledCorpus.isWordInCorpus(word);
	Assert.assertTrue("The word "+word+" is in the corpus with successful analysis; result should be true",result.booleanValue()==true);
		
		word = "uvlimik";
		result = compiledCorpus.isWordInCorpus(word);
	Assert.assertTrue("The word "+word+" is in the corpus with unsuccessful analysis; result should be false",result.booleanValue()==false);
	}
	
	@Test
	public void test__getWordsContainingMorpheme() throws Exception {
		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq sinilauqtuq uvlimik takulauqtunga"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
		
        List<WordWithMorpheme> words = compiledCorpus.getWordsContainingMorpheme("lauq");
        Assert.assertEquals("", 2, words.size());
        AssertObject.assertDeepEquals("", new WordWithMorpheme("sinilauqtuq","lauq/1vv","{sinik/1v}{lauq/1vv}{juq/1vn}",(long)1), words.get(0));
        AssertObject.assertDeepEquals("", new WordWithMorpheme("takulauqtunga","lauq/1vv","{taku/1v}{lauq/1vv}{junga/tv-ger-1s}",(long)1), words.get(1));
	}
	

	private void assertContains(CompiledCorpus compiledCorpus,
			String[] segs, long expFreq, String[] expLongestTerminal) 
					throws Exception {
		TrieNode gotNode = compiledCorpus.trie.getNode(segs);
		String seqs_asString = String.join(", ", segs);
		String jsonCorpus = compiledCorpus.trie.toJSON();
		Assert.assertTrue(
			"Trie should have contained sequence: "+seqs_asString+
			"\nCorpus is:\n"+jsonCorpus, 
			gotNode != null);
		long gotFreq = gotNode.getFrequency();
		Assert.assertEquals("Frequency was not as expected for segmenets: "+seqs_asString, expFreq, gotFreq);
		
		if (expLongestTerminal != null) {
			TrieNode mostFrequent = compiledCorpus.getMostFrequentTerminal(gotNode);
			String gotMostFreqTerminalTxt = mostFrequent.keysAsString();
			String expMostFreqTerminalTxt = String.join(" ", expLongestTerminal);
			AssertHelpers.assertStringEquals("Most frequent terminal was not as expected for segs "+seqs_asString, 
					expMostFreqTerminalTxt, gotMostFreqTerminalTxt);
			
		}
	}

	public static File compileToFile(String[] words) throws Exception {
		return compileToFile(words,null);
	}
	
	
	public static File compileToFile(String[] words, String fileId) throws Exception {
		CompiledCorpus tempCorp = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
		tempCorp.setVerbose(false);
		InputStream iStream = IOUtils.toInputStream(String.join(" ", words), "utf-8");
		InputStreamReader iSReader = new InputStreamReader(iStream);
		BufferedReader br = new BufferedReader(iSReader);
		tempCorp.processDocumentContents(br, "dummyFilePath");
		String fileName = "compiled_corpus";
		if (fileId != null)
			fileName += "-"+fileId;
		File tempFile = File.createTempFile(fileName, ".json");
		tempCorp.saveCompilerInJSONFile(tempFile.toString());
		return tempFile;
	}
}
