package ca.nrc.datastructure.trie;

import java.io.File;

import org.junit.Test;

import ca.nrc.testing.AssertObject;

public class RW_TrieNodeTest {

	///////////////////////////////////////////
	// DOCUMENTATION TEST
	///////////////////////////////////////////
		
	@Test
	public void test__RW_TrieNode__Synopsis() throws Exception {
		// Say you have a TrieNode...
		//
		String word = "greetings";
		String[] constituents = new String[] {"greet", "ing", "s"};
		TrieNode node = new TrieNode(constituents, word);
		String stem = "greet";
		node.setField("stem", stem);
		
		// Now you want to read/write it from/to a file
		RW_TrieNode rw = new RW_TrieNode();
		File tempFile = File.createTempFile("node", ".json");
		tempFile.deleteOnExit();
		rw.writeNode(tempFile, node);
		TrieNode readNode = rw.readNode(tempFile);
	}

	///////////////////////////////////////////
	// VERIFICATION TEST
	///////////////////////////////////////////
	
	@Test
	public void test__toFromJson__HappyPath() throws Exception {
		String word = "greetings";
		String[] constituents = new String[] {"greet", "ing", "s"};
		TrieNode origNode = new TrieNode(constituents, word);
		String[][] origSampleSentences = new String[][] {
			new String[] {"greetings", "world"},
			new String[] {"I", "extend", "my", "greetings"}
		};
		origNode.setField("sampleSentences", origSampleSentences);
		
		
		File tempFile = File.createTempFile("node", ".json");
		tempFile.deleteOnExit();
		
		RW_TrieNode rw = new RW_TrieNode();
		rw.writeNode(tempFile, origNode);;
		TrieNode readNode = rw.readNode(tempFile);
		
		AssertObject.assertDeepEquals(
			"Read node should have been the same as the original", 
			origNode, readNode);
		
		String[][] readSampleSentences = 
			readNode.getField("sampleSentences", new String[0][]);
		AssertObject.assertDeepEquals(
			"Read value for field 'sampleSentences' was not the same as the original", 
			readSampleSentences, origSampleSentences);
	}
	
}
