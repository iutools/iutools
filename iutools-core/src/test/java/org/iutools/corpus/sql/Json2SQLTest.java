package org.iutools.corpus.sql;

import ca.nrc.testing.AssertFile;
import ca.nrc.testing.TestDirs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.nio.file.Path;

public class Json2SQLTest {

	protected TestInfo testInfo = null;
	Path smallCorpusJsonFilePath = null;

	@BeforeEach
	public void setUp(TestInfo _testInfo) throws Exception {
		this.testInfo = _testInfo;
		TestDirs testDirs = new TestDirs(testInfo);
		String corpusJsonFileName = "smallCorpus.json";
		testDirs.copyResourceFileToInputs("org/iutools/corpus/testdata/"+corpusJsonFileName);
		smallCorpusJsonFilePath = testDirs.inputsFile(corpusJsonFileName);;
	}

	////////////////////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////////////////////

	@Test
	public void test__Json2SQL__Synopsis(TestInfo testInfo) throws Exception {
		TestDirs testDirs = new TestDirs(testInfo);
		Path smallCorpusSqlFilePath = testDirs.outputsFile("smallCorpus.sql");
		String corpusName = "test-corpus";

		new Json2SQL().convert(smallCorpusJsonFilePath, smallCorpusSqlFilePath,
			corpusName);
	}

	////////////////////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////////////////////

	@Test
	public void test__convert__HappyPath(TestInfo testInfo) throws Exception {
		TestDirs testDirs = new TestDirs(testInfo);
		Path smallCorpusSqlFilePath = testDirs.outputsFile("smallCorpus.sql");
		String corpusName = "test-corpus";
		AssertFile.assertDoesNotExist(smallCorpusSqlFilePath,
			"Converted SQL file should NOT have existed before conversion");
		new Json2SQL().convert(smallCorpusJsonFilePath, smallCorpusSqlFilePath,
			corpusName);
		AssertFile.assertExists(smallCorpusSqlFilePath,
			"Converted SQL file SHOULD have existed after conversion");

		for (String expLine: new String[] {
			"  `corpus_name` text NOT NULL,",
			"INSERT INTO `CorpusData` (`word`, `corpus_name`, `frequency`, `lang`, `decompositions_sample`) VALUES",
			"('Haakimik', '"+corpusName+"', 1, 'en', '[]'),",
			"('Haakiqataurataalaurmata', '"+corpusName+"', 1, 'en', '[[\"Haakiq/1v\",\"qatau/1vv\",\"rataaq/1vv\",\"lauq/1vv\",\"mata/tv-caus-4p\"]]'),"
		}) {
			AssertFile.assertFileContains(
				"The converted SQL file was missing a string.\n"+
				"  File: "+smallCorpusSqlFilePath+"\n"+
				"  Exp string: "+expLine,
				smallCorpusSqlFilePath.toFile(), expLine, false, false
			);

		}
	}


}
