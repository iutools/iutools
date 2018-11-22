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
		
		// TODO: Benoit, compile le Trie pour l'ensemble de fichiers contenus dans
		//   le répertoire corpusDir, et sauve le dans le fichier trieFile.
		//
		echo("Command not implemented.");
		
	}


}
