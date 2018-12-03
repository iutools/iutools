package ca.nrc.datastructure.trie;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import ca.nrc.testing.AssertHelpers;

import com.google.gson.Gson;

public class TrieTest {
	
	/******************************************
	 * DOCUMENTATION TESTS
	 ******************************************/
	
	@Test
	public void test__Trie__Synopsis() {
		//
		// Use a Trie to index a sequence of 'parts' of a string.
		//
		// Depending on your application, each part may consist of
		//
		// - an individual character
		// - a word
		// - a morpheme
		// - anything else that makes sense to you (as long as it is a string)
		//
		// Let's say you want to use individual characters as the 
		// parts. You would then create a Trie as follows:
		//
		Trie trie = new Trie();
		
		// For the rest of the test we will use a character-based trie.
		//
		// The first thing you need to do is add words to the trie:
		//
		try {
			trie.add(new String[]{"h","e","l","l","o"});
		} catch (TrieException e) {
		}
		
		// Then, you can retrieve the node that corresponds to a particular string.
		// The argument to getNode is an array of keys:
		//
		TrieNode node = trie.getNode("hell".split(""));
		if (node == null) {
			// This means the string was not found in the Trie
		}
		
		// 	
	}
	
	@Test
	public void test__add_get__Char() {
		Trie charTrie = new Trie();
		try {
			charTrie.add(new String[]{"h","e","l","l","o"});
			charTrie.add(new String[]{"h","e","l","l"," ","b","o","y"});
		} catch (TrieException e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode node = charTrie.getNode("hello".split(""));
		assertTrue("The node for 'hello' is not null.",node!=null);
		assertEquals("The key for this node is correct.","hello",node.getText());
		assertTrue("This node represents a full word.",node.isWord());
		
		node = charTrie.getNode("hell".split(""));
		assertTrue("The node for 'hell' is not null.",node!=null);
		assertEquals("The key for this node is correct.","hell",node.getText());
		assertEquals("The frequency for this node is correct.",2,node.getFrequency());
		assertFalse("This node does not represent a full word.",node.isWord());
	}

	@Test
	public void test__add_get__Word() {
		Trie wordTrie = new Trie();
		try {
			wordTrie.add(new String[]{"hello","there"});
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
		Trie iumorphemeTrie = new Trie();
		String[] takujuq_segments = null;
		try {
			takujuq_segments = iuSegmenter.segment("takujuq");
			iumorphemeTrie.add(takujuq_segments);
		} catch (Exception e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		try {
			TrieNode secondTakujuqNode = iumorphemeTrie.add(takujuq_segments);
			assertTrue("The node added for the second 'takujuq' should not be null.",secondTakujuqNode!=null);
		} catch (TrieException e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
	}
	
	@Test
	public void test__add_get__IUMorpheme_one_word() {
		StringSegmenter iuSegmenter = new StringSegmenter_IUMorpheme();
		Trie iumorphemeTrie = new Trie();
		String[] takujuq_segments = null;
		try {
			takujuq_segments = iuSegmenter.segment("takujuq");
			iumorphemeTrie.add(takujuq_segments);
		} catch (Exception e) {
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
		Trie charTrie = new Trie();
		try {
		charTrie.add("hello".split(""));
		charTrie.add("world".split(""));
		charTrie.add("hell boy".split(""));
		charTrie.add("heaven".split(""));
		charTrie.add("worship".split(""));
		charTrie.add("world".split(""));
		charTrie.add("heaven".split(""));
		charTrie.add("world".split(""));
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
		Trie charTrie = new Trie();
		try {
		charTrie.add("hello".split(""));
		charTrie.add("hint".split(""));
		charTrie.add("helicopter".split(""));
		charTrie.add("helios".split(""));
		charTrie.add("helicopter".split(""));
		} catch (Exception e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode mostFrequent = charTrie.getMostFrequentTerminal("hel".split(""));
		assertEquals("The frequency of the most frequent found is wrong.",2,mostFrequent.getFrequency());
		assertEquals("The text of the the most frequent found is wrong.","helicopter",mostFrequent.getText());
	}

	@Test
	public void test_getAllTerminals() throws Exception {
		Trie charTrie = new Trie();
		charTrie.add("hello".split(""));
		charTrie.add("hit".split(""));
		charTrie.add("abba".split(""));
		charTrie.add("helios".split(""));
		charTrie.add("helm".split(""));
		charTrie.add("ok".split(""));
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
	public void test_toJSON__Char() throws TrieException {
		Trie charTrie = new Trie();
		charTrie.add("he".split(""));
		charTrie.add("hit".split(""));
		charTrie.add("ok".split(""));
		String json = charTrie.toJSON();
		String expected = new String(
			"{\"size\":3,\"root\":{\"text\":\"\",\"isWord\":false,\"frequency\":0,\"children\":{\"h\":{\"text\":\"h\",\"isWord\":false,\"frequency\":2,\"children\":{\"e\":{\"text\":\"he\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}},\"i\":{\"text\":\"hi\",\"isWord\":false,\"frequency\":1,\"children\":{\"t\":{\"text\":\"hit\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"stats\":{}}},\"stats\":{}},\"o\":{\"text\":\"o\",\"isWord\":false,\"frequency\":1,\"children\":{\"k\":{\"text\":\"ok\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"stats\":{}}},\"stats\":{}}}");
		
		AssertHelpers.assertStringEquals("The generated JSON representation of the trie is not correct.",expected,json);
//		Assert.assertEquals("The generated JSON representation of the trie is not correct.",expected,json);
	}

}
