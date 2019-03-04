package ca.pirurvik.iutools.webservice;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

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

	public static String emitServiceExceptionResponse(String string, MalformedURLException exc) {
		String output = null;
		// Copy and paste from Dedupster project implementation
		int x = 1/0;
		return output;
	}

}
