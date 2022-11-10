package org.iutools.concordancer.tm;

import ca.nrc.datastructure.CloseableIterator;
import ca.nrc.file.ResourceGetter;
import static ca.nrc.testing.RunOnCases.*;

import ca.nrc.testing.RunOnCases;
import org.iutools.concordancer.*;
import org.iutools.sql.SQLLeakMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public abstract class TranslationMemoryTest {

	public abstract TranslationMemory makeTM(String memoryName);

	protected static final String esIndexName = "test_tm";

	protected TranslationMemory tm = null;

	SQLLeakMonitor sqlLeakMonitor = null;

	@BeforeEach
	public void setUp() throws Exception {
		sqlLeakMonitor = new SQLLeakMonitor();
		tm = makeTM(esIndexName);
		Path tmFile = Paths.get(
			ResourceGetter.getResourcePath(
				"org/iutools/concordancer/small_tm.tm.json"));
		tm.loadFile(tmFile);
		return;
	}

	@AfterEach
	public void tearDown() throws Exception {
		tm.removeAligmentsFromDoc("test-doc");
		sqlLeakMonitor.assertNoLeaks("This test leaked some SQL resources");
		return;
	}

	////////////////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////////////////

	@Test
	public void test__TranslationMemory__Synopsis() throws Exception {
		// To create a TranslationMemory, you need to provide the name of
		// an elasticsearch index
		//
		String esIndexName = "test_tm";
		TranslationMemory tm = makeTM(esIndexName);

		// The es index is initially empty, but you can load data into
		// form JSON files
		//
		Path tmFile = Paths.get(ResourceGetter.getResourcePath("org/iutools/concordancer/small_tm.tm.json"));
		tm.loadFile(tmFile);

		// Assuming that a TM has been populated with some alignments, you can
		// then search for alignments that contain a particular expression
		//
		String sourceLang = "en";
		String sourceExpr = "legislative";
		String targetLang = "iu";
		// Note: We use try-with so that the TM data-store resources will be closed
		// when we are done.
		try (CloseableIterator<Alignment> iter =
			tm.search(sourceLang, sourceExpr, targetLang)) {
			while (iter.hasNext()) {
				Alignment alignment = iter.next();
			}
		}
	}

	////////////////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////////////////


	@Test
	public void test__search__VariousCases() throws Exception {
		Case[] cases = new Case[] {

			new CaseSearch("en-official", "en", "official").setMinHits(1),
			new CaseSearch("iu-sivuliqpaat-ROMAN", "iu", "sivuliqpaat").setMinHits(1),
			new CaseSearch("iu-sivuliqpaat-SYLL", "iu", "ᓯᕗᓕᖅᐹᑦ").setMinHits(1),
			new CaseSearch("iu-short-word", "iu", "ᐊᒥᖅ").setMinHits(1),
			new CaseSearch("en-short-word", "en", "speak").setMinHits(1),
			new CaseSearch("iu-hyphenatedword (covid-19)", "iu", "nuvagjuarnaq-19")
				// The term 'covid' is present, but NOT 'covid-19'
				.setMaxHits(0),
			new CaseSearch("iu-word-with-apostrophes (chicken)", "iu", "a'a'aak")
				// Term "aak" is present, but not "a'a'aak"
				.setMaxHits(0),
		};

		Consumer<Case> runner =
			(uncastCase) ->
			{
				try {
					CaseSearch aCase = (CaseSearch)uncastCase;
					String sourceLang = aCase.sourceLang;
					String sourceExpr = aCase.sourceExpr;

					String targetLang = "iu";
					if (sourceLang.equals("iu")) {
						targetLang = "en";
					}
					try (CloseableIterator<Alignment> alignsIter =
						  tm.search(sourceLang, sourceExpr, targetLang)) {

						AssertAlignment_Iter asserter =
							new AssertAlignment_Iter(alignsIter);
						if (aCase.minHits != null) {
							asserter.atLeastNHits(aCase.minHits);
						}
						if (aCase.maxHits != null) {
							asserter.atMostNHits(aCase.maxHits);
						}
						asserter.hitsMatchQuery(sourceLang, sourceExpr)
						;
					}

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(6)
//			.onlyCasesWithDescr("en-SEARCH-housing")
			.run();

		return;
	}

	public static class CaseSearch extends Case {
		public String sourceLang = null;
		public Integer minHits = null;
		public Integer maxHits = null;
		public String sourceExpr = null;

		public CaseSearch(String _descr, String _sourceLang, String _sourceExpr) {
			super(_descr, null);
			this.sourceLang = _sourceLang;
			this.sourceExpr = _sourceExpr;
		}

		public CaseSearch setMinHits(Integer _minHits) {
			this.minHits = _minHits;
			return this;
		}

		public CaseSearch setMaxHits(Integer _maxHits) {
			this.maxHits = _maxHits;
			return this;
		}
	}
}
