package org.iutools.webservice.search;

import ca.nrc.json.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.iutools.morphrelatives.MorphRelativesFinder;
import org.iutools.morphrelatives.MorphRelativesFinderException;
import org.iutools.morphrelatives.MorphologicalRelative;
import org.iutools.webservice.EndPointHelper;
import org.iutools.webservice.ServiceResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.PrintWriter;

public class ExpandQueryEndpoint extends HttpServlet {

	protected void doGet(HttpServletRequest request,
								HttpServletResponse response) throws ServletException, IOException {
		Logger logger = Logger.getLogger("org.iutools.webservice.search.ExpandQueryEndpoint.doGet");
		logger.debug("doGet()");
	}

	public void doPost(HttpServletRequest request,
							 HttpServletResponse response) throws IOException {
		EndPointHelper.log4jReload();
		Logger tLogger = Logger.getLogger("org.iutools.webservice.search.ExpandQueryEndpoint.doPost");

		String jsonResponse = null;

		EndPointHelper.setContenTypeAndEncoding(response);

		ExpandQueryInputs inputs = null;
		ExpandQueryResponse results = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, ExpandQueryInputs.class);
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("inputs="+ PrettyPrinter.print(inputs));
			}
			results = executeEndPoint(inputs);
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}

//		tLogger.trace("returning results.expandedQuery"+results.expandedQuery);

		writeJsonResponse(response, jsonResponse);
	}

	private ExpandQueryResponse executeEndPoint(ExpandQueryInputs inputs) {
		MorphRelativesFinder relsFinder = null;
		try {
			relsFinder = new MorphRelativesFinder();
		} catch (MorphRelativesFinderException e) {
			throw new WebServiceException(
			"Could not instantiate the related words finder", e);
		}

		MorphologicalRelative[] relatedWords = null;
		if (!queryAlreadyExpanded(inputs.origQuery)) {
			try {
				relatedWords = relsFinder.findRelatives(inputs.origQuery);
			} catch (MorphRelativesFinderException e) {
				throw new WebServiceException(
				"Exception raised while searching for related words", e);
			}
		}

		ExpandQueryResponse response =
			new ExpandQueryResponse(inputs.origQuery, relatedWords);

		return response;
	}

	private boolean queryAlreadyExpanded(String query) {
		boolean alreadyExpanded = (query.matches("^\\s*\\(.*\\)\\s*$"));
		return alreadyExpanded;
	}

	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.writeJsonResponse");

		tLogger.debug("json="+json);
		PrintWriter writer = response.getWriter();

		writer.write(json);
		writer.close();
	}
}
