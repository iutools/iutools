package org.iutools.webservice.logaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.gist.GistPrepareContentInputs;
import org.iutools.webservice.tokenize.TokenizeInputs;
import org.json.JSONObject;

import java.util.Map;

/**
 * Specifies the details of a UI task to be logged.
 */
public class LogActionInputs extends ServiceInputs {

	public static enum Action {
		DICTIONARY_SEARCH, SPELL, GIST_TEXT, WORD_LOOKUP, MORPHEME_SEARCH,
		SEARCH_WEB;
	}

	public Action action = null;
	public Map<String,Object> taskData = null;

	public LogActionInputs() throws ServiceException {
		init_LogInputs((Action)null, (JSONObject)null);
	}

	public LogActionInputs(Action _action, JSONObject _taskData) throws ServiceException {
		init_LogInputs(_action, _taskData);
	}

	public LogActionInputs(Action _action, Map<String,Object> _taskData) throws ServiceException {
		init_LogInputs(_action, _taskData);
	}

	private void init_LogInputs(Action _action, JSONObject _taskInputs)
		throws ServiceException {

		if (_taskInputs != null) {
			String json = _taskInputs.toString();
			try {
				Map<String,Object> taskDataMap =
					new ObjectMapper().readValue(json, Map.class);
				init_LogInputs(_action, taskDataMap);
			} catch (JsonProcessingException e) {
				throw new ServiceException(e);
			}
		}

		return;
	}

	private void init_LogInputs(Action _action, Map<String,Object> _taskData)
		throws ServiceException {

		this.action = _action;
		this.taskData = _taskData;

		return;
	}

	@Override
	public Map<String, Object> summarizeForLogging() throws ServiceException {
		Map<String,Object> data = taskData;

		ServiceInputs inputsToSummarize = null;
		if (action == Action.GIST_TEXT) {
			inputsToSummarize =
				GistPrepareContentInputs.instantiateFromMap(
					taskData,
					GistPrepareContentInputs.class);
		} else if (action == Action.SPELL) {
			inputsToSummarize =
				TokenizeInputs.instantiateFromMap(
					taskData,
				TokenizeInputs.class);
		}
		if (inputsToSummarize != null) {
			data = inputsToSummarize.summarizeForLogging();
		}
		// Note: We prefix action with an underscore so it will come first
		// in the list of fields when we JSONifiy the map
		data.put("_action", action.name());
		return data;
	}
}