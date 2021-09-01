package org.iutools.morph.l2rAlain;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeException;

public class WrittenMorpheme {
	public static final WrittenMorpheme head;

	static {
		try {
			head = new WrittenMorpheme(null, "");
		} catch (MorphemeException e) {
			throw new RuntimeException(e);
		}
	}

	public String morphID = null;
	public String writtenForm = null;
	public String attachesTo = null;
	public String resultsIn = null;
	private String _regex = null;
	private String _canonicalForm = null;

//	final Pattern morphIDPatt = Pattern.compile("^([^/]*)/(\\d*)([nv]?)([nv])$");
//	final Pattern morphIDPatt = Pattern.compile("^([^/]*)/(\\d*)(.*)$");
	
	public WrittenMorpheme(String id, String surface) throws MorphemeException {
		this.morphID = id;
		this.writtenForm = surface;
		Pair<String,String> typeConstraints = Morpheme.typeConstraints(id);
		attachesTo = typeConstraints.getLeft();
		if (attachesTo != null) {
			attachesTo = attachesTo.toUpperCase();
		}
		resultsIn = typeConstraints.getRight();
		if (resultsIn != null) {
			resultsIn = resultsIn.toUpperCase();
		}
	}

	public boolean equals(WrittenMorpheme other) {
		Boolean answer = true;
		if (!this.writtenForm.equals(other.writtenForm)) {
			answer = false;
		}
		if (answer == null && !this.morphID.equals(other.morphID)) {
			answer = false;
		}
		if (answer == null) {
			answer = true;
		}
		return answer;
	}

	public String writtenFormWithHeadConstraints() {
		return attachesTo+writtenForm;
	}

	public String canonicalForm() throws MorphemeException {
		if (_canonicalForm == null) {
			try {
				_canonicalForm = Morpheme.splitMorphID(morphID).getLeft();
			} catch (MorphemeException e) {
				throw new MorphemeException(e);
			}
		}
		return _canonicalForm;
		
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

	public String regex()  {
		if (_regex == null) {
			_regex = attachesTo+writtenForm;
		}
		
		return _regex;
	}
	
}
