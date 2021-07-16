package org.iutools.linguisticdata;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Given a Morpheme ID, this class generates a human-readable description of
 * what that morpheme is about.
 */
public class MorphemeHumanReadableDescr implements Comparable<MorphemeHumanReadableDescr> {

	public String id = null;
	public String canonicalForm = null;
	public String grammar = null;
	public String meaning = null;

	private static Map<String,String> caseAbbrevs = new HashMap<String,String>();
	static {
		caseAbbrevs.put("loc", "locative");
		caseAbbrevs.put("dec", "declarative");
		caseAbbrevs.put("caus", "causative");
	}


	private static Pattern pattSeparateCanonicalFromRoles =
		Pattern.compile("^([^/]*)/\\d?(.*)$");

	public MorphemeHumanReadableDescr() throws MorphemeException {
	}


	public MorphemeHumanReadableDescr(String morphID) throws MorphemeException {
		init__MorphemeHumanReadableDescr(morphID, (String)null);
	}

	public MorphemeHumanReadableDescr(String morphID, String _definition)
		throws MorphemeException {
		init__MorphemeHumanReadableDescr(morphID, _definition);
	}

	private void init__MorphemeHumanReadableDescr(
		String morphID, String _definition) throws MorphemeException {
		this.meaning = _definition;
		this.id = morphID;
		Pair<String,String> parsed = parseMorphID(morphID);
		this.canonicalForm = parsed.getLeft();
		this.grammar = parsed.getRight();
	}

	private Pair<String, String> parseMorphID(String morphID) throws MorphemeException {
		Pair<String, String[]> splitID = splitMorphID(morphID);
		String canonical = splitID.getLeft();
		String[] attributes = splitID.getRight();
		String gramm = null;

		Matcher matcher = pattSeparateCanonicalFromRoles.matcher(morphID);

		if (matcher.matches()) {
			canonical = matcher.group(1);
			String attributesString = matcher.group(2);

			String transitivityOrPosessivity = transitivityOrPosessivity(attributes);
			String partOfSpeechDescr = partOfSpeech(attributes);
			String positionDescr = position(attributes);
			String caseDescr = caseFor(attributes);
			String primaryPersAndNumber = primaryPersonAndNumber(attributes);
			String secondaryPersAndNumber = secondaryPersonAndNumber(attributes);

			gramm = "";
			gramm = expandDescr(gramm, transitivityOrPosessivity);
			gramm = expandDescr(gramm, partOfSpeechDescr);
			gramm = expandDescr(gramm, positionDescr);
			if (caseDescr != null || primaryPersAndNumber != null) {
				gramm += ";";
			}
			gramm = expandDescr(gramm, caseDescr);
			gramm = expandDescr(gramm, primaryPersAndNumber);
			if (secondaryPersAndNumber != null) {
				gramm += ";";
			}
			gramm = expandDescr(gramm, secondaryPersAndNumber);
		}

		return Pair.of(canonical, gramm);
	}

	public static String descriptiveText(String morphID) throws MorphemeException {
		MorphemeHumanReadableDescr descr =
			new MorphemeHumanReadableDescr(morphID);
		String descrTxt = descr.canonicalForm + " (" + descr.grammar + ")";
		return descrTxt;
	}

	protected static Pair<String,String[]> splitMorphID(String morphID) {
		Pair<String, String[]> splitID = null;
		Matcher matcher = pattSeparateCanonicalFromRoles.matcher(morphID);


		if (matcher.matches()) {
			String canonicalForm = matcher.group(1);
			String attributesString = matcher.group(2);
			String[] attributes = attributesString.split("-");
			splitID = Pair.of(canonicalForm, attributes);
		}

		return splitID;
	}

	private static String expandDescr(String attrsDescr, String toAppend) {
		if (toAppend != null) {
			if (attrsDescr != null && attrsDescr.length() > 0) {
				attrsDescr += " ";
			}
			attrsDescr += toAppend;
		}
		return attrsDescr;
	}

	private static String secondaryPersonAndNumber(String[] attributes) throws MorphemeException {
		String persNum = null;
		String firstAttr = attributes[0];
		if (firstAttr.startsWith("t")) {
			if (attributes.length > 3) {
				persNum = personAndNumber4abbrev(attributes[3]);
//				if (persNum == null) {
//					throw new MorphemeException("4th attribute was neither person nor number: "+attributesString);
//				}
			}
		}
		if (persNum != null) {
			if (firstAttr.equals("tn")) {
				persNum += " posessor";
			} else if (firstAttr.equals("tv")) {
				persNum += " object";
			}
		}

		return persNum;
	}

	private static String primaryPersonAndNumber(String[] attributes) {
		String persNum = null;
		String firstAttr = attributes[0];
		if (firstAttr.startsWith("t")) {
			if (attributes.length > 2) {
				persNum = personAndNumber4abbrev(attributes[2]);
			}
		}
		return persNum;
	}

	private static String transitivityOrPosessivity(String[] attributes) {
		String transOrPosess = null;
		String firstAttr = attributes[0];
		if (firstAttr.startsWith("t")) {
			boolean has4thAttribute = (attributes.length == 4);
			if (firstAttr.equals("tn")) {
				// This is a noun morpheme. Is it posessive or not?
				if (has4thAttribute) {
					transOrPosess = "posessive";
				}
			} else if (firstAttr.equals("tv")) {
				// This is a verb morpheme. Is it transitive or intransitive?
				if (has4thAttribute) {
					transOrPosess = "transitive";
				} else {
					transOrPosess = "intransitive";
				}
			}
		}
		return transOrPosess;
	}

	private static String caseFor(String[] attributes) throws MorphemeException {
		String caseDescr = null;
		String caseAbbr = null;
		String firstAttr = attributes[0];
		if (firstAttr.startsWith("t")) {
			caseAbbr = attributes[1];
		}
		if (caseAbbr != null) {
			caseDescr = caseAbbrevs.get(caseAbbr);
		}

		return caseDescr;
	}

	private static String personAndNumber4abbrev(String abbrev) {
		String persNum = null;

		String pers = null;
		if (abbrev.startsWith("1")) {
			pers = "1st";
		} else if (abbrev.startsWith("2")) {
			pers = "2nd";
		} else if (abbrev.startsWith("3")) {
			pers = "3rd";
		} else if (abbrev.startsWith("4")) {
			pers = "4th";
		}
		if (pers != null) {
			pers += " person";
		}

		String num = null;
		if (abbrev.contains("s")) {
			num = "singular";
		} else if (abbrev.contains("p")) {
			num = "plural";
		}

		if (num != null || pers != null) {
			persNum = "";
			if (pers != null) {
				persNum += pers;
			}
			if (num != null) {
				if (persNum.length() > 0) {
					persNum += " ";
				}
				persNum += num;
			}
		}

		return persNum;
	}

	private static String number4abbrev(String abbrev) {
		String number = null;
		if (abbrev.equals("s")) {
			number = "singular";
		} else if (abbrev.equals("p")) {
			number = "plural";
		}
		return number;
	}

	private static String caseName4caseAbbrev(String abbrev) {
		String name = null;
		if (abbrev.equals("loc")) {
			name = "locative";
		}
		return name;
	}

	private static String partOfSpeech(String[] attributes) {
		String posDescr = "";
		String firstAttr = attributes[0];
		if (firstAttr.startsWith("tn")) {
			posDescr = "noun";
		} else if (firstAttr.equals("tv")) {
			posDescr = "verb";
		} else if (attributes.length == 1 && attributes[0].length() < 3) {
			for (int ii=0; ii < firstAttr.length(); ii++) {
				if (ii > 0) {
					posDescr += "-to-";
				}
				char roleChar = firstAttr.charAt(ii);
				posDescr += partofSpeechName(roleChar);
			}
		}
		return posDescr;
	}

	private static String position(String[] attributes) {
		String location = null;
		String firstAttr = attributes[0];
		if (firstAttr.length() == 1) {
			if (firstAttr.matches("[nv]")) {
				location = "root";
			}
		} else if (firstAttr.startsWith("s")) {
			location = "suffix";
		} else if (firstAttr.startsWith("t")) {
			location = "ending";
		} else if (firstAttr.length() == 2) {
			location = "suffix";
		}

		return location;
	}

	private static String partofSpeechName(char roleChar) {
		String posName = new String(new char[] {roleChar});
		if (roleChar == 'n') {
			posName = "noun";
		} else if (roleChar == 'v') {
			posName = "verb";
		} else if (roleChar == 'p') {
			posName = "pronoun";
		} else if (roleChar == 'c') {
			posName = "conjunction";
		} else if (roleChar == 'a') {
			posName = "adverb";
		} else if (roleChar == 'e') {
			posName = "expression/disclaimer";
		} else if (roleChar == 'q') {
			posName = "tail element";
		}
		return posName;
	}

	@Override
	public int compareTo(@NotNull MorphemeHumanReadableDescr o) {
		return this.id.compareTo(o.id);
	}
}
