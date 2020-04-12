package ca.inuktitutcomputing.morph.expAlain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.morph.MorphInukException;
import ca.nrc.datastructure.Pair;

public class WrittenMorpheme {
	public static final WrittenMorpheme head = new WrittenMorpheme(null, "");
	
	public String morphID = null;
	public String writtenForm = null;
	private String _attachesTo = null;
	private String _type = null;
	private String _regex = null;
	private String _canonicalForm = null;
	
	final Pattern morphIDPatt = Pattern.compile("^([^/]*)/(\\d*)([nv]?)([nv])$");	
	
	public WrittenMorpheme(String id, String surface) {
		this.morphID = id;
		this.writtenForm = surface;
	}
	
	public String atachesTo() throws MorphInukException {
		if (_attachesTo == null) {
			parseID();
		}
		return _attachesTo;
	}

	public String type() throws MorphInukException {
		if (_type == null) {
			parseID();
		}
		return _type;
	}
	
	public String canonicalForm() throws MorphInukException {
		if (_canonicalForm == null) {
			parseID();
		}
		return _canonicalForm;
		
	}
	
	private void parseID() throws MorphInukException {
		if (morphID == null) {
			_attachesTo = null;
			_type = "1";
		} else try {
			Matcher matcher = morphIDPatt.matcher(morphID);
			if (matcher.matches()) {
				_canonicalForm = matcher.group(1);
				_attachesTo = matcher.group(3).toUpperCase();
				if (_attachesTo.isEmpty()) {
					_attachesTo = "S"; // Means it attaches to start of word
				}
				_type = matcher.group(4).toUpperCase();
			} else {
				throw new MorphInukException(
					"Morpheme ID was invalid: "+morphID);
			}
		} catch (Exception e) {
			throw new MorphInukException(
					"Morpheme ID was invalid: "+morphID);			
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append(writtenForm);
		builder.append(":");
		builder.append(morphID);
		builder.append("}");
		
		return builder.toString();
	}

	public String regex() throws MorphInukException {
		if (_regex == null) {
			_regex = atachesTo()+writtenForm;
		}
		
		return _regex;
	}
	
}
