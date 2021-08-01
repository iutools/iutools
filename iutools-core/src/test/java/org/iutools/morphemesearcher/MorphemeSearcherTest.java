package org.iutools.morphemesearcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iutools.corpus.*;
import org.iutools.utilities.StopWatch;
import org.iutools.datastructure.trie.MockStringSegmenter_IUMorpheme;
import ca.nrc.dtrc.elasticsearch.StreamlinedClient;
import ca.nrc.testing.AssertNumber;
import org.junit.Test;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.*;

public class MorphemeSearcherTest {


	private MorphemeSearcher morphemeSearcher = null;
	private CompiledCorpus smallCorpus;
	
	@Before
	public void setUp() throws Exception {
		smallCorpus = makeCorpus();
		smallCorpus.addWordOccurences(
        	new String[] {"inuit", "nunami", "iglumik", "inuglu"});
		morphemeSearcher = new MorphemeSearcher();
		morphemeSearcher.useCorpus(smallCorpus);
		return;
	}

	protected CompiledCorpus makeCorpus() throws Exception {
		String indexName = CompiledCorpusTest.testIndex;
		new StreamlinedClient(indexName).deleteIndex();
		CompiledCorpus corpus = new CompiledCorpus(indexName);
		corpus.setSegmenterClassName(MockStringSegmenter_IUMorpheme.class.getName());
		return corpus;
	}

	
	@Test
	public void test__MorphemeSearcher__Synopsis() throws Exception {
		//
		MorphemeSearcher morphemeSearcher = new MorphemeSearcher();

		// Here is how you build an instance of MorphemeSearcher 
		//
		CompiledCorpus corpus = smallCorpus;
		morphemeSearcher.useCorpus(corpus);
		
		//
		// You can then look for words that contain a given morpheme.
		//
		// Note that you provide the "canonical" form of the morpheme.
		// There can be different actual morphemes that correspond to this
		// canonical form (ex: verbal vs noun forms)
		//
		String canonicalMorpheme = "nunami";
		List<MorphSearchResults> wordsForMorphemes = 
			morphemeSearcher.wordsContainingMorpheme(canonicalMorpheme);
		
		// You can then loop through the explicit morphemes found
		//
		for (MorphSearchResults aMorpheme: wordsForMorphemes) {
			// This is the non-canonical ID of a morpheme that matches
			// the canonical one.
			String morphID = aMorpheme.morphemeWithId;
			
			// This is sample of words that are "good" examples of 
			// use of the morpheme.
			List<ScoredExample> scoredExamples = aMorpheme.words;
			for (ScoredExample anExample: scoredExamples) {
				// This is the word
				String word = anExample.word;
				
				// This is the word's frequency
				long freq = anExample.frequency;
			}
		}
	}
	
	public void test__MorphemeExtractor__Synopsis__No_corpus_defined() throws Exception {
		MorphemeSearcher morphemeExtractor = new MorphemeSearcher();
		String morpheme = "nunami";
		List<MorphSearchResults> wordsForMorphemes = morphemeExtractor.wordsContainingMorpheme(morpheme);
	}
		
	/**********************************
	 * VERIFICATION TESTS
	 **********************************/

	@Test
	public void test__wordsContainingMorpheme__SpeedTest() throws Exception {
		MorphemeSearcher morphemeSearcher = new MorphemeSearcher();
		String[] morphemes = new String[] {"inuk", "tut", "siuq"};
		long start = StopWatch.nowMSecs();
		for (String morpheme: morphemes) {
			List<MorphSearchResults> wordsForMorphemes =
					morphemeSearcher.wordsContainingMorpheme(morpheme);
		}
		double elapsedSecs = StopWatch.elapsedMsecsSince(start) / 1000.0;
		double avgSecs = elapsedSecs / morphemes.length;
		AssertNumber.performanceHasNotChanged(
	"Average secs per morpheme", avgSecs, 1.0, 1.0, false);
	}

	@Test
	public void test__wordsContainingMorpheme__root() throws Exception {
		String morpheme = "inuk";
		List<MorphSearchResults> msearchResults = 
			morphemeSearcher.wordsContainingMorpheme(morpheme);
		new AssertMorphSearchResults(msearchResults, "")
			.foundMorphemes("inuk/1n")
			.examplesForMorphemeAre("inuk/1n", Pair.of("inuit", new Long(1)))
			;
		
		morpheme = "nuna";
		msearchResults = this.morphemeSearcher.wordsContainingMorpheme(morpheme);
		new AssertMorphSearchResults(msearchResults, "")
			.foundMorphemes("nuna/1n")
			.examplesForMorphemeAre("nuna/1n", Pair.of("nunami", new Long(1)))
			;
	}
	
	@Test
	public void test__wordsContainingMorpheme__ending() throws Exception {
		HashMap<String,String> dictionary = new HashMap<String,String>();
		dictionary.put("inuit", "{inuk/1n} {it/tn-nom-p}");
		dictionary.put("nunami", "{nuna/1n} {mi/tn-loc-s}");
		dictionary.put("iglumik", "{iglu/1n} {mik/tn-acc-s}");
		dictionary.put("inuglu", "{inuk/1n} {lu/1q}");
		dictionary.put("iglumut", "{iglu/1n} {mut/tn-dat-s}");
		dictionary.put("nunamut", "{nuna/1n} {mut/tn-dat-s}");
		dictionary.put("igluvimmut", "{iglu/1n} {vim:vik/1nn} {mut/tn-dat-s}");

		MockCompiledCorpus mockCompiledCorpus = new MockCompiledCorpus();
		mockCompiledCorpus.setDictionary(dictionary);
		String[] wordsToAdd = new String[] {
			"inuit", "nunami", "iglumik", "inuglu", "iglumut", "nunamut", "igluvimmut"
		};
		mockCompiledCorpus.addWordOccurences(wordsToAdd);

		morphemeSearcher.useCorpus(mockCompiledCorpus);

		String morpheme = "mut";
		List<MorphSearchResults> wordsForMorphemes =
			this.morphemeSearcher.wordsContainingMorpheme(morpheme);
		
		new AssertMorphSearchResults(wordsForMorphemes, "")
			.foundMorphemes("mut/tn-dat-s")
			.examplesForMorphemeAre(
				"mut/tn-dat-s", 
				Pair.of("iglumut", new Long(1)),
				Pair.of("nunamut", new Long(1)))
		;
	}
	
    @Test
	public void test__wordsContainingMorpheme__infix__gaq1vn() throws Exception {
		String[] corpWords = new String[] {
				"makpigarni", "mappigarni", "inuglu"
				};
		CompiledCorpus compiledCorpus = makeCorpus();
        compiledCorpus.addWordOccurences(corpWords);
		
        MorphemeSearcher morphemeSearcher = new MorphemeSearcher();
        morphemeSearcher.useCorpus(compiledCorpus);
        
 		String morpheme = "gaq";
		List<MorphSearchResults> wordsForMorphemes =
			morphemeSearcher.wordsContainingMorpheme(morpheme);
		
		new AssertMorphSearchResults(wordsForMorphemes, "")
			.foundMorphemes("gaq/1vn")
			.examplesForMorphemeAre("gaq/1vn", Pair.of("makpigarni", new Long(1)))
			;
	}


    @Test
    public void test__morphFreqInAnalyses__HappyPath() throws Exception {
        String morpheme = "gaq/2vv";
        String word = "makpigarni";
        String bestDecomp = "{makpi:makpiq/1v}{gar:gaq/1vn}{ni:ni/tn-loc-p}";
        String[][] sampleDecomps = new String[][] {
			new String[]{"makpiq/1v", "gaq/1vn", "tn-loc-p"},
			new String[]{"makpiq/1v", "gaq/1vn", "tn-loc-s-2s"}
		};
        Long freq = new Long(10);
		WordWithMorpheme morphExample =
			new WordWithMorpheme(word, morpheme, bestDecomp, freq, sampleDecomps);
        Double freq2vv = morphemeSearcher.morphFreqInAnalyses(morphExample, true);

        morpheme = "gaq/1vn";
		morphExample =
			new WordWithMorpheme(word, morpheme, bestDecomp, freq, sampleDecomps);
        Double freq1vn = morphemeSearcher.morphFreqInAnalyses(morphExample, true);
        
        Assert.assertTrue(
			"Frequency of gaq/1vn should have been much higher than frequency of gaq/2vv.",
			freq1vn > 1.5 * freq2vv);
    }

	@Test
	public void test__wordsContainingMorpheme__QueryIsPartialMorpheme__FindsAllMorphemesThatStartWithQuery() throws Exception {
		morphemeSearcher.useCorpus(new CompiledCorpusRegistry().getCorpus());

		String morpheme = "siu";
		List<MorphSearchResults> wordsForMorphemes =
			this.morphemeSearcher.wordsContainingMorpheme(morpheme);

		new AssertMorphSearchResults(wordsForMorphemes, "")
			.foundMorphemes(
				"siuraq/1n",
				"siuk/tv-imp-2p-3s",
				"siuk/1n",
				"siuq/1nv",
				"siut/1nn",
				"siut/1n"
			)
		;
	}

	@Test
	public void test__separateWordsByRoot() throws Exception {
		String[] corpusWords = new String[] {
				"makpigarni", "mappigarni", "inuglu"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(corpusWords);
		CompiledCorpus compiledCorpus = makeCorpus();
        compiledCorpus.addWordOccurences(corpusWords);
        MorphemeSearcher morphemeSearcher = new MorphemeSearcher();
        morphemeSearcher.useCorpus(compiledCorpus);
        
		HashMap<String,List<WordWithMorpheme>> morphid2wordsFreqs = new HashMap<String,List<WordWithMorpheme>>();
		List<WordWithMorpheme> wordsFreqs = new ArrayList<WordWithMorpheme>();
		wordsFreqs.add(new WordWithMorpheme("takujuq","juq/1vn","{taku:taku/1v}{juq:juq/1vn}",(long)15));
		wordsFreqs.add(new WordWithMorpheme("siniktuq","juq/1vn","{sinik:sinik/1v}{tuq:juq/1vn}",(long)10));
		morphid2wordsFreqs.put("juq/1vn", wordsFreqs);
		
        MorphemeSearcher.Bin[] rootBins = morphemeSearcher.separateWordsByRoot(morphid2wordsFreqs);
        Assert.assertEquals("The number of bins is incorrect.", 2, rootBins.length);
        Assert.assertTrue("No bins returned with the right morpheme ids.", rootBins[0].rootId.equals("taku/1v") || rootBins[0].rootId.equals("sinik/1v"));
        Assert.assertEquals("", 1, rootBins[0].morphid2wordsFreqs.keySet().size());
        Assert.assertTrue("", rootBins[0].morphid2wordsFreqs.containsKey("juq/1vn"));
        Assert.assertEquals("", 1, rootBins[1].morphid2wordsFreqs.keySet().size());
        Assert.assertTrue("", rootBins[1].morphid2wordsFreqs.containsKey("juq/1vn"));
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
