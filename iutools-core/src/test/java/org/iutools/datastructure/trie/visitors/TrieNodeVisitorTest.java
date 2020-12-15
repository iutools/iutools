package org.iutools.datastructure.trie.visitors;

import org.iutools.datastructure.trie.TrieException;
import org.iutools.datastructure.trie.TrieNode;

import java.util.HashSet;
import java.util.Set;

public class TrieNodeVisitorTest {

    public static class DummyVisitor extends TrieNodeVisitor {

        Set<String> visitedNodes = new HashSet<String>();

        @Override
        public void visitNode(TrieNode node) throws TrieException {
            visitedNodes.add(node.keysAsString());
        }
    }
}
