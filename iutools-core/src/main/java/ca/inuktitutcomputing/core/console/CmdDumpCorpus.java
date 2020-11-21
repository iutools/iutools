package ca.inuktitutcomputing.core.console;

import ca.pirurvik.iutools.corpus.*;

import java.io.File;

public class CmdDumpCorpus extends ConsoleCommand {

    public CmdDumpCorpus(String name) {
        super(name);
    }

    @Override
    public String getUsageOverview() {
        return "Dump the content of a corpus to a JSON file.";
    }

    @Override
    public void execute() throws Exception {
        String corpusName = getCorpusName(false);
        CompiledCorpus corpus = new CompiledCorpus(corpusName);
        boolean wordsOnly = getWordsOnlyOpt();
        File outputFile = getDataFile(true);
        new CorpusDumper(corpus).dump(outputFile, wordsOnly);
    }
}
