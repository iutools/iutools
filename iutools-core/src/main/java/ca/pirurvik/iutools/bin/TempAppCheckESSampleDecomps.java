package ca.pirurvik.iutools.bin;

import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import ca.pirurvik.iutools.corpus.CompiledCorpus_ES;
import ca.pirurvik.iutools.corpus.WordInfo_ES;

import java.util.Iterator;

public class TempAppCheckESSampleDecomps {

    public static void main(String[] args) throws Exception {
        CompiledCorpus_ES corpus = (CompiledCorpus_ES) CompiledCorpusRegistry.getCorpusWithName();
        Iterator<String> iter = corpus.allWords();
        int wordCounter = 0;
        int wordsWithMoreThanOneDecomps = 0;
        for (Iterator<String> it = iter; it.hasNext(); ) {
            String word = it.next();
            wordCounter++;
            System.out.println("word #"+wordCounter+"="+word);
            WordInfo_ES winfo = (WordInfo_ES) corpus.info4word(word);
            if (winfo.decompositionsSample != null &&
                winfo.decompositionsSample.length > 1) {
                wordsWithMoreThanOneDecomps++;
                System.out.println("   Word had > 1 decomps: "+winfo.decompositionsSample.length);
            }
        }

        System.out.println("Number of words with > 1 decomps: "+wordsWithMoreThanOneDecomps);
    }
}
