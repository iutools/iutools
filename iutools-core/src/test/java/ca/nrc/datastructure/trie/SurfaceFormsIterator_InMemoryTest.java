package ca.nrc.datastructure.trie;

public class SurfaceFormsIterator_InMemoryTest extends SurfaceFormsIteratorTest{
    @Override
    protected Trie makeTrie() throws Exception {
        return new Trie_InMemory();
    }
}
