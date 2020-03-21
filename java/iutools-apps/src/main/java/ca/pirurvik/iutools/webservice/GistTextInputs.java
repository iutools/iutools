package ca.pirurvik.iutools.webservice;

public class GistTextInputs extends ServiceInputs {
	
	public String word = null;
	
	public GistTextInputs() {}

	public GistTextInputs(String _word) {
		this.word = _word;
	}
}
