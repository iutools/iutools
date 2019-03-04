package ca.pirurvik.iutools.webservice;

public class IUTSSearchHit {
	public String url;
	public String title;
	public String snippet;
	public String content;
	
	public IUTSSearchHit(String _url, String _title, String _content, String _snippet) {
		this.url = _url;
		this.title = _title;
		this.content = _content;
		this.snippet = _snippet;
	}
}
