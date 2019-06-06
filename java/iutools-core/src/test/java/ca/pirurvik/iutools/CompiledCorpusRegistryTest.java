package ca.pirurvik.iutools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieNode;

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
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus();
		
		//
		// If you want to get a specific corpus, do this:
		//
		String corpusName = "Hansard1999-2002";
		corpus = CompiledCorpusRegistry.getCorpus(corpusName);
		
		//
		// The registry comes with some pre-packaged corpora.
		// You can however register your own corpora in it.
		// 
		// Assuming that file jsonFile contains a JSON serialization of 
		// a CompiledCorpus object, then you can add a new corpus as 
		// follows
		//
		corpusName = "myCorpus";
		CompiledCorpusRegistry.registerCorpus(corpusName, jsonFile);
		
		// 
		// Once you have registered your own corpus, you can get an
		// instance of it as usual
		//
		corpus = corpus = CompiledCorpusRegistry.getCorpus(corpusName);
	}
	
	
	//////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////

	@Test
	public void test__getCorpus__No_argument__Returns_default_corpus() throws CompiledCorpusRegistryException {
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus();
		Trie trie = corpus.getTrie();
		TrieNode[] ammaTerminals = trie.getAllTerminals(new String[] {"{amma/1c}"});
		int got = ammaTerminals.length;
		assertTrue("Incorrect number of terminals for amma/1c;\nexpected more than 0",got>0);
	}
	
//	@Test
//	public void test__getCorpus__get_from_file_path() throws Exception {
//		String[] words = new String[] {"nunami","iglumik"};
//		File corpusFile = CompiledCorpusTest.compileToFile(words);
//		corpusFile.deleteOnExit();
//		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus(corpusFile);
//		Trie trie = corpus.getTrie();
//		TrieNode[] nunaTerminals = trie.getAllTerminals(new String[] {"{nuna/1n}"});
//		int got = nunaTerminals.length;
//		assertTrue("Incorrect number of terminals for nuna/1n;\nexpected more than 0",got>0);
//		
//		Map<String,CompiledCorpus> corpusCache = CompiledCorpusRegistry.getCorpusCache();
//		assertTrue("The registry does not contain the corpus specified by the file path.",corpusCache.containsKey("FILE="+corpusFile));
//
//		CompiledCorpus corpus2words = CompiledCorpusRegistry.getCorpus(corpusFile);
//		Trie trie2words = corpus2words.getTrie();
//		TrieNode[] igluTerminals = trie2words.getAllTerminals(new String[] {"{iglu/1n}"});
//		got = igluTerminals.length;
//		assertTrue("Incorrect number of terminals for iglu/1n;\nexpected more than 0",got>0);
//	}
	
	@Test
	public void test__getCorpus__get_from_corpus_name_statically_initialized() throws CompiledCorpusRegistryException {
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus("Hansard1999-2002");
		Trie trie = corpus.getTrie();
		TrieNode[] ammaTerminals = trie.getAllTerminals(new String[] {"{amma/1c}"});
		int got = ammaTerminals.length;
		assertTrue("Incorrect number of terminals for amma/1c;\nexpected more than 0",got>0);
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
	
	@Test
	public void test__getCorpus__get_from_custom_registered_corpus() throws Exception {
		String[] words = new String[] {"nunami","iglumik"};
		File corpusFile = CompiledCorpusTest.compileToFile(words);
		corpusFile.deleteOnExit();
		CompiledCorpusRegistry.registerCorpus("2words", corpusFile);
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus("2words");
		Trie trie = corpus.getTrie();
		TrieNode[] nunaTerminals = trie.getAllTerminals(new String[] {"{nuna/1n}"});
		int got = nunaTerminals.length;
		assertTrue("Incorrect number of terminals for nuna/1n;\nexpected more than 0",got>0);
	}
	
	@Test
	public void test__registerCorpus__already_defined() throws CompiledCorpusRegistryException {
		CompiledCorpusRegistry.registerCorpus("XYZ", new File("file_A"));
		
		boolean allreadydefined = false;
		try {
			CompiledCorpusRegistry.registerCorpus("XYZ", new File("file_B"));
		} catch (CompiledCorpusRegistryException e) {
			allreadydefined = e.getMessage().contains("is already associated");
		}
		assertTrue("The exception 'is already associated' should have been thrown.",allreadydefined);
	}
	


}
