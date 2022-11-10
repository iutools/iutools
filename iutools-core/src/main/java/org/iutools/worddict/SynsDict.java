package org.iutools.worddict;

import java.util.*;

/**
 * Captures synonymous relations
 */
public class SynsDict {

	public String lang = null;
	protected Map<String, Set<String>> synsIndex =
		new HashMap<String,Set<String>>();

	// Empty constructor for Jackson serialization
	public SynsDict() {
		init_Synset((String)null);
	}


	public SynsDict(String _lang) {
		init_Synset(_lang);
	}

	private void init_Synset(String _lang) {
		this.lang = _lang;
	}

	public void addSynset(String... synonymousExpressions) {
		lowercaseExpressions(synonymousExpressions);
		for (String word: synonymousExpressions) {
			if (!synsIndex.containsKey(word)) {
				synsIndex.put(word, new HashSet<String>());
			}
			Set<String> syns4word = synsIndex.get(word);
			for (String otherWord: synonymousExpressions) {
				if (!otherWord.equals(word)) {
					syns4word.add(otherWord);
				}
			}
			int x = 0;
		}
	}

	private void lowercaseExpressions(String[] expressions) {
		if (expressions != null) {
			for (int ii=0; ii < expressions.length; ii++) {
				expressions[ii] = expressions[ii].toLowerCase();
			}
		}
	}

	public Set<String> synonymsFor(String word) {
		Set<String> syns = new HashSet<String>();
		if (synsIndex.containsKey(word)) {
			syns = synsIndex.get(word);
		}
		return syns;
	}
}
