package ca.nrc.datastructure.trie;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Ignore;
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
		String[] expExtended = new String[] {"h","i","$"};
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
		String[] keys = new String[] {"h","i","$"};
		String[] gotEscaped = trie.escapeKeys(keys);
		String[] expEscaped = new String[] {
				"h","i","$"};
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
	
	/////////////////////////////////////////////////////////
	// Override and ignore some tests from the parent test 
	// These are tests that are not yet working for the FS 
	// implementation of the Trie
	///////////////////////////////////////////////////////////
	
	@Test @Ignore
	public void test_add__check_terminal_inuktitut() throws Exception {}
		
	@Test @Ignore
	public void test__add_get__IUMorpheme_one_word() throws Exception {}
	
	@Test @Ignore
	public void test__frequenciesOfWords() throws Exception {}
	
	@Test @Ignore
	public void test_getAllTerminals() throws Exception {}
	
	@Test @Ignore
	public void test_getAllTerminals__Case2() throws Exception {}
	
	@Test @Ignore
	public void test_getNbOccurrences() throws Exception {}
	
	@Test @Ignore
	public void test_toJSON__Char() throws Exception {}
	
	@Test @Ignore
	public void test_getMostFrequentTerminal() throws Exception {}
	
	@Test @Ignore
	public void test_getMostFrequentTerminal__Case2() throws Exception {}	
	
	@Test @Ignore
	public void test_getMostFrequentTerminals() throws Exception {}
	
	@Test @Ignore
	public void test__getMostFrequentTerminals__Case2() throws Exception {}
	
	
	@Test @Ignore
	public void test__mostFrequentSequenceForRoot__Char() throws Exception {}
	
	@Test @Ignore
	public void test__mostFrequentSequenceForRoot__IUMorpheme() throws Exception {}
	
	@Test @Ignore
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__1() throws Exception {}
	
	@Test @Ignore
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__2() throws Exception {}
	
	@Test @Ignore
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__3() throws Exception {}	
}
