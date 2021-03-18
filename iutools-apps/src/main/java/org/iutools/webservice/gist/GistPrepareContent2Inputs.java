package org.iutools.webservice.gist;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.iutools.webservice.ServiceInputs;

import java.net.URL;

public class GistPrepareContent2Inputs extends ServiceInputs {

	public String textOrUrl = null;

	public GistPrepareContent2Inputs() {}

	public GistPrepareContent2Inputs(String _text) {
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
