package org.iutools.webservice.log;

import org.iutools.webservice.ServiceInputs;
import org.json.JSONObject;


/**
 * Specifies the details of a UI task to be logged.
 */
public class LogUITaskInputs extends ServiceInputs {

	public static enum Action {SPELL, GIST_TEXT, SEARCH_WEB}

	public Action action = null;
	public String taskDescr = null;

	public LogUITaskInputs() {
		init_LogUITaskInputs((Action)null, (JSONObject)null);
	}

	public LogUITaskInputs(Action _action, JSONObject _taskInputs) {
		init_LogUITaskInputs(_action, _taskInputs);
	}

	private void init_LogUITaskInputs(Action _action, JSONObject _taskInputs) {
		this.action = _action;
		if (_taskInputs != null) {
			this.taskDescr = _taskInputs.toString();
		}

		return;
	}
}
