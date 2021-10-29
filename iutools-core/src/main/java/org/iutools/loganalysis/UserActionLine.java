package org.iutools.loganalysis;

import org.json.JSONObject;

public class UserActionLine extends LogLine {

	public String action = null;

	public UserActionLine() {
		super();
	}
	public UserActionLine(JSONObject _json) {
		super(_json);
		init__UserActionLine();
	}

	private void init__UserActionLine() {
		if (taskData().has("_action")) {
			action = taskData().getString("_action");
		}
	}

	public LogLine setPhase(String _phase) {
		this.taskData().put("_phase", _phase);
		this.phase = _phase;
		return this;
	}

	public LogLine setAction(String _action) {
		this.taskData().put("_action", _action);
		this.action = _action;
		return this;
	}
}
