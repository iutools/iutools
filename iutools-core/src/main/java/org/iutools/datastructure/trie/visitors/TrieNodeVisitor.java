package org.iutools.datastructure.trie.visitors;

import org.iutools.datastructure.trie.TrieException;
import org.iutools.datastructure.trie.TrieNode;

public abstract class  TrieNodeVisitor {
	public abstract void visitNode(TrieNode node) throws TrieException;
}
