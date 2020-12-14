package org.iutools.cli;

import java.io.File;

import ca.nrc.ui.commandline.UserIO;
import ca.pirurvik.iutools.corpus.*;

public class CmdCompileCorpus extends ConsoleCommand {

	@Override
	public String getUsageOverview() {
		return "Compile a corpus from a series of corpus files.";
	}

	public CmdCompileCorpus(String name) {
		super(name);
	}

	@Override
	public void execute() throws Exception {
		String corpusName = getCorpusName(true);
		if (corpusName == null) {
			this.usageMissingOption(OPT_CORPUS_NAME);
		}

		boolean verbose = (getVerbosity() != UserIO.Verbosity.Level0);
		File corpusDir = getInputDir();
		File outputDir = getOutputDir();
		File inputDir = getInputDir();
		CorpusCompiler compiler = new CorpusCompiler(outputDir);
		compiler
			.setCorpusDir(corpusDir)
			.setCorpusName(corpusName);

		if (compiler.progress.corpusTextsRoot == null &&
			compiler.progress == null) {
			usageMissingOption(
				OPT_INPUT_DIR,
				"You must provide an input directory when starting a brand new compilation");
		}
		compiler.setCorpusDir(corpusDir);
		compiler.setOutputDir(outputDir);

		compiler.compile();
	}

	private void compileWordFrequencies(File corpusDir, String corpusSavePath, boolean verbose) throws CorpusCompilerException, ConsoleException {
		boolean compileFreqs =
			user_io.prompt_yes_or_no(
				"A value was provided for "+
				OPT_INPUT_DIR +"\n"+
				"Would you like to compile word frequencies from corpus text files: "+
				corpusDir);

		if (compileFreqs) {
			String corpusName = getCorpusName();
			echo("\nCompiling word frequencies from corpus:\n");
			echo(1);
			{
				echo(
					"  corpus txt files root : "+corpusDir+"\n"+
					"  corpus name           : "+corpusName);
			}
			echo(-1);

			CorpusCompiler compiler =
				new CorpusCompiler(corpusDir)
				.setVerbose(verbose)
				.setUserIO(user_io)
				;
			compiler.compileWordFrequencies(corpusName);
		}
	}

	private void updateWordDecompositions(File corpusDir, boolean verbose)  throws CorpusCompilerException {

		CorpusCompiler compiler =
			new CorpusCompiler(corpusDir)
			.setVerbose(verbose);

		boolean updateDecomps = false;
		if (updateDecomps) {

			echo("Updating word morphological decompositions for corpus");
			echo(1);
			{
				echo("corpus name                    : " + compiler.corpusName);
				echo("decompositions read from file : " + compiler.decompositionsFile());
			}
			echo(-1);
			compiler.updateWordDecompositions();
		}
	}
}
