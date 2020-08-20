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
     * If true, it means that node's aggregate stats about their descendantes
     * (ex: total occurences) is out of date and needs to be recomputed.
     */
    boolean aggregateStatsAreStale = false;
}
