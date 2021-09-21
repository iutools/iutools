package org.iutools.webservice.logaction;

import org.iutools.utilities.StopWatch;
import org.iutools.webservice.EndpointResult;
import org.json.JSONObject;

public class LogActionResult extends EndpointResult {
	public Long elapsedMsecs = null;

	public LogActionResult() {super();}

	public LogActionResult(Long startedOn) {
		super();
//		if (startedOn != null) {
//			this.elapsedMsecs = StopWatch.elapsedMsecsSince(startedOn);
//		}
	}



	@Override
	public JSONObject resultLogEntry() {
		return null;
	}
}
