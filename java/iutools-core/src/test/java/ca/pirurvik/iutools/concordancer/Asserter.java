package ca.pirurvik.iutools.concordancer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Asserter {

	protected String baseMessage = "";
	
	protected Object gotObject = null;
	
	public Asserter(Object _gotObject, String mess, Class<?> gotObjectClass) 
		throws Exception {
		this.baseMessage = mess;
		
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(_gotObject);
		gotObject = mapper.readValue(json, gotObjectClass);
	}
	
}
