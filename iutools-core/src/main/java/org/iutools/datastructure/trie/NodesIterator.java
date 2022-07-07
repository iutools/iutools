package org.iutools.datastructure.trie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iutools.datastructure.trie.NodesIterationState.Step;

/**
 * Iterates through nodes of a Trie
 */
public class NodesIterator implements Iterator<TrieNode> {

    private Trie trie;
    private NodesIterationState state = null;

    public NodesIterator(Trie _trie) throws TrieException {
        init_NodesIterator(_trie, new String[0]);
    }

    public NodesIterator(Trie _trie, String[] startKeys) throws TrieException {
        init_NodesIterator(_trie, startKeys);
    }

    private void init_NodesIterator(Trie _trie, String[] _startKeys)
            throws TrieException {
        this.trie = _trie;
        this.state = new NodesIterationState(_startKeys);
        stepUntilNextNode();
    }

    @Override
    public boolean hasNext() {
        return (state.nextNodeKeys != null);
    }

    @Override
    public TrieNode next() {
        Logger tLogger = LogManager.getLogger("ca.nrc.datastructure.trie.NodesIterator.next");
        TrieNode node = null;
        try {
            node = trie.node4keys(state.nextNodeKeys);
            state.nextNodeKeys = null;
            if (state.nextStep != Step.DONE) {
                stepUntilNextNode();
            }
        } catch (TrieException e) {
            throw new RuntimeException(e);
        }

        if (tLogger.isTraceEnabled()) {
            tLogger.trace("Returning node="+node.keysAsString());
        }

        return node;
    }

    private void stepUntilNextNode() throws TrieException {
        while (state.nextStep != Step.DONE && state.nextNodeKeys == null) {
            doStep(state);
        }
    }
    
    private void doStep(NodesIterationState state)
            throws TrieException {
        Logger tLogger = LogManager.getLogger("ca.nrc.datastructure.trie.NodesIterator.doStep");

        Step step = state.nextStep;

        if (tLogger.isTraceEnabled()) {
            tLogger.trace("Upon entry, state is\n"+state.toString());
        }

        if (step == Step.EXTEND_CHOICE_TREE) {
            doExtendChoiceTree(state);
        } else if (step == Step.BACKTRACK) {
            doBacktrack(state);
        } else if (step == Step.MOVE_DEEPEST_CURSOR) {
            doMoveDeepestCursor(state);
        } else {
            throw new TrieException("Unsupported step "+step);
        }

        state.prevStep = step;

        if (tLogger.isTraceEnabled()) {
            tLogger.trace("Upon exit, state is\n"+state.toString());
        }

        return;
    }

    /**
     * Change the key selected for the deepest level of the choice tree
     *
     * @param state
     */
    private void doMoveDeepestCursor(NodesIterationState state) {
        Step next = null;
        boolean success = state.moveDeepestLevelCursor();
        if (success) {
            // The deepest level still had one option.
            // Next step is to extend the tree from that next option.
            //
            state.nextNodeKeys = state.currentNodeKeys();
            next = Step.EXTEND_CHOICE_TREE;
        } else {
            // The deepest level did not have any more options.
            // Next step is therefore to extend with choices at the next
            //
            next = Step.BACKTRACK;
        }

        state.nextStep = next;
    }

    /**
     * Backtrack in the choice tree, until we find a level that still has
     * some options.
     *
     * @param state
     */
    private void doBacktrack(NodesIterationState state) {
        state.removeDepeestLevel();

        if (state.choiceTree.isEmpty()) {
            // We have backtracked to the very first level and there were
            // no more choices there.
            //
            state.nextStep = Step.DONE;
        } else {
            state.nextStep = Step.MOVE_DEEPEST_CURSOR;
        }
    }

    /**
     * Add a new level in the choice tree.
     *
     * @param state
     * @throws TrieException
     */
    private void doExtendChoiceTree(NodesIterationState state)
            throws TrieException {
        Logger tLogger = LogManager.getLogger("ca.nrc.datastructure.trie.NodesIterator.doExtendChoiceTree");

        if (tLogger.isTraceEnabled()) {
            tLogger.trace("Upon entry, state=\n"+state.toString());
        }

        state.extendChoiceTree(nextLevelChoices(state));
        state.nextStep = Step.MOVE_DEEPEST_CURSOR;
    }

    /**
     * Generate a list of options for the next level in the choice tree.
     *
     * @param state
     * @return
     * @throws TrieException
     */
    private List<String> nextLevelChoices(NodesIterationState state)
            throws TrieException {

        TrieNode node = currrentLevelNode(state);
        List<String> choices = new ArrayList<String>();
        choices.addAll(node.children.keySet());
        return choices;
    }

    private TrieNode currrentLevelNode(NodesIterationState state) throws TrieException {
        List<String> keys = state.currentNodeKeys();
        TrieNode node = trie.node4keys(keys);
        return node;
    }
}
