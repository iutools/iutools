package ca.pirurvik.iutools.corpus;

import ca.nrc.ui.commandline.ProgressMonitor;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;

import java.io.File;
import java.util.Iterator;

public class AppSetESCorpusFrequencies {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage("Missing arguments");
            return;
        }

        File corp1File = new File(args[0]);
        String esCorpName = args[1];

        System.out.println("Setting word freqs of\n  ES corpus: "+esCorpName+
            "\n  based on corpus: "+corp1File+"\n");

        CompiledCorpus corp1 = RW_CompiledCorpus.read(corp1File);
        CompiledCorpus_ES esCorpus = new CompiledCorpus_ES(esCorpName);

        setESFreqsOfWordsPresentInCorp1(corp1, esCorpus);
        setFreqOfWordsAbsentFromCorp1(esCorpus);
    }

    private static void setESFreqsOfWordsPresentInCorp1(CompiledCorpus corp1, CompiledCorpus_ES esCorpus) throws CompiledCorpusException {
        long totalWords = corp1.totalWords();
        ProgressMonitor progMonitor =
            new ProgressMonitor_Terminal(
                totalWords, "Setting ES freq based on corp1 freqs", 10);
        Iterator<String> iter = corp1.allWords();
        while (iter.hasNext()) {
            progMonitor.stepCompleted();
            String word = iter.next();
            System.out.println("processing word "+word);
            WordInfo winfoCorp1 = corp1.info4word(word);
            if (winfoCorp1 == null) {
                System.out.println("  no corp1 winfo for word; skipping it");
                continue;
            }
            WordInfo_ES winfoES = (WordInfo_ES) esCorpus.info4word(word);
            String[][] sampleDecomps = winfoCorp1.decompositionsSample;
            int totalDecomps = winfoCorp1.totalDecompositions;
            long esFreq = 0;
            if (winfoES != null) {
                sampleDecomps = winfoES.decompositionsSample;
                totalDecomps = winfoES.totalDecompositions;
                esFreq = winfoES.frequency;
            }
            if (esFreq == 0) {
                System.out.println("  Setting ES frequency to "+winfoCorp1.frequency);
                esCorpus.addWordOccurence(word, sampleDecomps, totalDecomps, winfoCorp1.frequency);
            } else {
                System.out.println("  ES frequency is already non-zero; leaving it alone");
            }
        }

        return;
    }


    private static void setFreqOfWordsAbsentFromCorp1(CompiledCorpus_ES esCorpus) throws CompiledCorpusException {
        Iterator<String> iter = esCorpus.allWords();
        while (iter.hasNext()) {
            String word = iter.next();
            WordInfo_ES winfo = (WordInfo_ES) esCorpus.info4word(word);
            if (winfo.frequency == 0) {
                esCorpus.addWordOccurence(word);
            }
        }
    }

    private static void usage(String mess) {
        mess = mess + "\n\n" +
            "Usage: AppSetESCorpusFrequencies corp1File esCorpName\n" +
            "\n" +
            "Set the frequencies of words in an Elastic Search corpus based on the frequencies \n"+
            "in another corpus.\n" +
            "\n" +
            "  corp1File: File of the other corpus.\n" +
            "  esCorpName: Name of the ES corpus."
            ;
        System.out.println(mess);
        System.exit(1);
    }
}
