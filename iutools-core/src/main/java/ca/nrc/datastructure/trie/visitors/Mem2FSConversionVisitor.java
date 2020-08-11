package ca.nrc.datastructure.trie.visitors;

import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.datastructure.trie.Trie_InFileSystem;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;

public class Mem2FSConversionVisitor extends TrieNodeVisitor {


    long totalWords = -1;
    Trie_InFileSystem fsTrie = null;
    ProgressMonitor_Terminal progMonitor = null;

    public Mem2FSConversionVisitor(
        long _totalWords, Trie_InFileSystem _fsTrie) throws TrieException {

        this.fsTrie = _fsTrie;
        this.totalWords = _totalWords;
        progMonitor = new ProgressMonitor_Terminal(_totalWords, "Converting InMemory trie to InFileSystem trie", 60);
    }

    @Override
    public void visitNode(TrieNode memNode) throws TrieException {
        if (!memNode.isTerminal()) {
            throw new TrieException("Node "+memNode.key()+" is not terminal");
        }

        if (progMonitor != null) {
            progMonitor.stepCompleted();
        }

        fsTrie.saveNode(memNode);
    }
}
