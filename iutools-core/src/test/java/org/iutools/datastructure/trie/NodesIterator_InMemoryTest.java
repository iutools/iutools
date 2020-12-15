package org.iutools.datastructure.trie;

public class NodesIterator_InMemoryTest extends NodesIteratorTest {
    @Override
    protected Trie makeTrie() {
        return new Trie_InMemory();
    }
}
