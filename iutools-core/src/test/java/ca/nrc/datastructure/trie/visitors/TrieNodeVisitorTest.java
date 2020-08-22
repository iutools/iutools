package ca.nrc.datastructure.trie.visitors;

import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.testing.AssertObject;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

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
