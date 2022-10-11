package org.iutools.morphemedict;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import ca.nrc.testing.AssertObject;
import org.iutools.corpus.*;
import org.iutools.utilities.StopWatch;
import org.iutools.datastructure.trie.MockStringSegmenter_IUMorpheme;
import ca.nrc.testing.AssertNumber;
import org.junit.Test;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.*;

public class MorphemeDictionaryTest {

	private MorphemeDictionary morphemeSearcher = null;
	private CompiledCorpus smallCorpus;
	
	@Before
	public void setUp() throws Exception {
		smallCorpus = makeCorpus();
		smallCorpus.deleteAll(true);
		smallCorpus.addWordOccurences(
        	new String[] {"inuit", "nunami", "iglumik", "inuglu"});
		morphemeSearcher = new MorphemeDictionary();
		morphemeSearcher.useCorpus(smallCorpus);
		return;
	}

	protected CompiledCorpus makeCorpus() throws Exception {
		String indexName = CompiledCorpusTest.testIndex;
		CorpusTestHelpers.deleteCorpusIndex(indexName);
//		CompiledCorpus_ES corpus = new CompiledCorpus_ES(indexName);
		CompiledCorpus corpus = CompiledCorpusRegistry.makeCorpus(indexName);
		corpus.setSegmenterClassName(MockStringSegmenter_IUMorpheme.class.getName());
		return corpus;
	}

	
	@Test
	public void test__MorphemeSearcher__Synopsis() throws Exception {
		//
		MorphemeDictionary morphemeSearcher = new MorphemeDictionary();

		// Here is how you build an instance of MorphemeDictionary
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
		List<MorphDictionaryEntry> wordsForMorphemes =
			morphemeSearcher.search(canonicalMorpheme);
		
		// You can then loop through the explicit morphemes found
		//
		for (MorphDictionaryEntry aMorpheme: wordsForMorphemes) {
			// This is the non-canonical ID of a morpheme that matches
			// the canonical one.
			String morphID = aMorpheme.morphemeWithId;
			
			// This is sample of words that are "good" examples of 
			// use of the morpheme.
			List<MorphWordExample> scoredExamples = aMorpheme.words;
			for (MorphWordExample anExample: scoredExamples) {
				// This is the word
				String word = anExample.word;
				
				// This is the word's frequency
				long freq = anExample.frequency;
			}
		}
	}
	
	public void test__MorphemeExtractor__Synopsis__No_corpus_defined() throws Exception {
		MorphemeDictionary morphemeExtractor = new MorphemeDictionary();
		String morpheme = "nunami";
		List<MorphDictionaryEntry> wordsForMorphemes =
			morphemeExtractor.search(morpheme);
	}
		
	/**********************************
	 * VERIFICATION TESTS
	 **********************************/

	@Test
	public void test__wordsContainingMorpheme__SpeedTest() throws Exception {
		MorphemeDictionary morphemeSearcher = new MorphemeDictionary();
		String[] morphemes = new String[] {"inuk", "tut", "siuq"};
		long start = StopWatch.nowMSecs();
		for (String morpheme: morphemes) {
			System.out.println("Searching examples for "+morpheme);
			List<MorphDictionaryEntry> wordsForMorphemes =
					morphemeSearcher.search(morpheme);
		}
		double elapsedSecs = StopWatch.elapsedMsecsSince(start) / 1000.0;
		double avgSecs = elapsedSecs / morphemes.length;
		System.out.println("Got avgSecs="+avgSecs);
		AssertNumber.performanceHasNotChanged(
			"Average secs per morpheme", avgSecs, 1.0, 1.0, false);
	}

	@Test
	public void test__wordsContainingMorpheme__root() throws Exception {
		String morpheme = "inuk";
		List<MorphDictionaryEntry> msearchResults =
			morphemeSearcher.search(morpheme);
		new AssertMorphSearchResults(msearchResults, "")
			.foundMorphemes("inuk/1n", "inuksuk/1n", "inukpak/1n", "inukjuak/1n")
			.examplesForMorphemeStartWith("inuk/1n", Pair.of("inuit", new Long(1)))
			// No example words found for this particular morpheme
			.examplesForMorphemeStartWith("inukjuak/1n")
			;
		
		morpheme = "nuna";
		msearchResults =
			this.morphemeSearcher.search(morpheme);
		new AssertMorphSearchResults(msearchResults, "")
			.foundMorphemes(
				"nuna/1n", "nunajaq/1n", "nunavut/1n", "nunavik/1n", "nunaqaq/1v",
				"nunalik/1n", "nunatsiaq/1n", "nunaqarvik/1n")
			.examplesForMorphemeStartWith("nuna/1n", Pair.of("nunami", new Long(1)))
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
		List<MorphDictionaryEntry> wordsForMorphemes =
			this.morphemeSearcher.search(morpheme);
		
		new AssertMorphSearchResults(wordsForMorphemes, "")
			.foundMorphemes("mut/tn-dat-s")
			.examplesForMorphemeStartWith(
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
		
        MorphemeDictionary morphemeSearcher = new MorphemeDictionary();
        morphemeSearcher.useCorpus(compiledCorpus);
        
 		String morpheme = "gaq";
		List<MorphDictionaryEntry> wordsForMorphemes =
			morphemeSearcher.search(morpheme);
		
		new AssertMorphSearchResults(wordsForMorphemes, "")
			.foundMorphemes("gaq/1vn", "gaq/2vv")
			.examplesForMorphemeStartWith("gaq/1vn", Pair.of("makpigarni", new Long(1)))
			;
	}

	@Test
	public void test__wordsContainingMorpheme__tut() throws Exception {
		CompiledCorpus compiledCorpus = new CompiledCorpusRegistry().getCorpus();

		MorphemeDictionary morphemeSearcher = new MorphemeDictionary();
		morphemeSearcher.useCorpus(compiledCorpus);

		String morpheme = "tut";
		List<MorphDictionaryEntry> wordsForMorphemes =
			morphemeSearcher.search(morpheme);

		new AssertMorphSearchResults(wordsForMorphemes, "")
			.foundMorphemes(
				"tut/tn-sim-s", "tut/1v", "tutik/1v", "tuti/1v", "tutaq/1n",
				"tutarut/1n", "tutiriaq/1n", "tutiriarmiutaq/1n")
			.examplesForMorphemeStartWith("tutik/1v", Pair.of("tutinnikkut", new Long(1)))
			.examplesForMorphemeStartWith("tut/tn-sim-s", Pair.of("inuinnaqtun", new Long(197)))
			.examplesForMorphemeStartWith("tut/1v", Pair.of("tutuu", new Long(501)))
			// No word examples found for this particular morpheme
			.examplesForMorphemeStartWith("tutiriaq/1n")
			;
	}

	@Test
	public void test__wordsContainingMorpheme__taq() throws Exception {
		CompiledCorpus compiledCorpus = new CompiledCorpusRegistry().getCorpus();

		MorphemeDictionary morphemeSearcher = new MorphemeDictionary();
		morphemeSearcher.useCorpus(compiledCorpus);

		// This one used to cause a null pointer exception
		String morpheme = "taq";
		List<MorphDictionaryEntry> wordsForMorphemes =
			morphemeSearcher.search(morpheme);

		new AssertMorphSearchResults(wordsForMorphemes, "")
			.foundMorphemes(
				"taq/1vv", "taq/2nv", "taqaq/1nv", "taqa/1v", "taqak/1n",
				"taqqiq/1n", "taquaq/1v", "taqqa/rad-sc", "taquaq/1n", "taqqut/1n",
				"taqqa/ad-sc", "taqqirsuq/1v", "taqqangna/pd-mlsc-s", "taqqaksu/rpd-mlsc-s",
				"taqqapku/rpd-mlsc-p", "taqqapkua/pd-mlsc-p")
			.examplesForMorphemeStartWith("taq/1vv", Pair.of("uqalimaaqtauningit", new Long(639)))
			// No word examples found for this particular morpheme
			.examplesForMorphemeStartWith("taquaq/1n")
			;
	}

	@Test
	public void test__wordsContainingMorpheme__QueryIsPartialMorpheme__FindsAllMorphemesThatStartWithQuery() throws Exception {
		morphemeSearcher.useCorpus(new CompiledCorpusRegistry().getCorpus());

		String morpheme = "siu";
		List<MorphDictionaryEntry> wordsForMorphemes =
			this.morphemeSearcher.search(morpheme);

		new AssertMorphSearchResults(wordsForMorphemes, "")
			.foundMorphemes(
				"siut/1nn", "siuk/tv-imp-2p-3s", "siuq/1nv", "siut/1n", "siuk/1n",
				"siuraq/1n"
			)
		;
	}

	@Test
	public void test__balanceExamplesByRoots() throws Exception {
		String morpheme = "taq/1_vv";
		// List of words containing the morpheme, sorted by decreasing order of
		// frequency.
		List<MorphWordExample> wordExamples = new ArrayList<MorphWordExample>();
		Collections.addAll(wordExamples, new MorphWordExample[] {
			new MorphWordExample(
				"uqalimaaqtauningit", morpheme, new Long(639),
				"{uqalimaaq/1v} {taq/1vv} {uq/3vv} {niq/2vn} {ngit/tn-nom-p-4s}"),
			// Note that this second word has same root as the first one.
			new MorphWordExample(
				"uqalimaaqtauninginnut", morpheme, new Long(151),
				"{uqalimaaq/1v} {taq/1vv} {uq/3vv} {niq/2vn} {nginnut/tn-dat-p-4s}"),
			// And so does this one
			new MorphWordExample(
				"uqalimaaqtauninga", morpheme, new Long(128),
				"{uqalimaaq/1v} {taq/1vv} {uq/3vv} {niq/2vn} {nga/tn-nom-s-4s}"),
			// This one has a different root from the first 3
			new MorphWordExample(
				"ammaluttau", morpheme, new Long(79),
				"{angmaluq/1v} {taq/1vv} {ut/1vn}"),
			// And this one presents a 3rd new root
			new MorphWordExample(
				"katittarvingmut", morpheme, new Long(49),
				"{katit/1v} {taq/1vv} {vik/3vn} {mut/tn-dat-s}"),
			// This one has a root that has already been seen
			new MorphWordExample(
				"katittarvingmi", morpheme, new Long(30),
				"{katit/1v} {taq/1vv} {vik/3vn} {mi/tn-loc-s}"),
		});

		wordExamples = morphemeSearcher.balanceExamplesByRoots(wordExamples, 20);
		assertExamplesAre(wordExamples,
			Pair.of("uqalimaaqtauningit", new Long(639)),
			Pair.of("ammaluttau", new Long(79)),
			Pair.of("katittarvingmut", new Long(49)),
			Pair.of("uqalimaaqtauninginnut", new Long(151)),
			Pair.of("katittarvingmi", new Long(30)),
			Pair.of("uqalimaaqtauninga", new Long(128)));
	}

	@Test
	public void test__bestExamplesForMorphID() throws Exception {
		String morphID = "tut/tn-sim-s";
		morphemeSearcher.useCorpus(new CompiledCorpusRegistry().getCorpus());
		List<MorphWordExample> gotExample = morphemeSearcher.bestExamplesForMorphID(morphID, 5);
		assertExamplesAre(gotExample,
			Pair.of("inuinnaqtun", new Long(197)),
			Pair.of("ajjigiinngittut", new Long(138)),
			Pair.of("pingasuujuqtut", new Long(61)),
			Pair.of("niqtunaqtut", new Long(53)),
			Pair.of("pattatuqtut", new Long(31))
		);
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

	private void assertExamplesAre(List<MorphWordExample> gotExamples,
		Pair<String,Long>... expWordsWithFreq) throws Exception {
    	Pair<String,Long>[] gotWordWithFreq = new Pair[gotExamples.size()];
    	for (int ii=0; ii < gotExamples.size(); ii++) {
			MorphWordExample currExample = gotExamples.get(ii);
    		gotWordWithFreq[ii] = Pair.of(currExample.word, currExample.frequency);
		}
		AssertObject.assertDeepEquals("Word examples not as expected",
			expWordsWithFreq, gotWordWithFreq);
	}

}
