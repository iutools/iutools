package ca.inuktitutcomputing.core.console;

import java.io.File;

import ca.nrc.datastructure.trie.StringSegmenter_AlwaysNull;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpusException;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InFileSystem;
import ca.pirurvik.iutools.corpus.CorpusCompiler;
import ca.pirurvik.iutools.corpus.CorpusCompilerException;

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
		
		if (corpusDir == null && decompsFile == null) {
			throw new ConsoleException(
				"Command requires at least one of options: "+
				ConsoleCommand.OPT_INPUT_DIR+" or "+
				ConsoleCommand.OPT_DECOMPOSITIONS_FILE);
		}
		
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
				// No corpus nor decomposition files were provided. 
				// Simply regenerate the morpheme ngrams index based on 
				// the word info that is already contained in the CompiledCorpus
				//
				regenerateMorphemesNgramIndex(corpusSavePath);
			}
		}
	}	

	private void regenerateMorphemesNgramIndex(String corpusSavePath) 
		throws CompiledCorpusException {
		boolean regenerate = 
			user_io.prompt_yes_or_no(
				"No values provided for "+
				ConsoleCommand.OPT_INPUT_DIR+" nor "+
				ConsoleCommand.OPT_DECOMPOSITIONS_FILE+".\n"+
				"Regenerate the morpheme ngrams index? ");
		if (regenerate) {
			CompiledCorpus compiledCorpus = 
				new CompiledCorpus_InFileSystem(new File(corpusSavePath))
				.setSegmenterClassName(StringSegmenter_AlwaysNull.class)
				;
			compiledCorpus.getMorphNgramsTrie();
		}
	}

	private void compileWordFrequencies(String corpusDirStr, String corpusSavePath) throws CorpusCompilerException {
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
			
			
			CompiledCorpus compiledCorpus = 
				new CompiledCorpus_InFileSystem(new File(corpusSavePath))
					.setSegmenterClassName(StringSegmenter_AlwaysNull.class)
					;
			
			CorpusCompiler compiler = new CorpusCompiler(compiledCorpus);
			File corpusDir = new File(corpusDirStr);
			compiler.compileWordFrequencies(corpusDir);	
		}
	}
	
	private void updateWordDecompositions(
		File decompsFile, String corpusSavePath) 
		throws CompiledCorpusException {
		
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
		
		
		CompiledCorpus compiledCorpus = 
			new CompiledCorpus_InFileSystem(new File(corpusSavePath))
			.setSegmenterClassName(StringSegmenter_AlwaysNull.class)
			;
		
		CorpusCompiler compiler = new CorpusCompiler(compiledCorpus);
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
}
