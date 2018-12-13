package ca.inuktitutcomputing.core.console;

import java.io.IOException;

import ca.inuktitutcomputing.core.CompiledCorpus;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;

public class CmdCompileTrie extends ConsoleCommand {

	@Override
	public String getUsageOverview() {
		return "Compile a Trie from a series of corpus files and save it to a JSON file.";
	}

	public CmdCompileTrie(String name) {
		super(name);
	}

	@Override
	public void execute() throws Exception {
		String corpusDir = getCorpusDir();
		
		String trieFile = getTrieFile();
		
		boolean fromScratch = this.cmdLine.hasOption("from-scratch");

		echo("\nCompiling Trie:\n");
		echo(1);
		{
			echo("corpus       : "+corpusDir+"\ntrie file    : "+trieFile+"\nfrom scratch : "+fromScratch+"\n");
		}
		echo(-1);
		
		CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
		boolean ok = compiledCorpus.setTrieFilePath(trieFile);
		if ( !ok ) {
			System.err.println("ERROR: The -trie-file argument points to a non-existent directory. Abort.");
			System.exit(1);
		}
		
		if (fromScratch)
			compiledCorpus.compileCorpusFromScratch(corpusDir);
		else
			compiledCorpus.compileCorpus(corpusDir);
		
		echo("\nThe result of the compilation has been saved in the file "+trieFile+".\n");
	}
}
