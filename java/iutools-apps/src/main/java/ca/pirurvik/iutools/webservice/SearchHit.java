package ca.pirurvik.iutools.webservice;

public class SearchHit {
	public String url;
	public String title;
	public String snippet;
	public String content;
	
	public SearchHit(String _url, String _title, String _content, String _snippet) {
		this.url = _url;
		this.title = _title;
		this.content = _content;
		this.snippet = _snippet;
	}
}
