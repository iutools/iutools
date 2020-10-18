package ca.pirurvik.iutools.morphrelatives;

import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpusException;
import ca.pirurvik.iutools.corpus.WordInfo;

import java.util.Iterator;
import java.util.Set;

public class MorphRelativesFinder_ES extends MorphRelativesFinder {
    public MorphRelativesFinder_ES() throws MorphRelativesFinderException {
    }

    public MorphRelativesFinder_ES(String corpusName) throws MorphRelativesFinderException {
        super(corpusName);
    }

    public MorphRelativesFinder_ES(CompiledCorpus _compiledCorpus) throws MorphRelativesFinderException {
        super(_compiledCorpus);
    }

    @Override
    protected Boolean collectDescendants(String origWord,
                                         String[] origWordMorphemes, String[] currentMorphemes, Set<MorphologicalRelative> collectedSoFar) throws MorphRelativesFinderException {

        Boolean keepGoing = null;
        try {
            Iterator<String> iterDescendants = compiledCorpus.wordsContainingMorphNgram(currentMorphemes);
            while (iterDescendants.hasNext()) {
                String descendant = iterDescendants.next();
                WordInfo descendantInfo = compiledCorpus.info4word(descendant);
                int x = 1;
//                if (!aWordNode.surfaceForm.equals(origWord)) {
//                    MorphologicalRelative neighbor =
//                            word2neigbhor(origWord, origWordMorphemes,
//                                    aWordNode.surfaceForm);
//                    collectedSoFar.add(neighbor);
//                }
//                for (String aSurfaceForm:
//                        aWordNode.getSurfaceForms().keySet()) {
//                    MorphologicalRelative neighbor =
//                            word2neigbhor(origWord, origWordMorphemes,
//                                    aSurfaceForm);
//                    if (!aSurfaceForm.equals(origWord)) {
//                        collectedSoFar.add(neighbor);
//                    }
//                }
//            }
//            if (collectedSoFar.size() > maxRelatives) {
//                // We have collected as many neighbors as reequired
//                // No more searching to be done
//                keepGoing = false;
//            }
            }
        } catch (CompiledCorpusException e) {
            throw new MorphRelativesFinderException(e);
        }

        return keepGoing;
    }

}
