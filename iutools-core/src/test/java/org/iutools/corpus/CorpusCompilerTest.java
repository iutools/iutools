package org.iutools.corpus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
	
    	wordDecompsFile = ResourceGetter.copyResourceToTempLocation("org/iutools/corpus/wordDecomps.json");
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
		// For a synopsis of this class, see the Synopsis of test case
		// CorpusCompilationProcedureTest
		//
	}
	
	///////////////////////////
	// VERIFICATION TESTS
	///////////////////////////


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
		CorpusCompiler compiler = null;
//        CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
//        CorpusCompiler compiler = new CorpusCompiler(compiledCorpus);
//
//        compiler.setVerbose(false);
//        compiler.saveFrequency = 3;
        
        return compiler;
	}
    
}
