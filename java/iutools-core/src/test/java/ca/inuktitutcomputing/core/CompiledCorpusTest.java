package ca.inuktitutcomputing.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.datastructure.trie.TrieReader;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertHelpers;

/**
 * Unit test for simple App.
 */
public class CompiledCorpusTest 
{
    /**
     * Rigorous Test :-)
     * @throws Exception 
     */
	@Test
	public void test__CompiledCorpus__Synopsis() throws Exception {
		//
		// Use a CompiledCorpus to trie-compile a corpus and compute statistics.
		//
		// By default, the compiler will use the char segmenter
		CompiledCorpus compiledCorpus;
		compiledCorpus = new CompiledCorpus();
		// or it will use the segmenter passed in argument as its class name
		compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());

		// Identify the full path of the corpus directory to be compiled
		String corpusDirectoryPathname = IUConfig.getIUDataPath()+"src/test/HansardCorpus1";
		
		// If wanted, identify the full path of a copy of the trie-compilation json file
		// that will be created after the compilation has terminated successfully.
		// If the path points to a non-existent directory, the copy will not be made.
		String trieCompilationFilePathname = "path/to/file";
		boolean result = compiledCorpus.setTrieFilePath(trieCompilationFilePathname);
		if ( !result ) {
			// do something (raise an exception, or abort, or ...)
		}

		// Compile the corpus given in argument as directory pathname from scratch
		compiledCorpus.compileCorpusFromScratch(corpusDirectoryPathname);
		
		// Compile the corpus given in argument as directory pathname (will resume where it was left after the last run)
		compiledCorpus.compileCorpus(corpusDirectoryPathname);
	}

	@Test
    public void test__resume_compilation_of_corpus_after_crash_or_abortion__1_file_in_corpus_directory() throws Exception
    {
    	// contains 1 file of 6 lines with 8 words :
    	// nunavut inuit
    	// takujuq
    	// amma
    	// kinaujaq
    	// iglumik takulaaqtuq
    	// nunait

        String corpusDir = IUConfig.getIUDataPath()+"src/test/HansardCorpus1";
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.saveFrequency = 3;
        compiledCorpus.stopAfter = 7; // should stop after takulaaqtuq
        try {
        	compiledCorpus.compileCorpusFromScratch(corpusDir);
        } catch(Exception e) {
        	System.err.println("Exiting from compiler");
        }
			
        CompiledCorpus retrievedCompiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        retrievedCompiledCorpus.readFromJson(corpusDir);
        
		Trie trie = retrievedCompiledCorpus.trie;
		long expectedCurrentFileWordCounter = 6;
		assertEquals("The value of the 'current file word counter' is wrong.", expectedCurrentFileWordCounter,
				retrievedCompiledCorpus.currentFileWordCounter);
		HashMap<String, String[]> segmentsCache = retrievedCompiledCorpus.segmentsCache;
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
		String expectedText = "{taku/1v}{juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.getKeys());
		TrieNode nuna_it_node = trie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
		assertTrue("The trie should not contain the node for 'nunait'.", nuna_it_node == null);

		// resume compilation
		retrievedCompiledCorpus.stopAfter = -1; // do not stop anymore; let compilation continue til the end
		retrievedCompiledCorpus.compileCorpus(corpusDir);

		expectedCurrentFileWordCounter = 8;
		assertEquals("The value of the 'current file word counter' is wrong.", expectedCurrentFileWordCounter,
				retrievedCompiledCorpus.currentFileWordCounter);
		segmentsCache = retrievedCompiledCorpus.segmentsCache;
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
		expectedText = "{taku/1v}{juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.getKeys());
		nuna_it_node = retrievedCompiledCorpus.trie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
		assertTrue("The trie should contain the node for 'nunait'.", nuna_it_node != null);
		expectedText = "{nuna/1n}{it/tn-nom-p}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, nuna_it_node.getKeys());
    }
        
    
    @Test
    public void test__resume_compilation_of_corpus_after_crash_or_abortion__2_files_in_corpus_directory() throws Exception
    {
    	// contains 2 files: 
    	// 1 of 6 lines with 8 words:      1 of 3 lines with 3 words:
    	// nunavut inuit                   umialiuqti
    	// takujuq                         iglumut
    	// amma                            sanalauqsimajuq
    	// kinaujaq
    	// iglumik takulaaqtuq
    	// nunait

        String corpusDir = IUConfig.getIUDataPath()+"src/test/HansardCorpus2";
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.saveFrequency = 3;
        compiledCorpus.stopAfter = 10; // should stop after iglumut
        try {
        	compiledCorpus.compileCorpusFromScratch(corpusDir);
        } catch(Exception e) {
        	System.err.println("Exiting from compiler");
        }
			
        CompiledCorpus retrievedCompiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        retrievedCompiledCorpus.readFromJson(corpusDir);

        Trie trie = retrievedCompiledCorpus.trie;
		long expectedCurrentFileWordCounter = 1;
		assertEquals("The value of the 'current file word counter' is wrong.", expectedCurrentFileWordCounter,
				retrievedCompiledCorpus.currentFileWordCounter);
		HashMap<String, String[]> segmentsCache = retrievedCompiledCorpus.segmentsCache;

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
		String expectedText = "{taku/1v}{juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.getKeys());
		TrieNode nuna_it_node = trie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
		assertTrue("The trie should not contain the node for 'nunait'.", nuna_it_node != null);

		// resume compilation
		retrievedCompiledCorpus.stopAfter = -1; // do not stop anymore; let compilation continue til the end
		retrievedCompiledCorpus.compileCorpus(corpusDir);

        Trie completeTrie = retrievedCompiledCorpus.trie;
		expectedCurrentFileWordCounter = 3;
		assertEquals("The value of the 'current file word counter' is wrong.", expectedCurrentFileWordCounter,
				retrievedCompiledCorpus.currentFileWordCounter);
		segmentsCache = retrievedCompiledCorpus.segmentsCache;
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
		expectedText = "{taku/1v}{juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.getKeys());
		nuna_it_node = completeTrie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
		assertTrue("The trie should contain the node for 'nunait'.", nuna_it_node != null);
		expectedText = "{nuna/1n}{it/tn-nom-p}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, nuna_it_node.getKeys());

		TrieNode iglu_mut_node = completeTrie.getNode(new String[] { "{iglu/1n}", "{mut/tn-dat-s}" });
		assertTrue("The trie should contain the node for 'iglumut'.", iglu_mut_node != null);
		expectedText = "{iglu/1n}{mut/tn-dat-s}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, iglu_mut_node.getKeys());

		TrieNode sana_lauqsima_juq_node = completeTrie
				.getNode(new String[] { "{sana/1v}", "{lauqsima/1vv}", "{juq/1vn}" });
		assertTrue("The trie should contain the node for 'sanalauqsimajuq'.", sana_lauqsima_juq_node != null);
		expectedText = "{sana/1v}{lauqsima/1vv}{juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText,
				sana_lauqsima_juq_node.getKeys());
		
		TrieNode[] allTerminals = completeTrie.getAllTerminals();
		for (int i=0; i<allTerminals.length; i++) System.out.println(allTerminals[i].getKeys());

		// 11 words, but one did not analyze.
		assertEquals("The frequency of the word sanalauqsimajuq should be 1.",1,sana_lauqsima_juq_node.getFrequency());
		assertEquals("The size of the trie should be 10.",10,completeTrie.getSize());
		
		
    }
    
    @Test
    public void test__compile__3_files_in_corpus_directory() throws Exception
    {
    	// contains 3 files: 
    	// 1 of 6 lines with 8 words:      1 of 3 lines with 3 words:
    	// nunavut inuit                   umialiuqti
    	// takujuq                         iglumut
    	// amma                            sanalauqsimajuq
    	// kinaujaq
    	// iglumik takulaaqtuq
    	// nunait

        String corpusDir = IUConfig.getIUDataPath()+"src/test/HansardCorpus3";
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.saveFrequency = 3;
        try {
        	compiledCorpus.compileCorpusFromScratch(corpusDir);
        } catch(Exception e) {
        	System.err.println("Exiting from compiler");
        }
			
        CompiledCorpus retrievedCompiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        retrievedCompiledCorpus.readFromJson(corpusDir);

		Trie trie = retrievedCompiledCorpus.trie;

		TrieNode iglu_mut_node = trie.getNode(new String[] { "{iglu/1n}", "{mut/tn-dat-s}" });
		assertTrue("The trie should contain the node for 'iglumut'.", iglu_mut_node != null);
		String expectedText = "{iglu/1n}{mut/tn-dat-s}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, iglu_mut_node.getKeys());

		TrieNode sana_lauqsima_juq_node = trie.getNode(new String[] { "{sana/1v}", "{lauqsima/1vv}", "{juq/1vn}" });
		assertTrue("The trie should contain the node for 'sanalauqsimajuq'.", sana_lauqsima_juq_node != null);
		expectedText = "{sana/1v}{lauqsima/1vv}{juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText,
				sana_lauqsima_juq_node.getKeys());
				
    }
    
    @Test
    public void test__canBeResumed() throws ConfigException {
        String corpusDirPathname = IUConfig.getIUDataPath()+"src/test/HansardCorpusNoJSON";
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        boolean canBeResumed = compiledCorpus.canBeResumed(corpusDirPathname);
        assertFalse("The compiler should not be able to resume; there is no JSON compilation backup.",canBeResumed);

        corpusDirPathname = IUConfig.getIUDataPath()+"src/test/HansardCorpusWithJSON";
        canBeResumed = compiledCorpus.canBeResumed(corpusDirPathname);
        assertTrue("The compiler should bebe able to resume; there is a JSON compilation backup.",canBeResumed);
}
    
	@Test
	public void test__processDocumentContents__happy_path() throws Exception {
		String documentContents = "inuit takujuq nunavut takujuq takulaaqtuq";
		BufferedReader br = new BufferedReader(new StringReader(documentContents));
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.processDocumentContents(br,null);
		
		String[] inuit_segments = new String[]{"{inuk/1n}","{it/tn-nom-p}"};
		String[] taku_segments = new String[]{"{taku/1v}"};
		String[] takujuq_segments = new String[]{"{taku/1v}", "{juq/1vn}"};
		
		assertContains(compiledCorpus, inuit_segments, 1, inuit_segments);
		assertContains(compiledCorpus, taku_segments, 3, takujuq_segments);
		assertContains(compiledCorpus, takujuq_segments, 2, takujuq_segments);
	}
	
	@Test
	public void test__readFromJson() throws Exception {
        String corpusDir = IUConfig.getIUDataPath()+"src/test/HansardCorpus1";
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.saveFrequency = 3;
        compiledCorpus.stopAfter = 7; // should stop after takulaaqtuq
        try {
        	compiledCorpus.compileCorpusFromScratch(corpusDir);
        } catch(Exception e) {
        	System.err.println("Exiting from compiler");
        }
        
        CompiledCorpus retrievedCompiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        retrievedCompiledCorpus.readFromJson(corpusDir);

		Trie trie = retrievedCompiledCorpus.trie;
		long expectedCurrentFileWordCounter = 6;
		assertEquals("The value of the 'current file word counter' is wrong.", expectedCurrentFileWordCounter,
				retrievedCompiledCorpus.currentFileWordCounter);
		HashMap<String, String[]> segmentsCache = retrievedCompiledCorpus.segmentsCache;
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
		String expectedText = "{taku/1v}{juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.getKeys());
		TrieNode nuna_it_node = trie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
		assertTrue("The trie should not contain the node for 'nunait'.", nuna_it_node == null);
}
	
	
	@Test
	public void test__mostFrequentWordWithRadical() {
		CompiledCorpus compiledCorpus = new CompiledCorpus();
		Trie charTrie = new Trie();
		try {
		charTrie.add("hello".split(""));
		charTrie.add("hint".split(""));
		charTrie.add("helicopter".split(""));
		charTrie.add("helios".split(""));
		charTrie.add("helicopter".split(""));
		compiledCorpus.trie = charTrie;
		} catch (Exception e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode mostFrequent = compiledCorpus.getMostFrequentTerminal("hel".split(""));
		assertEquals("The frequency of the most frequent found is wrong.",2,mostFrequent.getFrequency());
		assertEquals("The text of the the most frequent found is wrong.","helicopter",mostFrequent.getKeys());
	}

	@Test
	public void test__getTerminalsSumFreq() throws TrieException {
		CompiledCorpus compiledCorpus = new CompiledCorpus();
		Trie charTrie = new Trie();
		charTrie.add("hello".split(""));
		charTrie.add("hint".split(""));
		charTrie.add("helicopter".split(""));
		charTrie.add("helios".split(""));
		charTrie.add("helicopter".split(""));
		compiledCorpus.trie = charTrie;
		long nbCompiledOccurrences = compiledCorpus.getNumberOfCompiledOccurrences();
		assertEquals("The sum of the frequencies of all terminals is incorrect.",5,nbCompiledOccurrences);
		
	}

	@Test
	public void test__mostFrequentSequenceToTerminals__Char() throws TrieException, IOException {
		CompiledCorpus compiledCorpus = new CompiledCorpus();
		Trie charTrie = new Trie();
		charTrie.add("hello".split(""));
		charTrie.add("hint".split(""));
		charTrie.add("helicopter".split(""));
		charTrie.add("helios".split(""));
		charTrie.add("helicopter".split(""));
		compiledCorpus.trie = charTrie;
		String[] mostFrequentSegments = compiledCorpus.getMostFrequentSequenceForRoot("h");
		//System.out.println(PrettyPrinter.print(mostFrequentSegments));
		String[] expected = new String[] {"h","e"};
		AssertHelpers.assertDeepEquals("The most frequent sequence should be heli.",expected,mostFrequentSegments);
	}
	
	@Test
	public void test__mostFrequentSequenceToTerminals__IUMorpheme() throws TrieException, IOException {
		CompiledCorpus compiledCorpus = new CompiledCorpus();
		Trie morphTrie = new Trie();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		compiledCorpus.trie = morphTrie;
		String[] mostFrequentSegments = compiledCorpus.getMostFrequentSequenceForRoot("{taku/1v}");
		//System.out.println(PrettyPrinter.print(mostFrequentSegments));
		String[] expected = new String[] {"{taku/1v}","{juq/1vn}"};
		AssertHelpers.assertDeepEquals("The most frequent sequence should be heli.",expected,mostFrequentSegments);
	}
	
	@Test
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__1() throws TrieException {
		CompiledCorpus compiledCorpus = new CompiledCorpus();
		Trie morphTrie = new Trie();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"});
		compiledCorpus.trie = morphTrie;
		TrieNode mostFrequentTerminal = compiledCorpus.getMostFrequentTerminalFromMostFrequentSequenceForRoot("{taku/1v}");
		String expected = "{taku/1v}{juq/1vn}";
		assertEquals("The most frequent term for 'taku' in the trie is not correct.",expected,mostFrequentTerminal.getKeys());
	}
	
	@Test
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__2() throws TrieException {
		CompiledCorpus compiledCorpus = new CompiledCorpus();
		Trie morphTrie = new Trie();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"});
		compiledCorpus.trie = morphTrie;
		TrieNode mostFrequentTerminal = compiledCorpus.getMostFrequentTerminalFromMostFrequentSequenceForRoot("{taku/1v}");
		String expected = "{taku/1v}{juq/1vn}";
		assertEquals("The most frequent term for 'taku' in the trie is not correct.",expected,mostFrequentTerminal.getKeys());
	}
	
	@Test
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__3() throws TrieException {
		CompiledCorpus compiledCorpus = new CompiledCorpus();
		Trie morphTrie = new Trie();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"});
		compiledCorpus.trie = morphTrie;
		TrieNode mostFrequentTerminal = compiledCorpus.getMostFrequentTerminalFromMostFrequentSequenceForRoot("{taku/1v}");
		String expected = "{taku/1v}{laaq/2vv}{juq/1vn}";
		assertEquals("The most frequent term for 'taku' in the trie is not correct.",expected,mostFrequentTerminal.getKeys());
	}
	
	

	private void assertContains(CompiledCorpus compiledCorpus,
			String[] segs, long expFreq) {
		assertContains(compiledCorpus, segs, expFreq, null);
	}

	private void assertContains(CompiledCorpus compiledCorpus,
			String[] segs, long expFreq, String[] expLongestTerminal) {
		TrieNode gotNode = compiledCorpus.trie.getNode(segs);
		String seqs_asString = String.join(", ", segs);
		Assert.assertTrue("Trie should have contained sequence: "+seqs_asString+"\nTrie contained:\n"+compiledCorpus.trie.toJSON(), gotNode != null);
		long gotFreq = gotNode.getFrequency();
		Assert.assertEquals("Frequency was not as expected for segmenets: "+seqs_asString, expFreq, gotFreq);
		
		if (expLongestTerminal != null) {
			String gotMostFreqTerminalTxt = gotNode.getMostFrequentTerminal().getKeys();
			String expMostFreqTerminalTxt = String.join("", expLongestTerminal);
			AssertHelpers.assertStringEquals("Most frequent terminal was not as expected for segs "+seqs_asString, 
					expMostFreqTerminalTxt, gotMostFreqTerminalTxt);
			
		}
	}
	

}
