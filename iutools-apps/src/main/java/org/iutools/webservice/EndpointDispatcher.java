package org.iutools.webservice;

import org.iutools.webservice.morphexamples.MorphemeExamplesEndpoint;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EndpointDispatcher extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws IOException {

		String jsonResponse = null;
		String epName = null;
		try {
			epName = endpointName(request.getRequestURI());
			Endpoint endPoint = null;
			if (epName.equals("morpheme_examples")) {
				endPoint = new MorphemeExamplesEndpoint();
			} else {
				throw new ServiceException("Unknown endpoint name: "+epName);
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
				"iutools/srv2/(expandquery|(gist)/(gistword|preparecontent)|morpheme_examples|"+
				"relatedwords|spell|tokenize)")
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