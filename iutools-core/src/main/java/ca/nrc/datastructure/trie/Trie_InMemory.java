package ca.nrc.datastructure.trie;

public class Trie_InMemory extends Trie {

	@Override
	protected TrieNode makeNode(String[] keys, Boolean isWord) {
		return new TrieNode(keys, isWord);
	}
}
