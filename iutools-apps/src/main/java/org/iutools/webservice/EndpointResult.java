package org.iutools.webservice;

import org.json.JSONObject;

public abstract class EndpointResult {

	public String taskID = null;
	public String status = null;
	public String errorMessage = null;
	public String stackTrace = null;
	public ServiceInputs failingInputs = null;

	public EndpointResult() {}

	public abstract JSONObject resultLogEntry();

	public EndpointResult setError(String _error) {
		this.errorMessage = _error;
		return this;
	}
}