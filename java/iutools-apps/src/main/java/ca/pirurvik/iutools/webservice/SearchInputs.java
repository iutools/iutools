package ca.pirurvik.iutools.webservice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.script.TransCoder;

public class SearchInputs extends ServiceInputs {
	public String query = "";
	public int hitsPerPage = 10;
	public Integer hitsPageNum = 0;
	public Set<String> excludedHits = new HashSet<String>();

	public SearchInputs() {
	}
	
	public SearchInputs(String _query) {
		this.query = _query;
	}
	
	public SearchInputs excludeURLs(List<String> urls) {
		this.excludedHits.addAll(urls);
		return this;
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

	public SearchInputs setHitsPerPage(int _hitsPerPage) {
		this.hitsPerPage = _hitsPerPage;
		return this;
	}
	
}
