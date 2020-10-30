package ca.pirurvik.iutools.corpus;

import java.io.File;

/**
 * Store the state of progress of compilation for a given corpus
 */
public class CorpusCompilationProgress {
    File corpusTextsRoot = null;
    File currentFile = null;
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

    public void onNewTxtFile(File newFile) {
        currentFile = newFile;
        wordInCurrentFile = 0;
    }

    public void onNewWord(String word) {
        wordInCurrentFile++;
    }
}
