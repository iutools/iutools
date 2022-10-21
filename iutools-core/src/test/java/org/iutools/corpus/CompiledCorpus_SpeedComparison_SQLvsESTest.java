package org.iutools.corpus;


import ca.nrc.data.file.ObjectStreamReader;
import org.apache.commons.lang3.ArrayUtils;
import org.iutools.config.IUConfig;
import org.iutools.corpus.elasticsearch.CompiledCorpus_ES;
import org.iutools.corpus.sql.CompiledCorpus_SQL;
import org.iutools.linguisticdata.LinguisticData;
import org.iutools.sql.CloseableIterator;
import org.iutools.sql.SQLTestHelpers;
import org.iutools.utilities.StopWatch;
import org.iutools.worddict.GlossaryEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

	protected static List<String> wordsToTest = null;

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

	@BeforeAll
	public static void beforeAll() throws Exception {
		new SQLTestHelpers().encurConnectionPoolOverhead();
		return;
	}

	@BeforeEach
	public void setUp() throws Exception {

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
//		SQLTestHelpers.assertIsFaster("info4word", "sql", times);
		SQLTestHelpers.assertNoSlowerThan("info4word", "sql", times, 0.05);
	}

	private double time_info4word(CompiledCorpus corpus)
		throws Exception {
		StopWatch sw = new StopWatch().start();
		for (String word: wordsToTest) {
			WordInfo winfo = corpus.info4word(word);
			int x = 1;
		}
		return sw.lapTime(TimeUnit.MILLISECONDS);
	}

	@Test
	public void test__wordsContainingNgram__startOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingNgram(esCorpus, startNgramsToTest));
		times.put("sql", time_wordsContainingNgram(sqlCorpus, startNgramsToTest));
		SQLTestHelpers.assertIsFaster(
			"wordsContainingNgram__startOfWord", "sql", times);
//		SQLTestHelpers.assertAboutSameSpeed("wordsContainingNgram__startOfWord", times, 0.05);
	}

	@Test
	public void test__wordsContainingNgram__endOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingNgram(esCorpus, endNgramsToTest));
		times.put("sql", time_wordsContainingNgram(sqlCorpus, endNgramsToTest));
		SQLTestHelpers.assertIsFaster(
			"wordsContainingNgram__endOfWord", "sql", times);
	}

	@Test
	public void test__wordsContainingNgram__middleOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingNgram(esCorpus, middleNgramsToTest));
		times.put("sql", time_wordsContainingNgram(sqlCorpus, middleNgramsToTest));
		SQLTestHelpers.assertIsFaster(
			"wordsContainingNgram__middleOfWord", "sql", times);
	}

	private Double time_wordsContainingNgram(CompiledCorpus corpus,
		Collection<String> ngramsToTest) throws Exception {
		System.out.println("   Timing wordsContainingNgram with corpus="+corpus.getClass());
		StopWatch sw = new StopWatch().start();
		for (String ngram: ngramsToTest) {
			try (CloseableIterator<String> iter = corpus.wordsContainingNgram(ngram)) {
				int countDown = 100;
				while (countDown > 0 && iter.hasNext()) {
					countDown--;
					String word = iter.next();
					int x = 1;
				}
			}
		}
		double msecsPerNgram =
			1.0 * sw.lapTime(TimeUnit.MILLISECONDS) / ngramsToTest.size();
		return msecsPerNgram;
	}

	
	@Test
	public void test__wordsContainingMorpheme__startOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingMorpheme(esCorpus, startMorphemesToTest));
		times.put("sql", time_wordsContainingMorpheme(sqlCorpus, startMorphemesToTest));
		SQLTestHelpers.assertIsFaster(
			"wordsContainingMorpheme__startOfWord", "sql", times);
	}

	@Test
	public void test__wordsContainingMorpheme__middleOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingMorpheme(esCorpus, middleMorphemesToTest));
		times.put("sql", time_wordsContainingMorpheme(sqlCorpus, middleMorphemesToTest));
//		SQLTestHelpers.assertAboutSameSpeed(
//			"wordsContainingMorpheme__middleOfWord", times, 0.05);
		SQLTestHelpers.assertIsFaster(
			"wordsContainingMorpheme_middleOfWord", "sql", times);
	}

	@Test
	public void test__wordsContainingMorpheme__endOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingMorpheme(esCorpus, endMorphemesToTest));
		times.put("sql", time_wordsContainingMorpheme(sqlCorpus, endMorphemesToTest));
		SQLTestHelpers.assertIsFaster(
			"wordsContainingMorpheme__endOfWord", "sql", times);
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
		SQLTestHelpers.assertIsFaster(
			"wordsContainingMorphNgram__startOfWord", "sql", times);
	}

	@Test
	public void test__wordsContainingMorphNgram__middleOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingMorphNgram(esCorpus, middleMorphNgramsToTest));
		times.put("sql", time_wordsContainingMorphNgram(sqlCorpus, middleMorphNgramsToTest));
		SQLTestHelpers.assertIsFaster(
			"wordsContainingMorphNgram__middleOfWord", "sql", times);
	}

	@Test
	public void test__wordsContainingMorphNgram__endOfWord() throws Exception {
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time_wordsContainingMorphNgram(esCorpus, endMorphNgramsToTest));
		times.put("sql", time_wordsContainingMorphNgram(sqlCorpus, endMorphNgramsToTest));
		// TODO: sql should be faster!
		SQLTestHelpers.assertIsFaster(
			"wordsContainingMorphNgram__endOfWord", "sql", times);
	}

	private Double time_wordsContainingMorphNgram(CompiledCorpus corpus,
		Collection<String[]> morphNgramsToTest) throws Exception {
		StopWatch sw = new StopWatch().start();
		for (String[] morphNgram: morphNgramsToTest) {
			Iterator<String> iter = corpus.wordsContainingMorphNgram(morphNgram);
			int countDown = 100;
			while (iter.hasNext()) {
				countDown--;
				if (countDown <= 0) {
					break;
				}
				String word = iter.next();
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
