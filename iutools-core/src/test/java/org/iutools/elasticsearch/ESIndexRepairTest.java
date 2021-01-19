package org.iutools.elasticsearch;

import ca.nrc.dtrc.elasticsearch.Document;
import ca.nrc.dtrc.elasticsearch.StreamlinedClient;
import ca.nrc.file.ResourceGetter;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.TestDirs;
import org.iutools.corpus.WordInfo;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ESIndexRepairTest {

	private static final String testIndexName = "test-index";
	private static final String winfoType = "winfo";
	StreamlinedClient esClient = null;
	Path jsonFile = null;
	ESIndexRepair repair = new ESIndexRepair(testIndexName);
	WordInfo goodDocPrototype = new WordInfo();


	class NotWordInfo extends Document {
		class NotWordNestedField {
			public int level2field = 1;
		}

		public String scroll_id = null;
		public NotWordNestedField nestedField = new NotWordNestedField();

		public NotWordInfo(String _id) {
			this.id = _id;
			this.scroll_id = "somevalue";
		}
	}

	@BeforeEach
	public void setUp(TestInfo testInfo) throws Exception {
		esClient = new StreamlinedClient(testIndexName);
		esClient.deleteIndex();
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
		corruptSomeDocuments("Haajsan", "Haaki");
		asserter.assertCorruptedDocsAre(
		"At this point, the index should have contained some corrupted records",
		new String[] {"Haajsan", "Haaki"}, winfoType, goodDocPrototype
		);

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


	@Test
	public void test__corruptedDocIDs__HappyPath() throws Exception {
		AssertESIndexRepair asserter = new AssertESIndexRepair(repair);
		asserter.assertCorruptedDocsAre(
		"Initially, the index should NOT have contained any corrupted documents",
		new String[0], winfoType, goodDocPrototype
		);

		String[] docsToCorrupt = {"Haajsan", "Haaki"};
		corruptSomeDocuments(docsToCorrupt);
		asserter.assertCorruptedDocsAre(
			"List of corrupted IDs was not as expected",
			docsToCorrupt, winfoType, goodDocPrototype);
	}

	@Test
	public void test__badFieldNames__HappyPath() throws Exception {
		AssertESIndexRepair asserter = new AssertESIndexRepair(repair);
		asserter.assertBadFieldNamesAre(
			"Initially, the index should NOT have contained any bad field names",
			new String[0], winfoType, goodDocPrototype
		);

		corruptSomeDocuments(new String[] {"Haajsan"});
		asserter.assertBadFieldNamesAre(
			"List of bad field names not as expected after introducing corrupted documents into the index",
			new String[] {"nestedField","scroll_id"}, winfoType, goodDocPrototype
		);
	}

	@Test
	public void test__queryCorruptedDocs__HappyPath() throws Exception {
		Set<String> badFields = new HashSet<String>();
		Collections.addAll(badFields, new String[] {"badFld1", "badFld2"});
		JSONObject gotQuery = repair.queryCorruptedDocs(badFields);
		String expQueryStr =
			"{"+
			"  \"query\": {"+
			"    \"bool\": {"+
			"      \"should\":  ["+
			"        {\"exist\": \"badFld1\"},"+
			"        {\"exist\": \"badFld2\"}"+
			"      ]"+
			"    }"+
			"  }"+
			"}"
			;
		expQueryStr = expQueryStr.replaceAll("\\s+", "");
		String gotQueryStr = gotQuery.toString();
		AssertString.assertStringEquals(
			"Query was not as expected",
			expQueryStr, gotQueryStr);
	}

	///////////////////////////////
	// TEST HELPERS
	///////////////////////////////

	private void corruptSomeDocuments(String... docIDs) throws Exception {
		for (String id: docIDs) {
			esClient.putDocument(winfoType, new NotWordInfo(id));
		}
		Thread.sleep(1000);
	}

}
