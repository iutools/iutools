package org.iutools.webservice;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AssertHttpServletResponse extends Asserter<MockHttpServletResponse> {
	Map<String,Object> outputAsMap = new HashMap<String,Object>();

	public AssertHttpServletResponse(MockHttpServletResponse _gotObject) {
		super(_gotObject);
		init__AssertHttpServletResponse(_gotObject);
	}

	public AssertHttpServletResponse(MockHttpServletResponse _gotObject, String mess) {
		super(_gotObject, mess);
		init__AssertHttpServletResponse(_gotObject);
	}

	private void init__AssertHttpServletResponse(MockHttpServletResponse gotObject) {
		try {
			String jsonStr = response().getOutput();
			outputAsMap = new ObjectMapper()
				.readValue(jsonStr, outputAsMap.getClass());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	public MockHttpServletResponse response() {
		return (MockHttpServletResponse)gotObject;
	}

	public void assertFieldEquals(String fldName, Object expValue) throws Exception {
		Object gotValue = outputAsMap.get(fldName);
		AssertObject.assertDeepEquals(
			baseMessage+"\nValue of field '"+fldName+"' was not as expected",
			expValue, gotValue
		);
	}
}
