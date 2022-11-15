package org.iutools.cli;

import java.io.File;

import ca.nrc.ui.commandline.CommandLineException;
import ca.nrc.ui.commandline.UserIO;
import org.iutools.corpus.CorpusCompiler;
import org.iutools.corpus.CorpusCompilerException;

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
