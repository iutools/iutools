package ca.nrc.datastructure.trie;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import ca.nrc.datastructure.trie.Trie.NodeOption;

public class Trie_InMemoryTest extends TrieTest {

	@Override
	public Trie_InMemory makeTrieToTest() {
		Trie_InMemory trie = new Trie_InMemory();
		return trie;
	}

	@Test
	public void test_toJSON__Char() throws Exception {
		Trie_InMemory charTrie = makeTrieToTest();
		charTrie.add("he".split(""),"he");
		charTrie.add("hit".split(""),"hit");
		charTrie.add("ok".split(""),"ok");
		String json = charTrie.toJSON();
		Gson gson = new Gson();
		Trie retrievedCharTrie = (Trie) gson.fromJson(json, charTrie.getClass());
		TrieNode node = retrievedCharTrie.getNode(new String[] {"h","i","t"}, NodeOption.TERMINAL);
		Assert.assertTrue("The node should be terminal.",node.isTerminal());

		new AssertTrieNode(node, "")
				.hasMostFrequentForm("hit");
	}	
	
}
