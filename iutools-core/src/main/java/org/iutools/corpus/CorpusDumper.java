package org.iutools.corpus;

import ca.nrc.datastructure.CloseableIterator;
import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import ca.nrc.ui.commandline.UserIO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ca.nrc.ui.commandline.UserIO.Verbosity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CorpusDumper {

    private CompiledCorpus corpus = null;
    private File outputFile = null;
    private FileWriter outputFileWriter = null;
    private UserIO userIO = new UserIO();

    public CorpusDumper(CompiledCorpus corpus) {
        init_CorpusDumper(corpus, (UserIO)null);
    }

    private void init_CorpusDumper(CompiledCorpus _corpus, UserIO _userIO) {
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
        Logger tLogger = LogManager.getLogger("org.iutools.corpus.CorpusDumper.dump");

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

            List<String> allWords = readAllWords();

			  int wordCount = 0;
			  for (String word: allWords) {
					wordCount++;
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
        return;
    }

	private List<String> readAllWords() throws CompiledCorpusException {
    	List<String> words = new ArrayList<String>();
		try (CloseableIterator<String> iterator = corpus.allWords()) {
			while (iterator.hasNext()) {
				words.add(iterator.next());
			}
		} catch (Exception e) {
			throw new CompiledCorpusException(e);
		}
		return words;
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
			  	String[] fieldsToIgnore = new String[] {
					"additionalFields", "creationDate", "lang", "_detect_language",
					"shortDescription", "_wordInOtherScript", "_wordRoman",
					"_wordSyllabic"
				};
			  	try {
					infoStr = wInfo.toJson(fieldsToIgnore);
				} catch (Exception e) {
			  		throw new CompiledCorpusException(e);
				}
        }
        try {
            outputFileWriter.write(infoStr+"\n");
        } catch (IOException e) {
            throw new CompiledCorpusException("Could not write to file:\n  "+
                outputFile);
        }
    }

	private void printWordInfo(WordInfo winfo, Boolean wordsOnly) throws CompiledCorpusException {
        String infoStr = winfo.word;
        if (!wordsOnly) {
			  	String[] fieldsToIgnore = new String[] {
					"additionalFields", "creationDate", "lang", "_detect_language",
					"shortDescription", "_wordInOtherScript", "_wordRoman",
					"_wordSyllabic"
				};
			  try {
				  infoStr = winfo.toJson(fieldsToIgnore);
			  } catch (ElasticSearchException e) {
				  throw new CompiledCorpusException(e);
			  }
		  }
        try {
            outputFileWriter.write(infoStr+"\n");
        } catch (IOException e) {
            throw new CompiledCorpusException("Could not write to file:\n  "+
                outputFile);
        }
	}


}
