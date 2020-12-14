package org.iutools.webservice;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import ca.nrc.json.JSONUtils;

public class ServiceResponse {

	public String status = null;
	public String errorMessage = null;
	public String stackTrace = null;
	public ServiceInputs failingInputs = null;

	public ServiceResponse() {}

	public void setException(Exception exc) {
		errorMessage = exc.getMessage();
		stackTrace = ExceptionUtils.getStackTrace(exc);
	}
	
	public static String jsonErrorMessage(String json) throws JsonParseException, JsonMappingException, IOException {
		Map<String,Object> jsonMap = JSONUtils.json2ObjectMap(json);
		
		String err = null;
		if (jsonMap.containsKey("errorMessage")) {
			err = (String)jsonMap.get("errorMessage");
		}
		return err;
	}
}



