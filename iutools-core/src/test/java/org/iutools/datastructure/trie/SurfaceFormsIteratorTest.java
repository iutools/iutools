package org.iutools.datastructure.trie;

import ca.nrc.testing.AssertSet;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public abstract class SurfaceFormsIteratorTest {
    protected abstract Trie makeTrie() throws Exception;

    Trie trie = null;

    @Before
    public void setUp() throws Exception {
        trie = makeTrie();
        for (String word: new String[] {"hello", "world"}) {
            trie.add(word.split(""), word);
        }
        // Add a second surface form for "hello"
        trie.add("hello".split(""), "HELLO");
    }

    //////////////////////////////////////
    // DOCUMENTATION TESTS
    //////////////////////////////////////

    @Test
    public void test__SurfaceFormsIterator__Synopsis() throws Exception {
        // Use this to iterate through the surface form of all
        // expressions stored in a Trie
        SurfaceFormsIterator iterator = new SurfaceFormsIterator(trie);
        while (iterator.hasNext()) {
            String aSurfaceForm = iterator.next();
        }
    }

    ///////////////////////////////////
    // VERIFICATION TESTS
    ///////////////////////////////////

    @Test
    public void test__SurfaceFormsIterator__IterateFromRoot() throws Exception {
        SurfaceFormsIterator iter = new SurfaceFormsIterator(trie);
        String[] expForms = new String[] {
                "hello", "HELLO", "world"};
        assertIteratedFormsEqual("", iter, expForms);
    }

    @Test
    public void test__SurfaceFormsIterator__IterateFromWithingTrie() throws Exception {
        String[] startKeys = new String[] {"h", "e"};
        SurfaceFormsIterator iter = new SurfaceFormsIterator(trie, startKeys);
        String[] expForms = new String[] {"hello", "HELLO"};
        assertIteratedFormsEqual("", iter, expForms);
    }

    @Test
    public void test__SurfaceFormsIterator__EmptyTrie() throws Exception {
        Trie trie = makeTrie();
        String[] startKeys = new String[] {"h", "e"};
        SurfaceFormsIterator iter = new SurfaceFormsIterator(trie, startKeys);
        String[] emptyNodes = new String[0];
        assertIteratedFormsEqual("", iter, emptyNodes);
    }

    @Test
    public void test__SurfaceFormsIterator__StartFromTerminalNode() throws Exception {
        Trie trie = makeTrie();
        String[] startKeys = new String[] {"h", "e", "l", "l", "o", "_$"};
        SurfaceFormsIterator iter = new SurfaceFormsIterator(trie, startKeys);
        String[] emptyNodes = new String[0];
        assertIteratedFormsEqual("", iter, emptyNodes);
    }

    ///////////////////////////////
    // HELPER METHODS
    ///////////////////////////////

    private void assertIteratedFormsEqual(String s, SurfaceFormsIterator iter, String[] expForms)
            throws Exception {
        Set<String> gotForms = new HashSet<String>();
        while (iter.hasNext()) {
            String form = iter.next();
            gotForms.add(form);
        }
        AssertSet.assertEquals(
                "Iterated surface forms were not as expected",
                expForms, gotForms);

    }}
