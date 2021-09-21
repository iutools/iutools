package org.iutools.webservice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class ServiceInputs {
	public String taskID = null;
	public Long taskStartTime = null;

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
		Map<String,Object> summary = null;
		try {
			String json = mapper.writeValueAsString(this);
			summary = mapper.readValue(json, Map.class);
		} catch (JsonProcessingException e) {
			throw new ServiceException(e);
		}
		return summary;
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
}
