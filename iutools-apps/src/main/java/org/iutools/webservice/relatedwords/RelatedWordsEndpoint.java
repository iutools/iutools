package org.iutools.webservice.relatedwords;

import ca.nrc.json.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.corpus.CompiledCorpusRegistryException;
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

public class RelatedWordsEndpoint extends HttpServlet {

protected void doGet(HttpServletRequest request,
								HttpServletResponse response) throws ServletException, IOException {
		Logger logger = Logger.getLogger("org.iutools.webservice.RelatedWordsEndpoint.doGet");
		logger.debug("doGet()");
	}

	public void doPost(HttpServletRequest request,
							 HttpServletResponse response) throws IOException {
		EndPointHelper.log4jReload();
		Logger tLogger = Logger.getLogger("org.iutools.webservice.RelatedWordsEndpoint.doPost");

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("invoked with request=\n" + PrettyPrinter.print(request));
		}

		String jsonResponse = null;

		EndPointHelper.setContenTypeAndEncoding(response);

		RelatedWordsInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, RelatedWordsInputs.class);
			tLogger.trace("inputs="+ PrettyPrinter.print(inputs));
			ServiceResponse results = executeEndPoint(inputs);
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}

		writeJsonResponse(response, jsonResponse);
	}

	private ServiceResponse executeEndPoint(RelatedWordsInputs inputs) {
		MorphRelativesFinder relsFinder = null;
		try {
			CompiledCorpus corpus =
				new CompiledCorpusRegistry().getCorpus(inputs.useCorpus);
			relsFinder = new MorphRelativesFinder();
		} catch (MorphRelativesFinderException | CompiledCorpusException | CompiledCorpusRegistryException e) {
			throw new WebServiceException(
				"Could not instantiate the related words finder", e);
		}

		MorphologicalRelative[] relatedWords = null;
		try {
			relatedWords = relsFinder.findRelatives(inputs.word);
		} catch (MorphRelativesFinderException e) {
			throw new WebServiceException(
				"Exception raised while searching for related words", e);
		}

		RelatedWordsResponse response =
			new RelatedWordsResponse(inputs.word, relatedWords);

		return response;
	}

	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.writeJsonResponse");

		tLogger.debug("json="+json);
		PrintWriter writer = response.getWriter();

		writer.write(json);
		writer.close();
	}	
}
