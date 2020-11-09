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
	
	public abstract void writeCorpus(CompiledCorpus corpus,
		File savePath) throws CompiledCorpusException;

	public abstract CompiledCorpus readCorpus(File savePath)
		throws CompiledCorpusException;

	protected abstract CompiledCorpus newCorpus(File savePath) throws CompiledCorpusException;

	protected Gson gson = new Gson();

	public RW_CompiledCorpus() {
		init_RW_CompiledCorpus((UserIO)null);
	}

	public RW_CompiledCorpus(UserIO io) {
		init_RW_CompiledCorpus(io);
	}

	private void init_RW_CompiledCorpus(UserIO io) {
		if (io == null) {
			io = new UserIO();
		}
		this.userIO = io;
	}

	public static void write(CompiledCorpus corpus, File _savePath) 
		throws CompiledCorpusException {
		RW_CompiledCorpus rw = makeRW(corpus.getClass());
		rw.writeCorpus(corpus, _savePath);
	}

	public static CompiledCorpus read(File savePath)
		throws CompiledCorpusException {
		RW_CompiledCorpus rw = new RW_CompiledCorpus_ES();
		CompiledCorpus corpus = rw.readCorpus(savePath);
		
		return corpus;
	}
	protected void echo(String mess) {
		if (userIO != null) {
			userIO.echo(mess, UserIO.Verbosity.Level1);
		}
	}
}
