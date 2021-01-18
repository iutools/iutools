package org.iutools.elasticsearch;

import ca.nrc.dtrc.elasticsearch.Document;
import ca.nrc.dtrc.elasticsearch.StreamlinedClient;
import ca.nrc.file.ResourceGetter;
import ca.nrc.testing.AssertIterator;
import ca.nrc.testing.TestDirs;
import org.iutools.corpus.WordInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ESIndexRepairTest {

	private static final String testIndexName = "test-index";
	private static final String winfoType = "winfo";
	StreamlinedClient esClient = null;
	Path jsonFile = null;

	class NotWordInfo extends Document {
		public String scroll_id = null;
		public NotWordInfo(String _id, String _scroll_id) {
			this.id = _id;
			this.scroll_id = _scroll_id;
		}
	}

	@BeforeEach
	public void setUp(TestInfo testInfo) throws Exception {
		esClient = new StreamlinedClient(testIndexName);
//		esClient.clearIndex();
		Path testDir = new TestDirs(testInfo).inputsDir();
		ResourceGetter.copyResourceFilesToDir("org/iutools/corpus/testdata", testDir);
		jsonFile = Paths.get(testDir.toString(), "smallCorpus.json");
		esClient.bulkIndex(jsonFile.toString(), winfoType);
	}

	//////////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////////

	@Test
	public void test__ESIndexRepair__Synopsis() throws Exception {
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
		ESIndexRepair repair = new ESIndexRepair(testIndexName);
		WordInfo goodDocPrototype = new WordInfo();
		Iterator<String> corruptedDocIDs =
			repair.corruptedDocIDs("winfo", goodDocPrototype);

		// If the index does contain some corrupted documents, you can reload
		// them from file...
		//
		if (corruptedDocIDs.hasNext()) {
			repair.repairCorruptedDocs(
				corruptedDocIDs, "winfo", goodDocPrototype, jsonFile);
		}
		return;
	}

	//////////////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////////////

	@Test
	public void test__ESIndexRepair__HappyPath() throws Exception {
		ESIndexRepair repair = new ESIndexRepair(testIndexName);
		WordInfo goodDocPrototype = new WordInfo();
		AssertESIndexRepair asserter = new AssertESIndexRepair(repair);
		asserter.assertCorruptedDocsAre(
		"Initially, the index should NOT have contained any corrupted documents",
			new String[0], winfoType, goodDocPrototype
		);

		// Suppose we somehow corrupt the ES record for words "Haajsan" and
		// "Haaki" by replacing it with documents whose fields do not correspond
		// to those of the WordInfo class.
		//
		esClient.putDocument(
			winfoType,
			new NotWordInfo("Haajsan", "asgmbvfgtertjert"));
		esClient.putDocument(
			winfoType,
			new NotWordInfo("Haaki", "klttutyhdfryretfdw"));
		Thread.sleep(1000);
		asserter.assertCorruptedDocsAre(
		"At this point, the index should have contained some corrupted records",
		new String[] {"Haajsan", "Haaki"}, winfoType, goodDocPrototype
		);
//		corruptedDocIDs =
//			repair.corruptedDocIDs("winfo", goodDocPrototype);
//		AssertIterator.assertElementsEquals(
//		"At this point, the index should have contained some corrupted records",
//			new String[] {"Haajsan", "Haaki"}, corruptedDocIDs);

		// Let's repair those records by reloading them from the JSON file
		Iterator<String> corruptedDocIDs =
			repair.corruptedDocIDs("winfo", goodDocPrototype);
		repair.repairCorruptedDocs(
			corruptedDocIDs, winfoType, goodDocPrototype, jsonFile);
		asserter.assertCorruptedDocsAre(
			"After repair, there should be no more corrupted documents",
			new String[0], winfoType, goodDocPrototype
		);

		return;
	}

}
