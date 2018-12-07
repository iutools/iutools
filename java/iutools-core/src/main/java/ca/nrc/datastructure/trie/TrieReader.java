package ca.nrc.datastructure.trie;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieWithSegmenterClassname;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class TrieReader {

	public Trie read(String trieFilePath) throws TrieException {
		
		try {
			FileReader fr;
			fr = new FileReader(trieFilePath);
			Gson gson = new Gson();
			Trie trie = gson.fromJson(fr, Trie.class);
			fr.close();
			return trie;
		} catch (IOException e) {
			throw new TrieException("Exception while reading all bytes of file '"+trieFilePath+"'.",e);
		}
	}

}
