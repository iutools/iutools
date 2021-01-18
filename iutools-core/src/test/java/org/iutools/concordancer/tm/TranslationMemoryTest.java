package org.iutools.concordancer.tm;

import ca.nrc.file.ResourceGetter;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.DocAlignment;
import org.iutools.concordancer.tm.TranslationMemory;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

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

		// The es index is initially empty, but you can load data into
		// form JSON files
		//
		Path tmFile = Paths.get(ResourceGetter.getResourcePath("org/iutools/concordancer/small_tm.tm.json"));
		tm.loadFile(tmFile);

		// Assume that this alignment is the 1st sentence pair in a document
		// called "Simple lexicon.txt"
		//
		Alignment_ES alignment =
			new Alignment_ES("Simple lexicon.txt", 1)
			.setSentence("en", "Hello world.")
			.setSentence("fr", "Bonjour le monde.")
			;
		tm.addAlignment(alignment);
	}
}
