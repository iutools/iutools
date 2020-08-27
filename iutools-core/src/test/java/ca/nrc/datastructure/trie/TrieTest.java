package ca.nrc.datastructure.trie;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import ca.nrc.datastructure.trie.Trie.NodeOption;
import ca.nrc.datastructure.trie.visitors.TrieNodeVisitor;
import ca.nrc.testing.AssertObject;

public abstract class TrieTest {
	
	public abstract Trie makeTrieToTest() throws Exception;
	
	public Trie makeTrieToTest(String[] words) throws Exception {
		Trie trie = makeTrieToTest();
		for (String word: words) {
			trie.add(word.split(""), word);
		}
		return trie;
	}
	
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
		TrieNode node = trie.node4keys(helloChars);
		if (node == null) {
			// This means the string was not found in the Trie
		} else {
		}
		
		// By default, node4keys() will create the node if it does not
		// exist. But you can override that with the NO_CREATE option
		//
		node = trie.node4keys("nonexistant".split(""), NodeOption.NO_CREATE);
		
		// You can also ask to get the terminal (aka leaf) node that corresponds to 
		// the sequence of segments. You do this using the TERMINAL option.
		//
		node = trie.node4keys("hello".split(""), NodeOption.TERMINAL);
		
		// You can also use NO_CREATE and TERMINAL together
		//
		node = trie.node4keys("nonexistant".split(""),
					NodeOption.NO_CREATE, NodeOption.TERMINAL);

		// You can find out the number of terminal nodes  
		// that match certain sequences of characters
		//
		//    For example, this returns the total number of terminal 
		//    nodes that start with "hel"
		//
		long total = trie.totalTerminals("hel".split(""));
		
		// You can also get lists of terminals that match certain 
		// sequences of charactercs
		
		// Get all of the terminals
		TrieNode[] nodes = trie.getTerminals();
		
		// Get all the terminals that START with "hel"
		nodes = trie.getTerminals("hel".split(""));
		
		// Get all the terminals that contain the ngram "el"
		nodes = trie.getTerminals("el".split(""), false);
		
		// You can also traverse all nodes and invoke 
		// a visitor on each node. For example, this
		// visitor will count the number of nodes
		//
		
		TrieNodeVisitor visitor = new TrieNodeVisitor() {
			public long count = 0;
            public void visitNode(TrieNode node) {
            	count++;
            }
        };
		trie.traverseNodes(visitor);
	}
	
	@Test
	public void test__node4keys__NO_CREATE() throws Exception {
		Trie charTrie = makeTrieToTest();
		String[] nonExistantKeys = "nonexistant".split("");
		
		TrieNode node = charTrie.node4keys(nonExistantKeys, NodeOption.NO_CREATE);
		Assert.assertTrue(
			"node4keys(NO_CREATE) should return null for non-existant key sequence",
			node == null);

		node = charTrie.node4keys(nonExistantKeys);
		Assert.assertTrue(
			"node4keys() should return new empty node for non-existant key sequence",
			node != null);
		
	}

	@Test
	public void test__node4keys__TERMINAL() throws Exception {
		Trie charTrie = makeTrieToTest();
		String[] helloChars = "hello".split("");
		charTrie.add(helloChars, "hello");
		TrieNode node = charTrie.node4keys(helloChars, NodeOption.TERMINAL);
		Assert.assertTrue(
			"node4keys(TERMINAL) should return a terminal node",
			node.isTerminal());
		
		node = charTrie.node4keys(helloChars);
		Assert.assertTrue(
			"node4keys() should return a non-terminal for keys: "+String.join(",", helloChars),
			!node.isTerminal());
	}
	
	@Test
	public void test__getParentNode() throws Exception {
		String[] words = new String[] {
			"hello", "hit", "abba", "helios", "helm", "ok"
		};
		Trie charTrie = makeTrieToTest(words);

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
	public void test__add__FreqIncrementGreaterThanOne() throws Exception {
		Trie charTrie = makeTrieToTest();
		String word = "hi";
		String[] wordChars = word.split("");
		charTrie.add(wordChars, word);
		
		TrieNode terminalNode = charTrie.node4keys(wordChars, NodeOption.TERMINAL);
		new AssertTrieNode(terminalNode, 
				"After adding word "+word+" withouth specifying a frequency increment")
			.hasFrequency(1);
		
		charTrie.add(wordChars, word, 10);
		terminalNode = charTrie.node4keys(wordChars, NodeOption.TERMINAL);
		new AssertTrieNode(terminalNode, 
				"After adding word "+word+" with frequency increment = 10")
			.hasFrequency(11);
		
	}

	@Test
	public void test__add__NullInputSegments() throws Exception {
		String word1 = "someWordThatDoesNotSegment";
		String word2 = "someOtherWordThatDoesNotSegment";
		String[] nullSegments = null;
		Trie charTrie = makeTrieToTest();
		charTrie.add(nullSegments, word1);
		charTrie.add(nullSegments, word2);
		TrieNode gotNode = charTrie.node4keys(nullSegments, NodeOption.TERMINAL);
		new AssertTrieNode(gotNode, "")
			.hasSurfaceForms(new String[] {word1, word2})
			;
	}
	
	@Test
	public void test_add__check_terminal() throws Exception {
		Trie charTrie = makeTrieToTest();
		String word = "hi";
		String[] wordChars = word.split("");
		charTrie.add(wordChars, word);
		
		TrieNode terminalNode = charTrie.node4keys(wordChars, NodeOption.TERMINAL);
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
			"{nalunaq/1n}", "{iq/1nv}", "{si/2vv}", "{vut/tv-dec-3p}"
		};
		TrieNode terminalNode = iumorphemeTrie.node4keys(segments, NodeOption.TERMINAL);
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
		
		String hello = "hello";
		String[] helloChars = hello.split("");
		charTrie.add(helloChars, hello);
		
		String hellBoy = "hell boy";
		String[] hellBoyChars = hellBoy.split("");
		charTrie.add(hellBoyChars, hellBoy);

		TrieNode node = charTrie.node4keys(helloChars, NodeOption.NO_CREATE);
		AssertTrieNode asserter = new AssertTrieNode(node, "");
		asserter.isNotNull();
		asserter
			.hasTerminalNode()
			.hasSegments(helloChars)
			.hasFrequency(1)
			;

		String[] hellChars = "hell".split("");
		node = charTrie.node4keys(hellChars);
		asserter = new AssertTrieNode(node, "");
		asserter
			.isNotNull();
		asserter
			.hasSegments(hellChars)
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
		TrieNode node = wordTrie.node4keys(new String[]{"hello"});
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
		TrieNode node = iumorphemeTrie.node4keys(new String[]{"{taku/1v}"});
		assertTrue("The node for 'taku/1n' should not be null.",node!=null);
		assertEquals("The key for this node is not correct.","{taku/1v}",node.keysAsString());
		assertFalse("This node should not a full word.",node.isTerminal());
	}
	
	@Test
	public void test__frequenciesOfWords() throws Exception {
//		Trie charTrie = makeTrieToTest();
//		charTrie.add("hello".split(""),"hello");
//		charTrie.add("world".split(""),"world");
//		charTrie.add("hell boy".split(""),"hell boy");
//		charTrie.add("heaven".split(""),"heaven");
//		charTrie.add("worship".split(""),"worship");
//		charTrie.add("world".split(""),"world");
//		charTrie.add("heaven".split(""),"heaven");
//		charTrie.add("world".split(""),"world");
		String[] words = new String[] {
			"hello", "world", "hell boy", "heaven", "worship",
			// These words appear more than once
			"world", "heaven", "world"
		};
		Trie charTrie = makeTrieToTest(words);
		
		AssertTrie asserter = new AssertTrie(charTrie, "");
		
		asserter.frequencyEquals("blah".split(""), 0);
		asserter.frequencyEquals("worship".split(""), 1);
		asserter.frequencyEquals("heaven".split(""), 2);
		asserter.frequencyEquals("world".split(""), 3);
	}
	
	@Test
	public void test_getAllTerminals() throws Exception {
//		Trie charTrie = makeTrieToTest();
//		charTrie.add("hello".split(""),"hello");
//		charTrie.add("hit".split(""),"hit");
//		charTrie.add("abba".split(""),"abba");
//		charTrie.add("helios".split(""),"helios");
//		charTrie.add("helm".split(""),"helm");
//		charTrie.add("ok".split(""),"ok");
		String[] words = new String[] {
			"hello", "hit", "abba", "helios", "helm", "ok"
		};
		Trie charTrie = makeTrieToTest(words);
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
		
		TrieNode hNode = charTrie.node4keys("h".split(""));
		TrieNode[] h_terminals = charTrie.getTerminals(hNode);
		Assert.assertEquals("The number of words starting with 'h' should be 4.",
				4,h_terminals.length);
		
		TrieNode helNode = charTrie.node4keys("hel".split(""));
		TrieNode[] hel_terminals = charTrie.getTerminals(helNode);
		Assert.assertEquals("The number of words starting with 'hel' should be 3.",
				3,hel_terminals.length);
		
		TrieNode oNode = charTrie.node4keys("o".split(""));
		TrieNode[] o_terminals = charTrie.getTerminals(oNode);
		Assert.assertEquals("The number of words starting with 'o' should be 2.",
				2,o_terminals.length);
		
		TrieNode okNode = charTrie.node4keys("ok".split(""));
		TrieNode[] ok_terminals = charTrie.getTerminals(okNode);
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
			.hasTotalTerminalOccurences(8)
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
		TrieNode helNode = charTrie.node4keys("hel".split(""));
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
		
			// Test with n > number of terminals
			.mostFrequentTerminalsEqual(2, "hel".split(""), 
				new String[] {"h e l i o s", "h e l i c o p t e r"})
		;
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
	
	@Test
	public void test__wordChars__HappyPath() throws Exception {
		String word = "hello";
		String[] gotChars = makeTrieToTest().wordChars(word);
		String[] expChars = new String[] {"h","e","l","l","o",TrieNode.TERMINAL_SEG};
		AssertObject.assertDeepEquals("", expChars, gotChars);
	}
	
	@Test
	public void test__getTerminalsMatchingNgram__HappyPath() throws Exception {
		Trie charTrie = makeTrieToTest();
		charTrie.add("hello".split(""),"hello");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("helios".split(""),"helios");
		charTrie.add("helm".split(""),"helm");
		
		new AssertTrie(charTrie, "")
			.terminalsMatchingNGramEqual(
				"el", new String[] {"h e l l o", "h e l i o s", "h e l m"})
		;
	}
	
	@Test
	public void test__ensureTerminal__SegmentsNotAlreadyTerminal__AppendsTerminalChar() throws Exception {
		String[] segments = new String[] {"hello", "world"};
		String[] gotTerminal = Trie.ensureTerminal(segments);
		String[] expTerminal = new String[] {"hello", "world", TrieNode.TERMINAL_SEG};
		AssertObject.assertDeepEquals(
			"The list of segments should have been made into a terminal list", 
			gotTerminal, expTerminal);
	}

	@Test
	public void test__ensureTerminal__SegmentsAlreadyTerminal__LeavesThemAlone() throws Exception {
		String[] segments = new String[] {"hello", "world", TrieNode.TERMINAL_SEG};
		String[] gotTerminal = Trie.ensureTerminal(segments);
		String[] expTerminal = new String[] {"hello", "world", TrieNode.TERMINAL_SEG};
		AssertObject.assertDeepEquals(
			"The list of segments should have been made into a terminal list", 
			gotTerminal, expTerminal);
	}
	
	@Test
	public void test__traverseNodes__HappyPath() throws Exception {
		String[] words = new String[] {"hello", "world"};
		Trie trie = makeTrieToTest();
		for (String aWord: words) {
			trie.add(aWord.split(""), aWord);
		}
		
		NodeCountingVisitor visitor1 = new NodeCountingVisitor();
		trie.traverseNodes(visitor1);
		Assert.assertEquals(
			"Number of nodes of any type was not as expected", 
			2, visitor1.count);

		visitor1 = new NodeCountingVisitor();
		trie.traverseNodes(visitor1, false);
		Assert.assertEquals(
			"Number of terminal nodes was not as expected", 
			13, visitor1.count);
		
		
	}

	@Test
	public void test__totalTerminals__HappyPath() throws Exception {
		String[] words = new String[] {
			"hello", "hello", // "hello" occurs twice
			"world", "helios"};
		Trie trie = makeTrieToTest(words);
		
		String[] chars = "hel".split("");
		new AssertTrie(trie, "")
			.totalTerminalsIs(chars, 2)
			.totalTerminalOccurencesIs(chars, 3)
		;		
	}
	
	@Test
	public void test__getSize__HappyPath() throws Exception {
		String[] words = new String[] {
				"hello", "hello", // "hello" occurs twice
				"world", "helios"};
		Trie trie = makeTrieToTest(words);
		
		String[] chars = "hel".split("");
		new AssertTrie(trie, "")
			.sizeIs(3)
		;		
		
	}

	@Test
	public void test__getRoot__HappyPath() throws Exception {
		TrieNode root =
			makeTrieToTest(new String[] {"hello", "hell"})
			.getRoot();
		AssertTrieNode asserter = new AssertTrieNode(root, "");
		asserter.isNotNull();
		asserter
			.isNotTerminal()
			.hasSegments(new String[0])
			;
	}
	
	////////////////////////////////////////
	// TEST HELPERS
	////////////////////////////////////////
	
	public static class NodeCountingVisitor extends TrieNodeVisitor {
		public long count = 0;

		@Override
		public void visitNode(TrieNode node) {
			count++;
		}
	}
}
