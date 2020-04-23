package ca.pirurvik.iutools.testing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Asserter {

	protected String baseMessage = "";
	
	protected Object gotObject = null;
	
	public Asserter(Object _gotObject, String mess, Class<?> gotObjectClass) 
		throws Exception {
		this.baseMessage = mess;
		this.gotObject = _gotObject;
	}
	
}
