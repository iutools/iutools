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

	public JSONObject resultLogEntry(long startMSecs) throws ServiceException {
		JSONObject entry = resultLogEntry();
		if (entry != null) {
			long elapsedMSecs = StopWatch.elapsedMsecsSince(startMSecs);
			entry.put("elapsedMSecs", elapsedMSecs);
		}
		return entry;
	}

	public JSONObject resultLogEntry() throws ServiceException {
		// By default, we don't log any specifics of the result.
		// The Endpoint class will however augment that log entry with
		// "universal" attributes that apply to all results (ex: elapsed time)
		//
		// Note: if you don't a log entry to be printed (not even one with those
		// "universal" attributes), then override the method so it returns null
		//
		return new JSONObject();
	}

	public EndpointResult setError(String _error) {
		this.errorMessage = _error;
		return this;
	}
}