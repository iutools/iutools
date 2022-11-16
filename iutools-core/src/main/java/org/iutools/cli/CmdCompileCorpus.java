package org.iutools.cli;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ca.nrc.ui.commandline.CommandLineException;
import ca.nrc.ui.commandline.UserIO;
import org.iutools.corpus.CorpusCompiler;

public class CmdCompileCorpus extends ConsoleCommand {

	@Override
	public String getUsageOverview() {
		return "Compile a corpus from a series of corpus files.";
	}

	public CmdCompileCorpus(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public void execute() throws Exception {
		String corpusName = getCorpusName(false);

		boolean verbose = (getVerbosity() != UserIO.Verbosity.Level0);
		File tmFilesDir = getInputDir(true);
		File outputDir = getOutputDir();
		CorpusCompiler compiler = new CorpusCompiler(outputDir);
			compiler
				.setTMFilesDir(tmFilesDir)
				.setCorpusName(corpusName);

		if (compiler.progress.corpusTextsRoot == null &&
			compiler.progress == null) {
			usageMissingOption(
				OPT_INPUT_DIR,
				"You must provide an input directory when starting a brand new compilation");
		}
		compiler.setTMFilesDir(tmFilesDir);
		compiler.setOutputDir(outputDir);

		compiler.compile(corpusName, tmFilesDir);
	}
}
