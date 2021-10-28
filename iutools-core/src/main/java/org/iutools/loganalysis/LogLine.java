package org.iutools.loganalysis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class LogLine {

	public abstract LogLine setPhase(String _phase);

	static Pattern pattLine = Pattern.compile(
		"^-- webservice\\.(endpoint|user_action).*?\\{(.*?)\\}\\s*$");

	JSONObject json = new JSONObject();
	String phase = null;
	Integer elapsedMSecs = null;

	public LogLine() {
		init__LogLine((JSONObject)null);
	}

	public LogLine(JSONObject _json) {
		init__LogLine(_json);
	}

	private void init__LogLine(JSONObject _json) {
		if (_json != null) {
			json = _json;
		}

		if (taskData().has("_phase")) {
			phase = taskData().getString("_phase");
		} else if (json.has("_phase")) {
			phase = json.getString("_phase");
		}

		if (taskData().has("taskElapsedMsecs")) {
			elapsedMSecs = taskData().getInt("taskElapsedMsecs");
		} else if (json.has("_taskElapsedMsecs")) {
			elapsedMSecs = json.getInt("_taskElapsedMsecs");
		}
	}

	public static LogLine parseLine(String line) {
		LogLine lineObj = null;
		Matcher matcher = pattLine.matcher(line);
		if (matcher.matches()) {
			String what = matcher.group(1);
			String jsonString = "{"+matcher.group(2)+"}";
			JSONObject jsonObj = new JSONObject(jsonString);
			if (what.equals("endpoint")) {
				lineObj = new EndpointLine(jsonObj);
			} else {
				lineObj = new UserActionLine(jsonObj);
			}
		}
		return lineObj;
	}

	@JsonIgnore
	public LogLine setElapsedMSecs(Integer _elapsed) {
		taskData().put("taskElapsedMsecs", _elapsed);
		elapsedMSecs = _elapsed;
		return this;
	}

	public JSONObject taskData() {
		if (!json.has("taskData")) {
			json.put("taskData", new JSONObject());
		}
		return json.getJSONObject("taskData");
	}
}
