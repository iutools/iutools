package ca.pirurvik.iutools.webservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.script.TransCoder;
import ca.nrc.datastructure.Pair;

public class SearchInputs extends ServiceInputs {
	
	public SearchResultsPage prevPage = null;
	
	public String query = null;
	
	private List<String> termsList = null;
	
	public int hitsPerPage = 10;
	
	public SearchInputs() {
		this.initialize("");
	}

	public SearchInputs(String _query) {
		initialize(_query);
	}
	
	public void initialize(String _query) {
		this.query = _query;
	}
		
	public String convertQueryToSyllabic() {
		if (this.query != null) {
			Matcher matcher = Pattern.compile("[a-zA-z]").matcher(this.query);
			if (matcher.find()) {
				this.query = TransCoder.romanToUnicode(this.query);
			}
		}
		
		return this.query;
	}

	public SearchInputs setHitsPerPage(int _hitsPerPage) {
		this.hitsPerPage = _hitsPerPage;
		return this;
	}
	

	@JsonIgnore
	public List<String> getTerms() {
		if (termsList == null) {
			List<Pair<String, Boolean>> tokens = ca.nrc.string.StringUtils.tokenizeNaively(query);
			
			termsList = new ArrayList<String>();
			for (Pair<String,Boolean> aToken: tokens) {
				Boolean isDelimiter = aToken.getSecond();
				String tokenStr = aToken.getFirst();
				if (!isDelimiter && !tokenStr.equals("OR")) {
					termsList.add(tokenStr);
					
				}
			}			
		}
		
		return termsList;
	}
	
}
