package ca.pirurvik.iutools.morphemesearcher;

import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import ca.inuktitutcomputing.data.LinguisticData;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.testing.AssertHelpers;
import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.CompiledCorpusException;
import ca.pirurvik.iutools.MockCompiledCorpus;
import ca.pirurvik.iutools.morphemesearcher.MorphemeSearcher;
import ca.pirurvik.iutools.morphemesearcher.ScoredExample;

import org.junit.*;

public class MorphemeSearcherTest {
	
	private MorphemeSearcher morphemeExtractor = new MorphemeSearcher();
	private MockCompiledCorpus mockCompiledCorpus;
	
	@Before
	public void setUp() throws CompiledCorpusException {
		HashMap<String,String> dictionary = new HashMap<String,String>();
//		dictionary.put("inuit", "{root1/idr1} {affix1/idaff1}");
//		dictionary.put("nunami", "{root2/idr2} {affix21/idaff21}");
//		dictionary.put("iglumik", "{root3/idr3} {affix21/idaff21} {affix32/idaff31}");
//		dictionary.put("inuksuk", "{root1/idr1} {affix4/idaff4}");
		
		dictionary.put("inuit", "{inuk/1n} {it/tn-nom-p}");
		dictionary.put("nunami", "{nuna/1n} {mi/tn-loc-s}");
		dictionary.put("iglumik", "{iglu/1n} {mik/tn-acc-s}");
		dictionary.put("inuglu", "{inuk/1n} {lu/1q}");
		
		mockCompiledCorpus = new MockCompiledCorpus();
		mockCompiledCorpus.setVerbose(false);
		// The MockCompiledCorpus's segmenter will use this dictionary instead of calling 
		// the morphological analyzer.
		mockCompiledCorpus.setDictionary(dictionary);
		String[] stringsOfWords = new String[] {
				"inuit nunami iglumik inuglu"
				};
        try {
    		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        	mockCompiledCorpus.compileCorpusFromScratch(corpusDirPathname);
            morphemeExtractor.useCorpus(mockCompiledCorpus);
        } catch(Exception e) {
        	System.err.println("Exiting from compiler");
        	System.err.println("because: "+e.getMessage());
        	System.exit(1);
        }
	}

	@Test
	public void test__MorphemeExtractor__Synopsis() throws Exception {
		//
		MorphemeSearcher morphemeExtractor = new MorphemeSearcher();

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
		List<MorphemeSearcher.Words> wordsForMorphemes = morphemeExtractor.wordsContainingMorpheme(morpheme);
	}
	
	@Test(expected=Exception.class)
	public void test__MorphemeExtractor__Synopsis__No_corpus_defined() throws Exception {
		MorphemeSearcher morphemeExtractor = new MorphemeSearcher();
		String morpheme = "nunami";
		List<MorphemeSearcher.Words> wordsForMorphemes = morphemeExtractor.wordsContainingMorpheme(morpheme);
	}
		
	/**********************************
	 * VERIFICATION TESTS
	 * @throws Exception 
	 **********************************/
	
	@Test
	public void test__wordsContainingMorpheme__root() throws Exception {
		LinguisticData.init();
		LinguisticData.getInstance();
//		String morpheme = "root1";
		String morpheme = "inuk";
		List<MorphemeSearcher.Words> wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		List<ScoredExample> words = wordsForMorphemes.get(0).words;	
		List<ScoredExample> expected = new ArrayList<ScoredExample>();
		expected.add(new ScoredExample("inuit",10001.0,new Long(1))); 
		expected.add(new ScoredExample("inuglu",10001.0,new Long(1)));
		Assert.assertEquals(2, words.size());
		AssertHelpers.assertDeepEquals("",expected,words);
		
//		morpheme = "root2";
		morpheme = "nuna";
		wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		words = wordsForMorphemes.get(0).words;
		expected = new ArrayList<ScoredExample>();
		expected.add(new ScoredExample("nunami",10001.0,new Long(1)));
		Assert.assertEquals(1, words.size());
		AssertHelpers.assertDeepEquals("",expected,words);
		
		HashMap<String,String> dictionary = new HashMap<String,String>();
//		dictionary.put("inuit", "{root1/idr1} {affix1/idaff1}");
//		dictionary.put("nunami", "{root2/idr2} {affix21/idaff21}");
//		dictionary.put("iglumik", "{root3/idr3} {affix21/idaff21} {affix32/idaff31}");
//		dictionary.put("inuksuk", "{root1/idr1} {affix41/idaff411}");
//		dictionary.put("iglumut", "{root3/idr3} {affix41/idaff411}");
//		dictionary.put("nunatsiaq", "{root2/idr2} {affix41/idaff412} {affix42/idaff42}");
		
		dictionary.put("inuit", "{inuk/1n} {it/tn-nom-p}");
		dictionary.put("nunami", "{nuna/1n} {mi/tn-loc-s}");
		dictionary.put("iglumik", "{iglu/1n} {mik/tn-acc-s}");
		dictionary.put("inuglu", "{inuk/1n} {lu/1q}");
		dictionary.put("iglumut", "{iglu/1n} {mut/tn-dat-s}");
		dictionary.put("nunamut", "{nuna/1n} {mut/tn-dat-s}");

		MockCompiledCorpus mockCompiledCorpus = new MockCompiledCorpus();
		mockCompiledCorpus.setVerbose(false);
		mockCompiledCorpus.setDictionary(dictionary);
		String[] stringsOfWords = new String[] {
				"inuit nunami iglumik inuglu iglumut nunamut"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        try {
        	mockCompiledCorpus.compileCorpusFromScratch(corpusDirPathname);
        } catch(Exception e) {
        	System.err.println("Exiting from compiler");
        }
        morphemeExtractor.useCorpus(mockCompiledCorpus);
        
//		morpheme = "affix41";
		morpheme = "mut";
		wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		
			words = wordsForMorphemes.get(0).words;
			expected = new ArrayList<ScoredExample>();
			expected.add(new ScoredExample("iglumut", 10001.0, new Long(1))); 
			expected.add(new ScoredExample("nunamut", 10001.0, new Long(1)));
			Assert.assertEquals(2, words.size());
			AssertHelpers.assertDeepEquals("",expected,words);
	}
	
	public void test__wordsContainingMorpheme__infix() throws Exception {
		String morpheme = "affix21";
		List<MorphemeSearcher.Words> wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		List<ScoredExample> words = wordsForMorphemes.get(0).words;	
		List<String> expected = new ArrayList<String>();
		expected.add("word2"); expected.add("word3");
		Assert.assertEquals(2, words.size());
		Assert.assertTrue(words.contains(expected.get(0)));
		Assert.assertTrue(words.contains(expected.get(1)));
	}
	
	public void test__wordsContainingMorpheme__ending() throws Exception {
		String morpheme = "affix4";
		List<MorphemeSearcher.Words> wordsForMorphemes = this.morphemeExtractor.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		List<ScoredExample> words = wordsForMorphemes.get(0).words;	
		List<String> expected = new ArrayList<String>();
		expected.add("word4");
		Assert.assertEquals(1, words.size());
		Assert.assertTrue(words.contains(expected.get(0)));
	}
	
    @Test
    public void test__morphFreqInAnalyses__HappyPath() throws Exception {
        String morpheme = "gaq/2vv";
        String word = "makpigarni";
        Double freq2vv = morphemeExtractor.morphFreqInAnalyses(morpheme, word, true);

        morpheme = "gaq/1vn";
        Double freq1vn = morphemeExtractor.morphFreqInAnalyses(morpheme, word, true);
        
        Assert.assertTrue("Frequency of gaq/1vn should have been much higher than frequency of gaq/2vv.", freq1vn > 1.5*freq2vv);
    }
    
    @Test
    public void test__numberOfWordsInCorpusWithSuiteOfMorphemes() throws IOException {
		String[] stringsOfWords = new String[] {
				"makpigarni", "mappigarni", "inuglu"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        try {
        	compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
        } catch(CompiledCorpusException | StringSegmenterException e) {
        }
		
        MorphemeSearcher morphemeSearch = new MorphemeSearcher();
        morphemeSearch.useCorpus(compiledCorpus);
    	
    	String decompositionExpression = "{makpi:makpiq/1v}{gar:gaq/1vn}{ni:ni/tn-loc-p}";
    	long nWords = morphemeSearch.numberOfWordsInCorpusWithSuiteOfMorphemes(decompositionExpression);
    	long expected = 2;
    	Assert.assertEquals("The number of words in the corpus with the given sequence of morphemes that was returned is incorrect.", expected, nWords);
    }

    @Test
	public void test__wordsContainingMorpheme__gaq1vn() throws Exception {
		String[] stringsOfWords = new String[] {
				"makpigarni", "mappigarni", "inuglu"
				};
		String corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        try {
        	compiledCorpus.compileCorpusFromScratch(corpusDirPathname);
        } catch(CompiledCorpusException | StringSegmenterException e) {
        }
		
        MorphemeSearcher morphemeSearcher = new MorphemeSearcher();
        morphemeSearcher.useCorpus(compiledCorpus);
        
 		String morpheme = "gaq";
		List<MorphemeSearcher.Words> wordsForMorphemes = morphemeSearcher.wordsContainingMorpheme(morpheme);
		Assert.assertTrue(wordsForMorphemes.size()==1);
		List<ScoredExample> words = wordsForMorphemes.get(0).words;	
		List<ScoredExample> expected = new ArrayList<ScoredExample>();
		expected.add(new ScoredExample("makpigarni",10002.0,(long)2));
		expected.add(new ScoredExample("mappigarni",10002.0,(long)2));
		Assert.assertEquals(2, words.size());
		AssertHelpers.assertDeepEquals("", expected.get(0), words.get(0));
		AssertHelpers.assertDeepEquals("", expected.get(1), words.get(1));
	}
	
	@Test @Ignore
	public void test() {
		String[] affixIDs = new String[] {"kutaaq/1nn","vaalliq/1vv","juu/1vv","jaqtuq/1vv","lu/1q","kia/1q","juq/1vn","naq/2vn","gulu/1nn","saali/1vv","giaq/1vv","liq/1vv","laaq/3vn","si/1vv","nanngit/1vv","mmiaq/1vv","mmarik/2vv","ligaa/1vv","galuaq/1vv","anik/1vv","mmarik/1nn","ralaaq/1nn","rusiq/1vn","luktaaq/1nn","nngaq/1nv","uq/1nv","saaq/1vn","kaallaq/1vv","it/1nv","kuvik/1nn","iruti/1nv","lik/1nn","vik/2vv","laukak/1vv","lik/3nv","siuq/1nv","uti/1vv","rujuk/1vv","qati/1vn","qati/1nn","qai/1q","rujuq/2nn","guk/1nv","tsiriaq/1vv","nnajuk/2vv","paluk/1vn","iqsuq/1nv","jaq/2vv","tsiaq/2nn","innaujaq/1vv","lauq/2vv","qu/1vv","tuinnariaqaq/1vv","jjiq/1nv","juit/1vv","kasak/1vv","iq/2vv","quuji/1vv","nnguaq/2vv","apik/1nn","junnanngit/1vv","tsaliq/1vv","vasik/1nv","qattaq/1vv","kammiq/1vv","liq/3nv","laaq/1nn","saaq/1vv","innaq/1nn","niaqqau/1vv","kautigi/1vv","niujaq/1vv","ujjuaq/1vv","nga/1vv","ttuq/1nv","siri/1nv","jarniq/1vv","pasaaq/1vv","uq/2nv","niqsaq/1vn","niaqtaksari/1vv","niq/1vv","ttauq/1q","i/1vv","rajaaq/1vv","mi/1vv","si/4nv","kulu/1nn","tainnaq/1vv","tiq/1vn","qalauq/1vv","nasaaq/1vv","raq/1vv","liaq/1nn","ri/1vv","tuinnau/1vv","giik/2nv","rusiq/2nn","sarait/1vv","qqautau/1vv"};
		HashMap<String, String[]> hashTest = new HashMap<String,String[]>();
		hashTest.put("kutaaq/1nn",new String[] {"aqutikutaamit","aturiakutaap","sullukutaami","atajukutaanut","nunasiutikutaanut"});
		hashTest.put("kutaaq/1vv", new String[] {"titikutaaqsimajuq","aajiiqatigiikutaaluaqtualuuninginnut","katimakutaalauratta","kiukutaaluakainnarama","nunaqaqsimakutaaqtuq"});
		hashTest.put("vaalliq/1vv", new String[] {"pivaallirutissanginnullu","sanngiliqpaalliqtittinirmut","piusivaalliqullugit","akausivaallirutiksanik","ilavaalliqpuq"});
		//hashTest.put("juu/1vv", new String[] {});
		hashTest.put("jaqtuq/1vv", new String[] {"tusaajaqtuqsimajunik","uqariaqtuqsimajut","katimajaqtuqsimajut","pulaariaqtuqsimajunik","apiqsuriaqtuqtaujullu","niruariaqturviit"});
		hashTest.put("lu/1q", new String[] {"asinginniglu","tamannalu","inulirijikkullu","inuusilirijikkunnullu","ilangillu"});
		hashTest.put("kia/1q", new String[] {"uvvalukiaq","qanukiaq","qangakiaq","kisukiaq","kinakiaq"});
		hashTest.put("juq/1vn", new String[] {"maannaujuq","niqtunaqtuq","ministaujuq","ilinniaqtulirinirmut","kajusijuq"});
		//hashTest.put("naq/2vn", new String[] {"mamiana","mamianaq","mamianaugalua","mamianaugaluaq"});
		//hashTest.put("gulu/1nn", new String[] {});
		hashTest.put("saali/1vv", new String[] {"atuinnarisaaliguttigut","atuqtaullattaalirajarninganut","kiujausaalijumajunga","pianiksaalijjuumilugit","pigiaqsaaligajaqtuni"});
		//hashTest.put("giaq/1vv", new String[] {});
		//hashTest.put("giaq/2vn", new String[] {});
		//hashTest.put("liq/1vv", new String[] {});
		//hashTest.put("liq/2nv", new String[] {});
		//hashTest.put("liq/3nv", new String[] {});
		hashTest.put("laaq/3vn", new String[] {"amisuulaanut","piulaamik","mikilaamik","quttilaanit","qanuinngilaaq"});
		hashTest.put("laaq/2vv", new String[] {"pinasuarusiulaaqtumi","arraaguulaaqtumi","pigiaqtittilaarama","qanuilingalaarmangaaq","takulaarivugut"});
		hashTest.put("laaq/1nn", new String[] {"gavamalaat","surusilaat","katimajilaat","nutaralaat","ilinniarvilaanguniqsamik"});
		hashTest.put("", new String[] {});

		hashTest.put("", new String[] {});
		fail("Not yet implemented");
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
