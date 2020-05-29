package ca.nrc.datastructure.trie;

import org.junit.Assert;
import org.junit.Test;


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
	public void test__add() throws Exception {
		Trie charTrie = new Trie_InMemory();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helicopter");						
		charTrie.add("hellon".split(""), "hellon");
		charTrie.add("hello".split(""), "hello");
		TrieNode hello = charTrie.getNode("hello".split(""));
		Assert.assertEquals("The frequency of the node is not correct.", 3, hello.frequency);
		TrieNode helloTerminal = charTrie.getNode("hello\\".split(""));
		Assert.assertEquals("The frequency of the terminal is not correct.", 2, helloTerminal.frequency);
		}

	
	@Test
	public void test_getAllTerminals() throws Exception {
		Trie charTrie = new Trie_InMemory();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("abba".split(""),"abba");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helm".split(""),"helm");
		charTrie.add("ok".split(""),"ok");
		charTrie.add("okdoo".split(""),"okdoo");
		
		TrieNode hNode = charTrie.getNode("h".split(""));
		TrieNode[] h_terminals = hNode.getAllTerminals();
		Assert.assertEquals("The number of words starting with 'h' should be 4.",
				4,h_terminals.length);
		
		TrieNode helNode = charTrie.getNode("hel".split(""));
		TrieNode[] hel_terminals = helNode.getAllTerminals();
		Assert.assertEquals("The number of words starting with 'hel' should be 3.",
				3,hel_terminals.length);
		
		TrieNode oNode = charTrie.getNode("o".split(""));
		TrieNode[] o_terminals = oNode.getAllTerminals();
		Assert.assertEquals("The number of words starting with 'o' should be 2.",
				2,o_terminals.length);
		
		TrieNode okNode = charTrie.getNode("ok".split(""));
		TrieNode[] ok_terminals = okNode.getAllTerminals();
		Assert.assertEquals("The number of words starting with 'ok' should be 2.",
				2,o_terminals.length);
	}
	
	
	@Test
	public void test_getMostFrequentTerminal() throws TrieException {
		Trie charTrie = new Trie_InMemory();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helios".split(""),"helios");
		TrieNode helNode = charTrie.getNode("hel".split(""));
		Assert.assertEquals("The most frequent terminal returned is faulty.","helios",helNode.getMostFrequentTerminal().surfaceForm);
	}
	
	
	@Test
	public void test_getMostFrequentTerminals() throws TrieException {
		Trie charTrie = new Trie_InMemory();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helios".split(""),"helios");
		TrieNode helNode = charTrie.getNode("hel".split(""));
		// test n < number of terminals
		TrieNode[] mostFrequentTerminals = helNode.getMostFrequentTerminals(2);
		Assert.assertEquals("The number of nodes returned is wrong.",2,mostFrequentTerminals.length);
		Assert.assertEquals("The first most frequent terminal returned is faulty.","helios",mostFrequentTerminals[0].surfaceForm);
		Assert.assertEquals("The second most frequent terminal returned is faulty.","helicopter",mostFrequentTerminals[1].surfaceForm);
		// test n > number of terminals
		TrieNode[] mostFrequentTerminals4 = helNode.getMostFrequentTerminals(4);
		Assert.assertEquals("The number of nodes returned is wrong.",3,mostFrequentTerminals4.length);
		// test with exclusion of nodes
		TrieNode nodeToExclude = charTrie.getNode("hello\\".split(""));
		TrieNode[] mostFrequentTerminalsExcl = helNode.getMostFrequentTerminals(4,new TrieNode[] {nodeToExclude});
		Assert.assertEquals("The number of nodes returned without excluded nodes is wrong.",2,mostFrequentTerminalsExcl.length);
		Assert.assertEquals("", "helios", mostFrequentTerminalsExcl[0].getSurfaceForm());
		Assert.assertEquals("", "helicopter", mostFrequentTerminalsExcl[1].getSurfaceForm());
	}
	
	
	@Test
	public void test_toString() throws TrieException {
		Trie charTrie = new Trie_InMemory();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helios".split(""),"helios");
		TrieNode helNode = charTrie.getNode("hel".split(""));
		Assert.assertFalse("The string returned for the first most frequent terminal is faulty.",helNode.toString().contains("surfaceForm"));
		TrieNode[] mostFrequentTerminals = helNode.getMostFrequentTerminals(2);
		Assert.assertTrue("The string returned for the first most frequent terminal is faulty.",mostFrequentTerminals[0].toString().contains("helios"));
		Assert.assertTrue("The string returned for the second most frequent terminal is faulty.",mostFrequentTerminals[1].toString().contains("helicopter"));
	}
}
