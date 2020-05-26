package ca.nrc.datastructure.trie;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

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
			trie.add(new String[]{"h","e","l","l","o"},"hello");
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
	public void test_getParentNode() throws TrieException {
		Trie charTrie = new Trie();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("abba".split(""),"abba");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helm".split(""),"helm");
		charTrie.add("ok".split(""),"ok");
		
		TrieNode parent;
		// pass keys as argument
		parent = charTrie.getParentNode(new String[] {});
		assertTrue("The parent node of the root should be null.",parent==null);
		parent = charTrie.getParentNode("hel".split(""));
		assertEquals("The parent node of 'hel' should be 'he'.","h e",parent.getKeysAsString());
		// pass node as argument
		parent = charTrie.getParentNode(parent);
		assertEquals("The parent node of 'he' should be 'h'.","h",parent.getKeysAsString());
	}
	
	@Test
	public void test_add__check_terminal() throws TrieException {
		Trie charTrie = new Trie();
		charTrie.add("hi".split(""), "hi");
		TrieNode terminalNode = charTrie.getNode("hi\\".split(""));
		assertEquals("The terminal node is not correct.","hi",terminalNode.getSurfaceForm());
		HashMap<String,Long> surfaceForms = terminalNode.getSurfaceForms();
		assertArrayEquals("",new String[] {"hi"},surfaceForms.keySet().toArray(new String[] {}));
		assertArrayEquals("",new Long[] {new Long(1)},surfaceForms.values().toArray(new Long[] {}));
	}
	
	@Test
	public void test_add__check_terminal_inuktitut() throws Exception {
		StringSegmenter iuSegmenter = new StringSegmenter_IUMorpheme();
		Trie iumorphemeTrie = new Trie();
		iumorphemeTrie.add(iuSegmenter.segment("takujuq"),"takujuq");
		iumorphemeTrie.add(iuSegmenter.segment("nalunaiqsivut"),"nalunaiqsivut");
		iumorphemeTrie.add(iuSegmenter.segment("nalunairsivut"),"nalunairsivut");
		iumorphemeTrie.add(iuSegmenter.segment("nalunaiqsivut"),"nalunaiqsivut");
		TrieNode terminalNode = iumorphemeTrie.getNode("{nalunaq/1n} {iq/1nv} {si/2vv} {vut/tv-dec-3p} \\".split(" "));
		assertEquals("The terminal node is not correct.","nalunaiqsivut",terminalNode.getSurfaceForm());
		HashMap<String,Long> surfaceForms = terminalNode.getSurfaceForms();
		assertEquals("The number of surface forms for {nalunaq/1n} {iq/1nv} {si/2vv} {vut/tv-dec-3p} is wrong.",2,surfaceForms.size());
		ArrayList<String> keys = new ArrayList<String>(Arrays.asList(surfaceForms.keySet().toArray(new String[] {})));
		assertTrue("The surface forms should contain 'nalunaiqsivut'",keys.contains("nalunaiqsivut"));
		assertTrue("The surface forms should contain 'nalunairsivut'",keys.contains("nalunairsivut"));
		assertEquals("The frequency of 'nalunaiqsivut' is wrong",new Long(2),surfaceForms.get("nalunaiqsivut"));
		assertEquals("The frequency of 'nalunairsivut' is wrong",new Long(1),surfaceForms.get("nalunairsivut"));
	}
	
	@Test
	public void test__add_get__Char() {
		Trie charTrie = new Trie();
		try {
			charTrie.add(new String[]{"h","e","l","l","o"},"hello");
			charTrie.add(new String[]{"h","e","l","l"," ","b","o","y"},"hello boy");
		} catch (TrieException e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode node = charTrie.getNode("hello".split(""));
		assertTrue("The node for 'hello' is not null.",node!=null);
		assertEquals("The key for this node is correct.","h e l l o",node.getKeysAsString());
		assertTrue("This node represents a full word.",node.hasTerminalNode());
		
		node = charTrie.getNode("hell".split(""));
		assertTrue("The node for 'hell' is not null.",node!=null);
		assertEquals("The key for this node is correct.","h e l l",node.getKeysAsString());
		assertEquals("The frequency for this node is correct.",2,node.getFrequency());
		assertFalse("This node does not represent a full word.",node.hasTerminalNode());
	}

	@Test
	public void test__add_get__Word() {
		Trie wordTrie = new Trie();
		try {
			wordTrie.add(new String[]{"hello","there"},"hello there");
		} catch (TrieException e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode node = wordTrie.getNode(new String[]{"hello"});
		assertTrue("The node for 'hello' is not null.",node!=null);
		assertEquals("The key for this node is correct.","hello",node.getKeysAsString());
		assertFalse("This node should not a full word.",node.isWord());
	}

	@Test
	public void test__add_get__IUMorpheme_same_word_twice() throws Exception {
		StringSegmenter iuSegmenter = new StringSegmenter_IUMorpheme();
		Trie iumorphemeTrie = new Trie();
		String[] takujuq_segments = null;
		takujuq_segments = iuSegmenter.segment("takujuq");
		iumorphemeTrie.add(takujuq_segments,"takujuq");
		TrieNode secondTakujuqNode = iumorphemeTrie.add(takujuq_segments,"takujuq");
		assertTrue("The node added for the second 'takujuq' should not be null.",secondTakujuqNode!=null);
	}
	
	@Test
	public void test__add_get__IUMorpheme_one_word() throws Exception {
		StringSegmenter iuSegmenter = new StringSegmenter_IUMorpheme();
		Trie iumorphemeTrie = new Trie();
		String[] takujuq_segments = null;
		try {
			takujuq_segments = iuSegmenter.segment("takujuq");
			iumorphemeTrie.add(takujuq_segments,"takujuq");
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
		charTrie.add("hello".split(""),"hello");
		charTrie.add("world".split(""),"world");
		charTrie.add("hell boy".split(""),"hell boy");
		charTrie.add("heaven".split(""),"heaven");
		charTrie.add("worship".split(""),"worship");
		charTrie.add("world".split(""),"world");
		charTrie.add("heaven".split(""),"heaven");
		charTrie.add("world".split(""),"world");
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
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("abba".split(""),"abba");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helm".split(""),"helm");
		charTrie.add("ok".split(""),"ok");
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
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("abba".split(""),"abba");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helm".split(""),"helm");
		charTrie.add("ok".split(""),"ok");
		charTrie.add("ok".split(""),"ok");
		Assert.assertEquals("The number of terminals should be 6.",6,charTrie.getAllTerminals().length);
		long nb = charTrie.getNbOccurrences();
		Assert.assertEquals("The number of occurrences is wrong.",8,nb);
	}
	
	
	
	@Test
	public void test_toJSON__Char() throws TrieException {
		Trie charTrie = new Trie();
		charTrie.add("he".split(""),"he");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("ok".split(""),"ok");
		String json = charTrie.toJSON();
		Gson gson = new Gson();
		Trie retrievedCharTrie = gson.fromJson(json,Trie.class);
		TrieNode node = retrievedCharTrie.getNode(new String[] {"h","i","t","\\"});
		Assert.assertTrue("The node should be terminal.",node.isWord);
		Assert.assertEquals("The surface form is not correct.", "hit", node.surfaceForm);
	}
	
	@Test
	public void test_getMostFrequentTerminal() throws TrieException {
		Trie charTrie = new Trie();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hells".split(""),"hells");
		charTrie.add("hellam".split(""),"hellam");
		charTrie.add("hellam".split(""),"hellam");
		charTrie.add("hellam".split(""),"hellam");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("abba".split(""),"abba");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helm".split(""),"helm");
		charTrie.add("ok".split(""),"ok");
		charTrie.add("ok".split(""),"ok");
		charTrie.add("ok".split(""),"ok");
		charTrie.add("ok".split(""),"ok");
		TrieNode mostFrequentTerminalRoot = charTrie.getMostFrequentTerminal();
		String expectedKeysMFTR = "o k \\";
		assertEquals("The terminal returned as the most frequent terminal of the whole trie is wrong.",expectedKeysMFTR,mostFrequentTerminalRoot.getKeysAsString());
		TrieNode mostFrequentTerminalHell = charTrie.getMostFrequentTerminal("hell".split(""));
		String expectedKeys = "h e l l a m \\";
		assertEquals("The terminal returned as the most frequent terminal related to 'hell' is wrong.",expectedKeys,mostFrequentTerminalHell.getKeysAsString());
		String expectedSurfaceForm = "hellam";
		assertEquals("The terminal returned as the most frequent terminal related to 'hell' is wrong.",expectedSurfaceForm,mostFrequentTerminalHell.surfaceForm);
	}
	
	@Test
	public void test_getNMostFrequentTerminals() throws TrieException {
		Trie charTrie = new Trie();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hells".split(""),"hells");
		charTrie.add("hellam".split(""),"hellam");
		charTrie.add("hellam".split(""),"hellam");
		charTrie.add("hellam".split(""),"hellam");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("abba".split(""),"abba");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helm".split(""),"helm");
		charTrie.add("ok".split(""),"ok");
		charTrie.add("ok".split(""),"ok");
		charTrie.add("ok".split(""),"ok");
		charTrie.add("ok".split(""),"ok");
		
		TrieNode[] mostFrequentTerminals;
		String[] expected;
		String[] got;
		
		// next case: there are more than enough candidates with regard to the number requested
		mostFrequentTerminals = charTrie.getNMostFrequentTerminals("hell".split(""),2);
		assertEquals("The number of terminals returned is wrong.",2,mostFrequentTerminals.length);
		expected = new String[] {"h e l l a m \\", "h e l l o \\"};
		got = new String[] {mostFrequentTerminals[0].getKeysAsString(),mostFrequentTerminals[1].getKeysAsString()};
		assertArrayEquals("The terminals returned as the 2 most frequent terminals related to 'hell' are wrong.",expected,got);
		// next case: there are less candidates than the number requested
		mostFrequentTerminals = charTrie.getNMostFrequentTerminals("hell".split(""),4);
		assertEquals("The number of terminals returned is wrong.",3,mostFrequentTerminals.length);
		expected = new String[] {"h e l l a m \\", "h e l l o \\", "h e l l s \\"};
		got = new String[] {mostFrequentTerminals[0].getKeysAsString(),mostFrequentTerminals[1].getKeysAsString(),mostFrequentTerminals[2].getKeysAsString()};
		assertArrayEquals("The terminals returned as the 4 most frequent terminals related to 'hell' are wrong.",expected,got);
		expected = new String[] {"hellam", "hello", "hells"};
		got = new String[] {mostFrequentTerminals[0].surfaceForm,mostFrequentTerminals[1].surfaceForm,mostFrequentTerminals[2].surfaceForm};
		assertArrayEquals("The surface forms for the terminals returned as the 3 most frequent terminals related to 'hell' are wrong.",expected,got);
		
		mostFrequentTerminals = charTrie.getNMostFrequentTerminals(1);
		expected = new String[] {"ok"};
		got = new String[] {mostFrequentTerminals[0].surfaceForm};
		assertArrayEquals("The surface forms for the terminals returned as the 1 most frequent terminal of the whole trie are wrong.",expected,got);
		
		mostFrequentTerminals = charTrie.getNMostFrequentTerminals(3);
		expected = new String[] {"ok","hellam","hello"};
		got = new String[] {mostFrequentTerminals[0].surfaceForm,mostFrequentTerminals[1].surfaceForm,mostFrequentTerminals[2].surfaceForm};
		assertArrayEquals("The surface forms for the terminals returned as the 3 most frequent terminal of the whole trie are wrong.",expected,got);
	}
	
	@Test
	public void test__mostFrequentSequenceForRoot__Char() throws TrieException, IOException {
		Trie charTrie = new Trie();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helipcopter");
		String[] mostFrequentSegments = charTrie.getMostFrequentSequenceForRoot("h");
		String[] expected = new String[] {"h","e"};
		AssertHelpers.assertDeepEquals("The most frequent sequence should be heli.",expected,mostFrequentSegments);
	}
	
	@Test
	public void test__mostFrequentSequenceForRoot__IUMorpheme() throws TrieException, IOException {
		Trie morphTrie = new Trie();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"},"takulaaqsimajuq");
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"},"takusimajuq");
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		String[] mostFrequentSegments = morphTrie.getMostFrequentSequenceForRoot("{taku/1v}");
		String[] expected = new String[] {"{taku/1v}","{juq/1vn}"};
		AssertHelpers.assertDeepEquals("The most frequent sequence should be heli.",expected,mostFrequentSegments);
	}
	
	@Test
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__1() throws TrieException {
		Trie morphTrie = new Trie();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"},"takulaaqsimajuq");
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"},"takusimajuq");
		TrieNode mostFrequentTerminal = morphTrie.getMostFrequentTerminalFromMostFrequentSequenceForRoot("{taku/1v}");
		String expected = "{taku/1v} {juq/1vn} \\";
		assertEquals("The most frequent term for 'taku' in the trie is not correct.",expected,mostFrequentTerminal.getKeysAsString());
	}
	
	@Test
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__2() throws TrieException {
		Trie morphTrie = new Trie();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"},"takulaaqsimajuq");
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"},"takusimajuq");
		TrieNode mostFrequentTerminal = morphTrie.getMostFrequentTerminalFromMostFrequentSequenceForRoot("{taku/1v}");
		String expected = "{taku/1v} {juq/1vn} \\";
		assertEquals("The most frequent term for 'taku' in the trie is not correct.",expected,mostFrequentTerminal.getKeysAsString());
	}
	
	@Test
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__3() throws TrieException {
		Trie morphTrie = new Trie();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"},"takulaaqsimajuq");
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"},"takusimajuq");
		TrieNode mostFrequentTerminal = morphTrie.getMostFrequentTerminalFromMostFrequentSequenceForRoot("{taku/1v}");
		String expected = "{taku/1v} {laaq/2vv} {juq/1vn} \\";
		assertEquals("The most frequent term for 'taku' in the trie is not correct.",expected,mostFrequentTerminal.getKeysAsString());
	}
	

}
