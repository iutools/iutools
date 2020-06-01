package ca.nrc.datastructure.trie;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public abstract class Trie_InFileSystemTest extends TrieTest {


	@Override
	public Trie makeTrieToTest() throws Exception {
		Path rootDir = Files.createTempDirectory("trie");
		Trie_InFileSystem trie = new Trie_InFileSystem(rootDir.toFile());
		return trie;
	}

}
