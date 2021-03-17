package org.iutools.webservice.log;

import org.apache.log4j.Logger;
import org.iutools.webservice.*;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;

public class LogEndpoint extends Endpoint {

	@Override
	protected ServiceInputs requestInputs(HttpServletRequest request)
		throws ServiceException {
			return jsonInputs(request, LogInputs.class);
	}

	@Override
	public EndpointResult execute(ServiceInputs inputs) throws ServiceException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.LogEndpoint.execute");

		LogInputs logInputs = (LogInputs)inputs;
		LogResult result = new LogResult();
		result.taskID = logInputs.taskID;
		if (result.taskID == null) {
			result.taskID = generateTaskID();
		}
		return result;
	}

	@Override
	public JSONObject logEntry(ServiceInputs inputs) {
		LogInputs logInputs = (LogInputs)inputs;
		JSONObject entry = new JSONObject()
			.put("action", logInputs.action)
			.put("taskID", logInputs.taskID)
			.put("taskData", logInputs.taskData)
		;
		return entry;
	}

	private String generateTaskID() {
		String id = Instant.now().toString();
		return id;
	}
}
