package org.iutools.corpus;


import ca.nrc.data.file.ObjectStreamReader;
import org.apache.commons.lang3.ArrayUtils;
import org.iutools.config.IUConfig;
import org.iutools.corpus.elasticsearch.CompiledCorpus_ES;
import org.iutools.corpus.sql.CompiledCorpus_SQL;
import org.iutools.linguisticdata.LinguisticData;
import ca.nrc.datastructure.CloseableIterator;
import org.iutools.sql.SQLLeakMonitor;
import org.iutools.sql.SQLTestHelpers;
import static org.iutools.sql.SQLTestHelpers.TimingResults;
import org.iutools.utilities.StopWatch;
import org.iutools.worddict.GlossaryEntry;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * These tests compare the speed of some basic operations for two implementations
 * of the CompiledCorpus:
 *
 * - CompiledCorpus_SQL
 * - CompiledCorpus_ES
 *
 */
public class CompiledCorpus_SpeedComparison_SQLvsESTest {

	protected final String corpusName = "hansard-1999-2002";

	// These are the words in the Wikipedia glossary for which we actually have
	// an entry in the CompiledCorpus.
	// an entry in the CompiledCorpus.
	protected static String[] wordsToTestArr = new String[] {
		"nunavut", "annuraanik", "qarasaujaq", "ilinniaqtuliriniq", "nunaujuq",
		"inuit", "kanata", "inuk", "inuktitut", "nunaqaqqaaqsimajut",
		"ingirrajjutit", "nunavik", "kupaik", "nunatsiaq", "aantiuriju",
		"aantiuriu", "murial", "akukittut", "qallunaatitut", "inuksuk", "taqqiq",
		"nuna", "nanuq", "avinngaq", "jannuali", "imialuk", "imigaq", "niqi",
		"mai", "juni", "julai", "iglu", "ilu", "ukpik", "mini", "aanniarvik",
		"iqaluk", "iqaluit", "jaamani", "isumaliurniq", "aggak", "naujaq",
		"qaujisarniq", "ukiuq", "siqiniq", "qau", "pisukti", "timi", "kuuk",
		"tuktu", "qilaut", "arvik", "ita", "suna", "asapi", "qallu", "kiggavik",
		"nunaliit", "anguti", "nipi", "uqaqtuq", "aput", "siuraq", "aqiaruq",
		"qikiqtaaluk", "sanna", "ukalik", "amaruk", "saina", "viu", "sili", "nuuk",
		"tupiq", "igunaq", "ikuma", "imaq", "ikpiarjuk", "arviat", "qamanittuaq",
		"kinngait", "igluligaarjuk", "kangiqtugaapik", "salliit", "uqsuqtuuq",
		"aujuittuq", "sanirajak", "iglulik", "kimmirut", "nanisivik", "aulajuq",
		"sainisititut", "ajjiliurut", "ajjinnguaq", "aqsaq", "atajuq", "aujaq",
		"uviniruq", "qangatasuukkuvik", "luuttaaq", "paliisikkut", "ujarak", "ulu",
		"umiaq", "umiarjuaq", "guulu", "aavuuta", "aki", "akkak", "allavvik",
		"amiat", "anaana", "anarvik", "aqiggiq", "aqpik", "imiq", "illu", "irniq",
		"uvagut", "uqausiq", "uqsuq", "ataata", "anuri", "arnaq", "tutu", "tasiq",
		"tariuq", "qaujisarvik", "katitiriniq", "naasausiriniq", "tuqunnaqtuq",
		"aanniaq", "aggak", "maniraq", "ugjuk", "sitni"
	};
	protected static List<String> wordsToTest = new ArrayList<String>();
	static {
		Collections.addAll(wordsToTest, wordsToTestArr);
	}

	protected static Set<String> startNgramsToTest = null;
	protected static Set<String> endNgramsToTest = null;
	protected static Set<String> middleNgramsToTest = null;

	protected static Set<String> startMorphemesToTest = null;
	protected static Set<String> middleMorphemesToTest = null;
	protected static Set<String> endMorphemesToTest = null;
	
	protected static Set<String[]> startMorphNgramsToTest = null;
	protected static Set<String[]> middleMorphNgramsToTest = null;
	protected static Set<String[]> endMorphNgramsToTest = null;

	protected static Path glossaryPath = null;
	static {
		try {
			glossaryPath = Paths.get(IUConfig.getIUDataPath("data/glossaries/wpGlossary.json"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	static CompiledCorpus_SQL sqlCorpus = null;
	static CompiledCorpus_ES esCorpus = null;

	SQLLeakMonitor sqlLeakMonitor = null;

	@BeforeAll
	public static void beforeAll() throws Exception {
		new SQLTestHelpers().encurConnectionPoolOverhead();
		return;
	}

	@BeforeEach
	public void setUp() throws Exception {
		sqlLeakMonitor = new SQLLeakMonitor();

		if (sqlCorpus == null) {
			sqlCorpus = new CompiledCorpus_SQL(corpusName);
		}
		if (esCorpus == null) {
			esCorpus = new CompiledCorpus_ES(corpusName);
		}
		generateWordsToTest();
		generateStartNgramsToTest();
		generateEndNgramsToTest();
		generateMiddleNgramsToTest();
		generateMorphemesToTest();
		generateMorphNgramsToTest();
		return;
	}

	@AfterEach
	public void tearDown() throws Exception  {
		sqlLeakMonitor.assertNoLeaks();
		return;
	}

	private void generateMorphemesToTest() throws Exception {
		if (startMorphemesToTest == null) {
			startMorphemesToTest = new HashSet<String>();
			middleMorphemesToTest = new HashSet<String>();
			endMorphemesToTest = new HashSet<String>();
		}
		CompiledCorpus corpus = new CompiledCorpus_ES(CompiledCorpusRegistry.defaultCorpusName);
		Iterator<String> wordsIter = corpus.allWords();
		// Test using morphemes from the first few words in the corpus
		// (in alphabetical order) until we have MAX_MORPHS morphemes in each
		// type of position
		final int MAX_MORPHS = 20;
		while (true) {
			if (! wordsIter.hasNext()) {
				break;
			}
			String word = wordsIter.next();
			WordInfo winfo = corpus.info4word(word);
			String[] decomp = winfo.topDecomposition();
			if (decomp != null && decomp.length > 0) {
				if (decomp[decomp.length-1].equals("\\")) {
					// Remove tailing \ pseudo-morpheme
					decomp = Arrays.copyOf(decomp, decomp.length-1);
				}
				for (int jj=0; jj < decomp.length; jj++) {
					decomp[jj] = decomp[jj].replaceAll("[\\{\\}]", "");
				}
				if (startMorphemesToTest.size() < MAX_MORPHS) startMorphemesToTest.add(decomp[0]);
				if (endMorphemesToTest.size() < MAX_MORPHS) endMorphemesToTest.add(decomp[decomp.length-1]);
				for (int jj=0; jj < decomp.length-1; jj++) {
					if (middleMorphemesToTest.size() < MAX_MORPHS) {
						middleMorphemesToTest.add(decomp[jj]);
					}
				}
				if (startMorphemesToTest.size() >= MAX_MORPHS &&
					middleMorphemesToTest.size() >= MAX_MORPHS &&
					endMorphemesToTest.size() >= MAX_MORPHS) {
					break;
				}
			}
		}
		return;
	}


	private void generateMorphemesToTest__OLD() {
		if (startMorphemesToTest == null) {
			startMorphemesToTest = new HashSet<String>();
			middleMorphemesToTest = new HashSet<String>();
			endMorphemesToTest = new HashSet<String>();
		}
		LinguisticData data = LinguisticData.getInstance();
		String[] allMorphemeIDs = data.allMorphemeIDs();
		for (String morphID: allMorphemeIDs) {
			startMorphemesToTest.add(morphID);
			middleMorphemesToTest.add(morphID);
			endMorphemesToTest.add(morphID);
		}
		return;
	}

	private void generateMorphNgramsToTest() throws Exception {
		if (startMorphNgramsToTest == null) {
			startMorphNgramsToTest = new HashSet<String[]>();
			middleMorphNgramsToTest = new HashSet<String[]>();
			endMorphNgramsToTest = new HashSet<String[]>();
			CompiledCorpus corpus = new CompiledCorpusRegistry().getCorpus();
			ObjectStreamReader reader =
				new ObjectStreamReader(glossaryPath.toFile());
			while (true) {
				GlossaryEntry entry = (GlossaryEntry) reader.readObject();
				if (entry == null) {
					break;
				}
				String word = entry.getTermInLang("iu_roman");
				WordInfo winfo = corpus.info4word(word);
				if (winfo != null) {
					String[] decomp = winfo.topDecomposition();
					if (decomp != null) {
						decomp = Arrays.copyOf(decomp, decomp.length-1);
						String[] startNgram =
							ArrayUtils.addAll(new String[] {"^"},
								Arrays.copyOf(decomp, decomp.length));
						if (startNgram.length > 0) {
							startMorphNgramsToTest.add(startNgram);
						}

						String[] endNgram =
							Arrays.copyOfRange(
								decomp, Math.max(0, decomp.length-3), decomp.length);
						if (endNgram.length > 0) {
							endMorphNgramsToTest.add(endNgram);
						}

						int toTrim = Math.max(0, (decomp.length-3)/2);
						String[] midNgram =
							Arrays.copyOfRange(
								decomp, toTrim, decomp.length - toTrim);
						if (midNgram.length > 0) {
							middleMorphNgramsToTest.add(midNgram);
						}
					}
				}
			}
		}
		return;
	}

	@Test
	public void test__info4word() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_info4word(esCorpus));
		times.put("sql", time_info4word(sqlCorpus));

		if (times.get("sql") > 5.0) {
			// We only worry about speed of sql implementation if it takes more than
			// 5 msecs per word.
			double tolerance = 1.2;
			SQLTestHelpers.assertSqlNotSignificantlySlowerThanES(
				"info4word", times, tolerance);
		}
	}

	private double time_info4word(CompiledCorpus corpus)
		throws Exception {
		System.out.println("Corpus implementation "+corpus.getClass().getSimpleName());
		StopWatch sw = new StopWatch().start();
		int totalNulls = 0;
		for (String word: wordsToTest) {
			WordInfo winfo = corpus.info4word(word);
			if (winfo == null) {
				totalNulls++;
				System.out.println("  returned null info for word "+word);
			} else {
//				System.out.println("  non-null info for word "+word);
			}
		}
//		System.out.println("\n  Returned "+totalNulls+" out of "+wordsToTest.size());
		Long msecsPerWord = sw.lapTime(TimeUnit.MILLISECONDS) / wordsToTest.size();
		return msecsPerWord;
	}

	@Test
	public void test__wordsContainingNgram__startOfWord() throws Exception {
		String focusOnNgram = null;
		Map<String,TimingResults> times = new HashMap<String,TimingResults>();
		times.put("es", time_wordsContainingNgram__NEW(esCorpus, startNgramsToTest, focusOnNgram));
		times.put("sql", time_wordsContainingNgram__NEW(sqlCorpus, startNgramsToTest, focusOnNgram));
		SQLTestHelpers.assertSqlNotSignificantlySlowerThanES_NEW(
			"wordsContainingNgram__startOfWord", times);
	}

	@Test
	public void test__wordsContainingNgram__endOfWord() throws Exception {
		String focusOnNgram = null;

		Map<String, TimingResults> times = new HashMap<String,TimingResults>();
		times.put("es", time_wordsContainingNgram__NEW(esCorpus, endNgramsToTest, focusOnNgram));
		times.put("sql", time_wordsContainingNgram__NEW(sqlCorpus, endNgramsToTest, focusOnNgram));
		SQLTestHelpers.assertSqlNotSignificantlySlowerThanES_NEW(
			"wordsContainingNgram__endOfWord", times);
	}


	@Test
	public void test__wordsContainingNgram__middleOfWord() throws Exception {
		Map<String, TimingResults> times = new HashMap<String, TimingResults>();

		String focusOnNgram = null;
//		focusOnNgram = "aqt";
		times.put("es", time_wordsContainingNgram__NEW(
			esCorpus, middleNgramsToTest, focusOnNgram));
		times.put("sql", time_wordsContainingNgram__NEW(
			sqlCorpus, middleNgramsToTest, focusOnNgram));

		SQLTestHelpers.assertSqlNotSignificantlySlowerThanES_NEW(
			"wordsContainingNgram__middleOfWord", times);
	}

	private Double time_wordsContainingNgram(CompiledCorpus corpus,
		Collection<String> ngramsToTest) throws Exception {
		System.out.println("Timing wordsContainingNgram with corpus="+corpus.getClass().getSimpleName());
		StopWatch sw = new StopWatch().start();
		long searchMsecs = 0;
		long iterateMSecs = 0;
		for (String ngram: ngramsToTest) {
			try (CloseableIterator<String> iter = corpus.wordsContainingNgram(ngram)) {
				searchMsecs += sw.lapTime(TimeUnit.MILLISECONDS);
				int countDown = 100;
				while (countDown > 0 && iter.hasNext()) {
					countDown--;
					String word = iter.next();
					int x = 1;
				}
				iterateMSecs += sw.lapTime(TimeUnit.MILLISECONDS);
			}
		}

		double msecsPerNgram =
			1.0 * sw.totalTime(TimeUnit.MILLISECONDS) / ngramsToTest.size();
		double msecsSearchPerNgram =
			1.0 * searchMsecs / ngramsToTest.size();
		double msecsIteratePerNgram =
			1.0 * iterateMSecs / ngramsToTest.size();

		System.out.println("  Times per ngram (msecs)");
		System.out.println("     search+iterate : "+msecsPerNgram);
		System.out.println("     search only    : "+msecsSearchPerNgram);
		System.out.println("     iterate only   : "+msecsIteratePerNgram);

		return msecsPerNgram;
	}


	private SQLTestHelpers.TimingResults time_wordsContainingNgram__NEW(CompiledCorpus corpus,
		Collection<String> ngramsToTest, String focusOnNgram) throws Exception {
		System.out.println("\n== Timing wordsContainingNgram with corpus="+corpus.getClass().getSimpleName());
		SQLTestHelpers.TimingResults results = new SQLTestHelpers.TimingResults();
		StopWatch sw = new StopWatch().start();
		StopWatch swSingleCase = new StopWatch().start();
		long searchMsecs = 0;
		long iterateMSecs = 0;
		for (String ngram: ngramsToTest) {
			if (focusOnNgram != null && !ngram.equals(focusOnNgram)) {
				continue;
			}
			swSingleCase.reset();
			try (CloseableIterator<String> iter = corpus.wordsContainingNgram(ngram)) {
				searchMsecs += swSingleCase.lapTime(TimeUnit.MILLISECONDS);
				int totalHits = 0;
				while (totalHits < 100 && iter.hasNext()) {
					totalHits++;
					String word = iter.next();
				}
				if (focusOnNgram != null) {
					System.out.println("  ngram="+ngram+" returned "+totalHits+" hits");
				}
				iterateMSecs += swSingleCase.lapTime(TimeUnit.MILLISECONDS);
				results.onNewCase(ngram, sw.lapTime(TimeUnit.MILLISECONDS));
			}
		}

		double msecsPerNgram =
			1.0 * sw.totalTime(TimeUnit.MILLISECONDS) / ngramsToTest.size();
		double msecsSearchPerNgram =
			1.0 * searchMsecs / ngramsToTest.size();
		double msecsIteratePerNgram =
			1.0 * iterateMSecs / ngramsToTest.size();

		System.out.println("  Times per ngram (msecs)");
		System.out.println("    search+iterate : "+msecsPerNgram);
		System.out.println("    search only    : "+msecsSearchPerNgram);
		System.out.println("    iterate only   : "+msecsIteratePerNgram);

		return results;
	}

	
	@Test
	public void test__wordsContainingMorpheme__startOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingMorpheme(esCorpus, startMorphemesToTest));
		times.put("sql", time_wordsContainingMorpheme(sqlCorpus, startMorphemesToTest));
		SQLTestHelpers.assertSqlNotSignificantlySlowerThanES(
			"wordsContainingMorpheme__startOfWord", times);
	}

	@Test
	public void test__wordsContainingMorpheme__middleOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingMorpheme(esCorpus, middleMorphemesToTest));
		times.put("sql", time_wordsContainingMorpheme(sqlCorpus, middleMorphemesToTest));
		SQLTestHelpers.assertSqlNotSignificantlySlowerThanES(
			"wordsContainingMorpheme_middleOfWord", times);
	}

	@Test
	public void test__wordsContainingMorpheme__endOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingMorpheme(esCorpus, endMorphemesToTest));
		times.put("sql", time_wordsContainingMorpheme(sqlCorpus, endMorphemesToTest));
		SQLTestHelpers.assertSqlNotSignificantlySlowerThanES(
			"wordsContainingMorpheme__endOfWord", times);
	}
	
	private Double time_wordsContainingMorpheme(CompiledCorpus corpus,
		Collection<String> morphemesToTest) throws Exception {
		StopWatch sw = new StopWatch().start();
		int counter = 0;
		for (String morpheme: morphemesToTest) {
			counter++;
			List<WordInfo> words = corpus.wordsContainingMorpheme(morpheme);
		}
		return 1.0 * sw.lapTime(TimeUnit.MILLISECONDS);
	}

	@Test
	public void test__wordsContainingMorphNgram__startOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingMorphNgram(esCorpus, startMorphNgramsToTest));
		times.put("sql", time_wordsContainingMorphNgram(sqlCorpus, startMorphNgramsToTest));
		SQLTestHelpers.assertSqlNotSignificantlySlowerThanES(
			"wordsContainingMorphNgram__startOfWord", times);
	}

	@Test
	public void test__wordsContainingMorphNgram__middleOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingMorphNgram(esCorpus, middleMorphNgramsToTest));
		times.put("sql", time_wordsContainingMorphNgram(sqlCorpus, middleMorphNgramsToTest));
		SQLTestHelpers.assertSqlNotSignificantlySlowerThanES(
			"wordsContainingMorphNgram__middleOfWord", times);
	}

	@Test
	public void test__wordsContainingMorphNgram__endOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingMorphNgram(esCorpus, endMorphNgramsToTest));
		times.put("sql", time_wordsContainingMorphNgram(sqlCorpus, endMorphNgramsToTest));
		SQLTestHelpers.assertSqlNotSignificantlySlowerThanES(
			"wordsContainingMorphNgram__endOfWord", times);
	}

	private Double time_wordsContainingMorphNgram(CompiledCorpus corpus,
		Collection<String[]> morphNgramsToTest) throws Exception {
		StopWatch sw = new StopWatch().start();
		for (String[] morphNgram: morphNgramsToTest) {
			try (CloseableIterator<String> iter =
				  corpus.wordsContainingMorphNgram(morphNgram)) {
				int countDown = 100;
				while (iter.hasNext()) {
					countDown--;
					if (countDown <= 0) {
						break;
					}
					String word = iter.next();
				}
			}
		}

		double secsPerMorpheme =
			1.0 * sw.lapTime(TimeUnit.MILLISECONDS) / morphNgramsToTest.size();
		return secsPerMorpheme;
	}


	//////////////////////////////////////////////////////////
	// TEST HELPERS
	//////////////////////////////////////////////////////////


	private void generateWordsToTest() throws Exception {
		if (wordsToTest == null) {
			wordsToTest = new ArrayList<String>();
			ObjectStreamReader reader =
			new ObjectStreamReader(glossaryPath.toFile());
			StopWatch sw = new StopWatch().start();
			while (true) {
				GlossaryEntry entry = (GlossaryEntry) reader.readObject();
				if (entry == null) {
					break;
				}
				String word = entry.getTermInLang("iu_roman");
				if (word.contains(" ")) {
					// We ingore multi-word glossary entries
					continue;
				}
				wordsToTest.add(word);
			}
		}
	}

	private void generateStartNgramsToTest() throws Exception {
		if (startNgramsToTest == null) {
			startNgramsToTest = new HashSet<String>();
			ObjectStreamReader reader =
			new ObjectStreamReader(glossaryPath.toFile());
			StopWatch sw = new StopWatch().start();
			while (true) {
				GlossaryEntry entry = (GlossaryEntry) reader.readObject();
				if (entry == null) {
					break;
				}
				String word = entry.getTermInLang("iu_roman");
				if (word.contains(" ")) {
					// We ingore multi-word glossary entries
					continue;
				}
				if (word.length() > 3) {
					word = word.substring(0, 3);
				}
				word = "^"+word;
				startNgramsToTest.add(word);
			}
		}
	}

	private void generateEndNgramsToTest() throws Exception {
		if (endNgramsToTest == null) {
			endNgramsToTest = new HashSet<String>();
			ObjectStreamReader reader =
			new ObjectStreamReader(glossaryPath.toFile());
			StopWatch sw = new StopWatch().start();
			while (true) {
				GlossaryEntry entry = (GlossaryEntry) reader.readObject();
				if (entry == null) {
					break;
				}
				String word = entry.getTermInLang("iu_roman");
				if (word.contains(" ")) {
					// We ingore multi-word glossary entries
					continue;
				}
				if (word.length() > 3) {
					word = word.substring(word.length()-3, word.length());
				}
				word = word+"$";
				endNgramsToTest.add(word);
			}
		}
	}

	private void generateMiddleNgramsToTest() throws Exception {
		if (middleNgramsToTest == null) {
			middleNgramsToTest = new HashSet<String>();
			ObjectStreamReader reader =
			new ObjectStreamReader(glossaryPath.toFile());
			StopWatch sw = new StopWatch().start();
			while (true) {
				GlossaryEntry entry = (GlossaryEntry) reader.readObject();
				if (entry == null) {
					break;
				}
				String word = entry.getTermInLang("iu_roman");
				if (word.contains(" ")) {
					// We ingore multi-word glossary entries
					continue;
				}
				if (word.length() > 5) {
					int toTrim = (word.length()-3)/2;
					int startPos = toTrim;
					int endPos = word.length()-toTrim;
					word = word.substring(startPos, endPos);
					middleNgramsToTest.add(word);
				}
			}
		}
	}

}
