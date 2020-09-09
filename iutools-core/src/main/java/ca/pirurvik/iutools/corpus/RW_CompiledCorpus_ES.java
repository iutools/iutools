package ca.pirurvik.iutools.corpus;

import ca.nrc.ui.commandline.UserIO;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RW_CompiledCorpus_ES extends RW_CompiledCorpus {
    static Pattern pattSavePath = Pattern.compile(".*?(^|[^/\\\\.]*)\\.ES\\.json$");

    @Override
    protected void writeCorpus(CompiledCorpus corpus, File savePath)
        throws CompiledCorpusException {

    }

    @Override
    protected CompiledCorpus readCorpus(File jsonFile) throws CompiledCorpusException {
        String corpusName = CompiledCorpus_ES.corpusName4File(jsonFile);
        CompiledCorpus_ES corpus =
                new CompiledCorpus_ES(corpusName);
        echo("Loading file "+jsonFile+
                " into ElasticSearch corpus "+corpusName);
        boolean verbose =
            (userIO != null &&
                userIO.verbosityLevelIsMet(UserIO.Verbosity.Level1));

        corpus.loadFromFile(jsonFile, verbose, false);

        return corpus;
    }

    @Override
    protected CompiledCorpus newCorpus(File savePath) throws CompiledCorpusException {
        String corpusName = CompiledCorpus_ES.corpusName4File(savePath);
        return new CompiledCorpus_ES(corpusName);
    }
}