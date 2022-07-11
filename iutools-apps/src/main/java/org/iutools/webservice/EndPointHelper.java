package org.iutools.webservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.nrc.json.PrettyPrinter;

public class EndPointHelper {

	public static void setContenTypeAndEncoding(HttpServletResponse response) {
		response.setCharacterEncoding("utf-8");
	}

	public static <T  extends ServiceInputs> T jsonInputs(
			HttpServletRequest request, Class<T> class1) throws IOException {
		Logger tLogger = LogManager.getLogger("org.iutools.webservice.EndPointHelper.jsonInputs");
		
		String jsonRequestBody = IOUtils.toString(request.getReader());		
		tLogger.trace("jsonRequestBody="+jsonRequestBody);
		T inputs = null;
		if (jsonRequestBody != null) {
			ObjectMapper mapper = new ObjectMapper();
			inputs = mapper.readValue(jsonRequestBody, class1);
		}
				
		tLogger.trace("returning inputs="+PrettyPrinter.print(inputs));
		
		return inputs;
	}
	
	public static String jsonBody(HttpServletRequest request) throws IOException {
		String body = IOUtils.toString(request.getReader());
		return body;
	}


	public static String emitServiceExceptionResponse(
		String message, Exception exc) {
		return emitServiceExceptionResponse(message, exc, (ServiceInputs)null);
	}
	
	public static String emitServiceExceptionResponse(
		String message, Exception exc, ServiceInputs failingInputs) {
		ServiceResponse results = new ServiceResponse();
		message += "\n"+exc.getMessage()+"\n"+ExceptionUtils.getFullStackTrace(exc);
		results.errorMessage = message;
		results.failingInputs = failingInputs;
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
	
	public static void log4jReload() {
		String log4jprops = System.getProperty("log4j.config");
		if (log4jprops != null) {
			PropertyConfigurator.configure(log4jprops);
		}
	}

	public static void writeJsonResponse(
	HttpServletResponse response, String json) throws IOException {
		Logger tLogger = LogManager.getLogger("org.iutools.webservice.IUTServiceTestHelpers.writeJsonResponse");
		PrintWriter writer = response.getWriter();
		writer.write(json);
		writer.close();
		tLogger.trace("Returning json="+json);
	}
}
