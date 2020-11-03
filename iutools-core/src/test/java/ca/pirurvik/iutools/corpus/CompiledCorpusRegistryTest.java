package ca.pirurvik.iutools.corpus;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.testing.AssertObject;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;

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
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpusWithName_ES();
		
		//
		// If you want to get a specific corpus, do this:
		//
		String corpusName = CompiledCorpusRegistry.defaultESCorpusName;
		corpus = CompiledCorpusRegistry.getCorpusWithName_ES(corpusName);
		
//		//
//		// The registry comes with some pre-packaged corpora.
//		// You can however register your own corpora in it.
//		//
//		// Assuming that file jsonFile contains a JSON serialization of
//		// a CompiledCorpus object, then you can add a new corpus as
//		// follows
//		//
//		corpusName = "myCorpus";
//		CompiledCorpusRegistry.registerCorpus(corpusName, jsonFile);
//
//		//
//		// Once you have registered your own corpus, you can get an
//		// instance of it as usual
//		//
//		corpus = CompiledCorpusRegistry.getCorpus(corpusName);
	}
		
	//////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////

	@Test
	public void test__getCorpus__No_argument__Returns_default_corpus() 
			throws Exception {
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpusWithName_ES();
		String morpheme = "{amma/1c}";
		long gotFreq = corpus.morphemeNgramFrequency(new String[] {"{amma/1c}"});
		Assert.assertEquals("Incorrect number of words with morpheme "+morpheme,
				23417, gotFreq);
	}
	
	@Test
	public void test__getCorpus__RegisteredCorpus()
			throws Exception {
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpusWithName_ES(CompiledCorpusRegistry.defaultESCorpusName);
		String morpheme = "{amma/1c}";
		long gotFreq = corpus.morphemeNgramFrequency(new String[] {"{amma/1c}"});
		Assert.assertEquals("Incorrect number of words with morpheme "+morpheme,
				23417, gotFreq);
	}
	
	@Test
	public void test__getCorpus__get_from_unknown_corpus_name() {
		boolean errorCaught = false;
		try {
			CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus("blah");
		} catch (CompiledCorpusRegistryException e) {
			errorCaught = e.getMessage().contains("Unknown corpus name");
		}
		assertTrue("The exception 'Unknown corpus name' should have been thrown.",errorCaught);
	}
	
	@Test @Ignore
	public void test__getCorpus__get_from_custom_registered_corpus() throws Exception {
		String[] words = new String[] {"nunami","iglumik"};
		File corpusFile = CompiledCorpusTest.compileToFile(words);
		corpusFile.deleteOnExit();
		CompiledCorpusRegistry.registerCorpus("2words", corpusFile);
		CompiledCorpus_InMemory corpus = CompiledCorpusRegistry.getCorpus("2words");
		Trie trie = corpus.getTrie();
		TrieNode[] nunaTerminals = trie.getTerminals(new String[] {"{nuna/1n}"});
		int got = nunaTerminals.length;
		assertTrue("Incorrect number of terminals for nuna/1n;\nexpected more than 0",got>0);
	}
	
	@Test
	public void test__registerCorpus__already_defined() throws CompiledCorpusRegistryException, IOException {
		File tempFileA = File.createTempFile("file_A", ".json");
		CompiledCorpusRegistry.registerCorpus("XYZ", tempFileA);
			
		boolean allreadydefined = false;
		try {
			File tempFileB = File.createTempFile("file_B", ".json");
			CompiledCorpusRegistry.registerCorpus("XYZ", tempFileB);
		} catch (CompiledCorpusRegistryException e) {
			allreadydefined = e.getMessage().contains("is already associated");
		}
		assertTrue("The exception 'is already associated' should have been thrown.",allreadydefined);
	}
	
	// With this change, this test is identical to test__getCorpus__get_from_unknown_corpus_name()
	@Test(expected=CompiledCorpusRegistryException.class)
	public void test__getCorpus__UnknownCorpus__RaisesException() 
			throws Exception {
		String corpusName = "blabla";
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus(corpusName);
	}
}
