package org.iutools.webservice.logaction;

import org.apache.log4j.Logger;
import org.iutools.webservice.*;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

public class LogActionEndpoint extends Endpoint {

	@Override
	protected ServiceInputs requestInputs(HttpServletRequest request)
		throws ServiceException {
			return jsonInputs(request, LogActionInputs.class);
	}

	@Override
	public EndpointResult execute(ServiceInputs inputs) throws ServiceException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.LogActionEndpoint.execute");

		LogActionInputs logInputs = (LogActionInputs)inputs;
		LogActionResult result = new LogActionResult();

		return result;
	}

	@Override
	public JSONObject logEntry(ServiceInputs inputs) {
		LogActionInputs logInputs = (LogActionInputs)inputs;
		JSONObject entry = new JSONObject()
			// Note: We prefix the action field with a _ so it will come out first
			// in the serialization of the log entry
			.put("_action", logInputs.action)
			.put("taskData", logInputs.taskData)
		;
		return entry;
	}
}
