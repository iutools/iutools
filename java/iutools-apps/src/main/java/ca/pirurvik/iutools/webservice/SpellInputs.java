package ca.pirurvik.iutools.webservice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.script.TransCoder;

public class SpellInputs extends ServiceInputs {
	public String text = null;
	
	public SpellInputs() {
		
	}
	
	public SpellInputs(String _text) {
		this.text = _text;
	}
}
