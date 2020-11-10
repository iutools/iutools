package ca.pirurvik.iutools.corpus;

import ca.nrc.ui.commandline.UserIO;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.regex.Pattern;

/*************************************
 * Class reading/writing CompiledCorpus objects from/to file.
 * 
 * @author desilets
 *
 *************************************/

public class RW_CompiledCorpus {

	UserIO userIO = null;

	static Pattern pattSavePath = Pattern.compile(".*?(^|[^/\\\\.]*)\\.ES\\.json$");

	private String _corpusName;

	public RW_CompiledCorpus() {
		init_RW_CompiledCorpus((String)null, (UserIO)null);
	}

	public RW_CompiledCorpus(UserIO io) {
		init_RW_CompiledCorpus((String)null, io);
	}

	public RW_CompiledCorpus(String _intoCorpusNamed) {
		init_RW_CompiledCorpus(_intoCorpusNamed, (UserIO)null);
	}

	public RW_CompiledCorpus(String _corpusName, UserIO _io) {
		init_RW_CompiledCorpus(_corpusName, _io);
	}

	private void init_RW_CompiledCorpus(String _corpusName, UserIO io) {
		if (io == null) {
			io = new UserIO();
		}
		this.userIO = io;
		this._corpusName = _corpusName;
	}

	public static void write(CompiledCorpus corpus, File _savePath) 
		throws CompiledCorpusException {
		RW_CompiledCorpus rw = new RW_CompiledCorpus();
		rw.writeCorpus(corpus, _savePath);
	}

	public static CompiledCorpus read(File savePath)
		throws CompiledCorpusException {
		RW_CompiledCorpus rw = new RW_CompiledCorpus();
		CompiledCorpus corpus = rw.readCorpus(savePath);
		
		return corpus;
	}

	protected void echo(String mess) {
		if (userIO != null) {
			userIO.echo(mess, UserIO.Verbosity.Level1);
		}
	}

	public void writeCorpus(CompiledCorpus corpus, File savePath)
			throws CompiledCorpusException {
	}

	public CompiledCorpus readCorpus(File jsonFile) throws CompiledCorpusException {
		String corpusName = corpusName(jsonFile);
		CompiledCorpus corpus =
				new CompiledCorpus(corpusName);
		echo("Loading file "+jsonFile+
				" into ElasticSearch corpus "+corpusName);
		boolean verbose =
				(userIO != null &&
						userIO.verbosityLevelIsMet(UserIO.Verbosity.Level1));

		corpus.loadFromFile(jsonFile, verbose, (Boolean)null, corpusName);

		return corpus;
	}

	protected CompiledCorpus newCorpus(File savePath) throws CompiledCorpusException {
		String corpusName = CompiledCorpus.corpusName4File(savePath);
		return new CompiledCorpus(corpusName);
	}

	protected String corpusName(File jsonFile) {
		if (_corpusName == null) {
			_corpusName = CompiledCorpus.corpusName4File(jsonFile);
		}
		return _corpusName;
	}
}
