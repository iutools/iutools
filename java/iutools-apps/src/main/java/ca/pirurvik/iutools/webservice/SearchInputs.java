package ca.pirurvik.iutools.webservice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.script.Roman;
import ca.inuktitutcomputing.script.Syllabics;
import ca.inuktitutcomputing.script.TransCoder;

public class SearchInputs extends ServiceInputs {
	public String query = "";
	private String _querySyllabic = null;

	public SearchInputs() {
	}
	
	public SearchInputs(String _query) {
		this.query = _query;
	}
	
	@JsonIgnore
	public String getQuerySyllabic() {
		String _querySyllabic = this.query;
		if (_querySyllabic != null) {
			Matcher matcher = Pattern.compile("[a-zA-z]").matcher(_querySyllabic);
			if (matcher.find()) {
				_querySyllabic = TransCoder.romanToUnicode(_querySyllabic);
			}
			
		}
		return _querySyllabic;
	}
	
}
