package org.iutools.datastructure.trie.visitors;

import org.iutools.datastructure.trie.TrieException;
import org.iutools.datastructure.trie.TrieNode;

public class VisitorNodeCounter extends TrieNodeVisitor {
	
	public long nodesCount = 0;
	public long occurencesCount = 0;

	public VisitorNodeCounter() {
		init_VisitorNodeCounter();
	}
	
	private void init_VisitorNodeCounter() {
	}
	
	public void reset() {
		nodesCount = 0;
		occurencesCount = 0;
	}

	@Override
	public void visitNode(TrieNode node) throws TrieException {
		nodesCount++;
		occurencesCount += node.getFrequency();
	}
}
