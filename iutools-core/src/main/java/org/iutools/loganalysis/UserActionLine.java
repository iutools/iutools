package org.iutools.loganalysis;

import org.json.JSONObject;

public class UserActionLine extends LogLine {

	public UserActionLine() {
		super();
	}
	public UserActionLine(JSONObject _json) {
		super(_json);
		init__UserActionLine();
	}

	private void init__UserActionLine() {
	}

	public LogLine setPhase(String _phase) {
		this.taskData().put("_phase", _phase);
		this.phase = _phase;
		return this;
	}

}
