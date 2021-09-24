package org.iutools.webservice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.ArrayUtils;

import java.util.Map;

public class ServiceInputs {
	public static final String[] validUserActions = new String[] {
		"DICTIONARY_SEARCH", "SPELL", "GIST_TEXT", "WORD_LOOKUP", "MORPHEME_SEARCH",
		"SEARCH_WEB"
	};

	// These are NOT private eventhough we preface them with underscores.
	// We do this so that those attributes will appear first when we
	// pretty print the inputs for logging.
	//
	public String _action = null;
	public String _taskID = null;
	public Long _taskStartTime = null;

	@JsonIgnore
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Create a "summary" of these outputs, to make its log entry shorter.
	 * By default, the summary contains every field of the input.
	 * Subclasses can override the method to provide a more concise summary.
	 *
	 * If the method returns null, it means this particular endpoint input
	 * is not to be logged.
	 *
	 * @return
	 */
	public Map<String, Object> summarizeForLogging() throws ServiceException {
		return asMap();
	}

	public Map<String,Object> asMap() throws ServiceException {
		Map<String,Object> inputsMap = null;
		try {
			String json = mapper.writeValueAsString(this);
			inputsMap = mapper.readValue(json, Map.class);
		} catch (JsonProcessingException e) {
			throw new ServiceException(e);
		}
		return inputsMap;
	}

	public static <I extends ServiceInputs> I
		instantiateFromMap(Map<String,Object> data, Class<I> clazz)
		throws ServiceException {
		ObjectMapper mapper = new ObjectMapper();
		I inputs = null;
		try {
			String json = mapper.writeValueAsString(data);
			inputs = mapper.readValue(json, clazz);
		} catch (JsonProcessingException e) {
			throw new ServiceException(e);
		}
		return inputs;
	}

	public void validate() throws ServiceException {
		String errMess = null;
		errMess = validateAction();

		if (errMess != null) {
			throw new ServiceException(errMess);
		}
	}

	public String validateAction() {
		String errMess = null;
		if (_action != null && !ArrayUtils.contains(validUserActions, _action)) {
			errMess = "Invalid action "+_action;
		}
		return errMess;
	}
}
