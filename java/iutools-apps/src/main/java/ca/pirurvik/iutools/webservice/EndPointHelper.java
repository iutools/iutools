package ca.inuktitutcomputing.iutools.webservice;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EndPointHelper {

	public static void setContenTypeAndEncoding(HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		response.setCharacterEncoding("utf-8");
	}

	public static <T  extends ServiceInputs> T jsonInputs(HttpServletRequest request, Class<T> inputClass) throws IOException {
		String jsonRequestBody = IOUtils.toString(request.getReader());		
		T inputs = null;
		if (jsonRequestBody != null) {
			ObjectMapper mapper = new ObjectMapper();
			inputs = mapper.readValue(jsonRequestBody, inputClass);
		}
				
		return inputs;
	}
	
	public static String jsonBody(HttpServletRequest request) throws IOException {
		String body = IOUtils.toString(request.getReader());
		return body;
	}		
	
	
	public static String emitServiceExceptionResponse(String message, Exception exc) {
		ServiceResponse results = new ServiceResponse();
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
