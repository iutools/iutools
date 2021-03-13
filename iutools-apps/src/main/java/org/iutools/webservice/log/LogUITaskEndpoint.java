package org.iutools.webservice.log;

import ca.nrc.json.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.iutools.webservice.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.iutools.webservice.EndPointHelper;
import java.io.PrintWriter;
import java.util.HashMap;

public class LogUITaskEndpoint {

	;

	public void doPost(
		HttpServletRequest request, HttpServletResponse response)
		throws IOException {

		EndPointHelper.log4jReload();
		Logger tLogger = Logger.getLogger("org.iutools.webservice.OccurenceSearchEndpoint.doPost");
		tLogger.trace("invoked");
		tLogger.trace("request URI= "+request.getRequestURI());

		String jsonResponse = null;

		LogUITaskInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, LogUITaskInputs.class);
			tLogger.trace("inputs= "+ PrettyPrinter.print(inputs));
			ServiceResponse results = executeEndPoint(inputs);
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}
		EndPointHelper.writeJsonResponse(response, jsonResponse);
	}

	private ServiceResponse executeEndPoint(LogUITaskInputs inputs) {
		Logger logger = Logger.getLogger("org.iutools.webservice.log.LogUITaskEndpoint.executeEndPoint");
		logger.trace("inputs= " + PrettyPrinter.print(inputs));
		OccurenceSearchResponse results = new OccurenceSearchResponse();



		return results;
	}
}
