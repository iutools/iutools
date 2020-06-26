package ca.nrc.datastructure.trie;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;

import ca.inuktitutcomputing.morph.failureanalysis.MorphFailureAnalyzer;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;

public class AssertTrieNode extends Asserter<TrieNode> {
	

	public AssertTrieNode(
			TrieNode _gotObject, String mess) {
		super(_gotObject, mess);
	}
	
	protected TrieNode node() {
		return gotObject;
	}

	public AssertTrieNode isTerminal() throws Exception {
		Assert.assertTrue(
			baseMessage+"\nNode should have been a terminal", 
			gotObject.isTerminal());
		return this;
	}

	public AssertTrieNode isNotTerminal() {
		Assert.assertFalse(
				baseMessage+"\nNode should NOT have been a terminal", 
				gotObject.isTerminal());
		return this;
	}
	
	public AssertTrieNode hasMostFrequentForm(String expSurfaceForm) 
			throws Exception {
		String gotSurfaceForm = gotObject.getTerminalSurfaceForm();
		Assert.assertEquals(
			baseMessage+"\nMost frequent surface form was not as expected", 
			expSurfaceForm, gotSurfaceForm);
		return this;
	}

	public AssertTrieNode surfaceFormFrequenciesEqual(
			Pair<String,Long>[] expFreqs) throws IOException {
		Map<String,Long> expFreqsMap = new HashMap<String,Long>();
		for (Pair<String,Long> anItem: expFreqs) {
			expFreqsMap.put(anItem.getLeft(), anItem.getRight());
		}
		
		AssertObject.assertDeepEquals(
			baseMessage+"\nMap of surface form frequencies was not as expected", 
				expFreqsMap, gotObject.surfaceForms);
		
		return this;
	}

	public AssertTrieNode hasTerminalNode() {
		Assert.assertTrue(
			baseMessage+"\nNode should have had a terminal child", 
			node().hasTerminalNode());
		return this;
	}
	
	public AssertTrieNode doesNotHaveATerminalNode() {
		Assert.assertFalse(
				baseMessage+"\nNode should NOT have had a terminal child", 
				node().hasTerminalNode());
			return this;
	}
	
	
	public AssertTrieNode hasSegments(String[] expSegments) throws Exception { 
		AssertObject.assertDeepEquals(
			"Node segments were not as expected", 
			expSegments, node().keys);
		return this;
	}

	public AssertTrieNode hasFrequency(long expFreq) {
		Assert.assertEquals(
			baseMessage+"\nFrequency of node was not as expected", 
			expFreq, node().getFrequency());
		return this;
	}

	public AssertTrieNode hasSurfaceForm(String expSurfForm) {
		String gotSurfForm = node().surfaceForm;
		Assert.assertEquals(
			baseMessage+"\nSurface form of the node was not as expected", 
			expSurfForm, gotSurfForm);
		return this;
	}

	public AssertTrieNode hasSurfaceForms(String[] expFormsArr) 
			throws Exception {
		Set<String> gotForms = node().surfaceForms.keySet();
		Set<String> expForms = new HashSet<String>();
		Collections.addAll(expForms, expFormsArr);
		
		AssertObject.assertDeepEquals(
			baseMessage+"\nSurface forms for the node were not as expected", 
			expForms, gotForms);
		return this;
	}
	
}
