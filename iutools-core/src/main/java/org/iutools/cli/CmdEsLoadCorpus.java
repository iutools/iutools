package org.iutools.cli;

import org.iutools.corpus.CompiledCorpusRegistry;

public class CmdEsLoadCorpus extends ConsoleCommand {

    public CmdEsLoadCorpus(String cmdName) {
        super(cmdName);
    }

    @Override
    public void execute() throws Exception {
//        File jsonFile = new File(getInputFile());
        String corpusName = getCorpusName();
        this.user_io.setVerbosity(getVerbosity());
//        boolean verbose = verbosityLevelIsMet(UserIO.Verbosity.Level1);
//        CompiledCorpus corpus =
//            new CompiledCorpus(corpusName);
//        System.out.println("Loading file "+jsonFile+
//            " into ElasticSearch corpus "+corpusName);
//        System.out.println((verbose?"":"non-")+"verbose ");
//        corpus.loadFromFile(jsonFile, verbose, true, corpusName);

        new CompiledCorpusRegistry().getCorpus(corpusName);
    }

    @Override
    public String getUsageOverview() {
        String mess =
            "Load WordInfo_ES records into an ElasticSearch index.";
            ;
        return mess;
    }
}
