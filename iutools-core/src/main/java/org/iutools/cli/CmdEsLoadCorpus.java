package org.iutools.cli;

import ca.nrc.ui.commandline.UserIO;
import org.iutools.corpus.CompiledCorpus;

import java.io.File;

public class CmdEsLoadCorpus extends ConsoleCommand {

    public CmdEsLoadCorpus(String cmdName) {
        super(cmdName);
    }

    @Override
    public void execute() throws Exception {
        File jsonFile = new File(getInputFile());
        String corpusName = getCorpusName();
        UserIO.Verbosity verbosity = getVerbosity();
        boolean verbose = verbosityLevelIsMet(UserIO.Verbosity.Level1);
        CompiledCorpus corpus =
            new CompiledCorpus(corpusName);
        System.out.println("Loading file "+jsonFile+
            " into ElasticSearch corpus "+corpusName);
        System.out.println((verbose?"":"non-")+"verbose ");
        corpus.loadFromFile(jsonFile, verbose, true, corpusName);
    }

    @Override
    public String getUsageOverview() {
        String mess =
            "Load WordInfo_ES records into an ElasticSearch index.";
            ;
        return mess;
    }
}
