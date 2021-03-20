package org.iutools.webservice;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Map;

public class AssertServiceInputs extends Asserter<ServiceInputs> {

	private ObjectMapper mapper = new ObjectMapper();
	{
		mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
		mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
	}

	public AssertServiceInputs(ServiceInputs _gotObject) {
		super(_gotObject);
	}

	public AssertServiceInputs(ServiceInputs _gotObject, String mess) {
		super(_gotObject, mess);
	}

	protected ServiceInputs inputs() {
		return (ServiceInputs)gotObject;
	}

	public AssertServiceInputs logSummaryIs(String expSummary) throws Exception {
		Map<String, Object> gotSummaryMap = inputs().summarizeForLogging();
		String gotSummary = mapper.writeValueAsString(gotSummaryMap);
		AssertString.assertStringEquals(
			baseMessage+"\nLog summary was not as expected.",
			expSummary, gotSummary);

		return this;
	}
}