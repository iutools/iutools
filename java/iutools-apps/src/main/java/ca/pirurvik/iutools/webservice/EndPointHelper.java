package ca.pirurvik.iutools.webservice;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.nrc.dtrc.dedupster.webservice.DedupsterServiceResults;

public class EndPointHelper {

	public static void setContenTypeAndEncoding(HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		response.setCharacterEncoding("utf-8");
	}

	public static IUTServiceInputs jsonInputs(HttpServletRequest request) throws IOException {
		String jsonRequestBody = IOUtils.toString(request.getReader());		
		IUTServiceInputs inputs = new IUTServiceInputs();
		if (jsonRequestBody != null) {
			ObjectMapper mapper = new ObjectMapper();
			inputs = mapper.readValue(jsonRequestBody, IUTServiceInputs.class);
		}
				
		return inputs;
	}
	
	public static String jsonBody(HttpServletRequest request) throws IOException {
		String body = IOUtils.toString(request.getReader());
		return body;
	}		
	
	
	public static String emitServiceExceptionResponse(String message, Exception exc) {
		IUTServiceResults results = new IUTServiceResults();
		message += "\n"+exc.getMessage()+"\n"+ExceptionUtils.getFullStackTrace(exc);
		results.errorMessage = message;
		String jsonResponse = "null";
		try {
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return jsonResponse;
	}

	public static String emitServiceExceptionResponse(String string, MalformedURLException exc) {
		String output = null;
		// Copy and paste from Dedupster project implementation
		int x = 1/0;
		return output;
	}

}
