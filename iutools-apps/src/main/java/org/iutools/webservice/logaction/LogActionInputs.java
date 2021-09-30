package org.iutools.webservice.logaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.gist.GistPrepareContentInputs;
import org.iutools.webservice.tokenize.TokenizeInputs;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Specifies the details of a UI task to be logged.
 */
public class LogActionInputs extends ServiceInputs {

	public static enum Action {
		DICTIONARY_SEARCH, SPELL, GIST_TEXT, WORD_LOOKUP, MORPHEME_SEARCH,
		SEARCH_WEB;
	}

	public String phase = "START";
	public Map<String,Object> taskData = new HashMap<String,Object>();

	public LogActionInputs() throws ServiceException {
		init_LogInputs((String)null, (Long)null, (JSONObject)null);
	}

	public LogActionInputs(String __action, JSONObject __taskData) throws ServiceException {
		init_LogInputs(__action, (Long)null, __taskData);
	}

	public LogActionInputs(String __action, Map<String,Object> __taskData) throws ServiceException {
		init_LogInputs(__action, __taskData);
	}

	private void init_LogInputs(String __action, Long __startedAt, JSONObject __taskInputs)
		throws ServiceException {

		if (__taskInputs != null) {
			String json = __taskInputs.toString();
			try {
				Map<String,Object> taskDataMap =
					new ObjectMapper().readValue(json, Map.class);
				init_LogInputs(__action, taskDataMap);
			} catch (JsonProcessingException e) {
				throw new ServiceException(e);
			}
		}

		validate();

		return;
	}

	private void init_LogInputs(String __action, Map<String,Object> __taskData)
		throws ServiceException {

		this._action = __action;
		this.taskData = __taskData;

		return;
	}

	@Override
	public void validate() throws ServiceException {
		super.validate();
		if (!phase.matches("^(START|END)$")) {
			throw new ServiceException("Invalid phase: '"+phase+"'");
		}
	}

	@Override
	public Map<String, Object> summarizeForLogging() throws ServiceException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.logaction.LogActionInputs.summarizeForLogging");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("_action="+_action+", taskData="+taskData);
		}
		Map<String,Object> data = taskData;
		ServiceInputs inputsToSummarize = null;
		if (_action.equals("GIST_TEXT")) {
			tLogger.trace("GIST_TEXT");
			inputsToSummarize =
				GistPrepareContentInputs.instantiateFromMap(
					taskData,
					GistPrepareContentInputs.class);
		} else if (this._action.equals("SPELL")) {
			tLogger.trace("SPELL");
			inputsToSummarize =
				TokenizeInputs.instantiateFromMap(
					taskData,
					TokenizeInputs.class);
		}
		tLogger.trace("inputsToSummarize="+inputsToSummarize);
		if (inputsToSummarize != null) {
			tLogger.trace("inputsToSummarize.getClass()="+inputsToSummarize.getClass());
			data = inputsToSummarize.summarizeForLogging();
			// Note: We prefix action and phase with an underscore so they will come
			// first in the list of fields when we JSONifiy the map
		}
		data.put("_action", _action);
		data.put("_phase", this.phase);
		data.put("_taskStartTime", this._taskStartTime);
		data.put("_taskID", this._taskID);

		return data;
	}
}