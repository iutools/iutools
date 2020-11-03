package ca.pirurvik.iutools.corpus;

import ca.nrc.file.ResourceGetter;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class CorpusCompilationProcedureTest {

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

    @Test
    public void test__CorpusCompiler__Synopsis() throws Exception {
        //
        // Use a CorpusCompiler to generate a JSON file containing
        // information about all words found in a collection of TXT files.
        //
        // This is done in 3 phases
        //
        // Phase 1: Compile word frequencies
        // - Given a series of TXT files stored in a directory,
        //   compile a JSON file listing all words and their frequencies.
        //
        // Phase 2: Add morphological decompositions to the file
        // - Run the 'add_decomps' console command in pipeline mode,
        //   feeding it the word frequencies file generated above.
        // - This will produce another JSON file that has the same word
        //   information as the first file, but with the decompositions added.
        //
        // Phase 3 (OPTIONAL): Reformat the word info file
        // - The file produced in Phase 2 has one JSON record per line.
        // - If you want to store that file in a version management system
        //   like DAGsHub, we recommend that you reformat the file so that
        //   each JSON record is spread over several lines (one line per field
        //   or element of a collection value). That way, if only one field
        //   or value in the record changes, the version managment sysstem
        //   can only update the delta for that one line.
        //
        // Below are the details of each step.
        //
        CorpusCompiler compiler = new CorpusCompiler(multiDirCorpus);

        // Set verbose to false if you don't want the compiler to print progress
        // messages on STDOUT
        //
        compiler.setVerbose(false);

        // Phase 1: Compile word frequencies from a series of txt files
        //
        String corpusName = "test_corpus";
        compiler.compileWordFrequencies(corpusName);

        // Phase 2: Generate the word decompositions
        //
        // wordsFile() provides a file containing all words that were seen in
        // the TXT files.
        //
        // You must then generate a JSON file that contains the morphological
        // decompositions of every word in that file.
        // This is typcally done using a cluster of machines that run the
        // Console command 'segment_iu' in pipeline mode.
        //
        File wordsFile = compiler.wordsFile();

        // Let's assume the JSON file containing the decomps has been generated
        // and that it has been put under the appropriate name in the cor
        //
        ResourceGetter.copyResourceFilesToDir(
            "decompsFile.json", multiDirCorpus.toPath());

        // You can then proceed with
        // Phase 3: Update the decompositions
        //
        compiler.updateWordDecompositions();

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

}
