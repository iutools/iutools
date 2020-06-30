package ca.pirurvik.iutools.corpus;

import org.junit.Assert;

import ca.nrc.datastructure.trie.Trie_InFileSystem;

public class AssertCompiledCorpus_InFileSystem extends AssertCompiledCorpus {

	public AssertCompiledCorpus_InFileSystem(CompiledCorpus _gotCorpus, String mess) {
		super(_gotCorpus, mess);
	}

	public CompiledCorpus_InFileSystem corpus() {
		return (CompiledCorpus_InFileSystem) super.corpus();
	}
	
	public AssertCompiledCorpus_InFileSystem isNotStale(Trie_InFileSystem trie) {
		Assert.assertFalse(
			baseMessage+"Trie "+trie.getRootDir().getName()+" should NOT have been stale\n     dir="+trie.getRootDir(), 
			corpus().isStale(trie));
		return this;
	}

	public AssertCompiledCorpus_InFileSystem isStale(Trie_InFileSystem trie) {
		Assert.assertTrue(
				baseMessage+"Trie "+trie.getRootDir().getName()+" SHOULD have been stale\n     dir="+trie.getRootDir(), 
				corpus().isStale(trie));
		return this;
	}

}
