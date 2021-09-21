package org.iutools.webservice;

import ca.nrc.json.PrettyPrinter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.iutools.webservice.logaction.LogActionInputs;
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
		Logger tLogger = Logger.getLogger("org.iutools.webservice.Endpoint.doPost");
		if (tLogger.isTraceEnabled()) {
			try {
				tLogger.trace("request json="+PrettyPrinter.print(IOUtils.toString(request.getReader())));
			} catch (IOException e) {
				// If we can't trace the request, just keep going
			}
		}
		I inputs = null;
		try {

			EndPointHelper.log4jReload();

			inputs = requestInputs(request);
			ensureInputTaskIDAndStartTimeAreDefined(inputs);
			logRequest(request, inputs);
			EndpointResult epResponse = execute(inputs);
			ensureOutputsTaskIDAndStartTimeAreDefined(inputs, epResponse);

			epResponse.taskID = inputs.taskID;
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("response="+mapper.writeValueAsString(epResponse));
			}
			logResult(request, epResponse, inputs);
			writeJsonResponse(epResponse, response);
		} catch (IOException e) {
			tLogger.trace("Caught exception: "+e);
			logError(e, inputs, request);
			throw new ServiceException(e);
		}
		tLogger.trace("POST completed");
	}

	private void ensureOutputsTaskIDAndStartTimeAreDefined(
		I inputs, EndpointResult epResponse) {
		epResponse.taskID = inputs.taskID;
		epResponse.taskStartTime = inputs.taskStartTime;
	}

	private void logResult(HttpServletRequest request, EndpointResult epResponse,
		I inputs) throws ServiceException {
		JSONObject json = epResponse.resultLogEntry();
		if (json != null) {
			long elapsed = System.currentTimeMillis() - inputs.taskStartTime;
			json.put("_endpointPhase", "END");
			json.put("_taskElapsedMsecs", elapsed);
			json.put("_uri", request.getRequestURI());
			endpointLogger().info(json.toString());
		}
	}

	public void logError(Exception e)  {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.Endpoint.logError");
		tLogger.trace("e="+e);

		logError(e, (I)null, (HttpServletRequest)null);
	}

	private void logError(Exception e, I inputs)  {
		logError(e, inputs, (HttpServletRequest)null);
	}


	private void logError(Exception e, I inputs, HttpServletRequest request)  {
		String epClass = this.getClass().getName();
		String inputsJson = null;
		try {
			if (inputs != null) {
				inputsJson = mapper.writeValueAsString(inputs);
			}
			if (inputsJson == null && request != null) {
				inputsJson = IOUtils.toString(request.getReader());
			}
		} catch (Exception e2) {
			throw new RuntimeException(e2);
		}
		errorLogger().error("Error processing inputs: "+inputsJson, e);
	}

	private void ensureInputTaskIDAndStartTimeAreDefined(I inputs) {
		if (inputs.taskID == null) {
			inputs.taskID = generateTaskID();
		}
		if (inputs.taskStartTime == null) {
			inputs.taskStartTime = System.currentTimeMillis();
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
				.put("_endpointPhase", "START")
				.put("taskData", inputSummary);
			String json = jsonifyLogEntry(logEntry);
			Logger logger = null;endpointLogger();
			if (inputs instanceof LogActionInputs) {
				logger = userActionLogger();
			} else {
				logger = endpointLogger();
			}
			logger.info(json);
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
		Logger tLogger = Logger.getLogger("org.iutools.webservice.EndpointDispatcher.jsonInputs");

		I inputs = null;
		String jsonRequestBody = null;
		try {
			jsonRequestBody = IOUtils.toString(request.getReader());
		} catch (IOException e) {
			throw new ServiceException("Could not read inputs from request object", e);
		}

		tLogger.trace("jsonRequestBody=" + jsonRequestBody);

		try {
			if (jsonRequestBody != null) {
				inputs = mapper.readValue(jsonRequestBody, inputClass);
			}
		} catch (IOException e) {
			throw new ServiceException(
				"JSON inputs did not have the structure of class '"+inputClass.getName()+
				"'.\nJSON was: '"+jsonRequestBody+"'", e);
		}

		tLogger.trace("returning inputs="+ PrettyPrinter.print(inputs));

		return inputs;
	}

	public static Logger userActionLogger() {
		Logger logger = Logger.getLogger("org.iutools.webservice.user_action");
		logger.setLevel(Level.INFO);
		return logger;
	}

	public static Logger endpointLogger() {
		Logger logger = Logger.getLogger("org.iutools.webservice.endpoint");
		logger.setLevel(Level.INFO);
		return logger;
	}

	public static Logger errorLogger() {
		Logger logger = Logger.getLogger("org.iutools.webservice.endpoint");
		logger.setLevel(Level.ERROR);
		return logger;
	}
}
