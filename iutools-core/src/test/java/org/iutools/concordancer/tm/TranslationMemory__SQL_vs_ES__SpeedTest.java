package org.iutools.concordancer.tm;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.datastructure.CloseableIterator;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.tm.elasticsearch.TranslationMemory_ES;
import org.iutools.concordancer.tm.sql.TranslationMemory_SQL;
import org.iutools.config.IUConfig;
import org.iutools.sql.SQLTestHelpers;
import static org.iutools.sql.SQLTestHelpers.TimingResults;
import org.iutools.utilities.StopWatch;
import org.iutools.worddict.GlossaryEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Compare the speed of the SQL vs ES based TM for various operations.
 */
@Disabled
public class TranslationMemory__SQL_vs_ES__SpeedTest {

	private static List<String> enWords = null;
	private static List<String> iuWords = null;

	@BeforeEach
	public void setUp() throws Exception {
		if (enWords == null) {
			enWords = new ArrayList<String>();
			iuWords = new ArrayList<String>();
			String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.gloss.json");
			ObjectStreamReader reader =
				new ObjectStreamReader(new File(glossaryPath));
			int entryNum = 0;
			while (true) {
				GlossaryEntry entry = (GlossaryEntry) reader.readObject();
				String enExpr = entry.firstTerm4Lang("en");
				String iuExpr = entry.firstTerm4Lang("iu_roman");
				// Make sure the IU and EN expressions have only one word
				if (enWords.size() <= 20 && !enExpr.contains(" ")) {
					enWords.add(enExpr);
				}
				if (iuWords.size() <= 20 && !iuExpr.contains(" ")) {
					iuWords.add(iuExpr);
				}
				if (enWords.size() >= 20 && iuWords.size() > 20) {
					break;
				}
			}
		}
	}

	@Test
	public void test__search__EN_words() throws Exception {
		Map<String, TimingResults> times = new HashMap<String,TimingResults>();
		TranslationMemory_ES tmES = new TranslationMemory_ES();
		times.put("es", time_search(tmES, "en", "iu", enWords));
		TranslationMemory_SQL tmSQL = new TranslationMemory_SQL();
		times.put("sql", time_search(tmSQL, "en", "iu", enWords));

		// This is a HUGE performance loss (5x slower)!
		// But it doesn't matter since we have dropped SQL for the TM and
		// will continue using ES for that component (eventhough we use SQL for
		// the compiled corpus).
		//
		double tolerance = 5.0;
		SQLTestHelpers.assertSqlNotSignificantlySlowerThanES_NEW("search en terms",
			times, tolerance);
	}


	@Test
	public void test__search__IU_words() throws Exception {
		Map<String,TimingResults> times = new HashMap<String,TimingResults>();
		TranslationMemory_ES tmES = new TranslationMemory_ES();
		times.put("es", time_search(tmES, "iu", "en", iuWords));
		TranslationMemory_SQL tmSQL = new TranslationMemory_SQL();
		times.put("sql", time_search(tmSQL, "iu", "en", iuWords));

		// This is a HUGE performance loss (20x slower)!
		// But it doesn't matter since we have dropped SQL for the TM and
		// will continue using ES for that component (eventhough we use SQL for
		// the compiled corpus).
		//
		double tolerance = 20.0;
		SQLTestHelpers.assertSqlNotSignificantlySlowerThanES_NEW("search iu terms",
			times, tolerance);
	}

	private TimingResults time_search(TranslationMemory tm, String sourceLang, String targetLang,
		Collection<String> expressionsToSearch) throws Exception {
		System.out.println("   Timing 'search' "+sourceLang+" terms with tm="+tm.getClass().getSimpleName());
		TimingResults results = new TimingResults();
		StopWatch sw = new StopWatch().start();
		for (String sourceExpr: expressionsToSearch) {
			List<Alignment> alignments = new ArrayList<Alignment>();
			try (CloseableIterator<Alignment> iter = tm.search(sourceLang, sourceExpr, targetLang)) {
				int countDown = 100;
				while (countDown > 0 && iter.hasNext()) {
					countDown--;
					alignments.add(iter.next());
				}
				long caseMsecs = sw.lapTime(TimeUnit.MILLISECONDS);
				results.onNewCase(sourceExpr, caseMsecs);
//				System.out.println("  '"+sourceExpr+"' took "+caseMsecs+" msecs");
			}
//			System.out.println("  '"+sourceExpr+"' yielded "+alignments.size()+" alignments");
		}
		return results;
	}


}
