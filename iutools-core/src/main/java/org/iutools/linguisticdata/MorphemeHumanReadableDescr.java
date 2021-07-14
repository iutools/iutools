package org.iutools.linguisticdata;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Given a Morpheme ID, this class generates a human-readable description of
 * what that morpheme is about.
 */
public class MorphemeHumanReadableDescr {

	private static Map<String,String> caseAbbrevs = new HashMap<String,String>();
	static {
		caseAbbrevs.put("loc", "locative");
		caseAbbrevs.put("dec", "declarative");
		caseAbbrevs.put("caus", "causative");
	}


	private static Pattern pattSeparateNameFromRoles =
		Pattern.compile("^([^/]*)/\\d?(.*)$");


	public static String humanReadableDescription(String morphID) {
		String descr = morphID;
		Matcher matcher = pattSeparateNameFromRoles.matcher(morphID);

		if (matcher.matches()) {
			String morphName = matcher.group(1);
			String attributesString = matcher.group(2);

			String transitivityOrPosessivity = transitivityOrPosessivity(attributesString);
			String partOfSpeechDescr = partOfSpeech(attributesString);
			String positionDescr = position(attributesString);
			String caseDescr = caseFor(attributesString);
			String primaryPersAndNumber = primaryPersonAndNumber(attributesString);
			String secondaryPersAndNumber = secondaryPersonAndNumber(attributesString);

			String attrsDescr = "";
			attrsDescr = expandDescr(attrsDescr, transitivityOrPosessivity);
			attrsDescr = expandDescr(attrsDescr, partOfSpeechDescr);
			attrsDescr = expandDescr(attrsDescr, positionDescr);
			if (caseDescr != null || primaryPersAndNumber != null) {
				attrsDescr += ";";
			}
			attrsDescr = expandDescr(attrsDescr, caseDescr);
			attrsDescr = expandDescr(attrsDescr, primaryPersAndNumber);
			if (secondaryPersAndNumber != null) {
				attrsDescr += ";";
			}
			attrsDescr = expandDescr(attrsDescr, secondaryPersAndNumber);

			descr = morphName+" ("+attrsDescr+")";
		}

		return descr;
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

	private static String secondaryPersonAndNumber(String attributesString) {
		String persNum = null;
		if (attributesString.startsWith("t")) {
			String[] attributes = attributesString.split("-");
			if (attributes.length > 3) {
				persNum = personAndNumber4abbrev(attributes[3]);
			}
		}
		if (persNum != null) {
			if (attributesString.startsWith("tn")) {
				persNum += " posessor";
			} else if (attributesString.startsWith("tv")) {
				persNum += " object";
			}
		}
		return persNum;
	}

	private static String primaryPersonAndNumber(String attributesString) {
		String persNum = null;
		if (attributesString.startsWith("t")) {
			String[] attributes = attributesString.split("-");
			if (attributes.length > 2) {
				persNum = personAndNumber4abbrev(attributes[2]);
			}
		}
		return persNum;
	}


	private static String transitivityOrPosessivity(String attributesString) {
		String transOrPosess = null;
		if (attributesString.startsWith("t")) {
			boolean has4thAttribute = (attributesString.split("-").length == 4);
			if (attributesString.startsWith("tn")) {
				// This is a noun morpheme. Is it posessive or not?
				if (has4thAttribute) {
					transOrPosess = "posessive";
				}
			} else if (attributesString.startsWith("tv")) {
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

	private static String caseFor(String attributesString) {
		String caseDescr = null;
		String caseAbbr = null;
		if (attributesString.startsWith("t")) {
			caseAbbr = attributesString.split("-")[1];
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

	private static String partOfSpeech(String attributesString) {
		String posDescr = "";
		if (attributesString.startsWith("tn")) {
			posDescr = "noun";
		} else if (attributesString.startsWith("tv")) {
			posDescr = "verb";
		} else if (!attributesString.contains("-") && attributesString.length() < 3) {
			for (int ii=0; ii < attributesString.length(); ii++) {
				if (ii > 0) {
					posDescr += "-to-";
				}
				char roleChar = attributesString.charAt(ii);
				posDescr += partofSpeechName(roleChar);
			}
		}
		return posDescr;
	}

	private static String position(String attributesString) {
		String location = null;
		if (attributesString.length() == 1) {
			if (attributesString.matches("[nv]")) {
				location = "root";
			}
		} else if (attributesString.length() == 2) {
			location = "suffix";
		} else if (attributesString.startsWith("s")) {
			location = " suffix";
		} else if (attributesString.startsWith("t")) {
			location = "ending";
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
}
