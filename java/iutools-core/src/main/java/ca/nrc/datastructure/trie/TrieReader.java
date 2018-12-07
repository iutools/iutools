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
		
		FileReader fr;
		try {
			fr = new FileReader(trieFilePath);
		} catch (IOException e) {
			throw new TrieException("Exception while reading all bytes of file '"+trieFilePath+"'.",e);
		}
		Gson gson = new Gson();
		Trie trie = gson.fromJson(fr, Trie.class);
		return trie;
	}

}
