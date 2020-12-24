package org.iutools.cli;

import org.iutools.corpus.CompiledCorpusRegistry;

public class CmdEsLoadCorpus extends ConsoleCommand {

    public CmdEsLoadCorpus(String cmdName) {
        super(cmdName);
    }

    @Override
    public void execute() throws Exception {
        String corpusName = getCorpusName();
        this.user_io.setVerbosity(getVerbosity());
        boolean force = getForce();
        new CompiledCorpusRegistry().getCorpus(corpusName, force);
    }

    @Override
    public String getUsageOverview() {
        String mess =
            "Load WordInfo_ES records into an ElasticSearch index.";
            ;
        return mess;
    }
}
