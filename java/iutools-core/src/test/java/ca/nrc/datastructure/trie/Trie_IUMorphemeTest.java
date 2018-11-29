package ca.nrc.datastructure.trie;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import ca.nrc.json.PrettyPrinter;

public class Trie_IUMorphemeTest {
	
	/******************************************
	 * DOCUMENTATION TESTS
	 ******************************************/
	
	@Test
	public void test__Trie_IUMorpheme__Synopsis() {
		//
		// Use a Trie_IUMorpheme to index a sequence of 'decompositions' of an Inuktitut word.
		//
		Trie_IUMorpheme iuTrie = new Trie_IUMorpheme();
		try {
			iuTrie.add("inuit");
		} catch (TrieException e) {
		}
		
		// Then, you can retrieve the node that corresponds to a particular decomposition.
		// The argument to getNode is an array of keys:
		//
		TrieNode node = iuTrie.getNode(new String[]{"{inuk/in}"});
		if (node == null) {
			// This means the string was not found in the Trie
		}
		// Or you can retrieve the node that corresponds to a particular surface form.
		// The argument to getNodeInSurface is an array of keys:
		//
		TrieNode node2 = iuTrie.getNodeInSurface(new String[]{"inu","it"});
		if (node2 == null) {
			// This means the string was not found in the Trie
		}
		
		// 	
	}
	
	
	@Test
	public void test__getNodeBySurfaceForm() {
		Trie_IUMorpheme iuTrie = new Trie_IUMorpheme();
		try {
			iuTrie.add("umialiuqti");
		} catch (TrieException e) {
		}
		TrieNode_IUMorpheme node1 = iuTrie.getNodeBySurfaceForm("umialiuqti");
		assertTrue("The node for 'umialiuqti' should not be null.",node1!=null);
		TrieNode_IUMorpheme node2 = iuTrie.getNodeBySurfaceForm("umialiuq");
		assertTrue("The node for 'umialiuq' should not be null.",node2!=null);
		TrieNode_IUMorpheme node3 = iuTrie.getNodeBySurfaceForm("inuit");
		assertTrue("The node for 'inuit' should be null.",node3==null);
	}

	/*
	@Test
	public void test__add_get__Word() {
		StringSegmenter wordSegmenter = new StringSegmenter_Word();
		Trie wordTrie = new Trie(wordSegmenter);
		try {
			wordTrie.add("hello there");
		} catch (TrieException e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode node = wordTrie.getNode(new String[]{"hello"});
		assertTrue("The node for 'hello' is not null.",node!=null);
		assertEquals("The key for this node is correct.","hello",node.getText());
		assertFalse("This node should not a full word.",node.isWord());
	}

	@Test
	public void test__add_get__IUMorpheme_same_word_twice() {
		StringSegmenter iuSegmenter = new StringSegmenter_IUMorpheme();
		Trie iumorphemeTrie = new Trie(iuSegmenter);
		try {
			iumorphemeTrie.add("takujuq");
		} catch (TrieException e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		try {
			TrieNode secondTakujuqNode = iumorphemeTrie.add("takujuq");
			assertTrue("The node added for the second 'takujuq' should not be null.",secondTakujuqNode!=null);
		} catch (TrieException e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
	}
	
	@Test
	public void test__add_get__IUMorpheme_one_word() {
		StringSegmenter iuSegmenter = new StringSegmenter_IUMorpheme();
		Trie iumorphemeTrie = new Trie(iuSegmenter);
		try {
			iumorphemeTrie.add("takujuq");
		} catch (TrieException e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		HashMap children = iumorphemeTrie.getRoot().getChildren();
		String[] childrenKeys = (String[]) children.keySet().toArray(new String[]{});
		TrieNode node = iumorphemeTrie.getNode(new String[]{"{taku/1v}"});
		assertTrue("The node for 'taku/1n' should not be null.",node!=null);
		assertEquals("The key for this node is not correct.","{taku/1v}",node.getText());
		assertFalse("This node should not a full word.",node.isWord());
	}
	
	@Test
	public void test__frequenciesOfWords() {
		StringSegmenter charSegmenter = new StringSegmenter_Char();
		Trie charTrie = new Trie(charSegmenter);
		try {
		charTrie.add("hello");
		charTrie.add("world");
		charTrie.add("hell boy");
		charTrie.add("heaven");
		charTrie.add("worship");
		charTrie.add("world");
		charTrie.add("heaven");
		charTrie.add("world");
		} catch (Exception e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		long freq_blah = charTrie.getFrequency("blah".split(""));
		assertEquals("The frequency of the word 'blah' is wrong.",0,freq_blah);
		long freq_worship = charTrie.getFrequency("worship".split(""));
		assertEquals("The frequency of the word 'worship' is wrong.",1,freq_worship);
		long freq_heaven = charTrie.getFrequency("heaven".split(""));
		assertEquals("The frequency of the word 'heaven' is wrong.",2,freq_heaven);
		long freq_world = charTrie.getFrequency("world".split(""));
		assertEquals("The frequency of the word 'world' is wrong.",3,freq_world);
	}
	
	@Test
	public void test__mostFrequentWordWithRadical() {
		StringSegmenter charSegmenter = new StringSegmenter_Char();
		Trie charTrie = new Trie(charSegmenter);
		try {
		charTrie.add("hello");
		charTrie.add("hint");
		charTrie.add("helicopter");
		charTrie.add("helios");
		charTrie.add("helicopter");
		} catch (Exception e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode mostFrequent = charTrie.getMostFrequentTerminal("hel".split(""));
		assertEquals("The frequency of the most frequent found is wrong.",2,mostFrequent.getFrequency());
		assertEquals("The text of the the most frequent found is wrong.","helicopter",mostFrequent.getText());
	}

	@Test
	public void test_getAllTerminals() throws Exception {
		StringSegmenter charSegmenter = new StringSegmenter_Char();
		Trie charTrie = new Trie(charSegmenter);
		charTrie.add("hello");
		charTrie.add("hit");
		charTrie.add("abba");
		charTrie.add("helios");
		charTrie.add("helm");
		charTrie.add("ok");
		TrieNode[] h_terminals = charTrie.getAllTerminals("h".split(""));
		Assert.assertEquals("The number of words starting with 'h' should be 4.",
				4,h_terminals.length);
		TrieNode[] hel_terminals = charTrie.getAllTerminals("hel".split(""));
		Assert.assertEquals("The number of words starting with 'hel' should be 3.",
				3,hel_terminals.length);
		TrieNode[] o_terminals = charTrie.getAllTerminals("o".split(""));
		Assert.assertEquals("The number of words starting with 'o' should be 1.",
				1,o_terminals.length);
	}
	
	@Test
	public void test_toJSON() throws TrieException {
		StringSegmenter charSegmenter = new StringSegmenter_Char();
		Trie charTrie = new Trie(charSegmenter);
		charTrie.add("he");
		charTrie.add("hit");
		charTrie.add("ok");
		String json = charTrie.toJSON();
		String expected = new String(
			"{\"size\":3,\"segmenterclassname\":\"ca.nrc.datastructure.trie.StringSegmenter_Char\",\"root\":{\"text\":\"\",\"isWord\":false,\"frequency\":0,\"children\":{\"h\":{\"text\":\"h\",\"isWord\":false,\"frequency\":2,\"children\":{\"e\":{\"text\":\"he\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}},\"i\":{\"text\":\"hi\",\"isWord\":false,\"frequency\":1,\"children\":{\"t\":{\"text\":\"hit\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"stats\":{}}},\"stats\":{}},\"o\":{\"text\":\"o\",\"isWord\":false,\"frequency\":1,\"children\":{\"k\":{\"text\":\"ok\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"stats\":{}}},\"stats\":{}}}");
		Assert.assertEquals("The generated JSON representation of the trie is not correct.",expected,json);
	}
	
	*/
}
