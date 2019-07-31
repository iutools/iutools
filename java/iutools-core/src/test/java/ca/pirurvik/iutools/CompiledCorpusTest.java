package ca.pirurvik.iutools;

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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.testing.AssertHelpers;
import ca.pirurvik.iutools.CompiledCorpus;
import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class CompiledCorpusTest extends TestCase
{
    /**
     * Rigorous Test :-)
     * @throws Exception 
     */
	
	private File corpusDirectory = null;
	
	@Override
    protected void tearDown() throws Exception {
        if (corpusDirectory != null) {
        	File[] listOfFiles = corpusDirectory.listFiles();
        	for (File file : listOfFiles)
        		file.delete();
        }
        corpusDirectory = null;
    }

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
		// compiler will segment words by morphemes.
		//
		compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());

		// Identify the full path of the corpus directory to be compiled
		//
		String corpusDirectoryPathname = "path/to/corpus/directory";
		
		// Compile the corpus given in argument as directory pathname from scratch
		compiledCorpus.setVerbose(false); // set verbose to false for tests only
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
	}

	@Test
    public void test__resume_compilation_of_corpus_after_crash_or_abortion__1_file_in_corpus_directory() throws CompiledCorpusException, StringSegmenterException, IOException 
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
		String expectedText = "{taku/1v} {juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.getKeysAsString());
		TrieNode nuna_it_node = trie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
		assertTrue("The trie should not contain the node for 'nunait'.", nuna_it_node == null);

		// resume compilation
		retrievedCompiledCorpus.stopAfter = -1; // do not stop anymore; let compilation continue til the end
		retrievedCompiledCorpus.compileCorpus(corpusDirPathname);

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
		expectedText = "{taku/1v} {juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.getKeysAsString());
		nuna_it_node = retrievedCompiledCorpus.trie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
		assertTrue("The trie should contain the node for 'nunait'.", nuna_it_node != null);
		expectedText = "{nuna/1n} {it/tn-nom-p}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, nuna_it_node.getKeysAsString());
    }
        
    
	@Test
    public void test__resume_compilation_of_corpus_after_crash_or_abortion__2_files_in_corpus_directory() throws CompiledCorpusException, StringSegmenterException, IOException 
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
		String expectedText = "{taku/1v} {juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.getKeysAsString());
		TrieNode nuna_it_node = trie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
		assertTrue("The trie should not contain the node for 'nunait'.", nuna_it_node != null);

		// resume compilation
		retrievedCompiledCorpus.stopAfter = -1; // do not stop anymore; let compilation continue til the end
		retrievedCompiledCorpus.compileCorpus(corpusDirPathname);

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
		expectedText = "{taku/1v} {juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.getKeysAsString());
		nuna_it_node = completeTrie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
		assertTrue("The trie should contain the node for 'nunait'.", nuna_it_node != null);
		expectedText = "{nuna/1n} {it/tn-nom-p}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, nuna_it_node.getKeysAsString());

		TrieNode iglu_mut_node = completeTrie.getNode(new String[] { "{iglu/1n}", "{mut/tn-dat-s}" });
		assertTrue("The trie should contain the node for 'iglumut'.", iglu_mut_node != null);
		expectedText = "{iglu/1n} {mut/tn-dat-s}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, iglu_mut_node.getKeysAsString());

		TrieNode sana_lauqsima_juq_node = completeTrie
				.getNode(new String[] { "{sana/1v}", "{lauqsima/1vv}", "{juq/1vn}" });
		assertTrue("The trie should contain the node for 'sanalauqsimajuq'.", sana_lauqsima_juq_node != null);
		expectedText = "{sana/1v} {lauqsima/1vv} {juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText,
				sana_lauqsima_juq_node.getKeysAsString());
		
		// 11 words, but one did not analyze.
		assertEquals("The frequency of the word sanalauqsimajuq should be 1.",1,sana_lauqsima_juq_node.getFrequency());
		assertEquals("The size of the trie should be 10.",10,completeTrie.getSize());
		
		
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

		Trie trie = retrievedCompiledCorpus.trie;

		TrieNode iglu_mut_node = trie.getNode(new String[] { "{iglu/1n}", "{mut/tn-dat-s}" });
		assertTrue("The trie should contain the node for 'iglumut'.", iglu_mut_node != null);
		String expectedText = "{iglu/1n} {mut/tn-dat-s}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, iglu_mut_node.getKeysAsString());

		TrieNode sana_lauqsima_juq_node = trie.getNode(new String[] { "{sana/1v}", "{lauqsima/1vv}", "{juq/1vn}" });
		assertTrue("The trie should contain the node for 'sanalauqsimajuq'.", sana_lauqsima_juq_node != null);
		expectedText = "{sana/1v} {lauqsima/1vv} {juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText,
				sana_lauqsima_juq_node.getKeysAsString());
				
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
	   assertEquals("The word segmentations string is not correct.", expected, wordSegmentations);
		
       String expectedSuite = ",,nunavut,,takujuq,,iglumik,,iijuq,,";
       String decomposedWordsSuite = compiledCorpus.getDecomposedWordsSuite();
	   assertEquals("The decomposed words suite string is not correct.", expectedSuite, decomposedWordsSuite);
	   
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
        assertFalse("The compiler should not be able to resume; there is no JSON compilation backup.",canBeResumed);

        File jsonFile = new File(corpusDirPathname+"/"+CompiledCorpus.JSON_COMPILATION_FILE_NAME);
        jsonFile.createNewFile();
        canBeResumed = compiledCorpus.canBeResumed(corpusDirPathname);
        assertTrue("The compiler should be able to resume; there is a JSON compilation backup.",canBeResumed);
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

		Trie trie = retrievedCompiledCorpus.trie;
		long trieSize = trie.getSize();
		long expectedSize = 5;
		assertEquals("The number of terminals in the retrieved trie is faulty.",expectedSize,trieSize);

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
		String expectedText = "{taku/1v} {juq/1vn}";
		assertEquals("The text of the node should be '" + expectedText + "'.", expectedText, taku_juq_node.getKeysAsString());
		TrieNode nuna_it_node = trie.getNode(new String[] { "{nuna/1n}", "{it/tn-nom-p}" });
		assertTrue("The trie should not contain the node for 'nunait'.", nuna_it_node == null);
		
}
	
	
	@Test
	public void test__mostFrequentWordWithRadical() {
		CompiledCorpus compiledCorpus = new CompiledCorpus();
        compiledCorpus.setVerbose(false);
		Trie charTrie = new Trie();
		try {
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helicopter");
		compiledCorpus.trie = charTrie;
		} catch (Exception e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode mostFrequent = compiledCorpus.getMostFrequentTerminal("hel".split(""));
		assertEquals("The frequency of the most frequent found is wrong.",2,mostFrequent.getFrequency());
		assertEquals("The text of the the most frequent found is wrong.","h e l i c o p t e r \\",mostFrequent.getKeysAsString());
	}

	@Test
	public void test__getTerminalsSumFreq() throws TrieException {
		CompiledCorpus compiledCorpus = new CompiledCorpus();
        compiledCorpus.setVerbose(false);
		Trie charTrie = new Trie();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helicopter");
		compiledCorpus.trie = charTrie;
		long nbCompiledOccurrences = compiledCorpus.getNumberOfCompiledOccurrences();
		assertEquals("The sum of the frequencies of all terminals is incorrect.",5,nbCompiledOccurrences);
		
	}

	
    private String createTemporaryCorpusDirectory(String[] stringOfWords) throws IOException {
        corpusDirectory = Files.createTempDirectory("").toFile();
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


	@Test
	public void test__getNbFailedSegmentations() throws Exception {
		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq amma kanaujaq iglumik takulaaqtuq nunait"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
        assertEquals("The number of words that failed segmentation is wrong.",1,
        		compiledCorpus.getNbWordsThatFailedSegmentations());
        assertEquals("The number of occurrences that failed segmentation is wrong.",1,
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
        assertEquals("The number of words that failed segmentation is wrong.",1,
        		compiledCorpus.getNbWordsThatFailedSegmentations());
        assertEquals("The number of occurrences that failed segmentation is wrong.",1,
        		compiledCorpus.getNbOccurrencesThatFailedSegmentations());

        String wordThatFailed = "saqkilauqtuq";
        String[] keysOfFailedWord = new String[] {"{saqqik/1v}","{lauq/1vv}","{juq/1vn}"};
        TrieNode trieNode1 = compiledCorpus.getTrie().getNode(keysOfFailedWord);
        assertFalse("The node should not exist in the trie.",trieNode1 != null);
        compiledCorpus.wordsFailedSegmentation.add(wordThatFailed);
        compiledCorpus.wordsFailedSegmentationWithFreqs.put(wordThatFailed, new Long(4));
        compiledCorpus.segmentsCache.put(wordThatFailed, new String[] {});
        assertEquals("The number of words that failed segmentation is wrong.",2,
        		compiledCorpus.getNbWordsThatFailedSegmentations());
        assertEquals("The number of occurrences that failed segmentation is wrong.",5,
        		compiledCorpus.getNbOccurrencesThatFailedSegmentations());
        
        compiledCorpus.recompileWordsThatFailedAnalysis(corpusDirPathname);
        
        assertEquals("The number of words that failed segmentation is wrong.",1,
        		compiledCorpus.getNbWordsThatFailedSegmentations());
        assertEquals("The number of occurrences that failed segmentation is wrong.",1,
        		compiledCorpus.getNbOccurrencesThatFailedSegmentations());
        assertEquals("","{saqqik/1v} {lauq/1vv} {juq/1vn}",
        		String.join(" ", compiledCorpus.segmentsCache.get(wordThatFailed)));
        TrieNode trieNode2 = compiledCorpus.getTrie().getNode(compiledCorpus.segmentsCache.get(wordThatFailed));
        assertTrue("The node should exist in the trie.",trieNode2 != null);
        assertEquals("The frequency of that node is not right.",4,trieNode2.getFrequency());
        // compilation file should be updated
        String compiledCorpusFilename = corpusDirPathname+"/"+CompiledCorpus.JSON_COMPILATION_FILE_NAME;
        CompiledCorpus newCompiledCorpus = CompiledCorpus.createFromJson(compiledCorpusFilename);
        assertEquals("(3) The number of words that failed segmentation is wrong.",1,
        		newCompiledCorpus.getNbWordsThatFailedSegmentations());
        assertEquals("(3) The number of occurrences that failed segmentation is wrong.",1,
        		newCompiledCorpus.getNbOccurrencesThatFailedSegmentations());
        assertEquals("(3)","{saqqik/1v} {lauq/1vv} {juq/1vn}",
        		String.join(" ", newCompiledCorpus.segmentsCache.get(wordThatFailed)));
        
        compiledCorpus.saveCompilerInJSONFile(completeCompilationFilePathname);
        TrieNode trieNode3 = CompiledCorpus.createFromJson(completeCompilationFilePathname).getTrie().getNode(compiledCorpus.segmentsCache.get(wordThatFailed));
        assertTrue("The node should exist in the trie.",trieNode3 != null);
        assertEquals("The frequency of that node is not right.",4,trieNode3.getFrequency());
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
		assertEquals("",",,nunavut,,inuit,,",savedCompiledCorpus.decomposedWordsSuite);
	}
	

	private void assertContains(CompiledCorpus compiledCorpus,
			String[] segs, long expFreq, String[] expLongestTerminal) {
		TrieNode gotNode = compiledCorpus.trie.getNode(segs);
		String seqs_asString = String.join(", ", segs);
		Assert.assertTrue("Trie should have contained sequence: "+seqs_asString+"\nTrie contained:\n"+compiledCorpus.trie.toJSON(), gotNode != null);
		long gotFreq = gotNode.getFrequency();
		Assert.assertEquals("Frequency was not as expected for segmenets: "+seqs_asString, expFreq, gotFreq);
		
		if (expLongestTerminal != null) {
			String gotMostFreqTerminalTxt = gotNode.getMostFrequentTerminal().getKeysAsString();
			String expMostFreqTerminalTxt = String.join(" ", expLongestTerminal);
			AssertHelpers.assertStringEquals("Most frequent terminal was not as expected for segs "+seqs_asString, 
					expMostFreqTerminalTxt, gotMostFreqTerminalTxt);
			
		}
	}

	public static File compileToFile(String[] words) throws Exception {
		CompiledCorpus tempCorp = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
		tempCorp.setVerbose(false);
		InputStream iStream = IOUtils.toInputStream(String.join(" ", words), "utf-8");
		InputStreamReader iSReader = new InputStreamReader(iStream);
		BufferedReader br = new BufferedReader(iSReader);
		tempCorp.processDocumentContents(br, "dummyFilePath");
		
		File tempFile = File.createTempFile("compiled_corpus", ".json");
		tempCorp.saveCompilerInJSONFile(tempFile.toString());
		return tempFile;
	}
	
	
	
	

}
