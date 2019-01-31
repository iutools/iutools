package ca.inuktitutcomputing.morph.exp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;

/*
 * Idée générale : à l'aide de structures Trie pour les formes de surface
 * des morphèmes
 */

public class Dec {

	private static Trie root_trie;
	private static Trie suffix_trie;
	
	
	/**
	 * MAIN
	 * @param args
	 */
	public static void main(String[] args) {
		String[] roots = new String[]{
				"umiaq/1n", "inuk/1n", "taku/1v"
		};
		String[] suffixes = new String[]{
				"liuq/1nv", "ti/1vn", "u/1nv",
				"juq/tv-ger-3s", "juq/1vn", "mik/tn-acc-s",
				"lu/1q"
		};
		root_trie = new Trie();
		for (int i=0; i<roots.length; i++) {
			try {
				root_trie.add(roots[i].split(""));
			} catch (TrieException e) {
				// TODO Auto-generated catch block
			}
		}
		suffix_trie = new Trie();
		for (int i=0; i<suffixes.length; i++) {
			try {
				suffix_trie.add(suffixes[i].split(""));
			} catch (TrieException e) {
				// TODO Auto-generated catch block
			}
		} 

		System.out.print("Enter a word: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String word;
		try {
			word = reader.readLine();
			process(word);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void process(String string) {
		String[] chars = string.split("");
		String nextPartOfWord = null;
		Vector<String> previousKeys = null;
		Vector<String> currentKeys = new Vector<String>();
		Vector<String> eatenKeys = new Vector<String>();
		TrieNode previousNode = null;
		TrieNode currentRootNode = null;
		TrieNode root = null;
		TrieNode[] rootsForCompleteSurfaceForm = new TrieNode[]{};
		TrieNode[] rootsForIncompleteSurfaceForm = new TrieNode[]{};
		TrieNode[] suffixesForCompleteSurfaceForm = new TrieNode[]{};
		TrieNode[] suffixesForIncompleteSurfaceForm = new TrieNode[]{};
		String currentKey = null;
		
		// root
		Object[] possibleRoot = findMorpheme(chars, root_trie);
		if (possibleRoot==null) {
			System.out.println("No root was found for this word.");
			return;
		}
		eatenKeys = (Vector<String>) possibleRoot[0];
		rootsForCompleteSurfaceForm = (TrieNode[]) possibleRoot[1];
		rootsForIncompleteSurfaceForm = (TrieNode[]) possibleRoot[2];
		
		System.out.println("Surface root: "
					+ Arrays.toString(eatenKeys.toArray(new String[] {})));
		for (int i = 0; i < rootsForCompleteSurfaceForm.length; i++) {
				System.out.println("Possible root (complete surface form): "
						+ rootsForCompleteSurfaceForm[i].getKeysAsString());
		}
		for (int i = 0; i < rootsForIncompleteSurfaceForm.length; i++) {
				System.out.println("Possible root (incomplete surface form): "
						+ rootsForIncompleteSurfaceForm[i].getKeysAsString());
		}
		System.out.println("\nAnalyser le reste du mot à partir du caractère "+(eatenKeys.size())+" : "+string.substring(eatenKeys.size()));
		
		// suffixes and endings
		nextPartOfWord = string.substring(eatenKeys.size());
		Object[] possibleSuffix = findMorpheme(nextPartOfWord.split(""),suffix_trie);
		if (possibleSuffix==null) {
			System.out.println("No suffix was found for this part of word: "+nextPartOfWord+".");
			return;
		}
		eatenKeys = (Vector<String>) possibleSuffix[0];
		suffixesForCompleteSurfaceForm = (TrieNode[]) possibleSuffix[1];
		suffixesForIncompleteSurfaceForm = (TrieNode[]) possibleSuffix[2];
		
		System.out.println("Surface suffix: "
					+ Arrays.toString(eatenKeys.toArray(new String[] {})));
		for (int i = 0; i < suffixesForCompleteSurfaceForm.length; i++) {
				System.out.println("Possible suffix (complete surface form): "
						+ suffixesForCompleteSurfaceForm[i].getKeysAsString());
		}
		for (int i = 0; i < suffixesForIncompleteSurfaceForm.length; i++) {
				System.out.println("Possible suffix (incomplete surface form): "
						+ suffixesForIncompleteSurfaceForm[i].getKeysAsString());
		}
		
		nextPartOfWord = nextPartOfWord.substring(eatenKeys.size());
		if (nextPartOfWord != "")
			System.out.println("\nAnalyser le reste du mot à partir du caractère "+(eatenKeys.size())+" : "+nextPartOfWord.substring(eatenKeys.size()));
	}

	private static Object[] findMorpheme(String[] chars, Trie trie) {
		Vector<String> previousKeys = null;
		Vector<String> currentKeys = new Vector<String>();
		Vector<String> eatenKeys = new Vector<String>();
		TrieNode previousNode = null;
		TrieNode currentNode = null;
		TrieNode morpheme = null;
		TrieNode[] morphemesForCompleteSurfaceForm = new TrieNode[]{};
		TrieNode[] morphemesForIncompleteSurfaceForm = new TrieNode[]{};
		String currentKey = null;
		int charCounter;
		// Parse the morpheme
		for (charCounter = 0; charCounter < chars.length; charCounter++) {
			previousKeys = (Vector<String>) currentKeys.clone();
			currentKey = chars[charCounter];
			currentKeys.add(chars[charCounter]);
			currentNode = trie.getNode(currentKeys
					.toArray(new String[] {}));
			if (currentNode == null) {
				if (previousNode != null) {
					morpheme = previousNode;
					break;
				} else {
					System.out.println("No morpheme starts with '"
							+ Arrays.toString(currentKeys
									.toArray(new String[] {})) + "'.");
					break;
				}
			} else {
				Vector<String> searchKeys = (Vector<String>) currentKeys
						.clone();
				searchKeys.add("/");
				System.out.println("Search for "+Arrays.toString(searchKeys.toArray(new String[] {})));
				TrieNode slashNode = trie.getNode(searchKeys
						.toArray(new String[] {}));
				if (slashNode != null) {
					morpheme = currentNode;
					morphemesForCompleteSurfaceForm = slashNode.getAllTerminals();
					eatenKeys = (Vector<String>) currentKeys.clone();
					break;
				}
			}
			previousNode = currentNode;
			eatenKeys = (Vector<String>) currentKeys.clone();
		}
		
		/*
		 *  Arrivés ici, on a trouvé un chemin dans le trie des racines avec un 
		 *  maximum de lettres du mot initial, mais ce n'est pas une racine complète.
		 *  Comme il est possible qu'une consonne finale ait été supprimée, on va 
		 *  avancer d'un nœud et vérifier si cela nous amène à une racine complète.
		 */
		System.out.println("morpheme: "+morpheme.getKeysAsString());
		
		if (morpheme==null) {
			return null;
		}
		
		if (morphemesForCompleteSurfaceForm.length==0) {
				HashMap<String, TrieNode> children = morpheme.getChildren();
				String[] childrenKeys = children.keySet().toArray(
						new String[] {});
				for (int i = 0; i < childrenKeys.length; i++) {
					TrieNode nextNode = children.get(childrenKeys[i]);
					System.out.println("nextNode: "+nextNode.getKeysAsString());
					HashMap<String, TrieNode> nextChildren = nextNode
							.getChildren();
					String[] nextChildrenKeys = nextChildren.keySet().toArray(
							new String[] {});
					for (int j = 0; j < nextChildrenKeys.length; j++) {
						if (nextChildrenKeys[j].equals("/")) {
							System.out.println("/");
							TrieNode slashNode = nextChildren.get(nextChildrenKeys[j]);
							TrieNode[] possibleRoots = slashNode.getAllTerminals();
							for (int k=0; k<possibleRoots.length; k++) {
								System.out.println("possible root: "+possibleRoots[k].getKeysAsString());
							}
							System.out.println("childrenKeys[i]: "+childrenKeys[i]);
							if (childrenKeys[i].equals("q")) {
								if (currentKey.equals("r")) {
									eatenKeys.add(currentKey);
								}
								morphemesForIncompleteSurfaceForm = possibleRoots
											.clone();
							} else if (childrenKeys[i].equals("k")) {
								if (currentKey.equals("g")||currentKey.equals("N")) {
									eatenKeys.add(currentKey);
								} else if (currentKey.equals("l")||currentKey.equals("v")) {
									eatenKeys.add(currentKey);
								}
							}
						}
					}
				}
		}
		return new Object[] {eatenKeys,morphemesForCompleteSurfaceForm,morphemesForIncompleteSurfaceForm};
	}
}
