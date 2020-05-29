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

public abstract class TrieTest {
	
	public abstract Trie_InMemory makeTrieToTest();
	
	/******************************************
	 * DOCUMENTATION TESTS
	 ******************************************/
	
	@Test
	public void test__Trie__Synopsis() {
		//
		// Use a Trie to index word by a key that consists of a sequence of 
		// strings.
		// 
		// The elements of a word's key can be anything you want, for example:
		// - The word's sequence of characters
		// - The word's sequence of morpheme IDs
		// - The word's sequence of morpheme written forms
		//
		// In the rest of this test, we will assume the first use case (i.e. 
		// index words by their sequence of characters).
		//
		//
		Trie_InMemory trie = makeTrieToTest();
		
		// For the rest of the test we will use a character-based trie.
		//
		// The first thing you need to do is add words to the trie:
		//
		String[] helloChars = "hello".split("");
		try {
			trie.add(helloChars, "hello");
		} catch (TrieException e) {
		}
		
		// Then, you can retrieve the node that corresponds to a particular 
		// sequence of chars.
		//
		TrieNode_InMemory node = trie.getNode(helloChars);
		if (node == null) {
			// This means the string was not found in the Trie
		} else {
		}
	}
	
	@Test
	public void test_getParentNode() throws TrieException {
		Trie_InMemory charTrie = makeTrieToTest();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("abba".split(""),"abba");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helm".split(""),"helm");
		charTrie.add("ok".split(""),"ok");
		
		TrieNode_InMemory parent;
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
		Trie_InMemory charTrie = makeTrieToTest();
		charTrie.add("hi".split(""), "hi");
		TrieNode_InMemory terminalNode = charTrie.getNode("hi\\".split(""));
		assertEquals("The terminal node is not correct.","hi",terminalNode.getSurfaceForm());
		HashMap<String,Long> surfaceForms = terminalNode.getSurfaceForms();
		assertArrayEquals("",new String[] {"hi"},surfaceForms.keySet().toArray(new String[] {}));
		assertArrayEquals("",new Long[] {new Long(1)},surfaceForms.values().toArray(new Long[] {}));
	}
	
	@Test
	public void test_add__check_terminal_inuktitut() throws Exception {
		StringSegmenter iuSegmenter = new StringSegmenter_IUMorpheme();
		Trie_InMemory iumorphemeTrie = makeTrieToTest();
		iumorphemeTrie.add(iuSegmenter.segment("takujuq"),"takujuq");
		iumorphemeTrie.add(iuSegmenter.segment("nalunaiqsivut"),"nalunaiqsivut");
		iumorphemeTrie.add(iuSegmenter.segment("nalunairsivut"),"nalunairsivut");
		iumorphemeTrie.add(iuSegmenter.segment("nalunaiqsivut"),"nalunaiqsivut");
		TrieNode_InMemory terminalNode = iumorphemeTrie.getNode("{nalunaq/1n} {iq/1nv} {si/2vv} {vut/tv-dec-3p} \\".split(" "));
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
		Trie_InMemory charTrie = makeTrieToTest();
		try {
			charTrie.add(new String[]{"h","e","l","l","o"},"hello");
			charTrie.add(new String[]{"h","e","l","l"," ","b","o","y"},"hello boy");
		} catch (TrieException e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode_InMemory node = charTrie.getNode("hello".split(""));
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
		Trie_InMemory wordTrie = makeTrieToTest();
		try {
			wordTrie.add(new String[]{"hello","there"},"hello there");
		} catch (TrieException e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode_InMemory node = wordTrie.getNode(new String[]{"hello"});
		assertTrue("The node for 'hello' is not null.",node!=null);
		assertEquals("The key for this node is correct.","hello",node.getKeysAsString());
		assertFalse("This node should not a full word.",node.isWord());
	}

	@Test
	public void test__add_get__IUMorpheme_same_word_twice() throws Exception {
		StringSegmenter iuSegmenter = new StringSegmenter_IUMorpheme();
		Trie_InMemory iumorphemeTrie = makeTrieToTest();
		String[] takujuq_segments = null;
		takujuq_segments = iuSegmenter.segment("takujuq");
		iumorphemeTrie.add(takujuq_segments,"takujuq");
		TrieNode_InMemory secondTakujuqNode = iumorphemeTrie.add(takujuq_segments,"takujuq");
		assertTrue("The node added for the second 'takujuq' should not be null.",secondTakujuqNode!=null);
	}
	
	@Test
	public void test__add_get__IUMorpheme_one_word() throws Exception {
		StringSegmenter iuSegmenter = new StringSegmenter_IUMorpheme();
		Trie_InMemory iumorphemeTrie = makeTrieToTest();
		String[] takujuq_segments = null;
		try {
			takujuq_segments = iuSegmenter.segment("takujuq");
			iumorphemeTrie.add(takujuq_segments,"takujuq");
		} catch (Exception e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode_InMemory node = iumorphemeTrie.getNode(new String[]{"{taku/1v}"});
		assertTrue("The node for 'taku/1n' should not be null.",node!=null);
		assertEquals("The key for this node is not correct.","{taku/1v}",node.getKeysAsString());
		assertFalse("This node should not a full word.",node.isWord());
	}
	
	@Test
	public void test__frequenciesOfWords() {
		Trie_InMemory charTrie = makeTrieToTest();
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
		Trie_InMemory charTrie = makeTrieToTest();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("abba".split(""),"abba");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helm".split(""),"helm");
		charTrie.add("ok".split(""),"ok");
		TrieNode_InMemory[] h_terminals = charTrie.getAllTerminals("h".split(""));
		Assert.assertEquals("The number of words starting with 'h' should be 4.",
				4,h_terminals.length);
		TrieNode_InMemory[] hel_terminals = charTrie.getAllTerminals("hel".split(""));
		Assert.assertEquals("The number of words starting with 'hel' should be 3.",
				3,hel_terminals.length);
		TrieNode_InMemory[] o_terminals = charTrie.getAllTerminals("o".split(""));
		Assert.assertEquals("The number of words starting with 'o' should be 1.",
				1,o_terminals.length);
	}
	
	
	@Test
	public void test_getAllTerminals__Case2() throws Exception {
		Trie_InMemory charTrie = makeTrieToTest();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("abba".split(""),"abba");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helm".split(""),"helm");
		charTrie.add("ok".split(""),"ok");
		charTrie.add("okdoo".split(""),"okdoo");
		
		TrieNode_InMemory hNode = charTrie.getNode("h".split(""));
		TrieNode_InMemory[] h_terminals = charTrie.getAllTerminals(hNode);
		Assert.assertEquals("The number of words starting with 'h' should be 4.",
				4,h_terminals.length);
		
		TrieNode_InMemory helNode = charTrie.getNode("hel".split(""));
		TrieNode_InMemory[] hel_terminals = charTrie.getAllTerminals(helNode);
		Assert.assertEquals("The number of words starting with 'hel' should be 3.",
				3,hel_terminals.length);
		
		TrieNode_InMemory oNode = charTrie.getNode("o".split(""));
		TrieNode_InMemory[] o_terminals = charTrie.getAllTerminals(oNode);
		Assert.assertEquals("The number of words starting with 'o' should be 2.",
				2,o_terminals.length);
		
		TrieNode_InMemory okNode = charTrie.getNode("ok".split(""));
		TrieNode_InMemory[] ok_terminals = charTrie.getAllTerminals(okNode);
		Assert.assertEquals("The number of words starting with 'ok' should be 2.",
				2,o_terminals.length);
	}	
	@Test
	public void test_getNbOccurrences() throws Exception {
		Trie_InMemory charTrie = makeTrieToTest();
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
		Trie_InMemory charTrie = makeTrieToTest();
		charTrie.add("he".split(""),"he");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("ok".split(""),"ok");
		String json = charTrie.toJSON();
		Gson gson = new Gson();
		Trie_InMemory retrievedCharTrie = (Trie_InMemory) gson.fromJson(json, Trie_InMemory.class);
		TrieNode_InMemory node = retrievedCharTrie.getNode(new String[] {"h","i","t","\\"});
		Assert.assertTrue("The node should be terminal.",node.isWord);
		Assert.assertEquals("The surface form is not correct.", "hit", node.surfaceForm);
	}
	
	@Test
	public void test_getMostFrequentTerminal() throws TrieException {
		Trie_InMemory charTrie = makeTrieToTest();
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
		TrieNode_InMemory mostFrequentTerminalRoot = charTrie.getMostFrequentTerminal();
		String expectedKeysMFTR = "o k \\";
		assertEquals("The terminal returned as the most frequent terminal of the whole trie is wrong.",expectedKeysMFTR,mostFrequentTerminalRoot.getKeysAsString());
		TrieNode_InMemory mostFrequentTerminalHell = charTrie.getMostFrequentTerminal("hell".split(""));
		String expectedKeys = "h e l l a m \\";
		assertEquals("The terminal returned as the most frequent terminal related to 'hell' is wrong.",expectedKeys,mostFrequentTerminalHell.getKeysAsString());
		String expectedSurfaceForm = "hellam";
		assertEquals("The terminal returned as the most frequent terminal related to 'hell' is wrong.",expectedSurfaceForm,mostFrequentTerminalHell.surfaceForm);
	}
	
	@Test
	public void test_getMostFrequentTerminal__Case2() throws TrieException {
		Trie_InMemory charTrie = new Trie_InMemory();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helios".split(""),"helios");
		TrieNode_InMemory helNode = charTrie.getNode("hel".split(""));
		Assert.assertEquals("The most frequent terminal returned is faulty.",
			"helios", 
			charTrie.getMostFrequentTerminal(helNode).surfaceForm);
	}	
	
	@Test
	public void test_getMostFrequentTerminals() throws TrieException {
		Trie_InMemory charTrie = makeTrieToTest();
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
		
		TrieNode_InMemory[] mostFrequentTerminals;
		String[] expected;
		String[] got;
		
		// next case: there are more than enough candidates with regard to the number requested
		mostFrequentTerminals = charTrie.getMostFrequentTerminals(2, "hell".split(""));
		assertEquals("The number of terminals returned is wrong.",2,mostFrequentTerminals.length);
		expected = new String[] {"h e l l a m \\", "h e l l o \\"};
		got = new String[] {mostFrequentTerminals[0].getKeysAsString(),mostFrequentTerminals[1].getKeysAsString()};
		assertArrayEquals("The terminals returned as the 2 most frequent terminals related to 'hell' are wrong.",expected,got);
		// next case: there are less candidates than the number requested
		mostFrequentTerminals = charTrie.getMostFrequentTerminals(4, "hell".split(""));
		assertEquals("The number of terminals returned is wrong.",3,mostFrequentTerminals.length);
		expected = new String[] {"h e l l a m \\", "h e l l o \\", "h e l l s \\"};
		got = new String[] {mostFrequentTerminals[0].getKeysAsString(),mostFrequentTerminals[1].getKeysAsString(),mostFrequentTerminals[2].getKeysAsString()};
		assertArrayEquals("The terminals returned as the 4 most frequent terminals related to 'hell' are wrong.",expected,got);
		expected = new String[] {"hellam", "hello", "hells"};
		got = new String[] {mostFrequentTerminals[0].surfaceForm,mostFrequentTerminals[1].surfaceForm,mostFrequentTerminals[2].surfaceForm};
		assertArrayEquals("The surface forms for the terminals returned as the 3 most frequent terminals related to 'hell' are wrong.",expected,got);
		
		mostFrequentTerminals = charTrie.getMostFrequentTerminals(1);
		expected = new String[] {"ok"};
		got = new String[] {mostFrequentTerminals[0].surfaceForm};
		assertArrayEquals("The surface forms for the terminals returned as the 1 most frequent terminal of the whole trie are wrong.",expected,got);
		
		mostFrequentTerminals = charTrie.getMostFrequentTerminals(3);
		expected = new String[] {"ok","hellam","hello"};
		got = new String[] {mostFrequentTerminals[0].surfaceForm,mostFrequentTerminals[1].surfaceForm,mostFrequentTerminals[2].surfaceForm};
		assertArrayEquals("The surface forms for the terminals returned as the 3 most frequent terminal of the whole trie are wrong.",expected,got);
	}
	
	@Test
	public void test__getMostFrequentTerminals__Case2() throws TrieException {
		Trie_InMemory charTrie = new Trie_InMemory();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helios".split(""),"helios");
		TrieNode_InMemory helNode = charTrie.getNode("hel".split(""));
		// test n < number of terminals
		TrieNode_InMemory[] mostFrequentTerminals = charTrie.getMostFrequentTerminals(2, helNode);
		Assert.assertEquals("The number of nodes returned is wrong.",2,mostFrequentTerminals.length);
		Assert.assertEquals("The first most frequent terminal returned is faulty.","helios",mostFrequentTerminals[0].surfaceForm);
		Assert.assertEquals("The second most frequent terminal returned is faulty.","helicopter",mostFrequentTerminals[1].surfaceForm);
		// test n > number of terminals
		TrieNode_InMemory[] mostFrequentTerminals4 = charTrie.getMostFrequentTerminals(4, helNode);
		Assert.assertEquals("The number of nodes returned is wrong.",3,mostFrequentTerminals4.length);
		// test with exclusion of nodes
		TrieNode_InMemory nodeToExclude = charTrie.getNode("hello\\".split(""));
		TrieNode_InMemory[] mostFrequentTerminalsExcl = 
			charTrie.getMostFrequentTerminals(4, helNode, 
				new TrieNode_InMemory[] {nodeToExclude});
		Assert.assertEquals("The number of nodes returned without excluded nodes is wrong.",2,mostFrequentTerminalsExcl.length);
		Assert.assertEquals("", "helios", mostFrequentTerminalsExcl[0].getSurfaceForm());
		Assert.assertEquals("", "helicopter", mostFrequentTerminalsExcl[1].getSurfaceForm());
	}
	
	
	@Test
	public void test__mostFrequentSequenceForRoot__Char() throws TrieException, IOException {
		Trie_InMemory charTrie = makeTrieToTest();
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
		Trie_InMemory morphTrie = makeTrieToTest();
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
		Trie_InMemory morphTrie = makeTrieToTest();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"},"takulaaqsimajuq");
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"},"takusimajuq");
		TrieNode_InMemory mostFrequentTerminal = morphTrie.getMostFrequentTerminalFromMostFrequentSequenceForRoot("{taku/1v}");
		String expected = "{taku/1v} {juq/1vn} \\";
		assertEquals("The most frequent term for 'taku' in the trie is not correct.",expected,mostFrequentTerminal.getKeysAsString());
	}
	
	@Test
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__2() throws TrieException {
		Trie_InMemory morphTrie = makeTrieToTest();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"},"takulaaqsimajuq");
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"},"takusimajuq");
		TrieNode_InMemory mostFrequentTerminal = morphTrie.getMostFrequentTerminalFromMostFrequentSequenceForRoot("{taku/1v}");
		String expected = "{taku/1v} {juq/1vn} \\";
		assertEquals("The most frequent term for 'taku' in the trie is not correct.",expected,mostFrequentTerminal.getKeysAsString());
	}
	
	@Test
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__3() throws TrieException {
		Trie_InMemory morphTrie = makeTrieToTest();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"},"takulaaqsimajuq");
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"},"takusimajuq");
		TrieNode_InMemory mostFrequentTerminal = morphTrie.getMostFrequentTerminalFromMostFrequentSequenceForRoot("{taku/1v}");
		String expected = "{taku/1v} {laaq/2vv} {juq/1vn} \\";
		assertEquals("The most frequent term for 'taku' in the trie is not correct.",expected,mostFrequentTerminal.getKeysAsString());
	}
}
