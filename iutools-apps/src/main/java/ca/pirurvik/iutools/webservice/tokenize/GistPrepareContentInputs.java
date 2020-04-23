package ca.pirurvik.iutools.webservice.tokenize;

import java.net.URL;

import ca.pirurvik.iutools.webservice.ServiceInputs;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GistPrepareContentInputs extends ServiceInputs {
	
	public String textOrUrl = null;

	public GistPrepareContentInputs() {
		
	}
	
	public GistPrepareContentInputs(String _text) {
		this.textOrUrl = _text;
	}

	@JsonIgnore
	public boolean isURL() {
		boolean answer = false;
		try {
			URL url = new URL(textOrUrl);
			answer = true;
		} catch (Exception e) {
			answer = false;
		}
		
		return answer;
	}
}
