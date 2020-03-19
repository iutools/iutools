package ca.pirurvik.iutools.webservice;

public class GistInputs extends ServiceInputs {
	
	public String word = null;
	
	public GistInputs() {}

	public GistInputs(String _word) {
		this.word = _word;
	}
}
