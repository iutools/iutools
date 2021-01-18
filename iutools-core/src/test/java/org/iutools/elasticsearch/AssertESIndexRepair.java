package org.iutools.elasticsearch;

import ca.nrc.dtrc.elasticsearch.Document;
import ca.nrc.testing.AssertIterator;
import ca.nrc.testing.Asserter;

import java.util.Iterator;

public class AssertESIndexRepair extends Asserter<ESIndexRepair> {
	public AssertESIndexRepair(ESIndexRepair _gotObject) {
		super(_gotObject);
	}

	public AssertESIndexRepair(ESIndexRepair _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertESIndexRepair assertCorruptedDocsAre(
		String mess, String[] expIDs, String inESType, Document goodDocPrototype)
		throws Exception {
		Iterator<String> gotIDs =
			repair().corruptedDocIDs(inESType, goodDocPrototype);
		AssertIterator.assertElementsEquals(
		baseMessage+"\n"+mess+
			"The corrupted doc IDs were not as expected for type: "+inESType+
			" ("+goodDocPrototype.getClass().getName()+")",
			expIDs, gotIDs);
		return this;
	}

	ESIndexRepair repair() {
		return gotObject;
	}
}
