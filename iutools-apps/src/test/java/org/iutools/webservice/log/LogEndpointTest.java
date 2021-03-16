package org.iutools.webservice.log;

import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.iutools.webservice.log.LogInputs.Action;

public class LogEndpointTest extends EndpointTest  {

	@Override
	public Endpoint makeEndpoint() {
		return new LogEndpoint();
	}

	@Override
	public void test__logEntry() {
		Assertions.fail("Nevermind");
	}

	@Test
	public void test__MorphemeExamplesEndpoint__SetsTheTaskID() throws Exception {
		Action action = Action.SEARCH_WEB;
		JSONObject actionData = new JSONObject()
			.put("word", "inuksuk");
		LogInputs logInputs =
			new LogInputs(action, actionData);

		LogResult epResult = (LogResult)endPoint.execute(logInputs);
		new AssertLogResult(epResult)
			.taskIDIsSet()
			;
	}

	@Test
	public void test__MorphemeExamplesEndpoint__DifferentCallsSetDifferentIDs() throws Exception {
		Action action = Action.SEARCH_WEB;
		JSONObject actionData = new JSONObject()
			.put("word", "inuksuk");
		LogInputs logInputs =
		new LogInputs(action, actionData);

		LogResult epResult = (LogResult)endPoint.execute(logInputs);
		new AssertLogResult(epResult)
			.taskIDIsSet()
		;

		Thread.sleep(1000);
		String firstTaskID = epResult.taskID;
		epResult = (LogResult)endPoint.execute(logInputs);
		new AssertLogResult(epResult)
			.taskIDIsSet()
			.taskIDIsNot(firstTaskID,
			"Task ID of second action should have differed from first")
		;
	}

	@Test
	public void test__MorphemeExamplesEndpoint__IDAlreadySet__ReturnsSameID() throws Exception {
		Action action = Action.SEARCH_WEB;
		JSONObject actionData = new JSONObject()
			.put("word", "inuksuk");
		LogInputs logInputs =
			new LogInputs(action, actionData);
		logInputs.taskID = "someid";

		LogResult epResult = (LogResult)endPoint.execute(logInputs);
		new AssertLogResult(epResult)
			.taskIDequals(logInputs.taskID,
				"Log results should have used the task ID that was received by the log service")
		;
	}
}
