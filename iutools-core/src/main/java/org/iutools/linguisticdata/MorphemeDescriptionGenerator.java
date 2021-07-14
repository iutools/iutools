package org.iutools.linguisticdata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Given a Morpheme ID, this class generates a human-readable description of
 * what that morpheme is about.
 */
public class MorphemeDescriptionGenerator {

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

			String attrsDescr =
				transitivityOrPosessivity + partOfSpeechDescr + positionDescr;
			if (caseDescr != null) {
				attrsDescr += "; " + caseDescr;
			}
			if (primaryPersAndNumber != null) {
				attrsDescr += "; " + primaryPersAndNumber;
			}
			if (secondaryPersAndNumber != null) {
				attrsDescr += "; " + secondaryPersAndNumber;
			}

			descr = morphName+" ("+attrsDescr+")";
		}

		return descr;
	}

	private static String secondaryPersonAndNumber(String attributesString) {
		String persNum = null;
		return persNum;
	}

	private static String primaryPersonAndNumber(String attributesString) {
		String persNum = null;
		return persNum;
	}

	private static String transitivityOrPosessivity(String attributesString) {
		String transitivity = "";
		return transitivity;
	}

	private static String caseFor(String attributesString) {
		String caseDescr = null;
		return caseDescr;
	}

	private static String personAndNumber4abbrev(String abbrev) {
		String persNum = null;
		if (abbrev.length() > 1) {
			char persChar = abbrev.charAt(0);
			if (persChar == '1') {
				persNum = "1st";
			} else if (persChar == '2') {
				persNum = "2nd";
			} else if (persChar == '3') {
				persNum = "3rd";
			} else if (persChar == '4') {
				persNum = "4th";
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
		String location = "";
		if (attributesString.length() == 1) {
			if (attributesString.matches("[nv]")) {
				location = " root";
			}
		} else if (attributesString.length() == 2) {
			location = " suffix";
		} else if (attributesString.startsWith("s")) {
			location = " suffix";
		} else if (attributesString.startsWith("t")) {
			location = " ending";
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
