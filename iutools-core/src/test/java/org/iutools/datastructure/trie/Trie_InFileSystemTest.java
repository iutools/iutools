package org.iutools.datastructure.trie;

import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import ca.nrc.testing.AssertObject;

public class Trie_InFileSystemTest extends TrieTest {

	@Override
	public Trie_InFileSystem makeTrieToTest() throws Exception {
		Path rootDir = Files.createTempDirectory("trie");
		Trie_InFileSystem trie = new Trie_InFileSystem(rootDir.toFile());
		return trie;
	}
	
	/////////////////////////
	// VERIFICATION TESTS
	/////////////////////////

	@Test
	public void test__appendTerminalKey__HappyPath() throws Exception {
		Trie_InFileSystem trie = makeTrieToTest();
		String[] keys = "hi".split("");
		String[] gotExtended = trie.appendTerminalKey(keys);
		String[] expExtended = new String[] {"h","i",TrieNode.TERMINAL_SEG};
		AssertObject.assertDeepEquals(
			"Terminal key not appended to original keys", 
			expExtended, gotExtended);		
	}
	
	@Test
	public void test__escape_unescapeKeys__HappyPath() throws Exception {
		Trie_InFileSystem trie = makeTrieToTest();
		String[] keys = new String[] {"h","i","!"};
		String[] gotEscaped = trie.escapeKeys(keys);
		String[] expEscaped = new String[] {
				"h","i","%21"};
		AssertObject.assertDeepEquals(
			"Escaped keys not as expected", 
			expEscaped, gotEscaped);
		
		String[] gotUnescaped = trie.unescapeKeys(keys);
		AssertObject.assertDeepEquals(
				"Unescaped keys should have been like the original keys", 
				keys, gotUnescaped);
	}
	
	@Test
	public void test__escape_unescapeKeys__TrailingCharIsDollar__LeavesItAlone() throws Exception {
		Trie_InFileSystem trie = makeTrieToTest();
		String[] keys = new String[] {"h","i",TrieNode.TERMINAL_SEG};
		String[] gotEscaped = trie.escapeKeys(keys);
		String[] expEscaped = new String[] {
				"h","i",TrieNode.TERMINAL_SEG};
		AssertObject.assertDeepEquals(
			"Escaped keys not as expected", 
			expEscaped, gotEscaped);
		
		String[] gotUnescaped = trie.unescapeKeys(keys);
		AssertObject.assertDeepEquals(
				"Unescaped keys should have been like the original keys", 
				keys, gotUnescaped);
	}
	
	@Test
	public void test__escape_unescapeKeys__DollarButNotInLastPosition__EscapesTheDollarSign() throws Exception {
		Trie_InFileSystem trie = makeTrieToTest();
		String[] keys = new String[] {"h","i","$","h","i"};
		String[] gotEscaped = trie.escapeKeys(keys);
		String[] expEscaped = new String[] {
				"h","i","%24","h","i"};
		AssertObject.assertDeepEquals(
			"Escaped keys not as expected", 
			expEscaped, gotEscaped);
		
		String[] gotUnescaped = trie.unescapeKeys(keys);
		AssertObject.assertDeepEquals(
				"Unescaped keys should have been like the original keys", 
				keys, gotUnescaped);
	}

	@Test
	public void test__escape_unescapeKeys__MixedCase() throws Exception {
		Trie_InFileSystem trie = makeTrieToTest();
		String[] keys = new String[] {"h","i","$","/","h","e","l","l","o","!"};
		String[] gotEscaped = trie.escapeKeys(keys);
		String[] expEscaped = new String[] {
				"h","i","%24","%2F","h","e","l","l","o","%21"};
		AssertObject.assertDeepEquals(
			"Escaped keys not as expected", 
			expEscaped, gotEscaped);
		
		String[] gotUnescaped = trie.unescapeKeys(keys);
		AssertObject.assertDeepEquals(
				"Unescaped keys should have been like the original keys", 
				keys, gotUnescaped);
	}	

	/////////////////////////////
	// TEST HELPERS
	/////////////////////////////

}
