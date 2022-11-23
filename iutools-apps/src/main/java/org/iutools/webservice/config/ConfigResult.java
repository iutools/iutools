package org.iutools.webservice.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.iutools.webservice.EndpointResult;

import java.util.HashMap;
import java.util.Map;

public class ConfigResult extends EndpointResult {
	public Map<String,Object> properties = new HashMap<String,Object>();

	@JsonIgnore
	public void setProperty(String propName, Object propValue) {
		properties.put(propName, propValue);
	}
}
