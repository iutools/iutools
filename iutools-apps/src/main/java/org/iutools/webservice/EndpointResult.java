package org.iutools.webservice;

import org.iutools.utilities.StopWatch;
import org.json.JSONObject;

public abstract class EndpointResult {

	public String taskID = null;
	public Long taskStartTime = null;
	public Long taskElapsedMsecs = null;
	public String status = null;
	public String errorMessage = null;
	public String stackTrace = null;
	public ServiceInputs failingInputs = null;

	public EndpointResult() {}

	public JSONObject resultLogEntry(long startMSecs) {
		JSONObject entry = resultLogEntry();
		if (entry != null) {
			long elapsedMSecs = StopWatch.elapsedMsecsSince(startMSecs);
			entry.put("elapsedMSecs", elapsedMSecs);
		}
		return entry;
	}

	public abstract JSONObject resultLogEntry();

	public EndpointResult setError(String _error) {
		this.errorMessage = _error;
		return this;
	}
}