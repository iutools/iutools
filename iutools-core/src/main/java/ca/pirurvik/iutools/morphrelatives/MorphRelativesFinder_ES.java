package ca.pirurvik.iutools.morphrelatives;

import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.pirurvik.iutools.corpus.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Set;

public class MorphRelativesFinder_ES extends MorphRelativesFinder {

    public MorphRelativesFinder_ES() throws MorphRelativesFinderException {
        CompiledCorpus corpus = null;
        try {
            corpus = CompiledCorpusRegistry.getCorpusWithName_ES();
        } catch (CompiledCorpusRegistryException e) {
            throw new MorphRelativesFinderException(e);
        }
        init_MorphRelativeFinder_ES(corpus);
        return;
    }

    public MorphRelativesFinder_ES(CompiledCorpus corpus) throws MorphRelativesFinderException {
        init_MorphRelativeFinder_ES(corpus);
    }

    public void init_MorphRelativeFinder_ES(CompiledCorpus corpus) throws MorphRelativesFinderException {
        compiledCorpus = corpus;
        compiledCorpus.setSegmenterClassName(StringSegmenter_IUMorpheme.class);

        return;
    }

    @Override
    protected Boolean collectDescendants(String origWord,
        String[] origWordMorphemes, String[] currentMorphemes,
        Set<MorphologicalRelative> collectedSoFar) throws MorphRelativesFinderException {

        Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.morphrelatives.MorphRelativesFinder.collectDescendants");
        if (tLogger.isTraceEnabled()) {
            traceRelatives(tLogger, collectedSoFar, "Invoked with origWord=" + origWord + ", currentMorphemes=" + String.join(", ", currentMorphemes));
        }

        Boolean keepGoing = null;
        try {
            currentMorphemes = ArrayUtils.insert(0, currentMorphemes, "^");
            Iterator<String> iterDescendants = compiledCorpus.wordsContainingMorphNgram(currentMorphemes);
            while (iterDescendants.hasNext()) {
                String descendant = iterDescendants.next();
                if (!descendant.equals(origWord)) {
                    MorphologicalRelative neighbor =
                    neighbor =
                        word2neigbhor(origWord, origWordMorphemes,
                            descendant);
                    collectedSoFar.add(neighbor);
                }
            }
            if (collectedSoFar.size() > maxRelatives) {
                // We have collected as many neighbors as reequired
                // No more searching to be done
                keepGoing = false;
            }
        } catch (CompiledCorpusException e) {
            throw new MorphRelativesFinderException(e);
        }

        if (tLogger.isTraceEnabled()) {
            traceRelatives(tLogger, collectedSoFar, "Returning with  origWord=" + origWord);
        }

        return keepGoing;
    }

}
