package ca.pirurvik.iutools.corpus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ca.inuktitutcomputing.applications.WordsNotDecomposed;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.file.ResourceGetter;

public class CorpusCompilerTest {
	
	File corporaDirectory = null;
	File singleFileCorpus = null;
	File multiDirCorpus = null;
	File wordDecompsFile = null;
	
	@Before
	public void setUp() throws Exception {
		corporaDirectory = Files.createTempDirectory("corpora").toFile();
		
		String[] stringsOfWords = new String[] {
				"nunavut inuit takujuq amma kanaujaq iglumik takulaaqtuq nunait",
				"umialiuqti iglumut sanalauqsimajuq",
				"uqaqti isumajunga qikiqtait"
				};
		
		singleFileCorpus = 
				createTemporaryCorpusDirectory(stringsOfWords, corporaDirectory);

		
		String[] stringsOfWords11 = new String[] {
				// Note: "takujuq" appears twice
				"nunavut", "takujuq", "iglumik", "plugak", "takujuq", "iijuq"
				};
		String[] stringsOfWords12 = new String[] {
				"iglumi", "takulauqtuq", "nanurmik"
				};
		String[] stringsOfWords21 = new String[] {
				"umiaq", "siniktuq", "kuummi"
				};
    	String[][][] subdirs = new String[][][] {
    		{ stringsOfWords11, stringsOfWords12 },
    		{ stringsOfWords21 }
    	};

    	multiDirCorpus = createTemporaryCorpusDirectoryWithSubdirectories(subdirs);
	
    	wordDecompsFile = ResourceGetter.copyResourceToTempLocation("ca/pirurvik/iutools/corpus/wordDecomps.json");
	}
	
	@After
    public void tearDown() throws Exception {
        if (corporaDirectory != null) {
        	File[] listOfFiles = corporaDirectory.listFiles();
        	for (File file : listOfFiles)
        		file.delete();
        }
        corporaDirectory = null;
    }

	/////////////////////////////////
	// DOCUMENTATION TESTS
	/////////////////////////////////
	
	@Test
	public void test__CorpusCompiler__Synopsis() throws Exception {
		//
		// Use a CorpusCompiler to compile stats about the various words
		// contained in a corpus.
		//
		CompiledCorpus_InMemory corpus = new CompiledCorpus_InMemory();
		CorpusCompiler compiler = new CorpusCompiler(corpus);
		
		// Set verbose to false if you don't want the compiler to print progress
		// messages on STDOUT
		//
		compiler.setVerbose(false);
		
		// You can compile the word frequencies for the files contained in a directory
		// Note that the compiler does not recursively walk through 
		// the directory and only looks at the files contained at the 
		// root directory.
		//
		compiler.compileWordFrequencies(corporaDirectory);
		
		// Note that compileWordFrequencies() does not compile information 
		// about the morphological decompositions of the various words.
		//
		// To add that information, you use updateWordDecompositions()
		// method
		
		compiler.updateWordDecompositions(wordDecompsFile);
		
		// At this point, information about the words will have been 
		// put into the CompiledCorpus object you provided at construction 
		// time
		//
		Iterator<String> iter = corpus.allWords();
		while (iter.hasNext()) {
			WordInfo wordInfo = corpus.info4word(iter.next());
			// etc...
		}
	}
	
	///////////////////////////
	// VERIFICATION TESTS
	///////////////////////////
	
    @Test
    public void test__compileWordFrequencies__SingleFileCorpus() throws Exception
    {
		CorpusCompiler compiler = makeCompiler();
		compiler.compileWordFrequencies(singleFileCorpus);
		
		new AssertCompiledCorpus(compiler.getCorpus(), "Before updating word decomps")
			.containsNWords(14)
			.containsWord("iglumut", "{iglu/1n}", "{mut/tn-dat-s}")
			.containsWord("sanalauqsimajuq", "{sana/1v}", "{lauqsima/1vv}", "{juq/1vn}")
		;
		
		compiler.updateWordDecompositions(wordDecompsFile);	
    }

    @Test
    public void test__compile__SingleFileCorpus() throws Exception
    {
		CorpusCompiler compiler = makeCompiler();
		compiler.compileCorpusFromScratch(singleFileCorpus);
		
		new AssertCompiledCorpus(compiler.getCorpus(), "")
			.containsNWords(14)
			.containsWord("iglumut", "{iglu/1n}", "{mut/tn-dat-s}")
			.containsWord("sanalauqsimajuq", "{sana/1v}", "{lauqsima/1vv}", "{juq/1vn}")
		;
    }
    
    @Test
    public void test__compile_MultiDirCorpus() throws Exception {

		CorpusCompiler compiler = makeCompiler();
		compiler.compileCorpusFromScratch(multiDirCorpus);
		
		new AssertCompiledCorpus(compiler.getCorpus(), "")
			.containsNWords(11)
		;
    }
    
	@Test
    public void test__compile__ResumeAfterPartialCompilation() throws Exception  
    {
		CorpusCompiler compiler = makeCompiler();
		
		// This simulates a situation where an exception is raised in the 
		// compiler at the 4th word.
		//
		compiler.stopAfter = 3;
		try {
			compiler.compileWordFrequencies(corporaDirectory);
		} catch (Exception e) {
			// We expect an error to be raised, because of the 
			// stopAfter.
		}
		new AssertCompiledCorpus(
			compiler.getCorpus(), 
			"Initial compilation did not yield expected results")
			.containsNWords(3)
		;
		
		// Resume compilation after the failure.
		compiler.stopAfter = -1;
		compiler.compileWordFrequencies(corporaDirectory);
		new AssertCompiledCorpus(
				compiler.getCorpus(), 
				"Resumed compilation did not yield expected results")
				.containsNWords(14)
			;
    }	

	////////////////////////////////////////////
	// START: Old tests from CompiledCorpus
	////////////////////////////////////////////
	
	    
	// TODO: This test really belongs in CompiledCorpusTest
    @Test
    public void test__compile__VerifyNgramFreqsAfterCompilation() throws Exception
    {
		String[] stringsOfWords = new String[] {
				// Note: "takujuq" appears twice
				"nunavut takujuq iglumik plugak takujuq iijuq"
				};
		File corpusDirPathname = createTemporaryCorpusDirectory(stringsOfWords);
		
		CorpusCompiler compiler = makeCompiler();
		compiler.compileCorpusFromScratch(corpusDirPathname);
		
		AssertCompiledCorpus asserter = 
			new AssertCompiledCorpus(compiler.getCorpus(), "");
		
		asserter.containsNWords(5);
		
		asserter
			// ngram with freq=1
			.totalWordsWithNgramEquals("^nun", 1)
			// ngram with freq > 1
			.totalWordsWithNgramEquals("juq$", 3)
			// ngram with freq = -
			.totalWordsWithNgramEquals("^nunavik$", 0)
			;
    }
	    
    @Test @Ignore
    public void test__canBeResumed() throws Exception {
    	CorpusCompiler compiler = makeCompiler();
        boolean canBeResumed = compiler.canBeResumed(singleFileCorpus);
        Assert.assertFalse("The compiler should not be able to resume; there is no JSON compilation backup.",canBeResumed);

        File jsonFile = new File(singleFileCorpus, CompiledCorpus_InMemory.JSON_COMPILATION_FILE_NAME);
        jsonFile.createNewFile();
        canBeResumed = compiler.canBeResumed(singleFileCorpus);
       Assert.assertTrue("The compiler should be able to resume; there is a JSON compilation backup.",canBeResumed);
	}
	    
	@Test
	public void test__processDocumentContents__HappyPath() throws Exception {
		String documentContents = "inuit takujuq nunavut takujuq takulaaqtuq";
		BufferedReader br = new BufferedReader(new StringReader(documentContents));
        CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
		CorpusCompiler compiler = new CorpusCompiler(compiledCorpus);
		compiler.setVerbose(false);
		File file = null;
		compiler.processDocumentContents(br,file, null);
		
		String[] inuit_segments = new String[]{"{inuk/1n}","{it/tn-nom-p}"};
		String[] taku_segments = new String[]{"{taku/1v}"};
		String[] takujuq_segments = new String[]{"{taku/1v}", "{juq/1vn}"};
		
		new AssertCompiledCorpus(compiledCorpus, "")
			.containsWord("inuit", inuit_segments)
			.containsWord("takujuq", takujuq_segments)
			;
	}
	
	////////////////////////////
	// TEST HELPERS
	////////////////////////////

	private File createTemporaryCorpusDirectory(String[] stringOfWords
		) throws IOException {
		File inDir = Files.createTempDirectory("").toFile();
		return createTemporaryCorpusDirectory(stringOfWords, inDir);
	}

		
	private File createTemporaryCorpusDirectory(
			String[] stringOfWords, File inDir) throws IOException {
       	Logger logger = Logger.getLogger("CompiledCorpusTest.createTemporaryCorpusDirectory");
        inDir.deleteOnExit();
        String corpusDirPath = inDir.getAbsolutePath();
        for (int i=0; i<stringOfWords.length; i++) {
        	File wordFile = new File(corpusDirPath+"/contents"+(i+1)+".txt");
        	BufferedWriter bw = new BufferedWriter(new FileWriter(wordFile));
        	bw.write(stringOfWords[i]);
        	bw.close();
        	logger.debug("wordFile= "+wordFile.getAbsolutePath());
        	logger.debug("contents= "+wordFile.length());
        }
        return new File(corpusDirPath);
	}

    private File createTemporaryCorpusDirectoryWithSubdirectories(
    		String[][][] subdirs) throws Exception {
		File inDir = Files.createTempDirectory("").toFile();
		return createTemporaryCorpusDirectoryWithSubdirectories(subdirs, inDir);    	
    }
	
    private File createTemporaryCorpusDirectoryWithSubdirectories(
    		String[][][] subdirs, File inDir) throws IOException {
        Path corpusDirectory = Files.createTempDirectory("corpus_");
        corpusDirectory.toFile().deleteOnExit();
        for (int isubdir=0; isubdir<subdirs.length; isubdir++) {
        	Path subDirectory = Files.createTempDirectory(corpusDirectory,"sub_");
        	subDirectory.toFile().deleteOnExit();
        	String [][] subdirFiles = subdirs[isubdir];
        	for (int ifile=0; ifile<subdirFiles.length; ifile++) {
        		String[] words = subdirFiles[ifile];
        		Path filepath = Files.createTempFile(subDirectory, "file_", ".txt");
        		filepath.toFile().deleteOnExit();
        		File file = filepath.toFile();
            	BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            	String lineOfWords = String.join(" ", words);
            	bw.write(lineOfWords);
            	bw.close();
        	}
        }
        return corpusDirectory.toFile();
    }

	private CorpusCompiler makeCompiler() {
        CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
        CorpusCompiler compiler = new CorpusCompiler(compiledCorpus);
        
        compiler.setVerbose(false);
        compiler.saveFrequency = 3;
        
        return compiler;
	}
    
}
