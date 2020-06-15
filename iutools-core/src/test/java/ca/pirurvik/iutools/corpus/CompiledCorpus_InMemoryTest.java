package ca.pirurvik.iutools.corpus;

import static org.junit.Assert.assertArrayEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie_InMemory;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.datastructure.trie.Trie_InMemory;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertHelpers;
import ca.nrc.testing.AssertObject;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import ca.pirurvik.iutools.corpus.WordInfo;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory.WordWithMorpheme;
import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class CompiledCorpus_InMemoryTest extends CompiledCorpusTest
{
	@Override
	protected CompiledCorpus makeCorpusUnderTest(
			Class<? extends StringSegmenter> segmenterClass) {			
		CompiledCorpus_InMemory corpus = new CompiledCorpus_InMemory();
		corpus.setSegmenterClassName(segmenterClass.getName());
		return corpus;
	}	
	
	///////////////////////////////////////////////////////////////
	// These CompiledCorpus_BaseTest tests are 
	// currently not working for CompiledCorpus_Memory class
	///////////////////////////////////////////////////////////////
	
	
	// Method morphemeNgramFrequency() was never supported by InMemory 
	// in the first place. 
	@Test @Ignore
	public void test__morphemeNgramFrequency__HappyPath() throws Exception {
	}
	
}
