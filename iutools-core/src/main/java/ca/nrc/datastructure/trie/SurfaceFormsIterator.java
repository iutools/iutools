package ca.nrc.datastructure.trie;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * Iterates through a Trie's surface forms.
 */
public class SurfaceFormsIterator implements Iterator<String> {

    NodesIterator nodesIterator = null;
    Iterator<String> currNodeFormsIterator = null;

    public SurfaceFormsIterator(Trie trie) throws TrieException {
        this.nodesIterator = new NodesIterator(trie);
    }

    public SurfaceFormsIterator(Trie trie, String[] startKeys) throws TrieException {
        init_SurfaceFormsIterator(trie, startKeys);
    }

    private void init_SurfaceFormsIterator(Trie trie, String[] startKeys) throws TrieException {
        this.nodesIterator = new NodesIterator(trie, startKeys);
    }

    @Override
    public boolean hasNext() {
        boolean answer = true;
        if (!nodesIterator.hasNext() &&
                (currNodeFormsIterator == null ||
                 !currNodeFormsIterator.hasNext()))
        {
            answer = false;
        }
        return answer;
    }

    @Override
    public String next() {
        Logger tLogger = Logger.getLogger("ca.nrc.datastructure.trie.SurfaceFormsIterator.next");
        String nextForm = null;
        if (currNodeFormsIterator == null || !currNodeFormsIterator.hasNext()) {
            currNodeFormsIterator = null;
            if (nodesIterator.hasNext()) {
                while (true) {
                    TrieNode currNode = nodesIterator.next();
                    if (!currNode.isTerminal()) {
                        continue;
                    }
                    currNodeFormsIterator = currNode.surfaceForms.keySet().iterator();
                    break;
                }
            }
        }

        if (currNodeFormsIterator != null) {
            nextForm = currNodeFormsIterator.next();
        }

        tLogger.trace("Returning nextForm="+nextForm);
        return nextForm;
    }
}
