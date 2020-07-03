package ca.nrc.datastructure.trie.visitors;

import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;

public abstract class  TrieNodeVisitor {
	public abstract void visitNode(TrieNode node) throws TrieException;
}
