package ca.pirurvik.iutools.corpus;

import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

public class AppUpdateESIndexWordFrequencies {

    public static void main(String[] args) throws Exception {
        CompiledCorpus_InMemory corpusInMem =
            CompiledCorpusRegistry.getCorpus();
        CompiledCorpus_ES corpusES =
                (CompiledCorpus_ES) CompiledCorpusRegistry.getCorpusWithName_ES();
        File outputFile = new File("/Users/desilets/Documents/Projects/kayak/Data/iutools-data/data/compiled-corpuses/HANSARD-1999-2002.v2020-10-24.withfreqs.ES.json");
        FileWriter fwriter = new FileWriter(outputFile);
        fwriter.write(
//        "bodyEndMarker=BLANK_LINE\n"+
        "bodyEndMarker=NEW_LINE\n"+
            "class=ca.pirurvik.iutools.corpus.WordInfo_ES\n\n");

        try {
            long totalWords = corpusInMem.totalWords();
            ProgressMonitor_Terminal monitor =
                    new ProgressMonitor_Terminal(
                            totalWords,
                            "Updating frequencies of words in ES index",
                            10);

            Iterator<String> iter = corpusInMem.allWords();
            long wordCounter = 0;
            ObjectMapper mapper = new ObjectMapper();
            while (iter.hasNext()) {
                wordCounter++;
                monitor.stepCompleted();
                String word = iter.next();
                System.out.print("word #"+wordCounter+"="+word);
                WordInfo_ES winfoES = (WordInfo_ES) corpusES.info4word(word);
                if (winfoES == null) {
                    System.out.println("\n   word NOT in ES index: SKIPPING");
                    continue;
                }
                long freqES =  winfoES.frequency;
                System.out.println("; fres in ES: "+freqES);

                if (freqES != 0) {
                    // No need to update ES frequency because it has already
                    // been set.
                    continue;
                }
                long freqInMem = 0;
                WordInfo winfoInMemory = corpusInMem.info4word(word);
                if (winfoInMemory != null) {
                    freqInMem = winfoInMemory.frequency;
                }
                if (freqInMem != 0) {
                    System.out.println("   Freq in ES == 0; Setting its freq from InMemory corpus = "+freqInMem);
                    winfoES.setFrequency(freqInMem);
                }
                String json = mapper.writeValueAsString(winfoES);
                fwriter.write(json+"\n\n");
            }

        } finally {
            fwriter.close();
        }
    }
}
