package ca.nrc.datastructure.trie;

import ca.nrc.testing.AssertCollection;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertSet;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class NodesIteratorTest {

    protected abstract Trie makeTrie() throws IOException;

    Trie trie;

    @Before
    public void setUp() throws Exception {
        trie = makeTrie();
        for (String word: new String[] {"hello", "hell", "world"}) {
            trie.add(word.split(""), word);
        }
    }

    ///////////////////////////////////
    // DOCUMENTATION TESTS
    ///////////////////////////////////

    @Test
    public void test__NodesIterator__Synopsis() throws Exception {
        // Use this class to iterate through nodes of a Trie
        NodesIterator iter = new NodesIterator(trie);
        while (iter.hasNext()) {
            TrieNode node = iter.next();
        }
    }

    ///////////////////////////////////
    // VERIFICATION TESTS
    ///////////////////////////////////

    @Test
    public void test__NodesIterator__IterateFromRoot() throws Exception {
        NodesIterator iter = new NodesIterator(trie);
        String[] expNodes = new String[] {
            "h e l l o _$", "h e l l o", "h e l l", "h e l l _$", "h e l", "h e", "h",
            "w o r l d _$", "w o r l d", "w o r l", "w o r", "w o", "w"};
        assertIteratedNodesEqual("", iter, expNodes);
    }

    @Test
    public void test__NodesIterator__IterateFromWithingTrie() throws Exception {
        String[] startKeys = new String[] {"h", "e"};
        NodesIterator iter = new NodesIterator(trie, startKeys);
        String[] expNodes = new String[] {
                "h e l l o _$", "h e l l o", "h e l l", "h e l l _$", "h e l"};
        assertIteratedNodesEqual("", iter, expNodes);
    }

    @Test
    public void test__NodesIterator__EmptyTrie() throws Exception {
        Trie trie = makeTrie();
        String[] startKeys = new String[] {"h", "e"};
        NodesIterator iter = new NodesIterator(trie, startKeys);
        String[] emptyNodes = new String[0];
        assertIteratedNodesEqual("", iter, emptyNodes);
    }

    @Test
    public void test__NodesIterator__StartFromTerminalNode() throws Exception {
        Trie trie = makeTrie();
        String[] startKeys = new String[] {"h", "e", "l", "l", "o", "_$"};
        NodesIterator iter = new NodesIterator(trie, startKeys);
        String[] emptyNodes = new String[0];
        assertIteratedNodesEqual("", iter, emptyNodes);
    }

    ///////////////////////////////
    // HELPER METHODS
    ///////////////////////////////

    private void assertIteratedNodesEqual(String s, NodesIterator iter, String[] expNodes)
        throws Exception {
        Set<String> gotNodes = new HashSet<String>();
        while (iter.hasNext()) {
            TrieNode node = iter.next();
            gotNodes.add(node.keysAsString());
        }
        AssertSet.assertEquals(
            "Iterated nodes were not as expected",
            expNodes, gotNodes);

    }
}
