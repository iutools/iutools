package ca.nrc.datastructure.trie;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import ca.nrc.testing.AssertHelpers;
import ca.nrc.testing.AssertObject;

public abstract class TrieTest {
	
	public abstract Trie makeTrieToTest() throws Exception;
	
	/******************************************
	 * DOCUMENTATION TESTS
	 ******************************************/
	
	@Test
	public void test__Trie__Synopsis() throws Exception {
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
		Trie trie = makeTrieToTest();
		
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
		TrieNode node = trie.getNode(helloChars);
		if (node == null) {
			// This means the string was not found in the Trie
		} else {
		}
	}
	
	@Test
	public void test_getParentNode() throws Exception {
		Trie charTrie = makeTrieToTest();
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
		assertEquals("The parent node of 'hel' should be 'he'.","h e",parent.keysAsString());
		// pass node as argument
		parent = charTrie.getParentNode(parent);
		assertEquals("The parent node of 'he' should be 'h'.","h",parent.keysAsString());
	}
	
	@Test
	public void test_add__check_terminal() throws Exception {
		Trie charTrie = makeTrieToTest();
		String word = "hi";
		charTrie.add(word.split(""), word);
		
		TrieNode terminalNode = charTrie.getNode((word+"\\").split(""));
		new AssertTrieNode(terminalNode, 
				"Terminal node for word "+word+" was not as expected")
			.isTerminal()
			.hasMostFrequentForm(word)
			.surfaceFormFrequenciesEqual(
				new Pair[] {
					Pair.of(word, new Long(1))
				})
			;
				
		
//		assertEquals(
//			"Surface form of terminal node is not correct for word "+word,
//			word,terminalNode.mostFrequentSurfaceForm());
//		Map<String,Long> surfaceForms = terminalNode.getSurfaceForms();
//		Map<String,Long> expSurfFormFreqs = new HashMap<String,Long>();
//		{
//			expSurfFormFreqs.put(word, new Long(1));
//		}
//		AssertObject.assertDeepEquals(
//			"Surface form frequencies not as expected for word "+word,
//			expSurfFormFreqs, terminalNode.getSurfaceForms());
	}
	
	@Test
	public void test_add__check_terminal_inuktitut() throws Exception {
		StringSegmenter iuSegmenter = new StringSegmenter_IUMorpheme();
		Trie iumorphemeTrie = makeTrieToTest();
		iumorphemeTrie.add(iuSegmenter.segment("takujuq"),"takujuq");
		iumorphemeTrie.add(iuSegmenter.segment("nalunaiqsivut"),"nalunaiqsivut");
		iumorphemeTrie.add(iuSegmenter.segment("nalunairsivut"),"nalunairsivut");
		iumorphemeTrie.add(iuSegmenter.segment("nalunaiqsivut"),"nalunaiqsivut");
		
		String[] segments = new String[] {
			"{nalunaq/1n}", "{iq/1nv}", "{si/2vv}", "{vut/tv-dec-3p}", "\\"
		};
		TrieNode terminalNode = iumorphemeTrie.getNode(segments);
		new AssertTrieNode(terminalNode, "Node for segments="+String.join(", ", segments))
				.isTerminal()
				.surfaceFormFrequenciesEqual(
					new Pair[] {
						Pair.of("nalunaiqsivut", new Long(2)),
						Pair.of("nalunairsivut", new Long(1))
					})
			;
		
		HashMap<String,Long> surfaceForms = terminalNode.getSurfaceForms();
		assertEquals("The number of surface forms for {nalunaq/1n} {iq/1nv} {si/2vv} {vut/tv-dec-3p} is wrong.",2,surfaceForms.size());
		ArrayList<String> keys = new ArrayList<String>(Arrays.asList(surfaceForms.keySet().toArray(new String[] {})));
		assertTrue("The surface forms should contain 'nalunaiqsivut'",keys.contains("nalunaiqsivut"));
		assertTrue("The surface forms should contain 'nalunairsivut'",keys.contains("nalunairsivut"));
		assertEquals("The frequency of 'nalunaiqsivut' is wrong",new Long(2),surfaceForms.get("nalunaiqsivut"));
		assertEquals("The frequency of 'nalunairsivut' is wrong",new Long(1),surfaceForms.get("nalunairsivut"));
	}
	
	@Test
	public void test__add_get__Char() throws Exception {
		Trie charTrie = makeTrieToTest();
		charTrie.add(new String[]{"h","e","l","l","o"},"hello");
		charTrie.add(new String[]{"h","e","l","l"," ","b","o","y"},"hello boy");
		
		TrieNode node = charTrie.getNode("hello".split(""));
		AssertTrieNode asserter = new AssertTrieNode(node, "");
		asserter.isNotNull();
		asserter
			.hasTerminalNode()
			.hasSegments("hello".split(""))
			.hasFrequency(1)
			;

		node = charTrie.getNode("hell".split(""));
		asserter = new AssertTrieNode(node, "");
		asserter.isNotNull();
		asserter
			.hasSegments("hell".split(""))
			.doesNotHaveATerminalNode()
			.hasFrequency(2)
			;		
	}

	@Test
	public void test__add_get__Word() throws Exception {
		Trie wordTrie = makeTrieToTest();
		try {
			wordTrie.add(new String[]{"hello","there"},"hello there");
		} catch (TrieException e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode node = wordTrie.getNode(new String[]{"hello"});
		assertTrue("The node for 'hello' is not null.",node!=null);
		assertEquals("The key for this node is correct.","hello",node.keysAsString());
		assertFalse("This node should not a full word.",node.isTerminal());
	}

	@Test
	public void test__add_get__IUMorpheme_same_word_twice() throws Exception {
		StringSegmenter iuSegmenter = new StringSegmenter_IUMorpheme();
		Trie iumorphemeTrie = makeTrieToTest();
		String[] takujuq_segments = null;
		takujuq_segments = iuSegmenter.segment("takujuq");
		iumorphemeTrie.add(takujuq_segments,"takujuq");
		TrieNode secondTakujuqNode = iumorphemeTrie.add(takujuq_segments,"takujuq");
		assertTrue("The node added for the second 'takujuq' should not be null.",secondTakujuqNode!=null);
	}
	
	@Test
	public void test__add_get__IUMorpheme_one_word() throws Exception {
		StringSegmenter iuSegmenter = new StringSegmenter_IUMorpheme();
		Trie iumorphemeTrie = makeTrieToTest();
		String[] takujuq_segments = null;
		try {
			takujuq_segments = iuSegmenter.segment("takujuq");
			iumorphemeTrie.add(takujuq_segments,"takujuq");
		} catch (Exception e) {
			assertFalse("An error occurred while adding an element to the trie.",true);
		}
		TrieNode node = iumorphemeTrie.getNode(new String[]{"{taku/1v}"});
		assertTrue("The node for 'taku/1n' should not be null.",node!=null);
		assertEquals("The key for this node is not correct.","{taku/1v}",node.keysAsString());
		assertFalse("This node should not a full word.",node.isTerminal());
	}
	
	@Test
	public void test__frequenciesOfWords() throws Exception {
		Trie charTrie = makeTrieToTest();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("world".split(""),"world");
		charTrie.add("hell boy".split(""),"hell boy");
		charTrie.add("heaven".split(""),"heaven");
		charTrie.add("worship".split(""),"worship");
		charTrie.add("world".split(""),"world");
		charTrie.add("heaven".split(""),"heaven");
		charTrie.add("world".split(""),"world");
		
		AssertTrie asserter = new AssertTrie(charTrie, "");
		
		asserter.frequencyEquals("blah".split(""), 0);
		asserter.frequencyEquals("worship".split(""), 1);
		asserter.frequencyEquals("heaven".split(""), 2);
		asserter.frequencyEquals("world".split(""), 3);
	}
	
	@Test
	public void test_getAllTerminals() throws Exception {
		Trie charTrie = makeTrieToTest();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("abba".split(""),"abba");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helm".split(""),"helm");
		charTrie.add("ok".split(""),"ok");
		
		new AssertTrie(charTrie)
				.terminalsForNodeEqual(
					"h".split(""), 
					new String[] {"helios", "hello", "helm", "hit"});
		
		new AssertTrie(charTrie)
			.terminalsForNodeEqual(
				"hel".split(""), 
				new String[] {"helios", "hello", "helm"});

		new AssertTrie(charTrie)
			.terminalsForNodeEqual(
				"o".split(""), 
				new String[] {"ok"});
	}
	
	
	@Test
	public void test_getAllTerminals__Case2() throws Exception {
		Trie charTrie = makeTrieToTest();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("abba".split(""),"abba");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helm".split(""),"helm");
		charTrie.add("ok".split(""),"ok");
		charTrie.add("okdoo".split(""),"okdoo");
		
		TrieNode hNode = charTrie.getNode("h".split(""));
		TrieNode[] h_terminals = charTrie.getAllTerminals(hNode);
		Assert.assertEquals("The number of words starting with 'h' should be 4.",
				4,h_terminals.length);
		
		TrieNode helNode = charTrie.getNode("hel".split(""));
		TrieNode[] hel_terminals = charTrie.getAllTerminals(helNode);
		Assert.assertEquals("The number of words starting with 'hel' should be 3.",
				3,hel_terminals.length);
		
		TrieNode oNode = charTrie.getNode("o".split(""));
		TrieNode[] o_terminals = charTrie.getAllTerminals(oNode);
		Assert.assertEquals("The number of words starting with 'o' should be 2.",
				2,o_terminals.length);
		
		TrieNode okNode = charTrie.getNode("ok".split(""));
		TrieNode[] ok_terminals = charTrie.getAllTerminals(okNode);
		Assert.assertEquals("The number of words starting with 'ok' should be 2.",
				2,o_terminals.length);
	}	
	@Test
	public void test_getNbOccurrences() throws Exception {
		Trie charTrie = makeTrieToTest();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("abba".split(""),"abba");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helm".split(""),"helm");
		charTrie.add("ok".split(""),"ok");
		charTrie.add("ok".split(""),"ok");
		
		new AssertTrie(charTrie, "")
			.hasTerminals(
				new String[] {
					"abba", "helios", "hello", "helm", "hit", "ok"
				})
			.hasNbOccurences(8)
			;
	}
	

	
	@Test
	public void test_getMostFrequentTerminal() throws Exception {
		Trie charTrie = makeTrieToTest();
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
		
		new AssertTrie(charTrie, "")
			.mostFrequentTerminalEquals(null, "ok")
			.mostFrequentTerminalEquals("hell".split(""), "hellam")
			;
	}
	
	@Test
	public void test_getMostFrequentTerminal__Case2() throws Exception {
		Trie charTrie = makeTrieToTest();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helios".split(""),"helios");
		TrieNode helNode = charTrie.getNode("hel".split(""));
		Assert.assertEquals("The most frequent terminal returned is faulty.",
			"helios", 
			charTrie.getMostFrequentTerminal(helNode).getTerminalSurfaceForm());
	}	
	
	@Test
	public void test_getMostFrequentTerminals() throws Exception {
		Trie charTrie = makeTrieToTest();
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
		
		new AssertTrie(charTrie, "")
			.mostFrequentTerminalsEqual(1, new String[] {"o k"})
			
			.mostFrequentTerminalsEqual(
				2, "hell".split(""), 
				new String[] {"h e l l a m", "h e l l o"})
			
			.mostFrequentTerminalsEqual(
					4, "hell".split(""), 
					new String[] {"h e l l a m", "h e l l o", "h e l l s"})
			
			;		
	}
	
	@Test
	public void test__getMostFrequentTerminals__Case2() throws Exception {
		Trie charTrie = makeTrieToTest();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hint".split(""),"hint");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helicopter".split(""),"helicopter");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helios".split(""),"helios");
		
		new AssertTrie(charTrie, "")
		
			// test n < number of terminals
			.mostFrequentTerminalsEqual(2, "hel".split(""), 
				new String[] {"h e l i o s", "h e l i c o p t e r"})
		
			// test with exclusion of nodes
		.mostFrequentTerminalsEqual(4, "hello".split(""), 
				new String[] {"h e l l o"})
		
		;
	}
	
	
	@Test
	public void test__mostFrequentSequenceForRoot__Char() throws Exception {
		Trie charTrie = makeTrieToTest();
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
	public void test__mostFrequentSequenceForRoot__IUMorpheme() throws Exception {
		Trie morphTrie = makeTrieToTest();
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
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__1() throws Exception {
		Trie morphTrie = makeTrieToTest();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"},"takulaaqsimajuq");
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"},"takusimajuq");
		
		new AssertTrie(morphTrie, "")
				.mostFrequentTerminalEquals(
						new String[] {"{taku/1v}"}, "{taku/1v}{juq/1vn}");
	}
	
	@Test
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__2() throws Exception {
		Trie morphTrie = makeTrieToTest();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"},"takulaaqsimajuq");
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"},"takusimajuq");
		
		new AssertTrie(morphTrie, "")
			.mostFrequentTerminalEquals(
				new String[] {"{taku/1v}"}, "{taku/1v}{juq/1vn}");
	}
	
	@Test
	public void test__getMostFrequentTerminalFromMostFrequenceSequenceFromRoot__3() throws Exception {
		Trie morphTrie = makeTrieToTest();
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{juq/1vn}"},"takujuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"},"takulaaqtuq");
		morphTrie.add(new String[] {"{taku/1v}","{laaq/2vv}","{sima/1vv}","{juq/1vn}"},"takulaaqsimajuq");
		morphTrie.add(new String[] {"{taku/1v}","{sima/1vv}","{juq/1vn}"},"takusimajuq");
		
		new AssertTrie(morphTrie, "")
			.mostFrequentTerminalEquals(
				new String[] {"{taku/1v}"}, "{taku/1v}{laaq/2vv}{juq/1vn}");
	}
}
