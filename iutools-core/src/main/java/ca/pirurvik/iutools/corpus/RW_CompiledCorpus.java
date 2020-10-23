package ca.pirurvik.iutools.corpus;

import ca.nrc.ui.commandline.UserIO;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*************************************
 * Class reading/writing CompiledCorpus objects from/to file.
 * 
 * @author desilets
 *
 *************************************/

public abstract class RW_CompiledCorpus {

	UserIO userIO = null;
	
	protected abstract void writeCorpus(CompiledCorpus corpus, 
		File savePath) throws CompiledCorpusException;

	protected abstract CompiledCorpus readCorpus(File savePath) 
		throws CompiledCorpusException;

	protected abstract CompiledCorpus newCorpus(File savePath) throws CompiledCorpusException;

	protected Gson gson = new Gson();

	public static void write(CompiledCorpus corpus, File _savePath) 
		throws CompiledCorpusException {
		RW_CompiledCorpus rw = makeRW(corpus.getClass());
		rw.writeCorpus(corpus, _savePath);
	}

	public static CompiledCorpus read(File savePath)
			throws CompiledCorpusException {
		return read(savePath, null);
	}

	public static CompiledCorpus read(File savePath, 
			Class<? extends CompiledCorpus> corpusClass) 
			throws CompiledCorpusException {
		RW_CompiledCorpus rw = makeRW(savePath, corpusClass);
		CompiledCorpus corpus = rw.readCorpus(savePath);
		
		return corpus;
	}

	private static RW_CompiledCorpus makeRW(
			Class<? extends CompiledCorpus> corpusClass) throws CompiledCorpusException {
		return makeRW(null, corpusClass);
	}

	private static RW_CompiledCorpus makeRW(
			File savePath,
			Class<? extends CompiledCorpus> corpusClass) throws CompiledCorpusException {
		if (savePath == null && corpusClass == null) {
			throw new CompiledCorpusException("Received null values for both savePath and corpusClass");
		}

		if (corpusClass == null) {
			// Set the corpusClass based on the characteristics of the savePath
			if (savePath.toString().matches("^.*\\.ES\\.json$")) {
				corpusClass = CompiledCorpus_ES.class;
			} else {
				corpusClass = CompiledCorpus_InMemory.class;
			}
		}

		RW_CompiledCorpus rw = null;
		if (corpusClass == CompiledCorpus_ES.class) {
			rw = new RW_CompiledCorpus_ES();
		} else if (corpusClass == CompiledCorpus_InMemory.class) {
			rw = new RW_CompiledCorpus_InMemory();
		} else {
			throw new CompiledCorpusException("No RW for corpus class "+corpusClass);
		}
		return rw;
	}

	protected void echo(String mess) {
		if (userIO != null) {
			userIO.echo(mess, UserIO.Verbosity.Level1);
		}
	}
}
