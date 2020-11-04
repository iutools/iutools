package ca.inuktitutcomputing.core.console;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import ca.nrc.ui.commandline.UserIO;
import ca.pirurvik.iutools.corpus.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        CompiledCorpus corpus = CompiledCorpusRegistry.getCorpusWithName_ES(corpusName);
        boolean wordsOnly = getWordsOnlyOpt();
        File outputFile = getDataFile(true);
        new CorpusDumper(corpus).dump(outputFile, wordsOnly);
    }
}
