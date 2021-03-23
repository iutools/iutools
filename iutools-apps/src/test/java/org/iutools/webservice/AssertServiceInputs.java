package org.iutools.webservice;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Assertions;

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

		String mess =
			baseMessage+"\nLog summary was not as expected.\n"+
			"Expected : "+expSummary+"\n"+
			"Actual   : "+gotSummary;
		if (gotSummaryMap == null || expSummary == null) {
			int nullsCount = 0;
			if (gotSummaryMap == null) {nullsCount++;}
			if (expSummary == null) {nullsCount++;}
			if (nullsCount != 2) {
				Assertions.fail(mess);
			}
		} else {
			AssertString.assertStringEquals(
			baseMessage + "\nLog summary was not as expected.",
			expSummary, gotSummary);
		}

		return this;
	}
}