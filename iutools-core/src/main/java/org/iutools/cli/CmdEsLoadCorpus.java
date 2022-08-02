package org.iutools.cli;

import ca.nrc.ui.commandline.CommandLineException;
import org.iutools.corpus.CompiledCorpusRegistry;

public class CmdEsLoadCorpus extends ConsoleCommand {

    public CmdEsLoadCorpus(String cmdName) throws CommandLineException {
        super(cmdName);
    }

    @Override
    public void execute() throws Exception {
        String corpusName = getCorpusName();
        this.user_io.setVerbosity(getVerbosity());
        boolean force = getForce();
        new CompiledCorpusRegistry().getCorpus(corpusName, force);
        return;
    }

    @Override
    public String getUsageOverview() {
        String mess =
            "Load WordInfo_ES records into an ElasticSearch index.";
            ;
        return mess;
    }
}
