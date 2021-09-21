package org.iutools.webservice.logaction;

import org.apache.log4j.Logger;
import org.iutools.webservice.*;

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
		LogActionResult result = new LogActionResult(logInputs.startedAt);

		return result;
	}
}
