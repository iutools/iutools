package ca.inuktitutcomputing.core.console;

import java.io.File;

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
			this.usageMissingOption(ConsoleCommand.OPT_CORPUS_NAME);
		}

		File corpusDir = getInputDir();
		File decompsFile = getDecompositionsFile();
		if (decompsFile == null && corpusDir == null) {
			this.usageBadOption(
				ConsoleCommand.OPT_INPUT_DIR+"|"+ConsoleCommand.OPT_DECOMPOSITIONS_FILE,
				"You must provide at least one of those options");
		}

		if (decompsFile != null && corpusDir != null) {
			this.usageBadOption(
				ConsoleCommand.OPT_INPUT_DIR+"|"+ConsoleCommand.OPT_CORPUS_NAME,
				"These options are mutually exclusive");
		}

		if (decompsFile != null) {
			// Assume we want to update the decompositions of a corpus whose
			// words and frequencies have already been compiled from a bunch
			// of text files
			//
			File jsonFile = getDataFile(true);
			updateWordDecompositions(decompsFile, corpusName, jsonFile);
		} else if (corpusDir != null) {
			// Assume we want to compile the frequency of words from a bunch of
			// text files contained in a directory (recursively)
			//
			compileWordFrequencies(corpusDir.toString(), corpusName);
		}
	}

	private void compileWordFrequencies(String corpusDirStr, String corpusSavePath) throws CorpusCompilerException, ConsoleException {
		boolean compileFreqs = 
				user_io.prompt_yes_or_no(
					"A value was provided for "+
					ConsoleCommand.OPT_INPUT_DIR+"\n"+
					"Would you like to compile word frequencies from corpus text files: "+corpusDirStr);
	
		if (compileFreqs) { 
			echo("\nCompiling word frequencies from corpus:\n");
			echo(1);
			{
				echo("corpus directory: "+corpusDirStr+"\noutput json file: "+corpusSavePath+"\n");
			}
			echo(-1);
			
			boolean ok = true;
			if ( corpusSavePath != null)
				ok = checkFilePath(corpusSavePath);
			if ( !ok ) {
				usageBadOption(OPT_CORPUS_SAVE_PATH, "The provided path does not exist");
			}
			
			
			CompiledCorpus corpus = compiledCorpus(corpusSavePath);

			CorpusCompiler compiler = new CorpusCompiler(corpus);
			File corpusDir = new File(corpusDirStr);
			compiler.compileWordFrequencies(corpusDir);	
		}
	}
	
	private void updateWordDecompositions(
		File decompsFile, String corpusName, File corpusJsonFile)
			throws CompiledCorpusException, ConsoleException, CorpusCompilerException {
		
		boolean updateDecomps = 
				user_io.prompt_yes_or_no(
					"No value was provided for "+
					ConsoleCommand.OPT_INPUT_DIR+" but a value was provided for "+
					ConsoleCommand.OPT_DECOMPOSITIONS_FILE+".\n"+
					"Would you like to update corpus '"+corpusName+"' word morphological decompositions using decompositions listed in file:\n   "
					+decompsFile+"?\n");
		
		
		echo("Updating word morphological decompositions for corpus");
		echo(1);
		{
			echo("corpus nam                    : "+corpusName);
			echo("decompositions read from file : "+decompsFile);
			echo("updated corpus output to file : "+corpusJsonFile);
		}
		echo(-1);
		

		CompiledCorpus corpus = compiledCorpus(corpusName);

		CorpusCompiler compiler = new CorpusCompiler(corpus);
		compiler.updateWordDecompositions(decompsFile, corpusJsonFile);
	}
	
	private boolean checkFilePath(String _trieFilePath) {
		File f = new File(_trieFilePath);
		File dirF = f.getParentFile();
		if ( dirF != null && !dirF.isDirectory() ) {
			return false;
		}
		return true;
	}

	protected CompiledCorpus compiledCorpus(String corpusName) throws ConsoleException {
		try {
			CompiledCorpus_ES corpus = new CompiledCorpus_ES(corpusName);
			return corpus;
		} catch (CompiledCorpusException e) {
			throw new ConsoleException(e);
		}
	}
}
