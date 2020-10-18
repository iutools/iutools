package ca.pirurvik.iutools.morphrelatives;

import ca.pirurvik.iutools.corpus.CompiledCorpus;

public class MorphRelativesFinder_ES extends MorphRelativesFinder {
    public MorphRelativesFinder_ES() throws MorphRelativesFinderException {
    }

    public MorphRelativesFinder_ES(String corpusName) throws MorphRelativesFinderException {
        super(corpusName);
    }

    public MorphRelativesFinder_ES(CompiledCorpus _compiledCorpus) throws MorphRelativesFinderException {
        super(_compiledCorpus);
    }
}
