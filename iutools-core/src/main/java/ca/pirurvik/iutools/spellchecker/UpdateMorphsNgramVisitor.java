package ca.pirurvik.iutools.spellchecker;

import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.datastructure.trie.Trie_InMemory;
import ca.nrc.datastructure.trie.visitors.TrieNodeVisitor;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;

public class UpdateMorphsNgramVisitor extends TrieNodeVisitor {


    private Trie_InMemory trie = null;
    private long totalOcccurences = -1;
    ProgressMonitor_Terminal progMonitor = null;

    public UpdateMorphsNgramVisitor(
            Trie_InMemory _trie, long _totalOccurences) {
        init_UpdateMorphsNgramVisitor(_trie, _totalOccurences, null);
    }

    public UpdateMorphsNgramVisitor(
        Trie_InMemory _trie, long _totalOccurences, Boolean displayProgress) {
        init_UpdateMorphsNgramVisitor(_trie, _totalOccurences, displayProgress);
    }

    private void init_UpdateMorphsNgramVisitor(
        Trie_InMemory _trie, long _totalOccurences, Boolean displayProgress) {
        this.trie = _trie;
        this.totalOcccurences = _totalOccurences;
        if (displayProgress != null && displayProgress) {
            progMonitor = new ProgressMonitor_Terminal(
                totalOcccurences, "Generation InMemory version of the morpheme ngrams trie");
        }
    }

    @Override
    public void visitNode(TrieNode node) throws TrieException {
        if (!node.isTerminal()) {
            throw new TrieException("Node "+node.key()+" is NOT terminal");
        }

        long freq = node.getFrequency();
        if (progMonitor != null) {
            progMonitor.stepCompleted(node.getFrequency());
        }
    }
}
