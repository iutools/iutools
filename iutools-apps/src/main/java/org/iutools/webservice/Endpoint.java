package org.iutools.webservice;

import ca.nrc.json.PrettyPrinter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Map;

public abstract class Endpoint
	<I extends ServiceInputs, R extends EndpointResult>  {

	protected abstract I requestInputs(HttpServletRequest request)
		throws ServiceException;

	ObjectMapper mapper = new ObjectMapper();
	{
		mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
		mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
	}

	public abstract EndpointResult execute(I inputs)
		throws ServiceException;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServiceException {
		I inputs = null;
		try {

			EndPointHelper.log4jReload();

			inputs = requestInputs(request);
			ensureTaskIDIsDefined(inputs);

			logRequest(request, inputs);
			EndpointResult epResponse = execute(inputs);
			epResponse.taskID = inputs.taskID;
			logResult(request, epResponse, inputs);
			writeJsonResponse(epResponse, response);
		} catch (IOException e) {
			logError(e, inputs);
			throw new ServiceException(e);
		}
	}

	private void logResult(HttpServletRequest request, EndpointResult epResponse, I inputs) throws ServiceException {
		JSONObject json = epResponse.resultLogEntry();
		if (json != null) {
			json.put("_uri", request.getRequestURI());
			resultLogger().info(json.toString());
		}
	}

	private Category resultLogger() {
		Logger logger = Logger.getLogger("org.iutools.webservice.endpoint_results");
		logger.setLevel(Level.INFO);
		return logger;
	}

	private void logError(Exception e, I inputs) throws ServiceException {
		String epClass = this.getClass().getName();
		try {
			String inputsJson = mapper.writeValueAsString(inputs);
		} catch (JsonProcessingException e2) {
			throw new ServiceException(e2);
		}
		errorLogger().error("", e);
	}

	private void ensureTaskIDIsDefined(I inputs) {
		if (inputs.taskID == null) {
			inputs.taskID = generateTaskID();
		}
	}

	private String generateTaskID() {
		String id = Instant.now().toString();
		return id;
	}


	private void logRequest(HttpServletRequest request, I inputs) throws ServiceException {

		Map<String,Object> inputSummary = inputs.summarizeForLogging();
		if (inputSummary != null) {

			JSONObject logEntry = new JSONObject()
				.put("_uri", request.getRequestURI())
				.put("_taskID", inputs.taskID)
				.put("taskData", inputSummary);
			String json = jsonifyLogEntry(logEntry);
			actionLogger().info(json);
		}
		return;
	}

	private String jsonifyLogEntry(JSONObject logEntry) throws ServiceException {
		// Jsonify the JSONObject;
		String json = logEntry.toString();

		// Re-jsonify the result, sorting the field names alphabetically
		try {
			Map<String,Object> entryMap = mapper.readValue(json, Map.class);
			json = mapper.writeValueAsString(entryMap);
		} catch (JsonProcessingException e) {
			throw new ServiceException(e);
		}

		return json;
	}

	private void writeJsonResponse(
		EndpointResult epResponse, HttpServletResponse httpResponse) throws IOException {

		Logger tLogger = Logger.getLogger("org.iutools.webservice.EndpointDispatcher.writeJsonResponse");

		String json = mapper.writeValueAsString(epResponse);
		PrintWriter writer = httpResponse.getWriter();
		writer.write(json);
		writer.close();
		tLogger.trace("Returning json="+json);
	}

	public I jsonInputs(
		HttpServletRequest request, Class<I> inputClass) throws ServiceException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.EndpointDispatcher..jsonInputs");

		I inputs = null;
		try {
			String jsonRequestBody = IOUtils.toString(request.getReader());
			tLogger.trace("jsonRequestBody=" + jsonRequestBody);

			if (jsonRequestBody != null) {
				inputs = mapper.readValue(jsonRequestBody, inputClass);
			}
		} catch (IOException e) {
			throw new ServiceException(
				"JSON inputs did not have the structure of class "+inputClass.getName(), e);
		}

		tLogger.trace("returning inputs="+ PrettyPrinter.print(inputs));

		return inputs;
	}

	public static Logger actionLogger() {
		Logger logger = Logger.getLogger("org.iutools.webservice.user_action");
		logger.setLevel(Level.INFO);
		return logger;
	}

	public static Logger errorLogger() {
		Logger logger = Logger.getLogger("org.iutools.webservice.endpoint");
		logger.setLevel(Level.ERROR);
		return logger;
	}
}
