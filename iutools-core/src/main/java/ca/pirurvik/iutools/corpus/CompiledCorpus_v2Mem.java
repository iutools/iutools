package ca.pirurvik.iutools.corpus;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.Trie_InFileSystem;
import ca.nrc.datastructure.trie.Trie_InMemory;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompiledCorpus_v2Mem extends CompiledCorpus_v2 {

    protected Set<Trie> staleTries = new HashSet<Trie>();

    public CompiledCorpus_v2Mem(File _corpusDir) {
        super(_corpusDir);
    }

    @Override
    protected Trie makeWordCharTrie() {
        Trie trie = new Trie_InMemory();
        return trie;
    }

    @Override
    protected Trie makeCharNgramsTrie() {
        Trie trie = new Trie_InMemory();
        return trie;
    }

    @Override
    protected Trie makeMorphNgramsTrie() {
        Trie trie = new Trie_InMemory();
        return trie;
    }

    @Override
    public void makeStale(Trie trie) throws CompiledCorpusException {
        staleTries.add(trie);
        return;
    }

    @Override
    public void makeNotStale(Trie trie) {
        staleTries.remove(trie);
    }

    @Override
    public boolean isStale(Trie trie) {
        boolean answer = staleTries.contains(trie);
        return answer;
    }
}
