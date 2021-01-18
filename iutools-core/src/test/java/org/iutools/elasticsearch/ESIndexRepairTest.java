package org.iutools.elasticsearch;

import ca.nrc.dtrc.elasticsearch.Document;
import ca.nrc.dtrc.elasticsearch.ESTestHelpers;
import ca.nrc.dtrc.elasticsearch.StreamlinedClient;
import ca.nrc.file.ResourceGetter;
import ca.nrc.testing.TestDirs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ESIndexRepairTest {

	private static final String testIndexName = "test-index";
	private static final String winfoType = "winfo";
	StreamlinedClient esClient = null;
	Path jsonFile = null;

	@BeforeEach
	public void setUp(TestInfo testInfo) throws Exception {
		esClient = new StreamlinedClient(testIndexName);;
		Path testDir = new TestDirs(testInfo).inputsDir();
		ResourceGetter.copyResourceFilesToDir("org/iutools/corpus", testDir);
		jsonFile = Paths.get(testDir.toString(), "smallCorpus.json");
	}

	//////////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////////

	@Test
	public void test__ESIndexRepair__Synopsis() {
		//
		// Use ESIndexRepair to identify and possibly repair faulty documents
		// in an ES index
		//
		// For example, say you have an index that is built form a json file
		// and contains a type 'winfo' that is supposed to contain objects of
		// type WordInfo.
		//
		// You can check if the index contains some corrupted documents as follows
		//


	}

}
