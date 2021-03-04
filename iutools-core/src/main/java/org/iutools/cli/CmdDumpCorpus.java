package org.iutools.cli;

import ca.nrc.ui.commandline.CommandLineException;
import ca.nrc.ui.commandline.UserIO;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.corpus.CorpusDumper;

import java.io.File;

public class CmdDumpCorpus extends ConsoleCommand {

    public CmdDumpCorpus(String name) throws CommandLineException {
        super(name);
    }

    @Override
    public String getUsageOverview() {
        return "Dump the content of a corpus to a JSON file.";
    }

    @Override
    public void execute() throws Exception {
        UserIO userIO = getUserIO();
        String corpusName = getCorpusName(false);
        CompiledCorpus corpus =
            new CompiledCorpusRegistry().getCorpus(corpusName);
        boolean wordsOnly = getWordsOnlyOpt();
        File outputFile = getDataFile(false);
        new CorpusDumper(corpus)
            .setUserIO(userIO)
            .dump(outputFile, wordsOnly);
    }
}
