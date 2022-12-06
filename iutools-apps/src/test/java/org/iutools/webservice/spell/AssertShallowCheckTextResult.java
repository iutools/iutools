package org.iutools.webservice.spell;

import ca.nrc.testing.AssertSet;
import ca.nrc.testing.AssertString;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AssertShallowCheckTextResult extends AssertEndpointResult {

	public AssertShallowCheckTextResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertShallowCheckTextResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}

	@Override
	protected ShallowCheckTextResult result() {
		return (ShallowCheckTextResult)gotObject;
	}


	public AssertShallowCheckTextResult correctedTextEquals(String expText) {
		AssertString.assertStringEquals(
			baseMessage+"\nCorrected text not as expected",
			expText, result().correctedText);
		return this;
	}

	public AssertShallowCheckTextResult badWordWere(
		Pair<String,String>... expBadWords) throws Exception {

		Set<Pair<String,String>> gotBadWords = new HashSet<Pair<String,String>>();
		for (Map.Entry entry: result().misspelledWords.entrySet()) {
			Pair<String,String> correction = Pair.of((String)entry.getKey(), (String)entry.getValue());
			gotBadWords.add(correction);
		}
		AssertSet.assertEquals(
			baseMessage+"\nBad words not as expected",
			expBadWords, gotBadWords);
		return this;
	}
}
