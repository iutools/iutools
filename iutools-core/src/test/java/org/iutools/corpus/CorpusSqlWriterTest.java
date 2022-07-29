package org.iutools.corpus;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import ca.nrc.file.ResourceGetter;
import ca.nrc.testing.TestDirs;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CorpusSqlWriterTest {

	CorpusSqlWriter corpusSqlWriter;

	@BeforeEach
	public void setUp() throws Exception {
		corpusSqlWriter = new CorpusSqlWriter();
	}
	
	@AfterEach
    public void tearDown() throws Exception {
    }

	/////////////////////////////////
	// DOCUMENTATION TESTS
	/////////////////////////////////
	
	@Test
	public void test__CorpusSqlWriter__Synopsis() throws Exception {
		CorpusSqlWriter writer_defaultOutputDirectory = new CorpusSqlWriter();

	}
	
	///////////////////////////
	// VERIFICATION TESTS
	///////////////////////////
	@Test
	public void test__writeCorpusDataSqlFiles(TestInfo testInfo) throws Exception {
		CorpusSqlWriter writer = new CorpusSqlWriter();
		TestDirs testDirs = new TestDirs(testInfo);
		String corpusJsonFileName = "smallCorpus.json";
		String corpusSqlFileName = "smallCorpus.sql";
		testDirs.copyResourceFileToInputs("org/iutools/corpus/testdata/"+corpusJsonFileName);
		Path smallCorpusJsonFilePath = testDirs.inputsFile(corpusJsonFileName);
		Path smallCorpusSqlFilePath = testDirs.outputsFile(corpusSqlFileName);
		String corpusName = "Small test corpus";
		String smallCorpusJsonFilePathname =  smallCorpusJsonFilePath.toString();
		String smallCorpusSqlFilePathname =  smallCorpusSqlFilePath.toString();
		writer.writeCorpusDataSqlFiles(smallCorpusJsonFilePathname,smallCorpusSqlFilePathname,corpusName);
		File smallCorpusSQLFile = smallCorpusSqlFilePath.toFile();
		Assertions.assertTrue(smallCorpusSQLFile.exists(), "The file "+smallCorpusSQLFile.getAbsoluteFile()+" should have been created.");
		String expectedFieldInCreateStatement = "  `corpus_name` text NOT NULL,";
		String expectedInsertStatement = "INSERT INTO `CorpusData` (`word`, `corpus_name`, `frequency`, `lang`, `decompositions_sample`) VALUES";
		String expectedInsertedData1 = "('Haakimik', 'Small test corpus', 1, 'en', '[]'),";
		String expectedInsertedData2 = "('Haakiqataurataalaurmata', 'Small test corpus', 1, 'en', '[[\"Haakiq/1v\",\"qatau/1vv\",\"rataaq/1vv\",\"lauq/1vv\",\"mata/tv-caus-4p\"]]'),";
		BufferedReader br = new BufferedReader(new FileReader(smallCorpusSQLFile));
		boolean fileContainsCorrectFieldStatement = false;
		boolean fileContainsCorrectInsertStatement = false;
		boolean fileContainsCorrectInsertedData1 = false;
		boolean fileContainsCorrectInsertedData2 = false;
		String line;
		while ( (line = br.readLine()) != null) {
			System.out.println(line);
			if (line.equals(expectedFieldInCreateStatement)) {
				fileContainsCorrectFieldStatement = true;
			} else if (line.equals(expectedInsertStatement)) {
				fileContainsCorrectInsertStatement = true;
			} else if (line.equals(expectedInsertedData1)) {
				fileContainsCorrectInsertedData1 = true;
			} else if (line.equals(expectedInsertedData2)) {
				fileContainsCorrectInsertedData2 = true;
			}
		}
		br.close();
		Assertions.assertTrue(fileContainsCorrectFieldStatement, "The file does not contain the expected field name in the table creation statement.");
		Assertions.assertTrue(fileContainsCorrectInsertStatement, "The file does not contain the expected INSERT statement.");
		Assertions.assertTrue(fileContainsCorrectInsertedData1, "The file does not contain the expected inserted data 1.");
		Assertions.assertTrue(fileContainsCorrectInsertedData2, "The file does not contain the expected inserted data 2.");
	}


	////////////////////////////
	// TEST HELPERS
	////////////////////////////

}
