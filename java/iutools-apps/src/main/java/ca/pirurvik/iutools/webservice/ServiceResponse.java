package ca.inuktitutcomputing.iutools.webservice;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ServiceResponse {

	public String status = null;
	public String errorMessage = null;
	public String stackTrace = null;

	public ServiceResponse() {}

	public void setException(Exception exc) {
		errorMessage = exc.getMessage();
		stackTrace = ExceptionUtils.getStackTrace(exc);
	}
}



