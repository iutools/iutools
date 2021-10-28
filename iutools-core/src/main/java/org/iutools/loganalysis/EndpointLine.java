package org.iutools.loganalysis;

import org.json.JSONObject;

public class EndpointLine extends LogLine {

	public EndpointLine() {
		super();
	}
	public EndpointLine(JSONObject _json) {
		super(_json);
		init__EndpointLine();
	}

	private void init__EndpointLine() {
	}

	public LogLine setPhase(String _phase) {
		json.put("_phase", _phase);
		this.phase = _phase;
		return this;
	}
}
