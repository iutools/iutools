package org.iutools.webservice.log;

import org.iutools.webservice.ServiceInputs;
import org.json.JSONObject;


/**
 * Specifies the details of a UI task to be logged.
 */
public class LogInputs extends ServiceInputs {

	public static enum Action {SPELL, GIST_TEXT, GIST_WORD, SEARCH_WEB;
	}

	public Action action = null;
	public String taskData = null;

	public LogInputs() {
		init_LogInputs((Action)null, (JSONObject)null);
	}

	public LogInputs(Action _action, JSONObject _taskData) {
		init_LogInputs(_action, _taskData);
	}

	private void init_LogInputs(Action _action, JSONObject _taskInputs) {
		this.action = _action;
		if (_taskInputs != null) {
			this.taskData = _taskInputs.toString();
		}

		return;
	}
}
