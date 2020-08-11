package ca.pirurvik.iutools.corpus;

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
	
	protected abstract void writeCorpus(CompiledCorpus corpus, 
		File savePath) throws CompiledCorpusException;

	protected abstract CompiledCorpus readCorpus(File savePath) 
		throws CompiledCorpusException;

	protected abstract CompiledCorpus newCorpus(File savePath);

	protected Gson gson = new Gson();

	public static void write(CompiledCorpus corpus, File _savePath) 
		throws CompiledCorpusException {
		RW_CompiledCorpus rw = makeRW(corpus.getClass());
		rw.writeCorpus(corpus, _savePath);
	}

	public static CompiledCorpus read(File savePath, 
			Class<? extends CompiledCorpus> corpusClass) 
			throws CompiledCorpusException {
		RW_CompiledCorpus rw = makeRW(corpusClass);
		CompiledCorpus corpus = rw.readCorpus(savePath);
		
		return corpus;
	}

	private static RW_CompiledCorpus makeRW(
			Class<? extends CompiledCorpus> corpusClass) throws CompiledCorpusException {
		RW_CompiledCorpus rw = null;
		if (corpusClass == CompiledCorpus_v2FS.class) {
			rw = new RW_CompiledCorpus_v2FS();
		} else if (corpusClass == CompiledCorpus_v2Mem.class) {
			rw = new RW_CompiledCorpus_v2Mem();
		} else if (corpusClass == CompiledCorpus_InMemory.class) {
			rw = new RW_CompiledCorpus_InMemory();
		} else {
			throw new CompiledCorpusException("No writer for corpus class "+corpusClass);
		}
		return rw;
	}
}
