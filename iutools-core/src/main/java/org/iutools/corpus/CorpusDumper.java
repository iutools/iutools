package org.iutools.corpus;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import ca.nrc.ui.commandline.UserIO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.log4j.Logger;

import static ca.nrc.ui.commandline.UserIO.Verbosity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class CorpusDumper {

    private CompiledCorpus corpus = null;
    private File outputFile = null;
    private FileWriter outputFileWriter = null;
    private UserIO userIO = new UserIO();

    public CorpusDumper(CompiledCorpus corpus) {
        init_CorpusDumper(corpus, (UserIO)null);
    }

    private void init_CorpusDumper(CompiledCorpus _corpus,
        UserIO _userIO) {
        this.corpus = _corpus;
        if (_userIO != null) {
            userIO = _userIO;
        }
    }

    @JsonIgnore
    public CorpusDumper setVerbosity(Verbosity level) {
        this.userIO.setVerbosity(level);
        return this;
    }

    public CorpusDumper setUserIO(UserIO _io) {
        userIO = _io;
        return this;
    }

    public void dump(File outputFile) throws CompiledCorpusException {
        dump(outputFile, (Boolean)null);
    }

    public void dump(File outputFile, Boolean wordsOnly)
        throws CompiledCorpusException {
        Logger tLogger = Logger.getLogger("org.iutools.corpus.CorpusDumper.dump");

        if (wordsOnly == null) {
            wordsOnly = false;
        }

        if (outputFile == null) {
            outputFile = CompiledCorpusRegistry.jsonFile4corpus(corpus).toFile();
        }

        System.out.println("Dumping corpus "+corpus.canonicalName()+" to file: "+outputFile);

        if (outputFile.exists()) {
            boolean overwrite = userIO.prompt_yes_or_no("The file "+outputFile+" already exists.\nOverwrite it?");
            if (!overwrite) {
                System.out.println("Aborting the command");
                return;
            }
        }

        long totalWords = corpus.totalWords();
        ProgressMonitor_Terminal progMonitor =
        new ProgressMonitor_Terminal(
            totalWords, "Dumping words of corpus to file: "+outputFile, 30);

        try(FileWriter fw = new FileWriter(outputFile)) {
            outputFileWriter = fw;

            printHeaders();

            int wordCount = 0;
            Iterator<String> iterator = corpus.allWords();
            while (iterator.hasNext()) {
                wordCount++;
                String word = iterator.next();
                userIO.echo(
                    "Dumping word #"+wordCount+": "+word ,
                    Verbosity.Level0);
                printWord(word, wordsOnly);
                progMonitor.stepCompleted();
            }
        } catch (IOException e) {
            throw new CompiledCorpusException(
                "Unable to open file for output\n  "+outputFile);
        }
    }

    private void printHeaders()
        throws CompiledCorpusException {
        try {
            outputFileWriter.write(

            "bodyEndMarker=BLANK_LINE\n"+
                "class=org.iutools.corpus.WordInfo\n\n");
        } catch (IOException e) {
            throw new CompiledCorpusException("Could not print headers to JSON file.");
        }
    }

    private void printWord(String word, boolean wordsOnly)
        throws CompiledCorpusException {

        String infoStr = word;
        if (!wordsOnly) {
            WordInfo wInfo = corpus.info4word(word);
            infoStr = PrettyPrinter.print(wInfo)+"\n";
        }
        try {
            outputFileWriter.write(infoStr+"\n");
        } catch (IOException e) {
            throw new CompiledCorpusException("Could not write to file:\n  "+
                outputFile);
        }
    }
}
