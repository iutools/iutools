package org.iutools.corpus;

import java.io.File;
import java.util.Iterator;

import ca.nrc.testing.AssertIterator;
import ca.nrc.testing.AssertString;
import org.apache.commons.lang3.tuple.Triple;
import org.iutools.script.TransCoder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.iutools.datastructure.trie.StringSegmenter;
import org.iutools.datastructure.trie.StringSegmenter_Char;
import org.iutools.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.testing.AssertObject;

public abstract class CompiledCorpusTest {

	protected abstract CompiledCorpus makeCorpusWithDefaultSegmenter() throws Exception;

	public final static String testIndex = "iutools_corpus_test";

	@Before
	public void setUp() throws Exception {
		CorpusTestHelpers.clearCorpus(testIndex);
		return;
	}

	@After
	public void tearDown() throws Exception {
		CorpusTestHelpers.deleteCorpusIndex(testIndex);
	}

	protected CompiledCorpus makeCorpusUnderTest()  throws Exception {
		CompiledCorpus corpus = makeCorpusUnderTest(StringSegmenter_Char.class);
		return corpus;
	}

	protected CompiledCorpus makeCorpusUnderTest(
			Class<? extends StringSegmenter> segmenterClass) throws Exception {
		CompiledCorpus corpus = makeCorpusWithDefaultSegmenter();
		corpus.setSegmenterClassName(segmenterClass);
		return corpus;
	}
	
	//////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////

	@Test
	public void test__CompiledCorpus__Synopsis() throws Exception {
		// Use a CompiledCorpus to keep stats about words contained in a corpus.
		//
		CompiledCorpus compiledCorpus = makeCorpusUnderTest();
		
		// 
		// By default, the compiler always computes character-ngrams.
		// 
		// But you can also provide a morpheme segmenter which will allow the 
		// corpus to keep stats on morphme-ngrams
		// 
		compiledCorpus.setSegmenterClassName(
				StringSegmenter_IUMorpheme.class.getName());

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
		
		// TODO-June2020: Show how you can a provide frequency increment != 1
//		compiledCorpus.addWordOccurence(word, 3); // Increase freq by +3
//		compiledCorpus.addWordOccurence(word, 0); // Freq will remain unchanged
		
		
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
					String[][] sampleDecomps = wInfo.decompositionsSample;
				}			
			}
		}
		

		// You can ask for information about the various character-ngrams 
		// that were seen in the corpus.
		//
		{
			// This iterator contains all the words that START with "nuna"
			//
			Iterator<String> wordsWithNgram =
					compiledCorpus.wordsContainingNgram("^nuna");

			// Words that END with "vut"
			//
			wordsWithNgram = 
					compiledCorpus.wordsContainingNgram("vut$");
	
			// Words that have "nav" ANYWHERE
			//
			wordsWithNgram = 
					compiledCorpus.wordsContainingNgram("nav");

			// You can limit the ngram search to words that are correctly
			// spelled
			wordsWithNgram =
					compiledCorpus.wordsContainingNgram("nav", CompiledCorpus.SearchOption.EXCL_MISSPELLED);

			// If you just want to know the NUMBER of words that contain
			// a particular ngram, you can do this:
			//
			long numWords = compiledCorpus.totalWordsWithCharNgram("nuna");
		}
		
		// Similarly, you can also ask for information about words that contain 
		// certain sequences of morphemes (aka morphem-ngrams)
		//
		{
			// This will find all the words that START with morphemes
			// inuk/1n and titut/tn-sim-p
			//
			String[] morphemes = new String[] {
				"^", "inuk/1n", "titut/tn-sim-p"};
			Iterator<String> wordsWithMorphemes =
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
	}
	
	
	///////////////////////////////
	// VERIFICATION TESTS
	///////////////////////////////
	
	@Test
	public void test__addWordOccurences__HappyPath() throws Exception {
		CompiledCorpus corpus = makeCorpusUnderTest();
		final String[] noWords = new String[] {};
		String nunavut = "nunavut";
		String nunavik = "nunavik";

		String ngram_nuna = "^nuna";
		String ngram_navik = "navik$";
		
		new AssertCompiledCorpus(corpus, "Initially...")
			.doesNotContainWords(nunavut, nunavik)
			.doesNotContainCharNgrams(ngram_nuna, "navik$")
			.doesNotContainCharNgrams(ngram_nuna, ngram_navik);
		
		corpus.addWordOccurence(nunavut);
		Thread.sleep(1*1000);
		new AssertCompiledCorpus(corpus, "After adding 1st word "+nunavut)
			.containsWords(nunavut)
			.containsCharNgrams(ngram_nuna)
			.doesNotContainCharNgrams(ngram_navik);
		
		corpus.addWordOccurence(nunavik);
		Thread.sleep(1*1000);
		new AssertCompiledCorpus(corpus, "After adding 2nd word "+nunavik)
			.containsWords(nunavik)
			.containsCharNgrams(ngram_nuna)
			.containsCharNgrams(ngram_navik);
		return;
	}
	
	@Test
	public void test__addWordOccurences__WordWithNoDecomps() throws Exception {
		CompiledCorpus corpus = makeCorpusUnderTest();
		final String[] noWords = new String[] {};
		
		String nnnunavut = "nnnunavut";
		String ngram_nuna = "nuna";		

		corpus.addWordOccurence(nnnunavut);
		new AssertCompiledCorpus(corpus, "After adding non-decomposable word "+nnnunavut)
			.containsWords(nnnunavut)
			.bestDecompositionIs(nnnunavut, null)
			;
	}


	@Test
	public void test__totalWords__HappyPath() throws Exception {
		CompiledCorpus corpus = makeCorpusUnderTest();
		corpus.setSegmenterClassName(MockStringSegmenter_IUMorpheme.class);
		
		String iiinuit = "iiinuit";
		String inuit = "inuit";
		String nunami = "nunami";
		final String[] words = new String[] {iiinuit, inuit, nunami};
		corpus.addWordOccurences(words);
		new AssertCompiledCorpus(corpus, "")
			.totalWordsIs(3)
			.totalWordsWithoutDecompsIs(1)
			.totalWordsWithDecompIs(2)
			;
	}

	@Test
	public void test__totalOccurences__OfVariousTypes() throws Exception {
		CompiledCorpus corpus = makeCorpusUnderTest();
		corpus.setSegmenterClassName(MockStringSegmenter_IUMorpheme.class);
		
		String iiinuit = "iiinuit";
		String inuit = "inuit";
		String nunami = "nunami";
		final String[] words = new String[] {
			iiinuit, iiinuit, inuit, nunami, nunami};
		corpus.addWordOccurences(words);
		new AssertCompiledCorpus(corpus, "")
			.totalOccurencesIs(5)
			.totalOccurencesWithNoDecompIs(2)
			.totalOccurencesWithDecompIs(3)
			;
	}
	
	@Test
    public void test__bestDecomposition__HappyPath() throws Exception {
		String[] words = new String[] {"nunavut", "takujuq", "plugak"};
		CompiledCorpus compiledCorpus =
				makeCorpusUnderTest(StringSegmenter_IUMorpheme.class);
		compiledCorpus.addWordOccurences(words);
		
		new AssertCompiledCorpus(compiledCorpus,"")
			.bestDecompositionIs("nunavut", "{nunavut/1n}")
			.bestDecompositionIs("takujuq", "{taku/1v}{juq/1vn}")
			// This is a word that does not decompose
			.bestDecompositionIs("plugak", null)
			;		
    }
	
	
    @Test
    public void test__charNGramFrequency__HappyPath() throws Exception
    {
		String[] words = new String[] {
			"nunavut", "takujuq", "iijuq"};
		CompiledCorpus compiledCorpus =
				makeCorpusUnderTest(StringSegmenter_IUMorpheme.class);
		compiledCorpus.addWordOccurences(words);
	   
		new AssertCompiledCorpus(compiledCorpus, "")
			// Ngram with freq = 1
			.charNgramFrequencyIs("^nun", 1)
			// Ngram with freq > 1
			.charNgramFrequencyIs("juq$", 2)
			;
    }
        
	@Test
	public void test__totalOccurences__HappyPath() throws Exception {
		CompiledCorpus compiledCorpus = makeCorpusUnderTest();
		compiledCorpus.addWordOccurences(
			new String[] {"hello", "hint", "helicopter", "helios",
					// Note: two occurences of "helicopter"
					"helicopter"});
	
		new AssertCompiledCorpus(compiledCorpus, "")
			.totalOccurencesIs(5)
			;
	}
	
	/*
	 * 
	 */

	@Test
	public void test__totalWordsWithNoDecomp__HappyPath() throws Exception {
		String[] stringsOfWords = new String[] {
				"nunavut", "inuit", "takujuq", "amma", "kanaujaq", "iglumik", "takulaaqtuq", "nunait"
				};
		CompiledCorpus compiledCorpus = makeCorpusUnderTest(StringSegmenter_IUMorpheme.class);
        compiledCorpus.addWordOccurences(stringsOfWords);
       Assert.assertEquals(
       	"The number of words that failed segmentation is wrong.",
		 		1, compiledCorpus.totalWordsWithNoDecomp());
       Assert.assertEquals("The number of occurrences that failed segmentation is wrong.",
		 		1, compiledCorpus.totalOccurencesWithNoDecomp());
	}

	@Test
	public void test__wordsContainingMorpheme__HappyPath() throws Exception {
		String[] stringsOfWords = new String[] {
				"nunavut", "inuit", "takujuq", "sinilauqtuq", "uvlimik", "takulauqtunga"
				};
		CompiledCorpus compiledCorpus = makeCorpusUnderTest(StringSegmenter_IUMorpheme.class);
        compiledCorpus.addWordOccurences(stringsOfWords);

        new AssertCompiledCorpus(compiledCorpus, "")
        		.wordsContainingMorphemeAre(
        			"lauq", "sinilauqtuq",  "takulauqtunga");
	}

	@Test
	public void test__totalWordsWithNgram__HappyPath() throws Exception {
		String[] stringsOfWords = new String[] {
				"nunavut", "inuit", "takujuq", "sinilauqtuq", "uvlimik", "takulauqtunga"
		};
		CompiledCorpus compiledCorpus = makeCorpusUnderTest();
		compiledCorpus.addWordOccurences(stringsOfWords);

		new AssertCompiledCorpus(compiledCorpus, "")
			.totalWordsWithNgramEquals("lauq", 2);
	}

	@Test
	public void test__totalWordsWithNgram__SyllabicsQuery() throws Exception {
		String[] stringsOfWords = new String[] {
				"nunavut", "inuit", "takujuq", "sinilauqtuq", "uvlimik", "takulauqtunga"
		};
		CompiledCorpus compiledCorpus = makeCorpusUnderTest();

		String ngram = TransCoder.inOtherScript("lauq");
		compiledCorpus.addWordOccurences(stringsOfWords);


		new AssertCompiledCorpus(compiledCorpus, "")
			.totalWordsWithNgramEquals(ngram, 2);
	}


	public static File compileToFile(String[] words) throws Exception {
		return compileToFile(words,null);
	}
	
	public static File compileToFile(String[] words, String fileId) throws Exception {
//		CompiledCorpus_InMemory tempCorp = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
//		CorpusCompiler compiler = new CorpusCompiler(tempCorp);
//		compiler.setVerbose(false);
//		InputStream iStream = IOUtils.toInputStream(String.join(" ", words), "utf-8");
//		InputStreamReader iSReader = new InputStreamReader(iStream);
//		BufferedReader br = new BufferedReader(iSReader);
//		compiler.tokenizeDocumentContents(br, "dummyFilePath", null);
//		String fileName = "compiled_corpus";
//		if (fileId != null)
//			fileName += "-"+fileId;
//		File tempFile = File.createTempFile(fileName, ".json");
//		RW_CompiledCorpus.write(tempCorp, tempFile);
//		return tempFile;
        return null;
	}
	
	@Test
	public void test__morphemeNgramFrequency__HappyPath() throws Exception {
		String[] words = new String[] {"inuit", "inuglu", "nunami"};
		CompiledCorpus corpus = makeCorpusUnderTest();
		corpus.setSegmenterClassName(MockStringSegmenter_IUMorpheme.class.getName());
		corpus.addWordOccurences(words);
		new AssertCompiledCorpus(corpus, "")
			// Morpheme with freq > 1
			.morphemeNgramFreqEquals(2, "{inuk/1n}")
			// Morpheme with freq = 1
			.morphemeNgramFreqEquals(1, "{nuna/1n}")
			// Morpheme that is not the root
			.morphemeNgramFreqEquals(1, "{lu/1q}")
			// Morpheme at end of word
			.morphemeNgramFreqEquals(1, "{it/tn-nom-p}$")
			// Sequence of two morphemes
			.morphemeNgramFreqEquals(1, "{inuk/1n}", "{it/tn-nom-p}")
			// Sequence of two morphemes at end of word
			.morphemeNgramFreqEquals(1, "{inuk/1n}", "{it/tn-nom-p}$")
			// Sequence of two morphemes at start of word
			.morphemeNgramFreqEquals(1, "^{inuk/1n}", "{it/tn-nom-p}")
			// Sequence of two morphemes that make up whole word
			.morphemeNgramFreqEquals(1, "^{inuk/1n}", "{it/tn-nom-p}$")
			;
	}
	
	@Test
	public void test__wordsContainingNgram__VariousCases() throws Exception {
    	boolean IN_ANY_ORDER = true;

		CompiledCorpus corpus = makeCorpusUnderTest();
		corpus.addWordOccurences(
			new String[] {"inuktitut", "inuksuk", "inuttitut", "inakkut", 
					"takuinuit", "taku", "intakuinuit"});
		
		String seq;
		String[] expected;
		Iterator<String> wordsWithSeq;

		// ngram in the middle of a word
		seq = "inu";
		wordsWithSeq = corpus.wordsContainingNgram(seq);
		expected = new String[] {
			"inuktitut", "inuksuk", "inuttitut", "takuinuit", "intakuinuit"};

		// ngram at beginning of word
		seq = "^inu";
		wordsWithSeq = corpus.wordsContainingNgram(seq);
		expected = new String[] {"inuktitut","inuksuk","inuttitut"};
		AssertIterator.assertElementsEquals("The list of words containing sequence "+seq+" was not as expected",
				expected, wordsWithSeq, IN_ANY_ORDER);

		seq = "itut$";
		wordsWithSeq = corpus.wordsContainingNgram(seq);
		expected = new String[] {"inuktitut","inuttitut"};
		AssertIterator.assertElementsEquals("The list of words containing sequence "+seq+" was not as expected",
				expected, wordsWithSeq, IN_ANY_ORDER);

		seq = "^taku$";
		wordsWithSeq = corpus.wordsContainingNgram(seq);
		expected = new String[] {"taku"};
		AssertIterator.assertContainsAll("The list of words containing sequence "+seq+" was not as expected",
			expected, wordsWithSeq);

		seq = TransCoder.inOtherScript("inuk");
		wordsWithSeq = corpus.wordsContainingNgram(seq);
		expected = new String[] {"inuksuk", "inuktitut"};
		AssertIterator.assertContainsAll(
			"The list of words containing sequence "+seq+" was not as expected",
			expected, wordsWithSeq);
	}	
	
	@Test
	public void test__info4word__WordIsInCorpus() throws Exception {
		StringSegmenter segmenter = new StringSegmenter_Char();

		// Note: "hello" appears twice
		String[] words = new String[] {"hello", "world", "hello", "again"};
		CompiledCorpus corpus =
				makeCorpusUnderTest(StringSegmenter_Char.class);		
		corpus.addWordOccurences(words);
		
		WordInfo gotInfo = corpus.info4word("hello");
		AssertWordInfo asserter = new AssertWordInfo(gotInfo, "");
		asserter.isNotNull();
		asserter
			.frequencyIs(2)
			.topDecompIs(segmenter.segment("hello"))
			;
		
		gotInfo = corpus.info4word("world");
		asserter = new AssertWordInfo(gotInfo, "");
		asserter.isNotNull();
		asserter
			.frequencyIs(1)
			.topDecompIs(segmenter.segment("world"))
			;
	}

	@Test
	public void test__info4word__WordNotInCorpus() throws Exception {
		String[] words = new String[] {"hello", "world"};
		CompiledCorpus corpus =
				makeCorpusUnderTest(StringSegmenter_Char.class);		
		corpus.addWordOccurences(words);
		
		WordInfo gotInfo = corpus.info4word("greetings");
		new AssertWordInfo(gotInfo, "").isNull();
	}
	
	@Test
	public void test__wordsContainingMorphNgram__HappyPath() throws Exception {
		CompiledCorpus corpus = makeCorpusUnderTest(MockStringSegmenter_IUMorpheme.class);
		String[] words = new String[] {"inuit", "inuglu", "nunami"};
		corpus.addWordOccurences(words);
		
		String[] morphNgram = new String[] {
				"inuk/1n"};
		Iterator<String> gotWords =
			corpus.wordsContainingMorphNgram(morphNgram);
		String[] expWords = new String[] {"inuglu", "inuit"};
		AssertObject.assertDeepEquals(
			"Wrong list of words for morpheme ngram "+String.join(",", morphNgram), 
			expWords, gotWords);

		morphNgram = new String[] {"^", "inuk/1n"};
		gotWords = corpus.wordsContainingMorphNgram(morphNgram);
		expWords = new String[] {"inuglu", "inuit"};
		AssertObject.assertDeepEquals(
			"Wrong list of words for morpheme ngram "+String.join(",", morphNgram), 
			expWords, gotWords);
	}
	
	@Test
	public void test__wordsWithNoDecomposition__HappyPath() throws Exception {
		CompiledCorpus corpus = makeCorpusUnderTest(MockStringSegmenter_IUMorpheme.class);
		String[] words = new String[] {"inuit", "inuglu", "nunami", "nunnnavut", "innnuglu"};
		corpus.addWordOccurences(words);
		new AssertCompiledCorpus(corpus, "")
			.wordsWithNoDecompositionAre(new String[] {"innnuglu", "nunnnavut"});
	}

	@Test
	public void test__wordsWithNoDecomposition__AllWordsDecompose() throws Exception {
		CompiledCorpus corpus = makeCorpusUnderTest(MockStringSegmenter_IUMorpheme.class);
		String[] words = new String[] {"inuit", "inuglu", "nunami"};
		corpus.addWordOccurences(words);
		new AssertCompiledCorpus(corpus, "")
			.wordsWithNoDecompositionAre(new String[0]);
	}

	@Test
	public void test__corpusName4File__HappyPath() {
		File jsonFile = new File("/some/path/some-corpus.ES.json");
		String gotName = CompiledCorpus.corpusName4File(jsonFile);
		AssertString.assertStringEquals(
				"Corpus name was not as expected for file: "+jsonFile,
				"some-corpus", gotName);
	}

	@Test
	public void test__changeLastUpdatedHistory__HappyPath() throws Exception {
    	CompiledCorpus corpus = makeCorpusUnderTest();

		AssertCompiledCorpus asserter = new AssertCompiledCorpus(corpus);
		asserter.lastLoadedDateEquals(0);

		// Add a word to the corpus, to make sure we test the method in a situation
		// where it contains records of type WordInfo as well as LastLoadedDate
		corpus.addWordOccurence("inuit");
		asserter.lastLoadedDateEquals(0);

		corpus.changeLastUpdatedHistory();
		long now = System.currentTimeMillis();
		asserter.lastLoadedDateEquals(now);
	}
}
