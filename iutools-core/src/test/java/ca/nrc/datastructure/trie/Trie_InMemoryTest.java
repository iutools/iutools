package ca.nrc.datastructure.trie;

public class Trie_InMemoryTest extends TrieTest {

	@Override
	public Trie makeTrieToTest() {
		Trie trie = new Trie_InMemory();
		return trie;
	}

}
