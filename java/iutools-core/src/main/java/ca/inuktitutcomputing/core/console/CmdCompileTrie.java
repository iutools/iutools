package ca.inuktitutcomputing.core.console;

import ca.inuktitutcomputing.core.CorpusTrieCompiler;
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
		
		CorpusTrieCompiler compiler = new CorpusTrieCompiler(StringSegmenter_IUMorpheme.class.getName());
		compiler.setCorpusDirectory(corpusDir);
		compiler.setTrieFilePath(trieFile);
		compiler.run(fromScratch);
	}
}
