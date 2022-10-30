package org.iutools.sql;

import ca.nrc.testing.AssertObject;
import org.iutools.corpus.CompiledCorpusException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public abstract class Row2PojoTest<T> {

	/** Create an instance of the subclass of Row2Pojo being tested
	 * @return*/
	protected abstract Row2Pojo<T> makeRow2Pojo();

	/** Create POJO to be fed to the Row2Pojo instance being tested */
	protected abstract T makeTestPojo() throws CompiledCorpusException;

	@Test
	public void test__toRow_toPOJO__RoundTrip() throws Exception {
		T origPojo = makeTestPojo();
		Row2Pojo<T> converter = makeRow2Pojo();
		JSONObject row = converter.toRowJson(origPojo);
		T rountTripPojo = converter.toPOJO(row);
		AssertObject.assertDeepEquals(
			"Round trip toRowJson-toPOJO should have restored the object to its original form",
			origPojo, rountTripPojo,
			// We ignore the ElasticSearch specific fields
			"id", "type", "idWithoutType");
	}
}
