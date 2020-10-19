package ca.pirurvik.iutools.morphrelatives;

import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpusException;

import java.util.Set;

public class MorphRelativesFinder_InMemory extends MorphRelativesFinder {

    public MorphRelativesFinder_InMemory() throws MorphRelativesFinderException {
    }

    public MorphRelativesFinder_InMemory(String corpusName) throws MorphRelativesFinderException {
        super(corpusName);
    }

    public MorphRelativesFinder_InMemory(CompiledCorpus _compiledCorpus) throws MorphRelativesFinderException {
        super(_compiledCorpus);
    }
}
