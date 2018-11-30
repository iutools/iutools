package ca.nrc.datastructure.trie;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

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
		StringSegmenter charSegmenter = new StringSegmenter_Char();
		Trie charTrie = new Trie(charSegmenter);
		
		// If instead you wanted to index string by words,
		// you would create you Trie as follows:
		//
		StringSegmenter wordSegmenter = new StringSegmenter_Word();
		Trie wordTrie = new Trie(wordSegmenter);

		// If instead you wanted to index string by inuktitut morphemes,
		// you would create you WordTrie as follows:
		//
		StringSegmenter morphemeSegmenter = new StringSegmenter_IUMorpheme();
		Trie morphemeTrie = new Trie(morphemeSegmenter);
		
		// For the rest of the test we will use a character-based trie.
		//
		// The first thing you need to do is add words to the trie:
		//
		try {
			charTrie.add("hello");
		} catch (TrieException e) {
		}
		try {
			charTrie.add("world");
		} catch (TrieException e) {
		}
		try {
			charTrie.add("hell boy");
		} catch (TrieException e) {
		}
		try {
			charTrie.add("heaven");
		} catch (TrieException e) {
		}
		try {
			charTrie.add("worship");
		} catch (TrieException e) {
		}
		
		// Then, you can retrieve the node that corresponds to a particular string.
		// The argument to getNode is an array of keys:
		//
		TrieNode node = charTrie.getNode("hell".split(""));
		if (node == null) {
			// This means the string was not found in the Trie
		}
		
		// 	
	}
	
	@Test
	public void test__add_get__Char() {
		StringSegmenter charSegmenter = new StringSegmenter_Char();
		Trie charTrie = new Trie(charSegmenter);
		try {
			charTrie.add("hello");
			charTrie.add("hell boy");
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
	public void test_toJSON__Char() throws TrieException {
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

	/**
	 * This test shows that one can't simply read a jsoned trie back into a trie.
	 * The 'segmenter' attribute, of the class StringSegmenter' cannot recreate
	 * a segmenter object because StringSegmenter is an abstract class.
	 * 
	 * @throws TrieException
	 */
	@Test(expected=RuntimeException.class)
	public void test_toJSON__IUMorpheme() throws TrieException {
		StringSegmenter segmenter = new StringSegmenter_IUMorpheme();
		Trie trie = new Trie(segmenter);
		trie.add("inuit");
		trie.add("takujuq");
		Gson gson = new Gson();
		String json = gson.toJson(trie);
		String expected = new String(
			"{\"size\":2,\"segmenter\":{},\"root\":{\"text\":\"\",\"isWord\":false,\"frequency\":0,\"children\":{\"{taku/1v}\":{\"text\":\"{taku/1v}\",\"isWord\":false,\"frequency\":1,\"children\":{\"{juq/1vn}\":{\"text\":\"{taku/1v}{juq/1vn}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"stats\":{}},\"{inuk/1n}\":{\"text\":\"{inuk/1n}\",\"isWord\":false,\"frequency\":1,\"children\":{\"{it/tn-nom-p}\":{\"text\":\"{inuk/1n}{it/tn-nom-p}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"stats\":{}}},\"stats\":{}}}");
		Assert.assertEquals("The generated JSON representation of the trie is not correct.",expected,json);
		Trie reconstitutedTrie = gson.fromJson(json,Trie.class);
	}

}
