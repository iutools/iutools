package ca.pirurvik.iutools.core;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieWithSegmenterClassname;

import com.google.gson.Gson;

public class CorpusTrieRetriever {

	public static void main(String[] args) throws Exception {
		
		String dirName = args[0];
		File dir = new File(dirName);
		String trieDumpFileName = dir.getName()+"-"+"trie_dump.txt";
		String fileString = new String(Files.readAllBytes(Paths.get(trieDumpFileName)));
		Gson gson = new Gson();
		TrieWithSegmenterClassname trieWithoutSegmenter = gson.fromJson(fileString, TrieWithSegmenterClassname.class);
		Trie trie = trieWithoutSegmenter.toTrie();
		System.out.println("size: "+trie.getSize());
		
	}

}
