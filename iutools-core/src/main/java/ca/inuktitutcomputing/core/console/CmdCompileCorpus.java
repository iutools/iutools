package ca.inuktitutcomputing.core.console;

import java.io.File;
import java.io.IOException;

import ca.nrc.datastructure.trie.StringSegmenter_AlwaysNull;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpusException;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InFileSystem;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import ca.pirurvik.iutools.corpus.CorpusCompiler;
import ca.pirurvik.iutools.corpus.CorpusCompilerException;
import ca.pirurvik.iutools.corpus.RW_CompiledCorpus;

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
		String corpusDirStr = getInputDir();
		String corpusSavePath = getCorpusSavePath();
		File decompsFile = getDecompositionsFile();
		
		if (decompsFile == null) {
			compileWordFrequencies(corpusDirStr, corpusSavePath);
		} else {
			updateWordDecompositions(decompsFile, corpusSavePath);
		}
		
//		echo("\nCompiling word frequencies from corpus:\n");
//		echo(1);
//		{
//			echo("corpus directory: "+corpusDirStr+"\noutput json file: "+corpusSavePath+"\n");
//		}
//		echo(-1);
//		
//		boolean ok = true;
//		if ( corpusSavePath != null )
//			ok = checkFilePath(corpusSavePath);
//		if ( !ok ) {
//			usageBadOption(OPT_CORPUS_SAVE_PATH, "The provided path does not exist");
//		}
//		
//		
//		CompiledCorpus compiledCorpus = 
//			new CompiledCorpus_InFileSystem(new File(corpusSavePath))
//			.setSegmenterClassName(StringSegmenter_AlwaysNull.class)
//			;
//		
//		CorpusCompiler compiler = new CorpusCompiler(compiledCorpus);
//		File corpusDir = new File(corpusDirStr);
//		compiler.compile(corpusDir);
	}	

	private void compileWordFrequencies(String corpusDirStr, String corpusSavePath) throws CorpusCompilerException {
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
	
	private void updateWordDecompositions(
		File decompsFile, String corpusSavePath) 
		throws CompiledCorpusException {
		
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
