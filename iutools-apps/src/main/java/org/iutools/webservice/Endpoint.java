package org.iutools.webservice;

import ca.nrc.json.PrettyPrinter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;

public abstract class Endpoint
	<I extends ServiceInputs, R extends EndpointResult>  {

	protected abstract I requestInputs(HttpServletRequest request)
		throws ServiceException;

	ObjectMapper mapper = new ObjectMapper();

	public abstract EndpointResult execute(I inputs)
		throws ServiceException;

	public JSONObject logEntry(I inputs) throws ServiceException {
		String json = null;
		try {
			json = mapper.writeValueAsString(inputs);
		} catch (JsonProcessingException e) {
			throw new ServiceException(e);
		}
		JSONObject entry = new JSONObject(json);
		entry.remove("taskID");
		return entry;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServiceException {

		EndPointHelper.log4jReload();

		I inputs = requestInputs(request);
		ensureTaskIDIsDefined(inputs);

		logRequest(request, inputs);
		EndpointResult epResponse = execute(inputs);
		epResponse.taskID = inputs.taskID;
		try {
			writeJsonResponse(epResponse, response);
		} catch (IOException e) {
			throw new ServiceException(e);
		}
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
		JSONObject logEntry = logEntry(inputs);
		logEntry.put("uri", request.getRequestURI());
		logEntry.put("taskID", inputs.taskID);
		String entryJson = logEntry.toString();
		logger().info(entryJson);
		return;
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

	public static Logger logger() {
		Logger logger = Logger.getLogger("org.iutools.webservice.log");
		logger.setLevel(Level.INFO);
		return logger;
	}
}
