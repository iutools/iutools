package org.iutools.webservice;

import org.apache.log4j.Logger;
import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.morph.MorphologicalAnalyzer;
import org.iutools.morph.MorphologicalAnalyzerException;
import org.iutools.webservice.gist.GistPrepareContentEndpoint;
import org.iutools.webservice.gist.GistWordEndpoint;
import org.iutools.webservice.logaction.LogActionEndpoint;
import org.iutools.webservice.morphexamples.MorphemeExamplesEndpoint;
import org.iutools.webservice.search.ExpandQueryEndpoint;
import org.iutools.webservice.spell.SpellEndpoint;
import org.iutools.webservice.tokenize.TokenizeEndpoint;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EndpointDispatcher extends HttpServlet {

	private static Pattern _endpointURIsPattern = null;

	private Map<String,Endpoint> endpoints = new HashMap<String,Endpoint>();
	{
		endpoints.put("log_action", new LogActionEndpoint());

		endpoints.put("morpheme_dictionary", new MorphemeExamplesEndpoint());

		// WHY DO WE STILL NEED THIS?
		endpoints.put("morpheme_examples", new MorphemeExamplesEndpoint());

		endpoints.put("search/expandquery", new ExpandQueryEndpoint());
		try {
			endpoints.put("spell", new SpellEndpoint());
		} catch (ServiceException e) {
			// Just ignore the exception and setup the remaining endpoints
		}
		endpoints.put("tokenize", new TokenizeEndpoint());
		endpoints.put("gist/preparecontent", new GistPrepareContentEndpoint());
		endpoints.put("gist/gistword", new GistWordEndpoint());
	}

	public EndpointDispatcher() {
		super();
		Logger tLogger = Logger.getLogger("org.iutools.webservice.EndpointDispatcher.constructor");
		tLogger.trace("invoked");

		// This ensures that the overhead of loading the linguistic data will be
		// encurred when Tomcat creates an EndpointDispatcher and puts it in its
		// thread pool.
		//
		// In turn, this means that we can write a jmeter with a setUp() step
		// that invokes a bunch of iutools http requests in close succession.
		// This forces Tomcat to create and initialize a number of threads which
		// will then be reused by the actual evaluation part of the jmeter test
		// plan. As a result, the speed statistics gathered by jmeter will not
		// be affected by the thread initialization time.
		//
		try {
			ensureLinguisticDataIsLoaded();
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		}
	}

	private void ensureLinguisticDataIsLoaded() throws ServiceException {
		try {
			// Decomposing a word will force loading of the data
			new MorphologicalAnalyzer().decomposeWord("inuksuk");
		} catch (TimeoutException | MorphologicalAnalyzerException |
			LinguisticDataException e) {
			throw new ServiceException(e);
		}
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