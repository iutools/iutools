package ca.nrc.datastructure.trie;

/**
 * High level information about a Trie
 */
public class TrieInfo {
    /**
     * Total number of expression occurences stored in the Trie.
     */
    Long totalOccurences = null;

    /**
     * Total number of terminal nodes in the Trie.
     */
    Long totalTerminals = null;

    /**
     * Time (as of System.currentTimeMillis()) at which we last updated a
     * terminal node. Node stats (ex: frequency) that were computed before
     * that time may have become stale.
     */
    long lastTerminalChangeTime = 0;
}
