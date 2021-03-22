package org.iutools.webservice;

import org.iutools.webservice.gist.GistPrepareContentEndpoint;
import org.iutools.webservice.gist.GistWordEndpoint;
import org.iutools.webservice.logaction.LogActionEndpoint;
import org.iutools.webservice.morphexamples.MorphemeExamplesEndpoint;
import org.iutools.webservice.search.ExpandQueryEndpoint;
import org.iutools.webservice.spell.Spell2Endpoint;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EndpointDispatcher extends HttpServlet {

	private static Pattern _endpointURIsPattern = null;

	private Map<String,Endpoint> endpoints = new HashMap<String,Endpoint>();
	{
		endpoints.put("log_action", new LogActionEndpoint());
		endpoints.put("morpheme_examples", new MorphemeExamplesEndpoint());
		endpoints.put("search/expandquery", new ExpandQueryEndpoint());
		try {
			endpoints.put("spell", new Spell2Endpoint());
		} catch (ServiceException e) {
			// Just ignore the exception and setup the remaining endpoints
		}
		endpoints.put("gist/preparecontent", new GistPrepareContentEndpoint());
		endpoints.put("gist/gistword", new GistWordEndpoint());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws IOException {

		EndPointHelper.setContenTypeAndEncoding(response);

		String jsonResponse = null;
		String epName = null;
		try {
			epName = endpointName(request.getRequestURI());
			Endpoint endPoint = endpointWithName(epName);
			endPoint.doPost(request, response);
		} catch (Exception exc) {
			jsonResponse =
				EndPointHelper.emitServiceExceptionResponse(
					"Service raised exception\n", exc);
			response.getWriter().write(jsonResponse);
		}

		return;
	}

	private synchronized Endpoint endpointWithName(String epName) {
		return endpoints.get(epName);
	}

	String endpointName(String requestURI) throws ServiceException {

		Pattern patt = endpointURIsPattern();
		Matcher matcher = patt.matcher(requestURI);
		String epName = null;
		if (matcher.find()) {
			epName = matcher.group(1);
		} else {
			throw new ServiceException("No known endpoint for URI "+requestURI);
		}
		return epName;
	}

	private synchronized Pattern endpointURIsPattern() {
		if (_endpointURIsPattern == null) {
			List<String> urisList = new ArrayList<String>();
			urisList.addAll(endpoints.keySet());
			Collections.sort(urisList);

			String regexp =
				"iutools/srv2/("+
				String.join("|", urisList)+
				")";

			_endpointURIsPattern = Pattern.compile(regexp);
		}
		return _endpointURIsPattern;
	}
}