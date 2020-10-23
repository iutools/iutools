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
		File corpusDir = getInputDir();
		String corpusSavePath = getCorpusSavePath();
		File decompsFile = getDecompositionsFile();

		if (corpusDir != null) {
			// We were give a directory of files that contains the text of 
			// a corpus to be analyzed. Compute the word frequencies from those
			// files. 
			compileWordFrequencies(corpusDir.toString(), corpusSavePath);
		} else {
			if (decompsFile != null) {
				// No corpus text files were provided, but a decompositions 
				// file was provided. Compute the morpheme ngrams index based 
				// on those decompositions and the word info already contained
				// in the CompiledCorpus.
				// 
				updateWordDecompositions(decompsFile, corpusSavePath);
			} else {
				this.usageBadOption(
					ConsoleCommand.OPT_INPUT_DIR+"|"+ConsoleCommand.OPT_CORPUS_SAVE_PATH,
					"You must provide at least one of those options");
			}
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
			
			
			CompiledCorpus corpus = compiledCorpus(new File(corpusSavePath));

			CorpusCompiler compiler = new CorpusCompiler(corpus);
			File corpusDir = new File(corpusDirStr);
			compiler.compileWordFrequencies(corpusDir);	
		}
	}
	
	private void updateWordDecompositions(
		File decompsFile, String corpusSavePath)
			throws CompiledCorpusException, ConsoleException {
		
		boolean updateDecomps = 
				user_io.prompt_yes_or_no(
					"No value was provided for "+
					ConsoleCommand.OPT_INPUT_DIR+" but a value was provided for "+
					ConsoleCommand.OPT_DECOMPOSITIONS_FILE+".\n"+
					"Would you like to generate the morpheme ngrams index based on decompositions listed in file "
					+decompsFile+"  ?");
		
		
		echo("Updating word morphological decompositions for corpus");
		echo(1);
		{
			echo("corpus save directory         : "+corpusSavePath);
			echo("decompositions read from file : "+decompsFile);
		}
		echo(-1);
		
		boolean ok = true;
		if ( corpusSavePath != null ) {
			ok = checkFilePath(corpusSavePath);
		}
		if ( !ok ) {
			usageBadOption(OPT_CORPUS_SAVE_PATH, "The provided path does not exist");
		}
		

		CompiledCorpus corpus = compiledCorpus(new File(corpusSavePath));

		CorpusCompiler compiler = new CorpusCompiler(corpus);
		compiler.updateWordDecompositions(decompsFile);	
	}
	
	private boolean checkFilePath(String _trieFilePath) {
		File f = new File(_trieFilePath);
		File dirF = f.getParentFile();
		if ( dirF != null && !dirF.isDirectory() ) {
			return false;
		}
		return true;
	}

	protected CompiledCorpus compiledCorpus(File corpusJsonFile) throws ConsoleException {
		try {
			CompiledCorpus_ES corpus =
				(CompiledCorpus_ES) RW_CompiledCorpus.read(corpusJsonFile);
			return corpus;
		} catch (CompiledCorpusException e) {
			throw new ConsoleException(e);
		}
	}
}
