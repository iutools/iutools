package org.iutools.corpus;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import ca.nrc.testing.AssertObject;
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
		CompiledCorpus corpus = new CompiledCorpusRegistry().getCorpus();
		
		//
		// If you want to get a specific corpus, do this:
		//
		String corpusName = CompiledCorpusRegistry.defaultCorpusName;
		corpus = new CompiledCorpusRegistry().getCorpus(corpusName);

		//
		// You can get the names of all available corpora
		//
		Set<String> corpora = CompiledCorpusRegistry.availableCorpora();
	}
		
	//////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////

	@Test
	public void test__getCorpus__No_argument__Returns_default_corpus() 
			throws Exception {
		CompiledCorpus corpus = new CompiledCorpusRegistry().getCorpus();
		String morpheme = "{amma/1c}";
		long gotFreq = corpus.morphemeNgramFrequency(new String[] {"{amma/1c}"});
		Assert.assertEquals("Incorrect number of words with morpheme "+morpheme,
		23417, gotFreq);
	}
	
	@Test
	public void test__getCorpus__RegisteredCorpus()
			throws Exception {
		CompiledCorpus corpus = new CompiledCorpusRegistry().getCorpus(CompiledCorpusRegistry.defaultCorpusName);
		String morpheme = "{amma/1c}";
		long gotFreq = corpus.morphemeNgramFrequency(new String[] {"{amma/1c}"});
		Assert.assertEquals("Incorrect number of words with morpheme "+morpheme,
		23417, gotFreq);
	}
	
	@Test
	public void test__getCorpus__get_from_unknown_corpus_name() throws Exception {
		boolean errorCaught = false;
		try {
			CompiledCorpus corpus = new CompiledCorpusRegistry().getCorpus("blah");
		} catch (CompiledCorpusRegistryException e) {
			errorCaught = e.getMessage().contains("There is no corpus by the name of");
		}
		assertTrue("The exception 'Unknown corpus name' should have been thrown.",errorCaught);
	}

	@Test(expected=CompiledCorpusRegistryException.class)
	public void test__getCorpus__UnknownCorpus__RaisesException() 
			throws Exception {
		String corpusName = "blabla";
		CompiledCorpus corpus = new CompiledCorpusRegistry().getCorpus(corpusName);
	}

	@Test
	public void test__availableCorpora__HappyPath() throws Exception {
		Set<String> gotCorpora = CompiledCorpusRegistry.availableCorpora();
		AssertObject.assertDeepEquals(
			"Available corpora not as expected",
			new String[] {"hansard-1999-2002"}, gotCorpora);
	}
}
