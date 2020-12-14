package org.iutools.webservice;

public class SpellInputs extends ServiceInputs {
	public String text = null;
	public boolean includePartiallyCorrect = false;
	
	public SpellInputs() {
		
	}
	
	public SpellInputs(String _text) {
		this.text = _text;
	}
}
