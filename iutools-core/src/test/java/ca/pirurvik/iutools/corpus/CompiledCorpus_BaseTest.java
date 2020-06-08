package ca.pirurvik.iutools.corpus;

import static org.junit.Assert.*;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_Char;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.datastructure.trie.Trie_InMemory;
import ca.nrc.testing.AssertHelpers;
import ca.nrc.testing.AssertObject;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory.WordWithMorpheme;

public abstract class CompiledCorpus_BaseTest {
		
	protected abstract CompiledCorpus_Base makeCorpusUnderTest(
		Class<? extends StringSegmenter> segmenterClass);
	
	protected CompiledCorpus_Base makeCorpusUnderTest() {
		return makeCorpusUnderTest(StringSegmenter_Char.class);
	}
	
	protected File corpusDirectory = null;
	
	@After
    public void tearDown() throws Exception {
        if (corpusDirectory != null) {
        	File[] listOfFiles = corpusDirectory.listFiles();
        	for (File file : listOfFiles)
        		file.delete();
        }
        corpusDirectory = null;
    }
	
	//////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////
	
	@Test
	public void test__CompiledCorpus_Base__Synopsis() throws Exception {
		//
		// Use a CompiledCorpus to trie-compile a corpus and compute statistics.
		//
		//
		CompiledCorpus_Base compiledCorpus = makeCorpusUnderTest();
		
		// 
		// By default, the compiler always computes character-ngrams.
		// 
		// But you can also provide a morpheme segmenter which will allow the 
		// corpus to keep stats on morphme-ngrams
		// 
		// But you can also pass it a different segmenter. For example, the following
		// compiler will segment words by inuktitut morphemes.
		//
		compiledCorpus.setSegmenterClassName(
				StringSegmenter_IUMorpheme.class.getName());

		// set verbose to false for tests only
		compiledCorpus.setVerbose(false); 
		
		// Set the maximum number of morphological decompositions that you want 
		// to keep for each word. Note that the entry for a word will always 
		// know how many decompositions existed, even if it only stores the 
		// first few of them.
		//
		compiledCorpus.setDecompsSampleSize(10);
		
		// Whenever you encounter an occurence of a word, invoke 
		// addWordOccurence(word)
		//
		// For example, say you encounter an occurence of word 'inuksuk'...
		// This will:
		// - Create a new entry for this word if this is the first time the 
		//   is encountered
		// - Increment the word's frequency by 1
		// - Increment frequency of each char-ngram contained in that word
		//
		String word = "inuksuk";
		compiledCorpus.addWordOccurence(word);
		
		// You can also provide a list of possible morphological decompositions 
		// for that word. This will:
		//
		// - Store the decompositions in the word's entry
		// - Update the stats for all the morpheme-ngrams contained in any of  
		//   the provided analyses.
		// 
		// Note that the word's entry will only store the first 
		// N=decompsSampleSize decompositions, but it will remember how many 
		// decompositions were passed to setWordDecompositions()
		//
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		Decomposition[] decomps = analyzer.decomposeWord(word);
		compiledCorpus.setWordDecompositions(decomps);
		
		// Once you have added all this information to the CompiledCorpus, you 
		// can do all sort of useful stuff with that info.
		//
		// For example:
		//
		
		// Loop through all words in the corpus
		Iterator<String> iter = compiledCorpus.allWords();
		while (iter.hasNext()) {
			String aWord = iter.next();
			
			// Get that word's information
			{
				WordInfo wInfo = compiledCorpus.info4word(aWord);
				if (wInfo == null) {
					// Means the corpus does not know about this word
					//
					// Note: Should not happen in this case, because we obtained 
					// 'word' through the allWords() iterator (so it know that this
					// word was seen in the corpus).
					//
				} else {
					// Frequency of the word
					long freq = wInfo.frequency;
					
					// Total number of morphological decompositions for this word, 
					// as well as a short list of the first few decompositions 
					// found.
					// 
					// If those two values are 'null', it means that the decomps 
					// have not been provided.
					// It does NOT mean that no decomps can be computed for this 
					// word.
					//
					Integer numDecomps = wInfo.totalDecompositions;
					String[] sampleDecomps = wInfo.topDecompositions;
				}			
			}
		}
		

		// You can ask for information about the various character-ngrams 
		// that were seen in the corpus.
		//
		{
			// This returns all the words that START with "nuna"
			//
			Set<String> wordsWithNgram = 
					compiledCorpus.wordsContainingNgram("^nuna");
			
			// Words that END with "vut"
			//
			wordsWithNgram = 
					compiledCorpus.wordsContainingNgram("vut$");
	
			// Words that have "nav" ANYWHERE
			//
			wordsWithNgram = 
					compiledCorpus.wordsContainingNgram("nav");
		}
		
		// Similarly, you can also ask for information about words that contain 
		// certain sequences of ngrams (aka morphem-ngrams)
		//
		{
			// This will find all the words that START with morphemes
			// inuk/1n and titut/tn-sim-p
			//
			String[] morphemes = new String[] {
				"^", "inuk/1n", "titut/tn-sim-p"};
			Set<String> wordsWithMorphemes = 
				compiledCorpus.wordsContainingMorphNgram(morphemes);

			// This will find all the words that END with titut/tn-sim-p
			//
			morphemes = new String[] {
				"titut/tn-sim-p", "$"};
			wordsWithMorphemes = 
				compiledCorpus.wordsContainingMorphNgram(morphemes);
		
			// This will find all the words that contain morphemes 
			// nasuk/1vv and niq/2vn ANYWHERE
			//
			morphemes = new String[] {
				"nasuk/1vv", "niq/2vn"};
			wordsWithMorphemes = 
				compiledCorpus.wordsContainingMorphNgram(morphemes);
		}
		
		
//		
//		// The most common way to populate a corpus is to compile it from a file 
//		// or series of text files.
//		//
//		// But you can also manually add some words to it.
//		// 
//		// You can add the word by itself, WITH or WITHOUT decompositions.
//		//
//		String word = "inukshuk";
//		compiledCorpus.addWord(word); // WITHOUT decompositions
//		word = "inuktut";
//		Decomposition[] decomps = new MorphologicalAnalyzer().decomposeWord(word);
//		String[] decompsStr = new String[decomps.length];
//		for (int ii=0; ii < decomps.length; ii++) {
//			decompsStr[ii] = decomps[ii].toStr2();
//		}
//		compiledCorpus.addWord(word, decompsStr); // WITH decompositions
//		
//		// You can override the decompositions of a word that is already in the 
//		// corpus
//		//
//		compiledCorpus.info4word(word)
//			.setDecompositions(new String[0]);
//		
//		// Attempting to add a word that is already registered raises a 
//		// WordAlreadinInCorpusException exception.
//		// 
//		// So you should check for the existence of 
//		// the word before adding it.
//		//
//		if (compiledCorpus.info4word(word) == null) {
//			compiledCorpus.addWord(word);
//		}
//				
//		// When you encounter an occurence of a word, you can tell 
//		// the corpus about it as follows.
//		//
//		compiledCorpus.incrementWordFreq(word);
	}
	
	
	@Test
	public void test__CompiledCorpus__Synopsis() throws Exception {
		//
		// Use a CompiledCorpus to trie-compile a corpus and compute statistics.
		//
		//
		CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory();
		
		// 
		// By default, the compiler segments words on a character by character basis.
		// 
		// But you can also pass it a different segmenter. For example, the following
		// compiler will segment words by inuktitut morphemes.
		//
		compiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());

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
	
	///////////////////////////////
	// VERIFICATION TESTS
	///////////////////////////////
	
	@Test
	public void test__addWordOccurences__HappyPath() throws Exception {
		CompiledCorpus_Base corpus = makeCorpusUnderTest();
		final String[] noWords = new String[] {};
		String nunavut = "nunavut";
		String nunavik = "nunavik";

		String ngram_nuna = "^nuna";
		String ngram_navik = "navik$";
		
		new AssertCompiledCorpus(corpus, "Initially...")
			.doesNotContainWords(nunavut, nunavik)
			.doesNotContainCharNgrams(ngram_nuna, ngram_navik);
		
		corpus.addWordOccurence(nunavut);
		new AssertCompiledCorpus(corpus, "After adding 1st word "+nunavut)
			.containsWords(nunavut)
			.containsCharNgrams(ngram_nuna)
			.doesNotContainCharNgrams(ngram_navik);
		
		corpus.addWordOccurence(nunavik);
		new AssertCompiledCorpus(corpus, "After adding 2nd word "+nunavik)
			.containsWords(nunavik)
			.containsCharNgrams(ngram_nuna)
			.containsCharNgrams(ngram_navik);
	}
	
	@Test
    public void test__topSegmentation__HappyPath() throws Exception {
		String[] words = new String[] {"nunavut", "takujuq", "plugak"};
		CompiledCorpus_Base compiledCorpus = 
				makeCorpusUnderTest(StringSegmenter_IUMorpheme.class);		
		compiledCorpus.addWordOccurences(words);
		
		new AssertCompiledCorpus(compiledCorpus,"")
			.topSegmentationIs("nunavut", "{nunavut/1n}")
			.topSegmentationIs("takujuq", "{taku/1v}{juq/1vn}")
			// This is a word that does not decompose
			.topSegmentationIs("plugak", null)
			;		
    }
	
	
    @Test
    public void test__charNGramFrequency__HappyPath() throws Exception
    {
		String[] words = new String[] {
			"nunavut", "takujuq", "iijuq"};
		CompiledCorpus_Base compiledCorpus = 
				makeCorpusUnderTest(StringSegmenter_IUMorpheme.class);		
		compiledCorpus.addWordOccurences(words);
	   
		new AssertCompiledCorpus(compiledCorpus, "")
			// Ngram with freq = 1
			.charNgramFrequencyIs("nun", 1)
			// Ngram with freq > 1
			.charNgramFrequencyIs("juq", 2)
			;
    }
    
    @Test
    public void test__canBeResumed() throws ConfigException, IOException {
		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq amma kanaujaq iglumik takulaaqtuq nunait"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        boolean canBeResumed = compiledCorpus.canBeResumed(corpusDirPathname);
       Assert.assertFalse("The compiler should not be able to resume; there is no JSON compilation backup.",canBeResumed);

        File jsonFile = new File(corpusDirPathname+"/"+CompiledCorpus_InMemory.JSON_COMPILATION_FILE_NAME);
        jsonFile.createNewFile();
        canBeResumed = compiledCorpus.canBeResumed(corpusDirPathname);
       Assert.assertTrue("The compiler should be able to resume; there is a JSON compilation backup.",canBeResumed);
}
    	
	@Test
	public void test__readFromJson() throws Exception {
		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq amma kanaujaq iglumik takulaaqtuq nunait"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
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
        
        CompiledCorpus_InMemory retrievedCompiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
        retrievedCompiledCorpus.resumeCompilation(corpusDirPathname);
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
		CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory();
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
		CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory();
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
        CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
       Assert.assertEquals("The number of words that failed segmentation is wrong.",1,
        		compiledCorpus.getNbWordsThatFailedSegmentations());
       Assert.assertEquals("The number of occurrences that failed segmentation is wrong.",1,
        		compiledCorpus.getNbOccurrencesThatFailedSegmentations());
	}
	

	@Test
	public void test__saveCompilerInJSONFile() throws IOException, CompiledCorpusException, StringSegmenterException, CompiledCorpusRegistryException {
		String[] stringsOfWords = new String[] {
				"nunavut inuit"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
		File tempFile = File.createTempFile("compiled_corpus", ".json");
		compiledCorpus.saveCompilerInJSONFile(tempFile.getAbsolutePath());
		CompiledCorpusRegistry.registerCorpus("compiled_corpus", tempFile);
		
		CompiledCorpus_InMemory savedCompiledCorpus = CompiledCorpusRegistry.getCorpus("compiled_corpus");
	Assert.assertEquals("",",,nunavut,,inuit,,",savedCompiledCorpus.decomposedWordsSuite);
	}
	
	
	@Test
	public void test__isWordInCorpus() throws IOException, CompiledCorpusException, StringSegmenterException {
		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq uvlimik"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
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
        CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
		
        List<WordWithMorpheme> words = compiledCorpus.getWordsContainingMorpheme("lauq");
        Assert.assertEquals("", 2, words.size());
        AssertObject.assertDeepEquals("", new WordWithMorpheme("sinilauqtuq","lauq/1vv","{sinik/1v}{lauq/1vv}{juq/1vn}",(long)1), words.get(0));
        AssertObject.assertDeepEquals("", new WordWithMorpheme("takulauqtunga","lauq/1vv","{taku/1v}{lauq/1vv}{junga/tv-ger-1s}",(long)1), words.get(1));
	}
	

	private void assertContains(CompiledCorpus_InMemory compiledCorpus,
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
		CompiledCorpus_InMemory tempCorp = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
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
