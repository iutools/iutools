package ca.inuktitutcomputing.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import ca.inuktitutcomputing.config.IUConfig;
import ca.inuktitutcomputing.core.CorpusTrieCompiler;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.testing.AssertHelpers;

/**
 * Unit test for simple App.
 */
public class CorpusTrieCompilerTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void test__resume_compilation_of_corpus_after_crash_or_abortion__1_file_in_corpus_directory()
    	throws Exception
    {
    	// contains 1 file of 6 lines with 8 words :
    	// nunavut inuit
    	// takujuq
    	// amma
    	// kinaujaq
    	// iglumik takulaaqtuq
    	// nunait

        String corpusDir = IUConfig.getIUDataPath()+"src/test/HansardCorpus1";
        CorpusTrieCompiler compiler = new CorpusTrieCompiler(StringSegmenter_IUMorpheme.class.getName());
        compiler.saveFrequency = 3;
        compiler.stopAfter = 7; // should stop after takulaaqtuq
        compiler.setCorpusDirectory(corpusDir);
            try {
        	compiler.run(true);
        } catch(Exception e) {
        	System.err.println("Exiting from compiler");
        }
			
		try {				
				CorpusTrieCompiler retrievedCompiler = CorpusTrieCompiler.readFromJSON(corpusDir);
				Trie trie = retrievedCompiler.trie;
				long expectedCurrentFileWordCounter = 6;
				assertEquals("The value of the 'current file word counter' is wrong.",
						expectedCurrentFileWordCounter,retrievedCompiler.currentFileWordCounter);
				HashMap<String,String[]> segmentsCache = retrievedCompiler.segmentsCache;
				String[] expected_takulaaqtuq_segments = null;
				assertArrayEquals("The cache should not contain the segments of 'takulaaqtuq'",
						expected_takulaaqtuq_segments,segmentsCache.get("takulaaqtuq"));
				String[] expected_nunait_segments = null;
				assertArrayEquals("The cache should not contain the segments of 'nunait'",
						expected_nunait_segments,segmentsCache.get("nunait"));
				String[] expected_iglumik_segments = new String[]{"{iglu/1n}","{mik/tn-acc-s}"};
				assertArrayEquals("The cache should contain the segments of 'iglumik'",expected_iglumik_segments,segmentsCache.get("iglumik"));
				TrieNode taku_juq_node = trie.getNode(new String[]{"{taku/1v}","{juq/1vn}"});
				String expectedText = "{taku/1v}{juq/1vn}";
				assertEquals("The text of the node should be '"+expectedText+"'.",expectedText,taku_juq_node.getText());
				TrieNode nuna_it_node = trie.getNode(new String[]{"{nuna/1n}","{it/tn-nom-p}"});
				assertTrue("The trie should not contain the node for 'nunait'.",nuna_it_node==null);
				
				// resume compilation
				retrievedCompiler.stopAfter = -1; // do not stop anymore; let compilation continue til the end
				retrievedCompiler.run(true);

				CorpusTrieCompiler completedCompiler = CorpusTrieCompiler.readFromJSON(corpusDir);
				Trie completeTrie = completedCompiler.trie;
				expectedCurrentFileWordCounter = 8;
				assertEquals("The value of the 'current file word counter' is wrong.",
						expectedCurrentFileWordCounter,completedCompiler.currentFileWordCounter);
				segmentsCache = completedCompiler.segmentsCache;
				expected_takulaaqtuq_segments = new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"};
				assertArrayEquals("The cache should contain the segments of 'takulaaqtuq'",
						expected_takulaaqtuq_segments,segmentsCache.get("takulaaqtuq"));
				expected_nunait_segments = new String[]{"{nuna/1n}","{it/tn-nom-p}"};
				assertArrayEquals("The cache should contain the segments of 'nunait'",
						expected_nunait_segments,segmentsCache.get("nunait"));
				expected_iglumik_segments = new String[]{"{iglu/1n}","{mik/tn-acc-s}"};
				assertArrayEquals("The cache should contain the segments of 'iglumik'",expected_iglumik_segments,segmentsCache.get("iglumik"));
				taku_juq_node = completeTrie.getNode(new String[]{"{taku/1v}","{juq/1vn}"});
				expectedText = "{taku/1v}{juq/1vn}";
				assertEquals("The text of the node should be '"+expectedText+"'.",expectedText,taku_juq_node.getText());
				nuna_it_node = completeTrie.getNode(new String[]{"{nuna/1n}","{it/tn-nom-p}"});
				assertTrue("The trie should contain the node for 'nunait'.",nuna_it_node!=null);
				expectedText = "{nuna/1n}{it/tn-nom-p}";
				assertEquals("The text of the node should be '"+expectedText+"'.",expectedText,nuna_it_node.getText());

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
        
    
    @Test
    public void test__resume_compilation_of_corpus_after_crash_or_abortion__2_files_in_corpus_directory()
    	throws Exception
    {
    	// contains 2 files: 
    	// 1 of 6 lines with 8 words:      1 of 3 lines with 3 words:
    	// nunavut inuit                   umialiuqti
    	// takujuq                         iglumut
    	// amma                            sanalauqsimajuq
    	// kinaujaq
    	// iglumik takulaaqtuq
    	// nunait

        String corpusDir = IUConfig.getIUDataPath()+"src/test/HansardCorpus2";
        CorpusTrieCompiler compiler = new CorpusTrieCompiler(StringSegmenter_IUMorpheme.class.getName());
        compiler.saveFrequency = 3;
        compiler.stopAfter = 10; // should stop after iglumut
        compiler.setCorpusDirectory(corpusDir);
            try {
        	compiler.run(true);
        } catch(Exception e) {
        	System.err.println("Exiting from compiler");
        }
			
		try {				
				CorpusTrieCompiler retrievedCompiler = CorpusTrieCompiler.readFromJSON(corpusDir);
				Trie trie = retrievedCompiler.trie;
				long expectedCurrentFileWordCounter = 1;
				assertEquals("The value of the 'current file word counter' is wrong.",
						expectedCurrentFileWordCounter,retrievedCompiler.currentFileWordCounter);
				HashMap<String,String[]> segmentsCache = retrievedCompiler.segmentsCache;
				
				String[] expected_iglumut_segments = null;
				assertArrayEquals("The cache should not contain the segments of 'iglumut'",
						expected_iglumut_segments,segmentsCache.get("iglumut"));
				String[] expected_nunait_segments = new String[]{"{nuna/1n}","{it/tn-nom-p}"};
				assertArrayEquals("The cache should contain the segments of 'nunait'",
						expected_nunait_segments,segmentsCache.get("nunait"));
				String[] expected_iglumik_segments = new String[]{"{iglu/1n}","{mik/tn-acc-s}"};
				assertArrayEquals("The cache should contain the segments of 'iglumik'",expected_iglumik_segments,segmentsCache.get("iglumik"));
				TrieNode taku_juq_node = trie.getNode(new String[]{"{taku/1v}","{juq/1vn}"});
				String expectedText = "{taku/1v}{juq/1vn}";
				assertEquals("The text of the node should be '"+expectedText+"'.",expectedText,taku_juq_node.getText());
				TrieNode nuna_it_node = trie.getNode(new String[]{"{nuna/1n}","{it/tn-nom-p}"});
				assertTrue("The trie should not contain the node for 'nunait'.",nuna_it_node!=null);
				
				// resume compilation
				retrievedCompiler.stopAfter = -1; // do not stop anymore; let compilation continue til the end
				retrievedCompiler.run(true);

				CorpusTrieCompiler completedCompiler = CorpusTrieCompiler.readFromJSON(corpusDir);
				Trie completeTrie = completedCompiler.trie;
				expectedCurrentFileWordCounter = 3;
				assertEquals("The value of the 'current file word counter' is wrong.",
						expectedCurrentFileWordCounter,completedCompiler.currentFileWordCounter);
				segmentsCache = completedCompiler.segmentsCache;
				String[] expected_takulaaqtuq_segments = new String[] {"{taku/1v}","{laaq/2vv}","{juq/1vn}"};
				assertArrayEquals("The cache should contain the segments of 'takulaaqtuq'",
						expected_takulaaqtuq_segments,segmentsCache.get("takulaaqtuq"));
				expected_nunait_segments = new String[]{"{nuna/1n}","{it/tn-nom-p}"};
				assertArrayEquals("The cache should contain the segments of 'nunait'",
						expected_nunait_segments,segmentsCache.get("nunait"));
				expected_iglumik_segments = new String[]{"{iglu/1n}","{mik/tn-acc-s}"};
				assertArrayEquals("The cache should contain the segments of 'iglumik'",expected_iglumik_segments,segmentsCache.get("iglumik"));
				taku_juq_node = completeTrie.getNode(new String[]{"{taku/1v}","{juq/1vn}"});
				expectedText = "{taku/1v}{juq/1vn}";
				assertEquals("The text of the node should be '"+expectedText+"'.",expectedText,taku_juq_node.getText());
				nuna_it_node = completeTrie.getNode(new String[]{"{nuna/1n}","{it/tn-nom-p}"});
				assertTrue("The trie should contain the node for 'nunait'.",nuna_it_node!=null);
				expectedText = "{nuna/1n}{it/tn-nom-p}";
				assertEquals("The text of the node should be '"+expectedText+"'.",expectedText,nuna_it_node.getText());
				
				TrieNode iglu_mut_node = completeTrie.getNode(new String[]{"{iglu/1n}","{mut/tn-dat-s}"});
				assertTrue("The trie should contain the node for 'iglumut'.",iglu_mut_node!=null);
				expectedText = "{iglu/1n}{mut/tn-dat-s}";
				assertEquals("The text of the node should be '"+expectedText+"'.",expectedText,iglu_mut_node.getText());
				
				TrieNode sana_lauqsima_juq_node = completeTrie.getNode(new String[]{"{sana/1v}","{lauqsima/1vv}","{juq/1vn}"});
				assertTrue("The trie should contain the node for 'sanalauqsimajuq'.",sana_lauqsima_juq_node!=null);
				expectedText = "{sana/1v}{lauqsima/1vv}{juq/1vn}";
				assertEquals("The text of the node should be '"+expectedText+"'.",expectedText,sana_lauqsima_juq_node.getText());
				

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
    
    @Test
    public void test__compile__3_files_in_corpus_directory()
    {
    	// contains 3 files: 
    	// 1 of 6 lines with 8 words:      1 of 3 lines with 3 words:
    	// nunavut inuit                   umialiuqti
    	// takujuq                         iglumut
    	// amma                            sanalauqsimajuq
    	// kinaujaq
    	// iglumik takulaaqtuq
    	// nunait

        String corpusDir = System.getenv("IUTOOLS")+"/java/iutools-data/src/test/HansardCorpus3";
        CorpusTrieCompiler compiler = new CorpusTrieCompiler(StringSegmenter_IUMorpheme.class.getName());
        compiler.saveFrequency = 3;
        compiler.setCorpusDirectory(corpusDir);
        try {
        	compiler.run(true);
        } catch(Exception e) {
        	System.err.println("Exiting from compiler");
        }
			
		try {				
				CorpusTrieCompiler retrievedCompiler = CorpusTrieCompiler.readFromJSON(corpusDir);
				Trie trie = retrievedCompiler.trie;
				
				TrieNode iglu_mut_node = trie.getNode(new String[]{"{iglu/1n}","{mut/tn-dat-s}"});
				assertTrue("The trie should contain the node for 'iglumut'.",iglu_mut_node!=null);
				String expectedText = "{iglu/1n}{mut/tn-dat-s}";
				assertEquals("The text of the node should be '"+expectedText+"'.",expectedText,iglu_mut_node.getText());
				
				TrieNode sana_lauqsima_juq_node = trie.getNode(new String[]{"{sana/1v}","{lauqsima/1vv}","{juq/1vn}"});
				assertTrue("The trie should contain the node for 'sanalauqsimajuq'.",sana_lauqsima_juq_node!=null);
				expectedText = "{sana/1v}{lauqsima/1vv}{juq/1vn}";
				assertEquals("The text of the node should be '"+expectedText+"'.",expectedText,sana_lauqsima_juq_node.getText());
				

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
    
	@Test
	public void test__processDocumentContents__happy_path() throws Exception {
		String documentContents = "inuit takujuq nunavut takujuq takulaaqtuq";
		BufferedReader br = new BufferedReader(new StringReader(documentContents));
		CorpusTrieCompiler trieCompiler = new CorpusTrieCompiler(StringSegmenter_IUMorpheme.class.getName());
		trieCompiler.processDocumentContents(br);
		
		String[] inuit_segments = new String[]{"{inuk/1n}","{it/tn-nom-p}"};
		String[] taku_segments = new String[]{"{taku/1v}"};
		String[] takujuq_segments = new String[]{"{taku/1v}", "{juq/1vn}"};
		
		assertContains(trieCompiler, inuit_segments, 1, inuit_segments);
		assertContains(trieCompiler, taku_segments, 3, takujuq_segments);
		assertContains(trieCompiler, takujuq_segments, 2, takujuq_segments);
	}
	
	
	
	

	private void assertContains(CorpusTrieCompiler trieCompiler,
			String[] segs, long expFreq) {
		assertContains(trieCompiler, segs, expFreq, null);
	}

	private void assertContains(CorpusTrieCompiler trieCompiler,
			String[] segs, long expFreq, String[] expLongestTerminal) {
		TrieNode gotNode = trieCompiler.trie.getNode(segs);
		String seqs_asString = String.join(", ", segs);
		Assert.assertTrue("Trie should have contained sequence: "+seqs_asString+"\nTrie contained:\n"+trieCompiler.trie.toJSON(), gotNode != null);
		long gotFreq = gotNode.getFrequency();
		Assert.assertEquals("Frequency was not as expected for segmenets: "+seqs_asString, expFreq, gotFreq);
		
		if (expLongestTerminal != null) {
			String gotMostFreqTerminalTxt = gotNode.getMostFrequentTerminal().getText();
			String expMostFreqTerminalTxt = String.join("", expLongestTerminal);
			AssertHelpers.assertStringEquals("Most frequent terminal was not as expected for segs "+seqs_asString, 
					expMostFreqTerminalTxt, gotMostFreqTerminalTxt);
			
		}
	}
	

}
