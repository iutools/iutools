package org.iutools.webservice;

import ca.nrc.json.MapperFactory;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertSequence;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import org.iutools.webservice.worddict.WordDictResult;
import org.junit.jupiter.api.Assertions;

public abstract class AssertEndpointResult extends Asserter<EndpointResult> {
	protected abstract EndpointResult result();

	public AssertEndpointResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertEndpointResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertEndpointResult jsonEquals(String expJson) throws Exception {
		String gotJson =
			MapperFactory
				.mapper(MapperFactory.MapperOptions.SORT_FIELD)
				.writeValueAsString(gotObject);
		AssertString.assertStringEquals(
			baseMessage+"\nJSON not as expected",
			expJson, gotJson
		);
		return this;
	}

	public AssertEndpointResult raisesNoError() {
		Assertions.assertEquals(
			null, result().errorMessage,
			baseMessage+"\nResponse raised error");
		return this;
	}

	public AssertEndpointResult foundWords(String... expWords)
		throws Exception {
		String message = baseMessage + "\nFound words not as expected";
		if (expWords.length == 1) {
			// If we provided a single expected word, then assert that the
			// list of matching words is EXACTLY that one word
			AssertObject.assertDeepEquals(message,
				expWords, wordDictResult().matchingWords);
		} else {
			// More than one expected words provided. Check that the list of
			// matching words STARTS with those expected words
			new AssertSequence<String>(
				wordDictResult().matchingWords.toArray(new String[0]), message)
				.startsWith(expWords);
		}

		return this;
	}

	public WordDictResult wordDictResult() {
		return (WordDictResult)gotObject;
	}
}