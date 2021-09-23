package org.iutools.webservice.logaction;

import org.apache.log4j.Logger;
import org.iutools.webservice.*;

import javax.servlet.http.HttpServletRequest;

public class LogActionEndpoint extends Endpoint {

	@Override
	protected ServiceInputs requestInputs(String jsonRequestBody)
		throws ServiceException {
			return jsonInputs(jsonRequestBody, LogActionInputs.class);
	}

	@Override
	public EndpointResult execute(ServiceInputs inputs) throws ServiceException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.LogActionEndpoint.execute");

		LogActionInputs logInputs = (LogActionInputs)inputs;
		LogActionResult result =
			new LogActionResult();
		return result;
	}
}
