package ca.inuktitutcomputing.core.console;



public class CmdSearchTrie extends ConsoleCommand {
	
	public CmdSearchTrie(String name) {
		super(name);
	}


	@Override
	public String getUsageOverview() {
		return "Read a saved Trie.";
	}
	

	@Override
	public void execute() throws Exception {
		String trieFile = getTrieFile();
		
		// TODO: Benoit, charge le Trie qui est dans trieFile
		
		while (true) {
			String morphemes = prompt("Enter a list of morphemes");	
			if (morphemes == null) {break;}
			echo("Searching for morphemes: "+morphemes);
			// TODO: Benoit, c'est parse la liste de morpheme, cherche les dans 
			//   le Trie, et imprime le résultat.
			echo("Search not implemented", 1);
		}
		
	}

}
