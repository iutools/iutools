package ca.inuktitutcomputing.core.console;

import ca.nrc.ui.commandline.UserIO;
import ca.pirurvik.iutools.corpus.CompiledCorpus_ES;

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
        CompiledCorpus_ES corpus =
            new CompiledCorpus_ES(corpusName);
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
