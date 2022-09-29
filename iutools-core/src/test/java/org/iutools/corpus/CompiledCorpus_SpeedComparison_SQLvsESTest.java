package org.iutools.corpus;


import ca.nrc.data.file.ObjectStreamReader;
import org.apache.commons.lang3.ArrayUtils;
import org.iutools.config.IUConfig;
import org.iutools.corpus.elasticsearch.CompiledCorpus_ES;
import org.iutools.corpus.sql.CompiledCorpus_SQL;
import org.iutools.linguisticdata.LinguisticData;
import org.iutools.sql.SQLTestHelpers;
import org.iutools.utilities.StopWatch;
import org.iutools.worddict.GlossaryEntry;
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

	protected static List<String> startNgramsToTest = null;
	protected static List<String> endNgramsToTest = null;
	protected static List<String> middleNgramsToTest = null;

	protected static List<String> startMorphemesToTest = null;
	protected static List<String> middleMorphemesToTest = null;
	protected static List<String> endMorphemesToTest = null;
	
	protected static List<String[]> startMorphNgramsToTest = null;
	protected static List<String[]> middleMorphNgramsToTest = null;
	protected static List<String[]> endMorphNgramsToTest = null;

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

	private void generateMorphemesToTest() {
		if (startMorphemesToTest == null) {
			startMorphemesToTest = new ArrayList<String>();
			middleMorphemesToTest = new ArrayList<String>();
			endMorphemesToTest = new ArrayList<String>();
		}
		LinguisticData data = LinguisticData.getInstance();
		String[] allMorphemeIDs = data.allMorphemeIDs();
//		for (String morphID: allMorphemeIDs) {
//			startMorphemesToTest.add("^"+morphID);
//			middleMorphemesToTest.add(morphID);
//			endMorphemesToTest.add(morphID+"$");
//		}
		middleMorphemesToTest.add("mut/tn-dat-s");
	}

	private void generateMorphNgramsToTest() throws Exception {
		if (startMorphNgramsToTest == null) {
			startMorphNgramsToTest = new ArrayList<String[]>();
			middleMorphNgramsToTest = new ArrayList<String[]>();
			endMorphNgramsToTest = new ArrayList<String[]>();
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
		Map<String,Long> times = new HashMap<String,Long>();
		times.put("es", time_info4word(esCorpus));
		times.put("sql", time_info4word(sqlCorpus));
		// TODO: sql should be faster!
		SQLTestHelpers.assertIsFaster("info4word", "es", times);
	}

	private long time_info4word(CompiledCorpus corpus)
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
		Map<String,Long> times = new HashMap<String,Long>();
		times.put("es", time_wordsContainingNgram(esCorpus, startNgramsToTest));
		times.put("sql", time_wordsContainingNgram(sqlCorpus, startNgramsToTest));
		SQLTestHelpers.assertIsFaster("wordsContainingNgram__startOfWord", "sql", times);
	}

	@Test
	public void test__wordsContainingNgram__endOfWord() throws Exception {
		Map<String,Long> times = new HashMap<String,Long>();
		times.put("es", time_wordsContainingNgram(esCorpus, endNgramsToTest));
		times.put("sql", time_wordsContainingNgram(sqlCorpus, endNgramsToTest));
		SQLTestHelpers.assertIsFaster("wordsContainingNgram__endOfWord", "sql", times);
	}

	@Test
	public void test__wordsContainingNgram__middleOfWord() throws Exception {
		Map<String,Long> times = new HashMap<String,Long>();
		times.put("es", time_wordsContainingNgram(esCorpus, middleNgramsToTest));
		times.put("sql", time_wordsContainingNgram(sqlCorpus, middleNgramsToTest));
		SQLTestHelpers.assertIsFaster("wordsContainingNgram__middleOfWord", "sql", times);
	}

	private Long time_wordsContainingNgram(CompiledCorpus corpus,
		List<String> ngramsToTest) throws Exception {
		System.out.println("   Timing wordsContainingNgram with corpus="+corpus.getClass());
		StopWatch sw = new StopWatch().start();
		for (String ngram: ngramsToTest) {
			Iterator<String> iter = corpus.wordsContainingNgram(ngram);
			int countDown = 100;
			while (countDown > 0 && iter.hasNext()) {
				countDown--;
				String word = iter.next();
				int x = 1;
			}
		}
		int y = 1;
		return sw.lapTime(TimeUnit.MILLISECONDS);
	}

	
	@Test
	public void test__wordsContainingMorpheme__startOfWord() throws Exception {
		Map<String,Long> times = new HashMap<String,Long>();
		times.put("es", time_wordsContainingMorpheme(esCorpus, startMorphemesToTest));
		times.put("sql", time_wordsContainingMorpheme(sqlCorpus, startMorphemesToTest));
		SQLTestHelpers.assertIsFaster("wordsContainingMorpheme__startOfWord", "sql", times);
	}

	@Test
	public void test__wordsContainingMorpheme__middleOfWord() throws Exception {
		Map<String,Long> times = new HashMap<String,Long>();
		times.put("es", time_wordsContainingMorpheme(esCorpus, middleMorphemesToTest));
		times.put("sql", time_wordsContainingMorpheme(sqlCorpus, middleMorphemesToTest));
		SQLTestHelpers.assertIsFaster("wordsContainingMorpheme__middleOfWord", "sql", times);
	}

	@Test
	public void test__wordsContainingMorpheme__endOfWord() throws Exception {
		Map<String,Long> times = new HashMap<String,Long>();
		times.put("es", time_wordsContainingMorpheme(esCorpus, endMorphemesToTest));
		times.put("sql", time_wordsContainingMorpheme(sqlCorpus, endMorphemesToTest));
		SQLTestHelpers.assertIsFaster("wordsContainingMorpheme__endOfWord", "sql", times);
	}
	
	private Long time_wordsContainingMorpheme(CompiledCorpus corpus,
		List<String> morphemesToTest) throws Exception {
		StopWatch sw = new StopWatch().start();
		for (String morpheme: morphemesToTest) {
			List<WordWithMorpheme> words = corpus.wordsContainingMorpheme(morpheme);
		}
		return sw.lapTime(TimeUnit.MILLISECONDS);
	}

	@Test
	public void test__wordsContainingMorphNgram__startOfWord() throws Exception {
		Map<String,Long> times = new HashMap<String,Long>();
		times.put("es", time_wordsContainingMorphNgram(esCorpus, startMorphNgramsToTest));
		times.put("sql", time_wordsContainingMorphNgram(sqlCorpus, startMorphNgramsToTest));
		// TODO: sql should be faster!
		SQLTestHelpers.assertIsFaster(
			"wordsContainingMorphNgram__startOfWord", "es", times);
	}

	@Test
	public void test__wordsContainingMorphNgram__middleOfWord() throws Exception {
		Map<String,Long> times = new HashMap<String,Long>();
		times.put("es", time_wordsContainingMorphNgram(esCorpus, middleMorphNgramsToTest));
		times.put("sql", time_wordsContainingMorphNgram(sqlCorpus, middleMorphNgramsToTest));
		SQLTestHelpers.assertIsFaster(
			"wordsContainingMorphNgram__middleOfWord", "es", times);
	}

	@Test
	public void test__wordsContainingMorphNgram__endOfWord() throws Exception {
		Map<String,Long> times = new HashMap<String,Long>();
		times.put("es", time_wordsContainingMorphNgram(esCorpus, endMorphNgramsToTest));
		times.put("sql", time_wordsContainingMorphNgram(sqlCorpus, endMorphNgramsToTest));
		// TODO: sql should be faster!
		SQLTestHelpers.assertIsFaster(
			"wordsContainingMorphNgram__endOfWord", "es", times);
	}

	private Long time_wordsContainingMorphNgram(CompiledCorpus corpus,
		List<String[]> morphNgramsToTest) throws Exception {
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
		return sw.lapTime(TimeUnit.MILLISECONDS);
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
			startNgramsToTest = new ArrayList<String>();
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
			endNgramsToTest = new ArrayList<String>();
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
			middleNgramsToTest = new ArrayList<String>();
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
