package org.iutools.webservice;

import ca.nrc.json.MapperFactory;
import ca.nrc.testing.*;
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

	public AssertEndpointResult raisesError(String expErr) {
		AssertString.assertStringEquals(
			baseMessage+"\nResponse did not raise the expected error",
			expErr, result().errorMessage);
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

	public AssertEndpointResult foundAtLeastNWords(Integer expMinHits) {
		if (expMinHits != null) {
			Integer gotTotalHits = wordDictResult().matchingWords.size();
			if (expMinHits == 0) {
				AssertNumber.assertEquals(
				baseMessage + "\nSearch should NOT have produced any hits",
				0, gotTotalHits, 0.0);
			} else {
				AssertNumber.isGreaterOrEqualTo(
				baseMessage + "Total number of words found was too small",
				expMinHits, gotTotalHits
				);
			}
		}

		return this;
	}

	public AssertEndpointResult foundAtMostNWords(Integer expMaxHits) {
		if (expMaxHits != null) {
			Integer gotTotalHits = wordDictResult().matchingWords.size();
			AssertNumber.isLessOrEqualTo(
				baseMessage + "Total number of words found was too large",
				gotTotalHits, expMaxHits);
		}

		return this;
	}

	public WordDictResult wordDictResult() {
		return (WordDictResult)gotObject;
	}

}