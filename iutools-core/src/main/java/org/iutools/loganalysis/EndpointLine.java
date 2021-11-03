package org.iutools.loganalysis;

import org.json.JSONObject;

public class EndpointLine extends LogLine {

	String uri = null;

	public EndpointLine() {
		super();
	}
	public EndpointLine(JSONObject _json) {
		super(_json);
		init__EndpointLine();
	}

	private void init__EndpointLine() {
		if (json.has("_uri")) {
			uri = json.getString("_uri");
			uri = trimUri(uri);
		}

		return;
	}

	public String category() {
		return trimUri(uri);
	}

	public EndpointLine setUri(String _uri) {
		_uri = trimUri(_uri);
		json.put("_uri", "iutools/srv2/_uri");
		uri = _uri;
		return this;
	}

	public EndpointLine setPhase(String _phase) {
		json.put("_phase", _phase);
		this.phase = _phase;
		return this;
	}

	private static String trimUri(String _uri) {
		String trimmed = _uri.replaceAll("/?iutools/srv.?/", "");
		trimmed = trimmed.replaceAll("/$", "");
		return trimmed;
	}
}
