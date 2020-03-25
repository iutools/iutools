package ca.pirurvik.iutools.webservice.tokenize;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.inuktitutcomputing.utilities.IUTokenizer;
import ca.nrc.datastructure.Pair;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.webservice.EndPointHelper;
import ca.pirurvik.iutools.webservice.SearchInputs;
import ca.pirurvik.iutools.webservice.ServiceResponse;

public class TokenizeEndpoint {
	
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		Logger logger = Logger.getLogger("SearchEndpoint.doGet");
		logger.debug("doGet()");
	}
	
	public void doPost(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.SearchEndpoint.doPost");
		
				
		String jsonResponse = null;

		EndPointHelper.setContenTypeAndEncoding(response);

		TokenizeInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, TokenizeInputs.class);
			tLogger.trace("inputs="+PrettyPrinter.print(inputs));
			ServiceResponse results = executeEndPoint(inputs);
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}
		
		writeJsonResponse(response, jsonResponse);
	}

	private ServiceResponse executeEndPoint(TokenizeInputs inputs) {
		IUTokenizer tokenizer = new IUTokenizer();
		
		tokenizer.tokenize(inputs.text);
		List<Pair<String,Boolean>> tokens = tokenizer.getAllTokens();
		
		TokenizeResponse response = new TokenizeResponse(tokens);		
		
		return response;
	}
	
	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.writeJsonResponse");
		
		tLogger.debug("json="+json);
		PrintWriter writer = response.getWriter();
		
		writer.write(json);
		writer.close();
	}
	
}
