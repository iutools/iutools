package org.iutools.corpus;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CompiledCorpusRegistryTest {
	
	File jsonFile = null;
	
	@Before
	public void setUp() throws Exception {
		String[] words = new String[] {"nunami","iglumik"};
		jsonFile = CompiledCorpusTest.compileToFile(words);
	}
	
	//////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////

	@Test
	public void test__CompiledCorpusRegistry__Synopsis() throws Exception {
		//
		// Use this class to acquire instances of CompiledCorpus
		//
		// For example, to get the "default" corpus, do this:
		//
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpusWithName();
		
		//
		// If you want to get a specific corpus, do this:
		//
		String corpusName = CompiledCorpusRegistry.defaultCorpusName;
		corpus = CompiledCorpusRegistry.getCorpusWithName(corpusName);
	}
		
	//////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////

	@Test
	public void test__getCorpus__No_argument__Returns_default_corpus() 
			throws Exception {
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpusWithName();
		String morpheme = "{amma/1c}";
		long gotFreq = corpus.morphemeNgramFrequency(new String[] {"{amma/1c}"});
		Assert.assertEquals("Incorrect number of words with morpheme "+morpheme,
		23417, gotFreq);
	}
	
	@Test
	public void test__getCorpus__RegisteredCorpus()
			throws Exception {
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpusWithName(CompiledCorpusRegistry.defaultCorpusName);
		String morpheme = "{amma/1c}";
		long gotFreq = corpus.morphemeNgramFrequency(new String[] {"{amma/1c}"});
		Assert.assertEquals("Incorrect number of words with morpheme "+morpheme,
		23417, gotFreq);
	}
	
	@Test
	public void test__getCorpus__get_from_unknown_corpus_name() throws Exception {
		boolean errorCaught = false;
		try {
			CompiledCorpus corpus = CompiledCorpusRegistry.getCorpusWithName("blah");
		} catch (CompiledCorpusRegistryException e) {
			errorCaught = e.getMessage().contains("There is no corpus by the name of");
		}
		assertTrue("The exception 'Unknown corpus name' should have been thrown.",errorCaught);
	}

	@Test(expected=CompiledCorpusRegistryException.class)
	public void test__getCorpus__UnknownCorpus__RaisesException() 
			throws Exception {
		String corpusName = "blabla";
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpusWithName(corpusName);
	}
}
