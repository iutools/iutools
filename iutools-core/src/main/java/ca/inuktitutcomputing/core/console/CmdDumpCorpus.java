package ca.inuktitutcomputing.core.console;

import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.CompiledCorpusRegistry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

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
        CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus(corpusName);
        boolean wordsOnly = getWordsOnlyOpt();
        File outputFile = getDataFile();
    }

    private void dumpCorpus(CompiledCorpus corpus, boolean wordsOnly, File outputFile) throws IOException {
        dumpCorpus(corpus, wordsOnly, outputFile);

        FileWriter fWriter = new FileWriter(outputFile);

        Iterator<String> iterator = corpus.allWords();
        while (iterator.hasNext()) {
            String word = iterator.next();
        }

    }
}
