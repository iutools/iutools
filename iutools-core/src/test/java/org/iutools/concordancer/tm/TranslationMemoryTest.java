package org.iutools.concordancer.tm;

import ca.nrc.file.ResourceGetter;
import org.iutools.concordancer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
		List<Alignment_ES> alignments =
			tm.search(sourceLang, sourceExpr, targetLangs);

		// Note that the list of target langauges is optional
		//
		alignments = tm.search(sourceLang, sourceExpr);
	}

	////////////////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////////////////


	@Test
	public void test__search__HappyPath() throws Exception {

		String sourceLang = "en";
		String sourceExpr = "legislative";
		String[] targetLangs = {"iu", "fr"};
		List<Alignment_ES> alignments =
			tm.search(sourceLang, sourceExpr, targetLangs);
		new AssertAlignment_ESList(alignments)
			.atLeastNHits(1)
			.allHitsMatchQuery(sourceLang, sourceExpr)
//			.includesTranslation("en", sourceExpr, "iu", "BLAH")
			;
	}
}
