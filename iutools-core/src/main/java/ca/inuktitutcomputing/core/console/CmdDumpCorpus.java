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
        dumpCorpus(corpus, wordsOnly, outputFile);
    }

    private void dumpCorpus(CompiledCorpus corpus, boolean wordsOnly, 
    		File outputFile) throws IOException, CLIException, CompiledCorpusException {

        long totalWords = corpus.totalWords();
        ProgressMonitor_Terminal progMonitor =
            new ProgressMonitor_Terminal(totalWords, "Dumping words of corpus to file");

        FileWriter fWriter = new FileWriter(outputFile);

        printHeaders(fWriter);

        Iterator<String> iterator = corpus.allWords();
        while (iterator.hasNext()) {
            String word = iterator.next();
            printWord(word, corpus, wordsOnly, fWriter);
            progMonitor.stepCompleted();
        }
        fWriter.close();
    }

    private void printHeaders(FileWriter fWriter) throws IOException {
        fWriter.write(
            "bodyEndMarker=BLANK_LINE\n"+
            "class=ca.pirurvik.iutools.corpus.WordInfo_ES\n\n");
    }

    private void printWord(String word, CompiledCorpus corpus, 
			boolean wordsOnly, FileWriter fWriter) 
			throws CLIException, IOException, CompiledCorpusException {

        if (verbosityLevelIsMet(UserIO.Verbosity.Level1)) {
            echo("Dumping info for word="+word);
        }

		String infoStr = word;
		if (!wordsOnly) {
            WordInfo wInfo = winfo4word(word, corpus);

            if (wInfo == null) {
                wInfo = new WordInfo_ES(word);
                wInfo.setDecompositions(new String[0][], 0);
            }
//			ObjectMapper mapper = new ObjectMapper();
//            infoStr = mapper.writeValueAsString(wInfo);
            infoStr = PrettyPrinter.print(wInfo)+"\n";
        }
		fWriter.write(infoStr+"\n");
	}

    private WordInfo_ES winfo4word(String word, CompiledCorpus corpus)
        throws CompiledCorpusException {
        WordInfo wInfo = corpus.info4word(word);

        String[][] decompsSample = new String[0][];
        Integer totalDecomps = 0;
        if (wInfo != null) {
            // wInfo == null can happen with an old InMemory corpus.
            // That class of corpus did not keep WordInfo about words that
            // do not decompose.
            //
            decompsSample = wInfo.decompositionsSample;
            totalDecomps = wInfo.totalDecompositions;
        }

        // Convert the WordInfo to WordInfo_ES
        WordInfo_ES wInfoES = new WordInfo_ES(word);
        wInfoES.setDecompositions(decompsSample, totalDecomps);

        return wInfoES;
    }
}
