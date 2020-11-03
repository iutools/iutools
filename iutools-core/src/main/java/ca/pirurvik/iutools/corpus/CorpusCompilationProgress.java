package ca.pirurvik.iutools.corpus;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Store the state of progress of compilation for a given corpus
 */
public class CorpusCompilationProgress {


    public static enum Phase {COMPUTE_WORD_FREQUENCIES,
        GENERATE_CORP_FILE_NO_DECOMPS, LOAD_CORPUS_NODECOMPS, CHECK_DECOMPS_FILE,
        GENERATE_CORP_FILE_WITH_DECOMPS, LOAD_FINALIZED_CORPUS,
        REFORMAT_FOR_VERSION_TRACKING, DONE};

    public String corpusName;
    public File corpusTextsRoot = null;

    Phase currentPhase = null;

    protected List<String> filesCompiled = new ArrayList<String>();
    File currentFile = null;

    // We don't save fileBeingProcessed to file becase it may be a
    // large file.
    @JsonIgnore
    public CorpusDocument_File fileBeingProcessed;
    public int currentFileWordCounter;

    long wordInCurrentFile = 0;
    
    public CorpusCompilationProgress() {
        init_CorpusCompilationProgress(null);
    }

    public CorpusCompilationProgress(File corpDir) {
        init_CorpusCompilationProgress(corpDir);
    }

    private void init_CorpusCompilationProgress(File corpDir) {
        this.corpusTextsRoot = corpDir;
    }

    public void setCurrentPhase(Phase _phase) {
        currentPhase = _phase;
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public void onNewTxtFile(File newFile) {
        currentFile = newFile;
        wordInCurrentFile = 0;
    }

    public void onNewWord(String word) {
        wordInCurrentFile++;
    }
}
