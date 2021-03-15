package org.iutools.webservice;

import ca.nrc.json.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class Endpoint
	<I extends ServiceInputs, R extends EndpointResult>  {

	protected abstract I requestInputs(HttpServletRequest request)
		throws ServiceException;

	ObjectMapper mapper = new ObjectMapper();

	public abstract EndpointResult execute(I inputs)
		throws ServiceException;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServiceException {

		I inputs = requestInputs(request);
		EndpointResult epResponse = execute(inputs);
		try {
			writeJsonResponse(epResponse, response);
		} catch (IOException e) {
			throw new ServiceException(e);
		}
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
}
