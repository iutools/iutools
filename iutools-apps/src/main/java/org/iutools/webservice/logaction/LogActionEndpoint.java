package org.iutools.webservice.logaction;

import ca.nrc.json.PrettyPrinter;
import org.apache.log4j.Logger;
import org.iutools.webservice.*;

public class LogActionEndpoint extends Endpoint {

	@Override
	protected ServiceInputs requestInputs(String jsonRequestBody)
		throws ServiceException {
			return jsonInputs(jsonRequestBody, LogActionInputs.class);
	}

	@Override
	public EndpointResult execute(ServiceInputs inputs) throws ServiceException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.LogActionEndpoint.execute");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("inputs="+ PrettyPrinter.print(inputs));
		}
		LogActionInputs logInputs = (LogActionInputs)inputs;
		LogActionResult result =
			new LogActionResult();
		return result;
	}
}
