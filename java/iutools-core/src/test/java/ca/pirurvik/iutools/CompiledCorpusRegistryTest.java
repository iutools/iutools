package ca.pirurvik.iutools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.junit.Test;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieNode;

public class CompiledCorpusRegistryTest {

	@Test
	public void test__getCorpus__No_argument_get_default() throws CompiledCorpusRegistryException {
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus();
		Trie trie = corpus.getTrie();
		TrieNode[] ammaTerminals = trie.getAllTerminals(new String[] {"{amma/1c}"});
		int got = ammaTerminals.length;
		assertTrue("Incorrect number of terminals for amma/1c;\nexpected more than 0",got>0);
	}
	
	@Test
	public void test__getCorpus__get_from_file_path() throws Exception {
		String[] words = new String[] {"nunami","iglumik"};
		File corpusFile = CompiledCorpusTest.compileToFile(words);
		corpusFile.deleteOnExit();
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus(corpusFile);
		Trie trie = corpus.getTrie();
		TrieNode[] nunaTerminals = trie.getAllTerminals(new String[] {"{nuna/1n}"});
		int got = nunaTerminals.length;
		assertTrue("Incorrect number of terminals for nuna/1n;\nexpected more than 0",got>0);
		
		Map<String,CompiledCorpus> corpusCache = CompiledCorpusRegistry.getCorpusCache();
		assertTrue("The registry does not contain the corpus specified by the file path.",corpusCache.containsKey("FILE="+corpusFile));

		CompiledCorpus corpus2words = CompiledCorpusRegistry.getCorpus(corpusFile);
		Trie trie2words = corpus2words.getTrie();
		TrieNode[] igluTerminals = trie2words.getAllTerminals(new String[] {"{iglu/1n}"});
		got = igluTerminals.length;
		assertTrue("Incorrect number of terminals for iglu/1n;\nexpected more than 0",got>0);
	}
	
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
	public void test__getCorpus__get_from_added_corpus() throws Exception {
		String[] words = new String[] {"nunami","iglumik"};
		File corpusFile = CompiledCorpusTest.compileToFile(words);
		corpusFile.deleteOnExit();
		CompiledCorpusRegistry.addCorpus("2words", corpusFile.getAbsolutePath());
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus("2words");
		Trie trie = corpus.getTrie();
		TrieNode[] nunaTerminals = trie.getAllTerminals(new String[] {"{nuna/1n}"});
		int got = nunaTerminals.length;
		assertTrue("Incorrect number of terminals for nuna/1n;\nexpected more than 0",got>0);
	}
	
	@Test
	public void test__addCorpus__already_defined() throws CompiledCorpusRegistryException {
		CompiledCorpusRegistry.addCorpus("XYZ", "file_A");
		
		boolean allreadydefined = false;
		try {
			CompiledCorpusRegistry.addCorpus("XYZ", "file_B");
		} catch (CompiledCorpusRegistryException e) {
			allreadydefined = e.getMessage().contains("is already associated");
		}
		assertTrue("The exception 'is already associated' should have been thrown.",allreadydefined);
	}
	
	@Test
	public void test__addCorpus__make_it_default() throws CompiledCorpusRegistryException {
		CompiledCorpusRegistry.addCorpus("XYZ", "file_A", true);
		Map<String,String> registry = CompiledCorpusRegistry.getRegistry();
		assertEquals("The added corpus has not been set as the default corpus.","file_A",registry.get("default"));
	}
	


}
