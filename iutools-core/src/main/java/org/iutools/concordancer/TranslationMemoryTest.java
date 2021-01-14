package org.iutools.concordancer;

import org.junit.jupiter.api.Test;

public class TranslationMemoryTest {

	////////////////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////////////////

	@Test
	public void test__TranslationMemory__Synopsis() throws Exception {
		// To create a TranslationMemory, you need to provide the name of
		// an elasticsearch index
		//
		String esIndexName = "test_tm";
		TranslationMemory tm = new TranslationMemory(esIndexName);

		// The translation memory stores aligned documents.
		// Each aligned document corresponds to the different language versions
		// of a same document, with alignment information (sentence level and
		// possibly token level alingnment)
		//
		// In this particular case, the doc is available in three languages:
		// English, French and Inuktitut.
		//
		DocAlignment docAl =
			new DocAlignment("en", "fr", "iu")
			.setPageMainText("en", "hello world")
			.setPageMainText("fr", "bonjour le monde")
			.setPageMainText("iu", "ai nunavut")
			;


	}
}
