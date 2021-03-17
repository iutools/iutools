package org.iutools.webservice;

import org.iutools.webservice.log.LogEndpoint;
import org.iutools.webservice.morphexamples.MorphemeExamplesEndpoint;
import org.iutools.webservice.search.ExpandQuery2Endpoint;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EndpointDispatcher extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws IOException {

		EndPointHelper.setContenTypeAndEncoding(response);

		String jsonResponse = null;
		String epName = null;
		try {
			epName = endpointName(request.getRequestURI());
			Endpoint endPoint = null;
			if (epName.equals("log")) {
				endPoint = new LogEndpoint();
			} else if (epName.equals("morpheme_examples")) {
				endPoint = new MorphemeExamplesEndpoint();
			} else if (epName.equals("search/expandquery")) {
				endPoint = new ExpandQuery2Endpoint();
			} else {
				throw new ServiceException("No handler for endpoint name: "+epName);
			}
			endPoint.doPost(request, response);
		} catch (Exception exc) {
			jsonResponse =
				EndPointHelper.emitServiceExceptionResponse(
					"Service raised exception\n", exc);
			response.getWriter().write(jsonResponse);
		}
	}

	String endpointName(String requestURI) throws ServiceException {
		Pattern patt =
			Pattern.compile(
				"iutools/srv2/(log|gist/(gistword|preparecontent)|morpheme_examples|"+
				"relatedwords|search/expandquery|spell|tokenize)")
				;
		Matcher matcher = patt.matcher(requestURI);
		String epName = null;
		if (matcher.find()) {
			epName = matcher.group(1);
		} else {
			throw new ServiceException("No known endpoint for URI "+requestURI);
		}
		return epName;
	}
}