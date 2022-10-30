package org.iutools.corpus;

import ca.nrc.dtrc.elasticsearch.StreamlinedClient;
import ca.nrc.file.ResourceGetter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CorpusCompilationProcedureTest {

    protected static final String testCorpusName = "test_corpus";

    File corporaDirectory = null;
    File singleFileCorpus = null;
    File txtFilesDir = null;
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

        txtFilesDir = createTemporaryCorpusDirectoryWithSubdirectories(subdirs);

        wordDecompsFile = ResourceGetter.copyResourceToTempLocation("org/iutools/corpus/wordDecomps.json");

        CorpusTestHelpers.deleteCorpusIndex(testCorpusName);
        return;
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

    @Test
    public void test__CorpusCompiler__Synopsis() throws Exception {
        //
        // Use a CorpusCompiler to generate a corpus containing
        // information about all words found in a collection of TXT files.
        //
        // This is done in 2 phases
        //
        // Phase 1: Compile word frequencies
        // - Given a series of TXT files stored in a directory,
        //   generate a corpus that contain frequency info about
        //   all of its words, but NO information about their
        //   morphological decompositions.
        //
        // Phase 2: Add morphological decompositions to the corpus
        //
        // Below are the details of each step.
        //


        // Create a compiler. You need to provide it with a
        // working directory that will be used to store temp and output
        // files.
        //
        Path workDir = Files.createTempDirectory("corpus-compiler");
        CorpusCompiler compiler = new CorpusCompiler(workDir.toFile());

        // Set verbose to false if you don't want the compiler to print progress
        // messages on STDOUT
        //
        compiler.setVerbose(false);

        // Phase 1: Compile word frequencies from a series of txt files
        //
        String corpusName = "test_corpus";
        compiler.compile(corpusName, txtFilesDir);

        // At this point:
        // - The corpus called corpusName has been created and contains
        //   the frequency info about all words in the TXT files
        //
        // - The working directory contains the following files:
        //
        //   - words.txt: A list of all words, one word per line
        //
        //   - corpus.nodecomps.json: A JSON file with the records
        //     that are currently loaded in the corpus'
        //     ElasticSearch index. Those records only include
        //     frequency information (no morphological decomps).
        //

        // Before we can proceed with Phase 2, we must generate
        // a JSON file that contains the decompositions for all
        // words listed in the 'words.txt' file.
        //
        // Once you have generate this file, you must copy it to the
        // working directory under the name 'decomps.json'.
        //
        ResourceGetter.copyResourceFilesToDir(
		  "org/iutools/corpus/decomps.json", workDir);

        // Phase 2: Generate the word decompositions
        //
        // Recreate the compiler, feeding it the working directory
        // used in Phase 1 and use it to resume compilation.
        //
        // Note that you don't have to provide the name of the corpus
        // as it will have been saved in the progress.json file. Also
        // you don't need to provide the path of the TXT files, as
        // the TXT files are not needed for Phase 2.
        //
        compiler = new CorpusCompiler(workDir.toFile());
        compiler.compile();

        // At this point:
        //
        // - The corpus called corpusName all information about
        //     in the corpus, including morphological decompositions
        //
        // - The working directory contains the following files:
        //
        //   - words.txt: A list of all words, one word per line
        //
        //   - <corpusName>.json: A JSON file with the records
        //     that are currently loaded in the corpus'
        //     ElasticSearch index. Those records  include
        //     frequency information as well as morphological
        //     decomps. The JSON file is formatted in a way
        //     that is appropriate for storage in a version tracking
        //     system like Git or DAGsHub.
        //

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
        Logger logger = LogManager.getLogger("CompiledCorpusTest.createTemporaryCorpusDirectory");
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
}
