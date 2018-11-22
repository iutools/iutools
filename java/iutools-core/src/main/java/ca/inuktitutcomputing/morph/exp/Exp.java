package ca.inuktitutcomputing.morph.exp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import ca.nrc.datastructure.trie.StringSegmenter_Char;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;

public class Exp {

	private static Trie root_trie;
	private static Trie suff_trie;

	public static void main(String[] args) {
		String[] roots = new String[]{
				"umiaq/1n"
		};
		String[] suffixes = new String[]{
				"liuq/1nv", "ti/1vn", "u/1nv",
				"juq/tv-ger-3s", "juq/1vn", "mik/tn-acc-s"
		};
		root_trie = new Trie(new StringSegmenter_Char());
		for (int i=0; i<roots.length; i++) {
			try {
				root_trie.add(roots[i]);
			} catch (TrieException e) {
				// TODO Auto-generated catch block
			}
		}
		suff_trie = new Trie(new StringSegmenter_Char());
		for (int i=0; i<suffixes.length; i++) {
			try {
				suff_trie.add(suffixes[i]);
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
		Vector<String> previousKeys = null;
		Vector<String> currentKeys = new Vector<String>();
		Vector<String> eatenKeys = new Vector<String>();
		TrieNode previousNode = null;
		TrieNode currentRootNode = null;
		TrieNode root = null;
		TrieNode[] rootsForCompleteSurfaceForm = new TrieNode[]{};
		TrieNode[] rootsForIncompleteSurfaceForm = new TrieNode[]{};
		String currentKey = null;

		int charCounter;
		// Parse the root
		for (charCounter = 0; charCounter < chars.length; charCounter++) {
			previousKeys = (Vector<String>) currentKeys.clone();
			currentKey = chars[charCounter];
			currentKeys.add(chars[charCounter]);
			currentRootNode = root_trie.getNode(currentKeys
					.toArray(new String[] {}));
			if (currentRootNode == null) {
				if (previousNode != null) {
					root = previousNode;
					break;
				} else {
					System.out.println("No root starts with '"
							+ Arrays.toString(currentKeys
									.toArray(new String[] {})) + "'.");
					break;
				}
			} else {
				Vector<String> searchKeys = (Vector<String>) currentKeys
						.clone();
				searchKeys.add("/");
				System.out.println("Search for "+Arrays.toString(searchKeys.toArray(new String[] {})));
				TrieNode slashNode = root_trie.getNode(searchKeys
						.toArray(new String[] {}));
				if (slashNode != null) {
					rootsForCompleteSurfaceForm = slashNode.getAllTerminals();
					eatenKeys = (Vector<String>) currentKeys.clone();
					break;
				}
			}
			previousNode = currentRootNode;
			eatenKeys = (Vector<String>) currentKeys.clone();
		}
		
		/*
		 *  Arrivés ici, on a trouvé un chemin dans le trie des racines avec un maximum de lettres du mot
		 *  initial, mais ce n'est pas une racine complète.
		 *  Comme il est possible qu'une consonne finale ait été supprimée, on va avancer d'un nœud et vérifier
		 *  si cela nous amène à une racine complète.
		 */
		System.out.println("root: "+root.getText());
		
		if (rootsForCompleteSurfaceForm.length==0) {
			if (root != null) {
				HashMap<String, TrieNode> children = root.getChildren();
				String[] childrenKeys = children.keySet().toArray(
						new String[] {});
				for (int i = 0; i < childrenKeys.length; i++) {
					TrieNode nextNode = children.get(childrenKeys[i]);
					System.out.println("nextNode: "+nextNode.getText());
					HashMap<String, TrieNode> nextChildren = nextNode
							.getChildren();
					String[] nextChildrenKeys = nextChildren.keySet().toArray(
							new String[] {});
					for (int j = 0; j < nextChildrenKeys.length; j++) {
						if (nextChildrenKeys[j].equals("/")) {
							System.out.println("/");
							
//							Vector<String> slashKeys = new Vector<String>();
//							slashKeys.addAll(previousKeys);
//							slashKeys.add(childrenKeys[i]);
//							slashKeys.add("/");
//							String[] keys = slashKeys
//									.toArray(new String[] {});
//							TrieNode slashNode = root_trie.getNode(keys);
							TrieNode slashNode = nextChildren.get(nextChildrenKeys[j]);
							TrieNode[] possibleRoots = slashNode.getAllTerminals();
							for (int k=0; k<possibleRoots.length; k++) {
								System.out.println("possible root: "+possibleRoots[k].getText());
							}
							System.out.println("childrenKeys[i]: "+childrenKeys[i]);
							if (childrenKeys[i].equals("q")) {
								if (currentKey.equals("r")) {
									eatenKeys.add(currentKey);
								}
								rootsForIncompleteSurfaceForm = possibleRoots
											.clone();
							}
						}
					}
				}
			} else {
				System.out.println("No root was found for this word.");
			}
			System.out.println("Surface root: "
					+ Arrays.toString(eatenKeys.toArray(new String[] {})));
			for (int i = 0; i < rootsForCompleteSurfaceForm.length; i++) {
				System.out.println("Possible root (complete surface form): "
						+ rootsForCompleteSurfaceForm[i].getText());
			}
			for (int i = 0; i < rootsForIncompleteSurfaceForm.length; i++) {
				System.out.println("Possible root (incomplete surface form): "
						+ rootsForIncompleteSurfaceForm[i].getText());
			}
			
			
			System.out.println("\nAnalyser le reste du mot à partir du caractère "+charCounter+" : "+string.substring(charCounter));
		}
	}
}
