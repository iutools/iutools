package ca.pirurvik.iutools.corpus;

import ca.nrc.datastructure.trie.Trie;
import org.junit.Assert;

import ca.nrc.datastructure.trie.Trie_InFileSystem;

public class AssertCompiledCorpus_v2 extends AssertCompiledCorpus {

	public AssertCompiledCorpus_v2(CompiledCorpus _gotCorpus, String mess) {
		super(_gotCorpus, mess);
	}

	public CompiledCorpus_v2 corpus() {
		return (CompiledCorpus_v2) super.corpus();
	}
	
	public AssertCompiledCorpus_v2 isNotStale(Trie trie) {
		Assert.assertFalse(
			baseMessage+"Trie should NOT have been stale",
			corpus().isStale(trie));
		return this;
	}

	public AssertCompiledCorpus_v2 isStale(Trie trie) {
		Assert.assertTrue(
				baseMessage+"Trie SHOULD have been stale",
				corpus().isStale(trie));
		return this;
	}

}
