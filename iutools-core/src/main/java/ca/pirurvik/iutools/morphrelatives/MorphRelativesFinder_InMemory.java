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

    @Override
    protected Boolean collectDescendants(String origWord,
                                       String[] origWordMorphemes, String[] currentMorphemes, Set<MorphologicalRelative> collectedSoFar) throws MorphRelativesFinderException {

        Boolean keepGoing = null;
        try {
            TrieNode[] wordNodes =
                    compiledCorpus.getMorphNgramsTrie()
                            .getTerminals(currentMorphemes);
            for (TrieNode aWordNode: wordNodes) {
                if (!aWordNode.surfaceForm.equals(origWord)) {
                    MorphologicalRelative neighbor =
                            word2neigbhor(origWord, origWordMorphemes,
                                    aWordNode.surfaceForm);
                    collectedSoFar.add(neighbor);
                }
                for (String aSurfaceForm:
                        aWordNode.getSurfaceForms().keySet()) {
                    MorphologicalRelative neighbor =
                            word2neigbhor(origWord, origWordMorphemes,
                                    aSurfaceForm);
                    if (!aSurfaceForm.equals(origWord)) {
                        collectedSoFar.add(neighbor);
                    }
                }
            }
            if (collectedSoFar.size() > maxRelatives) {
                // We have collected as many neighbors as reequired
                // No more searching to be done
                keepGoing = false;
            }
        } catch (TrieException | CompiledCorpusException e) {
            throw new MorphRelativesFinderException(e);
        }

        return keepGoing;
    }

}
