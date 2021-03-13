package org.iutools.webservice.log;

import ca.nrc.ui.web.testing.MockHttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.iutools.webservice.IUTServiceTestHelpers;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.iutools.webservice.log.LogUITaskInputs.Action;

public class LogUITaskEndpointTest {
	@Test
	public void test__LogUITaskEndpoint__HappyPath() throws Exception {
		JSONObject taskInputs = new JSONObject().put("text", "blah blah blah");
		LogUITaskInputs logInputs =
			new LogUITaskInputs(Action.SPELL, taskInputs);

		MockHttpServletResponse response =
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.LOG,
					logInputs
				);
//		new AssertLogUITaskResponse(logInputs, response);
	}
}
