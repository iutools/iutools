package ca.pirurvik.iutools.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieNode;

/**
 * Unit test for simple App.
 */
public class CorpusTrieCompilerTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void test__compilation_with_save_at_10_words()
    {
        String corpusDir = System.getenv("IUTOOLS")+"/java/iutools-data/src/test/HansardCorpus";
        try {
        	CorpusTrieCompiler compiler = new CorpusTrieCompiler();
        	compiler.saveFrequency = 3;
        	compiler.dirName = corpusDir;
        	compiler.run();
			BufferedReader br = new BufferedReader(new FileReader(compiler.outputFile.getAbsolutePath()));
			int expectedNbSavingLines = 2;
			int nbSavingLines = 0;
			String line;
			while ( (line=br.readLine()) != null) {
				if (line.contains("saving verbose and jsoned trie"))
					++nbSavingLines;
			}
			assertEquals("The number of verbose lines containing 'saving verbose and jsoned trie' should be "+expectedNbSavingLines,expectedNbSavingLines,nbSavingLines);
			br.close();
			
			try {
				Trie trie = compiler.readTrieFromJSON();
				TrieNode node = trie.getNode(new String[]{"{taku/1v}","{juq/1vn}"});
				String expectedText = "{taku/1v}{juq/1vn}";
				assertEquals("The text of the node should be '"+expectedText+"'.",expectedText,node.getText());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	@Test
    public void test__form_a_trie_from_json_string() {
    	String json = "{\"size\":5,\"segmenterclassname\":\"ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme\",\"root\":{\"surfaceForm\":\"\",\"childrenInSurface\":{\"inu\":{\"surfaceForm\":\"inu\",\"childrenInSurface\":{\"it\":{\"surfaceForm\":\"inuit\",\"childrenInSurface\":{},\"text\":\"{inuk/1n}{it/tn-nom-p}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"text\":\"{inuk/1n}\",\"isWord\":false,\"frequency\":1,\"children\":{\"{it/tn-nom-p}\":{\"text\":\"{inuk/1n}{it/tn-nom-p}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"stats\":{}},\"taku\":{\"surfaceForm\":\"taku\",\"childrenInSurface\":{\"juq\":{\"surfaceForm\":\"takujuq\",\"childrenInSurface\":{},\"text\":\"{taku/1v}{juq/1vn}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"text\":\"{taku/1v}\",\"isWord\":false,\"frequency\":1,\"children\":{\"{juq/1vn}\":{\"text\":\"{taku/1v}{juq/1vn}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"stats\":{}},\"iglu\":{\"surfaceForm\":\"iglu\",\"childrenInSurface\":{\"mik\":{\"surfaceForm\":\"iglumik\",\"childrenInSurface\":{},\"text\":\"{iglu/1n}{mik/tn-acc-s}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"text\":\"{iglu/1n}\",\"isWord\":false,\"frequency\":1,\"children\":{\"{mik/tn-acc-s}\":{\"text\":\"{iglu/1n}{mik/tn-acc-s}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"stats\":{}},\"nunavut\":{\"surfaceForm\":\"nunavut\",\"childrenInSurface\":{},\"text\":\"{nunavut/1n}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}},\"amma\":{\"surfaceForm\":\"amma\",\"childrenInSurface\":{},\"text\":\"{amma/1c}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"text\":\"\",\"isWord\":false,\"frequency\":0,\"children\":{\"{amma/1c}\":{\"text\":\"{amma/1c}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}},\"{iglu/1n}\":{\"text\":\"{iglu/1n}\",\"isWord\":false,\"frequency\":1,\"children\":{\"{mik/tn-acc-s}\":{\"text\":\"{iglu/1n}{mik/tn-acc-s}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"stats\":{}},\"{taku/1v}\":{\"text\":\"{taku/1v}\",\"isWord\":false,\"frequency\":1,\"children\":{\"{juq/1vn}\":{\"text\":\"{taku/1v}{juq/1vn}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"stats\":{}},\"{nunavut/1n}\":{\"text\":\"{nunavut/1n}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}},\"{inuk/1n}\":{\"text\":\"{inuk/1n}\",\"isWord\":false,\"frequency\":1,\"children\":{\"{it/tn-nom-p}\":{\"text\":\"{inuk/1n}{it/tn-nom-p}\",\"isWord\":true,\"frequency\":1,\"children\":{},\"stats\":{}}},\"stats\":{}}},\"stats\":{}}}";
    	Gson gson = new Gson();
    	Trie trie = gson.fromJson(json, Trie.class);
    	long trieSize = trie.getSize();
    	assertEquals("The size of the trie is wrong.",5,trieSize);
    }
	
	@Test
	public void test__processDocumentContents__happy_path() throws Exception {
		String documentContents = "inuit takujuq nunavut takujuq takulaaqtuq";
		BufferedReader br = new BufferedReader(new StringReader(documentContents));
		CorpusTrieCompiler trieCompiler = new CorpusTrieCompiler(new StringSegmenter_IUMorpheme());
		trieCompiler.processDocumentContents(br);
		
		String[] inuit_segments = new String[]{"{inuk/1n}","{it/tn-nom-p}"};
		assertContains(trieCompiler, inuit_segments, 1);
		
		String[] taku_segments = new String[]{"{taku/1v}"};
		assertContains(trieCompiler, taku_segments, 3);
		
		String[] takujuq_segments = new String[]{"{taku/1v}", "{juq/1vn}"};
		assertContains(trieCompiler, takujuq_segments, 2);
	}

	private void assertContains(CorpusTrieCompiler trieCompiler,
			String[] segs, long expFreq) {
		TrieNode gotNode = trieCompiler.corpusTrie.getNode(segs);
		String seqs_asString = String.join(", ", segs);
		Assert.assertTrue("Trie should have contained sequence: "+seqs_asString+"\nTrie contained:\n"+trieCompiler.corpusTrie.toJSON(), gotNode != null);
		long gotFreq = gotNode.getFrequency();
		Assert.assertEquals("Frequency was not as expected for segmenets: "+seqs_asString, expFreq, gotFreq);
	}
	

}
