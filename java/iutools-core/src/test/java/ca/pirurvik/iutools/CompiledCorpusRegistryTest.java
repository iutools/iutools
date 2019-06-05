package ca.pirurvik.iutools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieNode;

public class CompiledCorpusRegistryTest {

	@Test
	public void test__getCorpus__No_argument_get_default() throws CompiledCorpusRegistryException {
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus();
		assertEquals("Incorrect name","default",corpus.name);
		Trie trie = corpus.getTrie();
		TrieNode[] ammaTerminals = trie.getAllTerminals(new String[] {"{amma/1c}"});
		int got = ammaTerminals.length;
		assertTrue("Incorrect number of terminals for amma/1c;\nexpected more than 0",got>0);
	}
	
	@Test
	public void test__getCorpus__get_from_file_path_and_corpus_name() throws Exception {
		String[] words = new String[] {"nunami","iglumik"};
		File corpusFile = CompiledCorpusTest.compileToFile(words);
		corpusFile.deleteOnExit();
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus(corpusFile,"2words");
		assertEquals("Incorrect name","2words",corpus.name);
		Trie trie = corpus.getTrie();
		TrieNode[] nunaTerminals = trie.getAllTerminals(new String[] {"{nuna/1n}"});
		int got = nunaTerminals.length;
		assertTrue("Incorrect number of terminals for nuna/1n;\nexpected more than 0",got>0);

		CompiledCorpus corpus2words = CompiledCorpusRegistry.getCorpus("2words");
		assertEquals("Incorrect name","2words",corpus2words.name);
		Trie trie2words = corpus2words.getTrie();
		TrieNode[] igluTerminals = trie.getAllTerminals(new String[] {"{iglu/1n}"});
		got = igluTerminals.length;
		assertTrue("Incorrect number of terminals for iglu/1n;\nexpected more than 0",got>0);
	}
	
	@Test
	public void test__getCorpus__get_from_corpus_name_statically_initialized() throws CompiledCorpusRegistryException {
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus("Hansard1999-2002");
		assertEquals("Incorrect name","Hansard1999-2002",corpus.name);
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
		assertTrue("The exception 'Unknown corpus name' should have been caught.",errorCaught);
	}

}
