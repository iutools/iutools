package ca.pirurvik.iutools.webservice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.script.TransCoder;
import ca.pirurvik.iutools.search.PageOfHits;

public class SearchInputs extends ServiceInputs {
	
	public PageOfHits prevPage = null;
	
	public SearchInputs() {
		this.initialize("");
	}

	public SearchInputs(String _query) {
		initialize(_query);
	}
	
	public void initialize(String _query) {
		this.prevPage = new PageOfHits(_query);
	}

		
	public String convertQueryToSyllabic() {
		if (this.prevPage.query != null) {
			Matcher matcher = Pattern.compile("[a-zA-z]").matcher(this.prevPage.query);
			if (matcher.find()) {
				this.prevPage.query = TransCoder.romanToUnicode(this.prevPage.query);
			}
		}
		
		return this.prevPage.query;
	}

	public SearchInputs setHitsPerPage(int _hitsPerPage) {
		this.prevPage.hitsPerPage = _hitsPerPage;
		return this;
	}
	
	public SearchInputs setPageNum(int pageNum) {
		this.prevPage.setHitsPageNum(pageNum);
		return this;
	}
	
}
