package ca.pirurvik.iutools.webservice;

public class SearchHit {
	public String url;
	public String title;
	public String snippet;
	
	public SearchHit() {
		
	}
	
	public SearchHit(String _url, String _title, String _snippet) {
		this.url = _url;
		this.title = _title;
		this.snippet = _snippet;
	}
}
