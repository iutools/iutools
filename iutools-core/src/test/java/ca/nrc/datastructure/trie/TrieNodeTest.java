package ca.nrc.datastructure.trie;

import org.junit.Assert;
import org.junit.Test;

import ca.nrc.datastructure.trie.Trie.NodeOption;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;

public class TrieNodeTest {
	
	/*************************************
	 * DOCUMENTATION TESTS
	 *************************************/
	
	@Test
	public void test__TrieNode__Synopsis() throws Exception {
		//
		// TrieNodes are used in a Trie datastructure to keep statistics about 
		// a particular sequence of strings.
		//
		// Because a Trie is a tree structure, you can think of a TrieNode
		// as corresponding to the sequence of strings of the path that 
		// leads from the root of the Trie to the node in question. The 
		// TrieNode then stores statistics about that sequence of strings.
		// 
		// When creating a node, you need to tell it the element of the sequence
		// it corresponds to.
		//
		String[] sequenceElements = new String[] { "e", "x", "a", "c", "t" };
		TrieNode node = new TrieNode(sequenceElements);
		
		//
		// You can use a node to store and manipulate the frequency at which you saw a sequence
		// of string that corresponds to that node's path
		//
		//		
		node.incrementFrequency();       // Increment that frequency by one
		long freq = node.getFrequency(); // Get the current frequency
		
		//
		// If you want to store other stats besides the above, you need to
		// define their names (but you can't define their types)
		// 
		// For example, say you want to calculate the average length of
		// strings that start with the path to this node. For that, 
		// you need to keep a running sum of the lenght of any sequence
		// that starts with the node's sequence.
		//
		// Let's call that stat "lengthSum"
		//
		node.defineStat("lengthSum");   // Define the attribute
		
		// Now, say we encounter a sequence of length 10 that starts with 
		// the node's path. You want to add 10 to the "lengthSum"
		//
		node.incrementStat("lengthSum", 10);
		
		
		// At any point, you can get a stat's value:
		Integer lengthSum = (Integer) node.getStat("lengthSum");
		
		// Instead of incrementing a stat, you can also set it directly
		node.setStat("lengthSum", 332);
		
		// If you try to set, increment or retrieve a stat whose name
		// you have not defined, you get a TrieNodeException
		//
		try {
			Long stat = (Long) node.getStat("doesNotExist");
			node.incrementStat("doesNotExist", 1);
			node.setStat("doesNotExist", 253);
		} catch (TrieNodeException exc) {
		}
		
	}
	
	/*************************************
	 * DOCUMENTATION TESTS
	 *************************************/
	
	@Test
	public void test__TrieNode__frequency__HappyPath() {
		TrieNode node = new TrieNode("hello".split(""));
		long gotFreq = node.getFrequency(); 	
		Assert.assertEquals("Frequency should have been 0 initialy", 0, gotFreq);
		node.incrementFrequency();       
		gotFreq = node.getFrequency(); 	
		Assert.assertEquals("Frequency should have been 1 after incrementing", 1, gotFreq);
	}
	
	@Test
	public void test__TrieNode__setgetStat__HappyPath() throws Exception {
		TrieNode node = new TrieNode("hello".split(""));
		String statName = "lengthSum";
		node.defineStat(statName);
		
		Integer val = new Integer(10);
		node.setStat(statName, val);
		Integer gotVal = (Integer) node.getStat(statName);
		
		Assert.assertEquals("getStat did not retrieve the value that was set using setVal", val, gotVal);
	}

	@Test(expected = TrieNodeException.class)
	public void test__TrieNode__setStat__RaisesExceptionIfStatNameIsUnknown() throws Exception {
		TrieNode node = new TrieNode("hello".split(""));
		String statName = "lengthSum";
		node.defineStat(statName);
		
		node.setStat("ergaewrqadf", 10);
	}

	@Test(expected = TrieNodeException.class)
	public void test__TrieNode__getStat__RaisesExceptionIfStatNameIsUnknown() throws Exception {
		TrieNode node = new TrieNode("hello".split(""));
		String statName = "lengthSum";
		node.defineStat(statName);
		
		node.getStat("ergaewrqadf");
	}

	@Test
	public void test__TrieNode__incrementStat__HappyPath() throws Exception {
		TrieNode node = new TrieNode("hello".split(""));
		String statName = "lengthSum";
		node.defineStat(statName);
		
		Integer val = new Integer(10);
		node.incrementStat(statName, val);
		Integer gotVal = (Integer) node.getStat(statName);
		Assert.assertEquals("First incrementation of the value did not yield expected results", gotVal, new Integer(10));
		
		val = new Integer(3);
		node.incrementStat(statName, val);
		gotVal = (Integer) node.getStat(statName);
		Assert.assertEquals("Second incrementation of the value did not yield expected results", gotVal, new Integer(13));
	}
	
	@Test
	public void test_toString() throws TrieException {
		Trie_InMemory charTrie = new Trie_InMemory();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helios".split(""),"helios");
		
		TrieNode helNode = charTrie.getNode("hel".split(""));
		String gotString = helNode.toString();
		String expString = 
			"[TrieNode:\n" + 
			"    segments = h e l\n" + 
			"    surfaceForm = null\n" +
			"    frequency = 6\n" + 
			"    ]";
		AssertString.assertStringEquals(
			"Stringified node was not as expected for 'hel'", 
			expString, gotString);
	}
	
	@Test
	public void test__keysNoTerminal__KeysEndWithTerminalSeg__RemovesTerminalSeb() 
			throws Exception {
		String hello = "hello";
		String[] helloTermKeys = 
			new String[] {"h","e","l","l","o",TrieNode.TERMINAL_SEG};
		TrieNode node = new TrieNode(helloTermKeys, hello);
		String[] gotKeys = node.keysNoTerminal();
		String[] expKeys = hello.split("");
		AssertObject.assertDeepEquals(
			"Terminal segment should have been removed", 
			expKeys, gotKeys);
	}

	@Test
	public void test__keysNoTerminal__KeysDoesNotEndWithTerminalSeg__LeavesKeyAlong() 
			throws Exception {
		String hello = "hello";
		String[] helloTermKeys = hello.split("");
		TrieNode node = new TrieNode(helloTermKeys, hello);
		String[] gotKeys = node.keysNoTerminal();
		String[] expKeys = hello.split("");
		AssertObject.assertDeepEquals(
			"Modified keys were not as expected", 
			expKeys, gotKeys);
	}

	@Test
	public void test__keysNoTerminal__EmptyKeys() 
			throws Exception {
		String emptyWord = "";
		String[] emptyKeys = new String[0];
		TrieNode node = new TrieNode(emptyKeys, emptyWord);
		String[] gotKeys = node.keysNoTerminal();
		String[] expKeys = emptyKeys;
		AssertObject.assertDeepEquals(
			"Terminal segment should have been removed", 
			expKeys, gotKeys);
	}
}