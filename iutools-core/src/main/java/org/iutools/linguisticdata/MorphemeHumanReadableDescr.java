package org.iutools.linguisticdata;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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

	private static Map<String,String> typesOfMorphemes = new HashMap<String,String>();
	static {
		// roots
		typesOfMorphemes.put("n", "noun root");
		typesOfMorphemes.put("v", "verb root");
		typesOfMorphemes.put("a", "adverb");
		typesOfMorphemes.put("c", "conjunction");
		typesOfMorphemes.put("e", "exclamation/disclaimer");
		typesOfMorphemes.put("q", "tail suffix");
		typesOfMorphemes.put("nn", "noun-to-noun suffix");
		typesOfMorphemes.put("nv", "noun-to-verb suffix");
		typesOfMorphemes.put("vn", "verb-to-noun suffix");
		typesOfMorphemes.put("vv", "verb-to-verb suffix");
		typesOfMorphemes.put("q", "tail suffix");
		typesOfMorphemes.put("pr", "pronoun");
		typesOfMorphemes.put("p", "pronoun");
		typesOfMorphemes.put("rpr", "pronoun root");
		typesOfMorphemes.put("rp", "pronoun root");
		typesOfMorphemes.put("ad", "demonstrative adverb");
		typesOfMorphemes.put("rad", "demonstrative adverb root");
		typesOfMorphemes.put("tad", "demonstrative adverb ending");
		typesOfMorphemes.put("pd", "demonstrative pronoun");
		typesOfMorphemes.put("rpd", "demonstrative pronoun root");
		typesOfMorphemes.put("tpd", "demonstrative prnoun ending");
		typesOfMorphemes.put("tn", "noun ending");
		typesOfMorphemes.put("tv", "verb ending");
	}

	private static Map<String,String> caseMoodAbbrevs = new HashMap<String,String>();
	static {
		// noun cases
		caseMoodAbbrevs.put("nom", "nominative");
		caseMoodAbbrevs.put("gen", "genitive");
		caseMoodAbbrevs.put("loc", "locative");
		caseMoodAbbrevs.put("acc", "accusative");
		caseMoodAbbrevs.put("abl", "ablative");
		caseMoodAbbrevs.put("dat", "dative");
		caseMoodAbbrevs.put("sim", "similaris");
		caseMoodAbbrevs.put("via", "vialis");
		// verb moods
		caseMoodAbbrevs.put("dec", "declarative");
		caseMoodAbbrevs.put("caus", "causative");
		caseMoodAbbrevs.put("part", "participial");
		caseMoodAbbrevs.put("int", "interrogative");
		caseMoodAbbrevs.put("imp", "imperative");
		caseMoodAbbrevs.put("dub", "dubitative");
		caseMoodAbbrevs.put("freq", "frequentative");
		caseMoodAbbrevs.put("ger", "gerundive");
		caseMoodAbbrevs.put("cond", "conditional");
		// demonstrative properties
		caseMoodAbbrevs.put("sc", "static/short referent");
		caseMoodAbbrevs.put("ml", "moving/long referent");
		caseMoodAbbrevs.put("mlsc", "referent of either moving/long OR static/short nature");
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
		String morphID, String _meaning) throws MorphemeException {
		if (_meaning == null) {
			try {
				_meaning = Morpheme.getMorpheme(morphID).englishMeaning;
			} catch (LinguisticDataException e) {
				throw new MorphemeException(e);
			}
		}
		this.meaning = _meaning;
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

			String transitivityOrPossessivity = transitivityOrPossessivity(attributes);
			String partOfSpeechDescr = partOfSpeech(attributes);
//			String positionDescr = position(attributes);
			String caseOrMoodDescr = caseOrMoodFor(attributes);
			String primaryPersAndNumber = primaryPersonAndNumber(attributes);
			String secondaryPersAndNumber = secondaryPersonAndNumber(attributes);

			gramm = "";
			gramm = expandDescr(gramm, transitivityOrPossessivity);
			gramm = expandDescr(gramm, partOfSpeechDescr);
//			gramm = expandDescr(gramm, positionDescr);
			if (caseOrMoodDescr != null || primaryPersAndNumber != null) {
				gramm += ";";
			}
			gramm = expandDescr(gramm, caseOrMoodDescr);
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
				persNum += " possessor";
			} else if (firstAttr.equals("tv")) {
				persNum += " object";
			}
		}

		return persNum;
	}

	private static String primaryPersonAndNumber(String[] attributes) {
		String persNum = null;
		String firstAttr = attributes[0];
		if (Arrays.stream(new String[]{"tn", "tv", "ad", "rad", "pd", "rpd"}).anyMatch(firstAttr::equals)) { //(firstAttr.startsWith("t")) {
			if (attributes.length > 2) {
				persNum = personAndNumber4abbrev(attributes[2]);
			}
		}
		return persNum;
	}

	private static String transitivityOrPossessivity(String[] attributes) {
		String transOrPossess = null;
		String firstAttr = attributes[0];
		if (firstAttr.startsWith("t")) {
			boolean has4thAttribute = (attributes.length > 3);
			if (firstAttr.equals("tn")) {
				// This is a noun morpheme. Is it possessive or not?
				if (has4thAttribute) {
					transOrPossess = "possessive";
				}
			} else if (firstAttr.equals("tv")) {
				// This is a verb morpheme. Is it transitive or intransitive?
				String secondAttr = attributes[1];
				if (has4thAttribute) {
					transOrPossess = "transitive";
				} else {
					transOrPossess = "intransitive";
				}
			}
		}
		return transOrPossess;
	}

	private static String caseOrMoodFor(String[] attributes) throws MorphemeException {
		String caseOrMoodDescr = null;
		String caseAbbr = null;
		String firstAttr = attributes[0];
		if (Arrays.stream(new String[]{"tn", "tv", "ad", "rad", "pd", "rpd"}).anyMatch(firstAttr::equals)) { //(firstAttr.startsWith("t")) {
			caseAbbr = attributes[1];
			if (caseAbbr.equals("?")) {
				caseAbbr = null;
			}
		}
		if (caseAbbr != null) {
			caseOrMoodDescr = caseMoodAbbrevs.get(caseAbbr);
			if (caseOrMoodDescr.equals("participial")) {
				String lastAttribute = attributes[attributes.length-1];
				if (lastAttribute.equals("fut")) {
					caseOrMoodDescr = "future " + caseOrMoodDescr;
				} else if (lastAttribute.equals("prespas")) {
					caseOrMoodDescr = "past/present " + caseOrMoodDescr;
				}
			}
		}

		return caseOrMoodDescr;
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
		} else if (abbrev.contains("d")) {
			num = "dual";
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
		} else if (abbrev.equals("d")) {
			number = "dual";
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
		String firstAttr = attributes[0];
		String posDescr = typesOfMorphemes.get(firstAttr);
		return posDescr;
	}


//	private static String partOfSpeech(String[] attributes) {
//		String posDescr = "";
//		String firstAttr = attributes[0];
//		if (firstAttr.equals("tn")) {
//			posDescr = "noun";
//		} else if (firstAttr.equals("tv")) {
//			posDescr = "verb";
//		} else if (Arrays.stream(new String[]{"nn", "nv", "vn", "vv"}).anyMatch(firstAttr::equals)) { //(attributes.length == 1 && attributes[0].length() < 3) {
//			for (int ii=0; ii < firstAttr.length(); ii++) {
//				if (ii > 0) {
//					posDescr += "-to-";
//				}
//				char roleChar = firstAttr.charAt(ii);
//				posDescr += partofSpeechName(roleChar);
//			}
//		}
//		return posDescr;
//	}

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

	public void ensureScript(TransCoder.Script inScript) throws MorphemeException {
		try {
			canonicalForm = TransCoder.ensureScript(inScript, canonicalForm);
		} catch (TransCoderException e) {
			throw new MorphemeException(e);
		}

	}
}
