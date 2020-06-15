package ca.inuktitutcomputing.core.console;

import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import ca.pirurvik.iutools.corpus.CompiledCorpusException;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import ca.pirurvik.iutools.corpus.WordInfo;

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
        CompiledCorpus_InMemory corpus = CompiledCorpusRegistry.getCorpus(corpusName);
        boolean wordsOnly = getWordsOnlyOpt();
        File outputFile = getDataFile(true);
        dumpCorpus(corpus, wordsOnly, outputFile);
    }

    private void dumpCorpus(CompiledCorpus_InMemory corpus, boolean wordsOnly, 
    		File outputFile) throws IOException, CLIException, CompiledCorpusException {

        FileWriter fWriter = new FileWriter(outputFile);

        Iterator<String> iterator = corpus.allWords();
        while (iterator.hasNext()) {
            String word = iterator.next();
            printWord(word, corpus, wordsOnly, fWriter);
        }
        fWriter.close();
    }

	private void printWord(String word, CompiledCorpus_InMemory corpus, 
			boolean wordsOnly, FileWriter fWriter) 
			throws CLIException, IOException, CompiledCorpusException {
		String infoStr = word;
		if (!wordsOnly) {
            WordInfo wInfo = corpus.info4word(word);
			ObjectMapper mapper = new ObjectMapper();
			try {
				infoStr = mapper.writeValueAsString(wInfo);
			} catch (JsonProcessingException e) {
				throw new CLIException(e);
			}
		}
		fWriter.write(infoStr+"\n");
	}
}
