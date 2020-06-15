package ca.inuktitutcomputing.core.console;

import java.io.File;
import java.io.IOException;

import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import ca.pirurvik.iutools.corpus.CorpusCompiler;

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
		
		String corpusDirStr = getCorpusDir();
		
		String compilationFile = getCompilationFile();
		
		echo("\nCompiling corpus:\n");
		echo(1);
		{
			echo("corpus directory: "+corpusDirStr+"\noutput json file: "+compilationFile+"\n");
		}
		echo(-1);
		
		CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
		boolean ok = true;
		if ( compilationFile != null )
			ok = checkFilePath(compilationFile);
		if ( !ok ) {
			System.err.println("ERROR: The --corpus-dir argument points to a non-existent directory. Abort.");
			System.exit(1);
		}
		
		CorpusCompiler compiler = new CorpusCompiler(compiledCorpus);
		File corpusDir = new File(corpusDirStr);
		compiler.compile(corpusDir);
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
