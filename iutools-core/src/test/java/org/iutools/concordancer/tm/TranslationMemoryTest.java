package org.iutools.concordancer.tm;

import ca.nrc.file.ResourceGetter;
import static ca.nrc.testing.RunOnCases.*;

import ca.nrc.testing.RunOnCases;
import org.iutools.concordancer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

public abstract class TranslationMemoryTest {

	public abstract TranslationMemory makeTM(String memoryName);

	protected static final String esIndexName = "test_tm";

	protected TranslationMemory tm = null;

	@BeforeEach
	public void setUp() throws Exception {
		tm = makeTM(esIndexName);
		Path tmFile = Paths.get(
			ResourceGetter.getResourcePath(
				"org/iutools/concordancer/small_tm.tm.json"));
		tm.loadFile(tmFile);
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
		String[] targetLangs = {"iu", "fr"};
		List<Alignment> alignments =
			tm.search(sourceLang, sourceExpr, targetLangs);

		// Note that the list of target langauges is optional
		//
		alignments = tm.search(sourceLang, sourceExpr);
	}

	////////////////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////////////////


	@Test
	public void test__search__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new CaseSearch("en-official", "en", "official").setMinHits(1),
			new CaseSearch("iu-inuk", "iu", "inuk").setMinHits(1),
		};

		Consumer<Case> runner =
			(uncastCase) ->
			{
				try {
					CaseSearch aCase = (CaseSearch)uncastCase;
					String sourceLang = aCase.sourceLang;
					String sourceExpr = aCase.sourceExpr;

					String[] targetLangs = new String[] {"iu"};
					if (sourceLang.equals("iu")) {
						targetLangs = new String[] {"en"};
					}
					List<Alignment> alignments =
						tm.search(sourceLang, sourceExpr, targetLangs);
					new AssertAlignment_List(alignments)
						.atLeastNHits(aCase.minHits)
						.hitsMatchQuery(sourceLang, sourceExpr)
						;

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(2)
//			.onlyCasesWithDescr("en-SEARCH-housing")
			.run();

		return;
	}

	public static class CaseSearch extends Case {
		public String sourceLang = null;
		public Integer minHits = null;
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
	}
}
