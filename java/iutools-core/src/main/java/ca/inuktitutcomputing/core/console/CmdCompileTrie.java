package ca.inuktitutcomputing.core.console;

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

		echo("\nCompiling Trie:\n");
		echo(1);
		{
			echo("corpus    : "+corpusDir+"\ntrie file : "+trieFile);
		}
		
		echo(-1);
		echo("\n\nCommand not implemented.\nTODO-Benoit: Appeler le CorpusTrieCompiler dans CmdCompileTrie.execute() ");			
		echo(-1);
	}
}
