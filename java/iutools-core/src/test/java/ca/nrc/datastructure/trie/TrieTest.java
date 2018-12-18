package ca.nrc.datastructure.trie;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import ca.nrc.testing.AssertHelpers;

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
		assertEquals("The key for this node is correct.","h e l l o",node.getKeysAsString());
		assertTrue("This node represents a full word.",node.isWord());
		
		node = charTrie.getNode("hell".split(""));
		assertTrue("The node for 'hell' is not null.",node!=null);
		assertEquals("The key for this node is correct.","h e l l",node.getKeysAsString());
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
		assertEquals("The key for this node is correct.","hello",node.getKeysAsString());
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
		TrieNode node = iumorphemeTrie.getNode(new String[]{"{taku/1v}"});
		assertTrue("The node for 'taku/1n' should not be null.",node!=null);
		assertEquals("The key for this node is not correct.","{taku/1v}",node.getKeysAsString());
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
	public void test_getNbOccurrences() throws Exception {
		Trie charTrie = new Trie();
		charTrie.add("hello".split(""));
		charTrie.add("hello".split(""));
		charTrie.add("hit".split(""));
		charTrie.add("abba".split(""));
		charTrie.add("helios".split(""));
		charTrie.add("helm".split(""));
		charTrie.add("ok".split(""));
		charTrie.add("ok".split(""));
		Assert.assertEquals("The number of terminals should be 6.",6,charTrie.getAllTerminals().length);
		long nb = charTrie.getNbOccurrences();
		Assert.assertEquals("The number of occurrences is wrong.",8,nb);
	}
	
	
	
	@Test
	public void test_toJSON__Char() throws TrieException {
		Trie charTrie = new Trie();
		charTrie.add("he".split(""));
		charTrie.add("hit".split(""));
		charTrie.add("ok".split(""));
		String json = charTrie.toJSON();
		String expected = new String(
				"{\"root\":{\"keys\":[],\"isWord\":false,\"frequency\":0," +
				"\"children\":{\"h\":{\"keys\":[\"h\"],\"isWord\":false,\"frequency\":2," + 
				"\"children\":{\"e\":{\"keys\":[\"h\",\"e\"],\"isWord\":true,\"frequency\":1," + 
				"\"children\":{},\"stats\":{}},\"i\":{\"keys\":[\"h\",\"i\"],\"isWord\":false,\"frequency\":1," + 
				"\"children\":{\"t\":{\"keys\":[\"h\",\"i\",\"t\"],\"isWord\":true,\"frequency\":1," + 
				"\"children\":{},\"stats\":{}}},\"stats\":{}}},\"stats\":{}},\"o\":{\"keys\":[\"o\"],\"isWord\":false,\"frequency\":1," + 
				"\"children\":{\"k\":{\"keys\":[\"o\",\"k\"],\"isWord\":true,\"frequency\":1," + 
				"\"children\":{},\"stats\":{}}},\"stats\":{}}},\"stats\":{}}}");			
		AssertHelpers.assertStringEquals("The generated JSON representation of the trie is not correct.",expected,json);
	}
	
	@Test
	public void test_getMostFrequentTerminal() throws TrieException {
		Trie charTrie = new Trie();
		charTrie.add("hello".split(""));
		charTrie.add("hello".split(""));
		charTrie.add("hells".split(""));
		charTrie.add("hellam".split(""));
		charTrie.add("hellam".split(""));
		charTrie.add("hellam".split(""));
		charTrie.add("hit".split(""));
		charTrie.add("abba".split(""));
		charTrie.add("helios".split(""));
		charTrie.add("helm".split(""));
		charTrie.add("ok".split(""));
		charTrie.add("ok".split(""));
		TrieNode mostFrequentTerminalRelated = charTrie.getMostFrequentTerminal("hell".split(""));
		String expected = "h e l l a m";
		assertEquals("The terminal returned as the most frequent terminal related to 'hell' is wrong.",expected,mostFrequentTerminalRelated.getKeysAsString());
	}
	
	@Test
	public void test_getNMostFrequentTerminals() throws TrieException {
		Trie charTrie = new Trie();
		charTrie.add("hello".split(""));
		charTrie.add("hello".split(""));
		charTrie.add("hells".split(""));
		charTrie.add("hellam".split(""));
		charTrie.add("hellam".split(""));
		charTrie.add("hellam".split(""));
		charTrie.add("hit".split(""));
		charTrie.add("abba".split(""));
		charTrie.add("helios".split(""));
		charTrie.add("helm".split(""));
		charTrie.add("ok".split(""));
		charTrie.add("ok".split(""));
		
		TrieNode[] mostFrequentTerminals;
		String[] expected;
		String[] got;
		
		// next case: there are more than enough candidates with regard to the number requested
		mostFrequentTerminals = charTrie.getNMostFrequentTerminals("hell".split(""),2);
		assertEquals("The number of terminals returned is wrong.",2,mostFrequentTerminals.length);
		expected = new String[] {"h e l l a m", "h e l l o"};
		got = new String[] {mostFrequentTerminals[0].getKeysAsString(),mostFrequentTerminals[1].getKeysAsString()};
		assertArrayEquals("The terminals returned as the 2 most frequent terminals related to 'hell' are wrong.",expected,got);
		// next case: there are less candidates than the number requested
		mostFrequentTerminals = charTrie.getNMostFrequentTerminals("hell".split(""),4);
		assertEquals("The number of terminals returned is wrong.",3,mostFrequentTerminals.length);
		expected = new String[] {"h e l l a m", "h e l l o", "h e l l s"};
		got = new String[] {mostFrequentTerminals[0].getKeysAsString(),mostFrequentTerminals[1].getKeysAsString(),mostFrequentTerminals[2].getKeysAsString()};
		assertArrayEquals("The terminals returned as the 4 most frequent terminals related to 'hell' are wrong.",expected,got);
	}
	
	@Test
	public void test__mostFrequentSequenceToTerminals__Char() throws TrieException, IOException {
		Trie charTrie = new Trie();
		charTrie.add("hello".split(""));
		charTrie.add("hint".split(""));
		charTrie.add("helicopter".split(""));
		charTrie.add("helios".split(""));
		charTrie.add("helicopter".split(""));
		String[] mostFrequentSegments = charTrie.getMostFrequentSequenceForRoot("h");
		String[] expected = new String[] {"h","e"};
		AssertHelpers.assertDeepEquals("The most frequent sequence should be heli.",expected,mostFrequentSegments);
	}
	
	@Test
	public void test__mostFrequentSequenceToTerminals__IUMorpheme() throws TrieException, IOException {
		Trie morphTrie = new Trie();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		String[] mostFrequentSegments = morphTrie.getMostFrequentSequenceForRoot("{taku/1v}");
		String[] expected = new String[] {"{taku/1v}","{juq/1vn}"};
		AssertHelpers.assertDeepEquals("The most frequent sequence should be heli.",expected,mostFrequentSegments);
	}
	
	@Test
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__1() throws TrieException {
		Trie morphTrie = new Trie();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"});
		TrieNode mostFrequentTerminal = morphTrie.getMostFrequentTerminalFromMostFrequentSequenceForRoot("{taku/1v}");
		String expected = "{taku/1v} {juq/1vn}";
		assertEquals("The most frequent term for 'taku' in the trie is not correct.",expected,mostFrequentTerminal.getKeysAsString());
	}
	
	@Test
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__2() throws TrieException {
		Trie morphTrie = new Trie();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"});
		TrieNode mostFrequentTerminal = morphTrie.getMostFrequentTerminalFromMostFrequentSequenceForRoot("{taku/1v}");
		String expected = "{taku/1v} {juq/1vn}";
		assertEquals("The most frequent term for 'taku' in the trie is not correct.",expected,mostFrequentTerminal.getKeysAsString());
	}
	
	@Test
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__3() throws TrieException {
		Trie morphTrie = new Trie();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"});
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"});
		TrieNode mostFrequentTerminal = morphTrie.getMostFrequentTerminalFromMostFrequentSequenceForRoot("{taku/1v}");
		String expected = "{taku/1v} {laaq/2vv} {juq/1vn}";
		assertEquals("The most frequent term for 'taku' in the trie is not correct.",expected,mostFrequentTerminal.getKeysAsString());
	}
	

}
