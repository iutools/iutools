package ca.pirurvik.iutools.corpus;

import ca.inuktitutcomputing.utilities.StopWatch;
import ca.inuktitutcomputing.utilities.StopWatchException;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class V1_v2Converter {

    public static enum TrieName {
        wordCharTrie, charNgramsTrie, morphNgramsTrie
    };

    public static class ConversionStatus {
        public TrieName whatTrie = null;
        public Set<String> processedWords = new HashSet<String>();
        public Long totalWords = null;

        public ConversionStatus() {

        }

        public ConversionStatus(TrieName _whatTrie) {
            this.whatTrie = _whatTrie;
        }
    }

    protected int saveEveryNSecs = 60;

    File v1_path = null;
    File v2_path = null;
    Class<? extends CompiledCorpus> v2Class;
    boolean newWordsAdded = false;
    ConversionStatus conversionStatus = new ConversionStatus(TrieName.wordCharTrie);

    CompiledCorpus_InMemory v1Corpus = null;
    CompiledCorpus_v2 v2Corpus = null;
    long lastSaveTime = -1;


    public static void main(String[] args) throws CompiledCorpusException {
        new V1_v2Converter(args).run();
    }

    public V1_v2Converter(String[] args) {
        if (args.length < 3 || args.length > 4) {
            usage();
        }
        v1_path = new File(args[0]);
        v2_path = new File(args[1]);
        try {
            v2Class = (Class<? extends CompiledCorpus>) Class.forName("ca.pirurvik.iutools.corpus." + args[2]);
        } catch (ClassNotFoundException e) {
            usage("Bad value for 3rd argument.");
        }
        if (args.length > 3) {
            saveEveryNSecs = Integer.parseInt(args[3]);
        }
    }

    private void run() throws CompiledCorpusException {

        v1Corpus = (CompiledCorpus_InMemory) RW_CompiledCorpus.read(v1_path, CompiledCorpus_InMemory.class);
        resumeFromLastSave();
        convert();
    }

    private void resumeFromLastSave() throws CompiledCorpusException {

        try {
            readAllJsonFiles();
        } catch (CompiledCorpusException e) {
            // Reading the last saved JSON files failed.
            // Possibly this is because the program previously crashed or was
            // interruped while it was saving to JSON
            //
            // Restore the backups from the previous save, and try to resume
            // from those
            //
            restoreBackupJsonFiles();
            readAllJsonFiles();
        }
    }

    private void readAllJsonFiles() throws CompiledCorpusException {
        v2Corpus = (CompiledCorpus_v2) RW_CompiledCorpus.read(v2_path, v2Class);
        readConversionStatus();
    }

    private void restoreBackupJsonFiles() throws CompiledCorpusException {
        File[] v2Files = v2_path.listFiles();
        for (File backupFile: v2Files) {
            if (!backupFile.toString().endsWith(".bak")) {
                continue;
            }
            String origFileStr = backupFile.toString().replaceAll("\\.bak$", "");
            File origFile = new File(origFileStr);
            try {
                echo("Restoring backup file "+backupFile);
                Files.copy(
                        backupFile.toPath(), origFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                echo("DONE Restoring backup file "+backupFile);
            } catch (IOException e) {
                throw new CompiledCorpusException("Could not restore backup file "+backupFile+" to "+origFile, e);
            }
        }
    }

    private void convert() throws CompiledCorpusException {
        echo("Converting corpus "+v1_path+" ("+v1Corpus.getClass().getName()+") to "+v2_path+" ("+v2Corpus.getClass().getName()+")...\n");
        echo("Picking up step: "+conversionStatus.whatTrie.name());
        convertWordCharTrie();
        convertCharNgramsTrie();
        convertMorphNgramsTrie();
    }

    private void readConversionStatus() throws CompiledCorpusException {
        File file = null;
        try {
            file = conversionStatusFile();
            if (file.exists()) {
                conversionStatus = new ObjectMapper().readValue(file, ConversionStatus.class);
            }
        } catch (IOException e) {
            throw new CompiledCorpusException("Unable to load conversion status file "+file, e);
        }
    }

    private void writeConversionStatus() throws CompiledCorpusException {
        File file = null;
        try {
            file = conversionStatusFile();
            new ObjectMapper().writeValue(file, conversionStatus);
        } catch (IOException e) {
            throw new CompiledCorpusException("Unable to load conversion status file "+file, e);
        }
    }

    private File conversionStatusFile() {
        File statusFile = new File(v2_path, "conversionStatus.json");
        return statusFile;
    }

    private void convertWordCharTrie() throws CompiledCorpusException {
        Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.corpus.V1_v2Converter.convertWordCharTrie");
        TimeUnit unit = TimeUnit.MILLISECONDS;
        String stepMess = "Updating word char trie...";
        if (conversionStatus.whatTrie != TrieName.wordCharTrie) {
            echo("Skipping completed step: "+stepMess);
        } else {
            echo(stepMess);
            conversionStatus.totalWords = v1Corpus.totalWords();
            ProgressMonitor_Terminal progMonitor =
                makeProgressMonitor(stepMess, conversionStatus.totalWords);
            Iterator<String> iter = v1Corpus.allWords();
            while (iter.hasNext()) {
                long iterStart = -1;
                if (tLogger.isTraceEnabled()) {
                    try {
                        iterStart = StopWatch.now(unit);
                    } catch (StopWatchException e) {
                        throw new CompiledCorpusException(e);
                    }
                }
                progMonitor.stepCompleted();
                String word = iter.next();
                conversionStatus.processedWords.add(word);
                if (v2Corpus.containsWord(word)) {
                    echo("Word " + word + " was already in the v2 corpus; skipping it");
                    continue;
                }
                if (!newWordsAdded) {
                    lastSaveTime = nowMsecs();
                    newWordsAdded = true;
                }

                String[][] sampleDecomps = null;
                Integer totalDecomps = null;
                long freq = 1;
                WordInfo winfo = v1Corpus.info4word(word);

                if (winfo != null) {
                    freq = winfo.frequency;
                    totalDecomps = winfo.totalDecompositions;
                    sampleDecomps = winfo.decompositionsSample;
                }

                echo("  Converting word: " + word + " (freq=" + freq + ")");
                v2Corpus.addWordOccurence(word, sampleDecomps, totalDecomps, freq);

                if (secsSinceLastSave() > saveEveryNSecs) {
                    lastSaveTime = nowMsecs();
                    saveCorpus();
                }

                if (tLogger.isTraceEnabled()) {
                    try {
                        tLogger.trace("COMPLETED word="+word+" in "+StopWatch.elapsedSince(iterStart, unit)+unit);
                    } catch (StopWatchException e) {
                        throw new CompiledCorpusException(e);
                    }
                }
            }
        }

        conversionStatus.whatTrie = TrieName.charNgramsTrie;
        saveCorpus();

        // We don't need th v1 corpus anymore since all its raw information is now
        // included in the v1 wordCharTrie.
        // So set it to null to allow reclaiming of memory
        //
        v1Corpus = null;
    }

    private void convertCharNgramsTrie() throws CompiledCorpusException {
        String stepMess = "Updating char ngrams trie...";
        if (conversionStatus.whatTrie != TrieName.charNgramsTrie) {
            echo("Skipping completed step: " + stepMess);
        } else {
            echo(stepMess);
            conversionStatus.totalWords = v1Corpus.totalWords();
            ProgressMonitor_Terminal progMonitor =
                    makeProgressMonitor(stepMess, conversionStatus.totalWords);
            Iterator<String> iter = v2Corpus.allWords();
            while (iter.hasNext()) {
                progMonitor.stepCompleted();
                String word =  iter.next();
                WordInfo winfo = v2Corpus.info4word(word);
                v2Corpus.updateCharNgramIndex(word, winfo.frequency);

                if (secsSinceLastSave() > saveEveryNSecs) {
                    lastSaveTime = nowMsecs();
                    saveCorpus();
                }
            }
        }

        conversionStatus.whatTrie = TrieName.morphNgramsTrie;
        saveCorpus();
    }

    private void convertMorphNgramsTrie() throws CompiledCorpusException {
        if (0 == 1-1) { return; }
        String stepMess = "Updating morpheme ngrams trie...";
        if (conversionStatus.whatTrie != TrieName.morphNgramsTrie) {
            echo("Skipping completed step: " + stepMess);
        } else {
            echo(stepMess);
            conversionStatus.totalWords = v1Corpus.totalWords();
            ProgressMonitor_Terminal progMonitor =
                    makeProgressMonitor(stepMess, conversionStatus.totalWords);
            Iterator<String> iter = v2Corpus.allWords();
            while (iter.hasNext()) {
                progMonitor.stepCompleted();
                String word =  iter.next();
                WordInfo winfo = v2Corpus.info4word(word);
                v2Corpus.updateDecompositionsIndex(winfo);
                if (secsSinceLastSave() > saveEveryNSecs) {
                    lastSaveTime = nowMsecs();
                    saveCorpus();
                }
            }
        }

        conversionStatus.whatTrie = null;
        saveCorpus();
    }


    private void saveCorpus() throws CompiledCorpusException {
        echo("Writing partially built corpus to directory "+v2_path+"...");
        RW_CompiledCorpus.write(v2Corpus, v2_path);
        writeConversionStatus();
        echo("DONE Writing partially built corpus to directory "+v2_path+"...");
    }

    private long secsSinceLastSave() {
        long elapsed = (nowMsecs() - lastSaveTime) / 1000;
        return elapsed;
    }

    private long nowMsecs() {
        return System.currentTimeMillis();
    }

    private ProgressMonitor_Terminal makeProgressMonitor(String mess, long steps) throws CompiledCorpusException {
        int refreshEveryNSecs = 10;
        ProgressMonitor_Terminal monitor =
            new ProgressMonitor_Terminal(steps, mess, refreshEveryNSecs);
        return monitor;
    }

    private void usage() {
        usage(null);
    }

    private void usage(String mess) {
        if (mess == null) {
            mess = "Missing command line arguments.\n";
        }
        echo(
                mess + "\n" +
                        "Usage: V1_v2Converter v1_path v2_path v2Class saveEverySecs?"
        );
        throw new RuntimeException(mess);
    }

    private void echo(String mess) {
        System.out.println(mess);
    }
}
