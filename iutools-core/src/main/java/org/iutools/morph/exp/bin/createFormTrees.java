package org.iutools.morph.exp.bin;

import org.iutools.datastructure.trie.Trie_InMemory;
import org.iutools.linguisticdata.SurfaceFormsHandler;

public class createFormTrees {

	/**
	 * This command generates all the surface forms of all the morphemes
	 * of the database and stores them in "trie" structures in JSON files
	 * in the resources directory.
	 * @param args String[] – args[0]: either "root" or "affix"
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String type = null;
		boolean verbose = false;
		int iarg = 0;
		System.out.println("args: "+String.join("; ",args));
		if (args.length==0) {
			usage("Missing argument(s).");
			System.exit(0);
		}
		if (args[iarg].startsWith("--")) {
			if (!args[iarg].equals("--verbose")) {
				usage("Wrong option or option misspelled.");
				System.exit(0);
			} else {
				verbose = true;
				iarg++;
			}
		}
		if (!args[iarg].equals("root") && !args[iarg].equals("affix")) {
			usage("Wrong argument or argument misspelled ("+args[iarg]+").");
			System.exit(0);
		} else {
			type = args[iarg];
		}
		SurfaceFormsHandler.setVerbose(verbose);
		Trie_InMemory trie = SurfaceFormsHandler.compileSurfaceFormsTrieForMorphemeType(type);
		SurfaceFormsHandler.saveTrieToFile(type, trie);
	}

	private static void usage(String message) {
		System.out.println("\nCommand not called properly: "+message);
		System.out.println("\nusage: appCreateFormTrees <options>* <type of morpheme>");
		System.out.println("       <options>: --verbose: print information during the process\n");
		System.out.println("       <type of morpheme>: the string 'root' or 'affix'\n");
	}


}
