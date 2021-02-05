package org.iutools.elasticsearch;

import ca.nrc.dtrc.elasticsearch.Document;
import ca.nrc.testing.AssertIterator;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;
import org.iutools.corpus.WordInfo;

import java.util.Iterator;
import java.util.Set;

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

	public AssertESIndexRepair assertBadFieldNamesAre(
		String mess, String[] expFields,
		String winfoType, WordInfo goodDocPrototype) throws Exception {
		Set<String> gotFields = repair().badFieldNames(winfoType, goodDocPrototype);
		AssertObject.assertDeepEquals(
			baseMessage+"\n"+mess+
			"\nList of bad fields was not as expected.",
			expFields, gotFields)	;
		return this;
	}


	ESIndexRepair repair() {
		return gotObject;
	}
}
