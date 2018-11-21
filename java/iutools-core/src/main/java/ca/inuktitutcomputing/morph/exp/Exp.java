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
import ca.nrc.datastructure.trie.TrieNode;

public class Exp {

	private static Trie root_trie;
	private static Trie suff_trie;

	public static void main(String[] args) {
		root_trie = new Trie(new StringSegmenter_Char());
		root_trie.add("umiaq/1n");
		suff_trie = new Trie(new StringSegmenter_Char());
		suff_trie.add("liuq/1nv");
		suff_trie.add("ti/1vn");
		suff_trie.add("u/1nv");
		suff_trie.add("juq/tv-ger-3s");
		suff_trie.add("juq/1vn");
		suff_trie.add("mik/tn-acc-s");

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

		// Parse the root
		for (int i = 0; i < chars.length; i++) {
			previousKeys = (Vector<String>) currentKeys.clone();
			currentKey = chars[i];
			currentKeys.add(chars[i]);
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
		if (rootsForCompleteSurfaceForm.length==0) {
			if (root != null) {
				HashMap<String, TrieNode> children = root.getChildren();
				String[] childrenKeys = children.keySet().toArray(
						new String[] {});
				for (int i = 0; i < childrenKeys.length; i++) {
					TrieNode nextNode = children.get(childrenKeys[i]);
					HashMap<String, TrieNode> nextChildren = nextNode
							.getChildren();
					String[] nextChildrenKeys = nextChildren.keySet().toArray(
							new String[] {});
					for (int j = 0; j < nextChildrenKeys.length; j++) {
						if (nextChildrenKeys[j].equals("/")) {
							Vector<String> slashKeys = new Vector<String>();
							slashKeys.addAll(previousKeys);
							slashKeys.add(childrenKeys[i]);
							slashKeys.add("/");
							String[] keys = slashKeys
									.toArray(new String[] {});
							TrieNode slashNode = root_trie.getNode(keys);
							TrieNode[] possibleRoots = slashNode
									.getAllTerminals();
							if (childrenKeys[i].equals("q")) {
								if (currentKey.equals("r")) {
									eatenKeys.add(currentKey);
									rootsForIncompleteSurfaceForm = possibleRoots
											.clone();
								}
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
		}
	}
}
