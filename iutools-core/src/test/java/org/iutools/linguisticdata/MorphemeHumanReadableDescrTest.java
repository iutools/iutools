package org.iutools.linguisticdata;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import static ca.nrc.testing.RunOnCases.Case;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;

public class MorphemeHumanReadableDescrTest {

	///////////////////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////////////////

	@Test
	public void test__MorphemeHumanReadableDescr__Synopsis() throws Exception {
		// Use this class to generate a human-readable description of
		// what a morpheme is about.
		//

		// Given a morpheme ID...
		String morphID = "umiaq/1n";

		// Create the description
		MorphemeHumanReadableDescr descr = new MorphemeHumanReadableDescr(morphID);

		// You can then get human-readable description of the meaning of
		// various parts specified by the ID

		// The "canonical" form. This is a bit analoguous to a word stem in
		// English.
		String canonicalForm = descr.canonicalForm;

		// Human-readable explanation about the grammatical role and constraints
		// for that morpheme
		String grammar = descr.grammar;

		// The ID that the explanation was generated from
		String id = descr.id;
	}


	///////////////////////////////////////
	// VERIFICATION TESTS
	///////////////////////////////////////

	@Test
	public void test__descriptiveText__HappyPath() throws Exception {
		String id = "umiaq/1n";
		String expDescr = "umiaq (noun root)";
		String gotDescr = MorphemeHumanReadableDescr.descriptiveText(id);
		AssertString.assertStringEquals(
				"Bad description for morpheme id "+id, expDescr, gotDescr);
	}

	@Test
	public void test__MorphemeHumanReadableDescr__SeveralCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("umiaq/1n", "umiaq/1n",  "umiaq",
				"noun root",
				"boat"),
			new Case("pisuk/1v", "pisuk/1v", "pisuk",
				"verb root",
				"to walk (trans.: on s.t.)"),
			new Case("amma/1c", "amma/1c", "amma",
				"conjunction",
				"and"),
			new Case("aakka/1a", "aakka/1a", "aakka",
				"adverb",
				"no (aakka: in N. Baffin and WCHB)"),
			new Case("aamai/1e", "aamai/1e", "aamai",
				"exclamation/disclaimer",
				"disclaimer: 'I don't know' ; 'I don't want to say' (N. Baffin, WCHB)"),
			new Case("uvanga/1p", "uvanga/1p", "uvanga",
				"pronoun",
				"I; me; mine"),

			new Case("quti/1nn", "quti/1nn", "quti",
				"noun-to-noun suffix",
				"own; s.t. that belongs to s.o."),
			new Case("liuq/1nv", "liuq/1nv", "liuq",
				"noun-to-verb suffix",
				"construction in progress: 'to be building s.t.' (trans.: for s.o.)"),
			new Case("ji/1vn", "ji/1vn", "ji",
				"verb-to-noun suffix",
				"one whose job is; agent"),
			new Case("nasuk/1vv", "nasuk/1vv", "nasuk",
				"verb-to-verb suffix",
				"endeavouring, striving: 'to be trying'"),
			new Case("lu/1q", "lu/1q", "lu",
				"tail suffix",
				"and"),

			new Case("mi/tn-loc-s", "mi/tn-loc-s", "mi",
				"noun ending; locative singular",
				"locative: in; on; upon a; the (one)"),
			new Case("mni/tn-loc-s-1s", "mni/tn-loc-s-1s", "mni",
				"possessive noun ending; locative singular; 1st person singular possessor",
				"locative: in; on; upon my (one thing) "),

			new Case("vugut/tv-dec-1p", "vugut/tv-dec-1p", "vugut",
				"intransitive verb ending; declarative 1st person plural",
				"declaration: we (many) ..."),
			new Case("gapku/tv-caus-1s-3s", "gapku/tv-caus-1s-3s", "gapku",
				"transitive verb ending; causative 1st person singular; 3rd person singular object",
				"becausative: because I ...him/her/it"),
			new Case("lunikku/tv-part-3d-3s-fut", "lunikku/tv-part-3d-3s-fut", "lunikku",
				"transitive verb ending; future participial 3rd person dual; 3rd person singular object",
				"part. future: while they (two) ...him/her/it"),

			new Case("taava/ad-ml", "taava/ad-ml", "taava",
				"demonstrative adverb; moving/long referent",
				"over there not beside me"),
			new Case("qaksu/rpd-mlsc-s", "qaksu/rpd-mlsc-s", "qaksu",
				"demonstrative pronoun root; referent of either moving/long OR static/short nature singular",
				"the one inside"),
			new Case("tagg/rad-sc", "tagg/rad-sc", "tagg",
				"demonstrative adverb root; static/short referent",
				"right here beside me"),
			new Case("taaksu/rpd-ml-s", "taaksu/rpd-ml-s", "taaksu",
				"demonstrative pronoun root; moving/long referent singular",
				"that one over there"),
		};

		Consumer<Case> runner = (aCase) -> {
			try {
				Set<String> ignoreFields = new HashSet<String>();
				ignoreFields.add("definition");
				String id = (String) aCase.data[0];
				String expCanonical = (String) aCase.data[1];
				String expGrammar = (String) aCase.data[2];
				String expMeaning = (String) aCase.data[3];
				MorphemeHumanReadableDescr gotDescr = new MorphemeHumanReadableDescr(id);
				Map<String, String> expDescr = new HashMap<String, String>();
				{
					expDescr.put("id", id);
					expDescr.put("canonicalForm", expCanonical);
					expDescr.put("grammar", expGrammar);
					expDescr.put("meaning", expMeaning);
				}
				AssertObject.assertDeepEquals(
						"Bad description for morpheme id " + id,
						expDescr, gotDescr, ignoreFields, 1);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(cases, runner)
//			.onlyCaseNums(18)
			.run();
	}

	@Test
	public void test__humanReadableDescr__RunOnAllMorphemesInDB() throws Exception {
		Map<String,List<String>> allDescriptions = new HashMap<String, List<String>>();
		String exceptions = "";
		for (String morphID: LinguisticData.getInstance().allMorphemeIDs()) {
			if (!morphID.startsWith("taqqapku/")) {
				continue;
			}
			try {
				String descr = MorphemeHumanReadableDescr.descriptiveText(morphID);
				String[] parts = descr.split("\\(");
				String grammDescr = "("+parts[1];
				if (!allDescriptions.containsKey(grammDescr)) {
					allDescriptions.put(grammDescr, new ArrayList<String>());
				}
				List<String> examples = allDescriptions.get(grammDescr);
				if (examples.size() < 10) {
					examples.add(morphID);
				}
				allDescriptions.put(grammDescr, examples);
			} catch (Exception e) {
				exceptions += "\nException raised for id: "+morphID+":\n"+e.getMessage();
			}
		}

		if (!exceptions.isEmpty()) {
			Assertions.fail(exceptions);
		}

		String[] expDescriptions = new String[] {
			"(possessive noun ending; locative plural; 2nd person possessor)",
			"(intransitive verb ending; 2nd person; 1st person singular object)",
			"(verb-to-noun suffix)",
		};
		String mess = "";
		for (String descr: allDescriptions.keySet()) {
			if (!Arrays.asList(expDescriptions).contains(descr)) {
				mess += "\n"+descr+":\n"+ PrettyPrinter.print(allDescriptions.get(descr));
			}
		}
//		if (!mess.isEmpty()) {
//			Assertions.fail(
//				"There were some unexpected morpheme descriptions.\n"+
//				"They are listed below with examples of morpheme ids that yieledd them.\n"+
//				mess);
//		}
//		AssertObject.assertDeepEquals(
//			"Bag of descriptions was not as expected", expDescriptions, allDescriptions);
	}

	@Test
	public void test__ListAllAttributeValues() throws Exception {
		final int MAX_POS = 10;

		boolean printValues = false;

		// Initialise the list of possible values for each position
		// in the ID (maximum of 10 positions)
		List<Set<String>> attrPossibleValues = new ArrayList<Set<String>>();
		for (int ii=0; ii < MAX_POS; ii++) {
			attrPossibleValues.add(new HashSet<String>());
		}

		// Fill the list with all values found in the morpheme database
		for (String morphID : LinguisticData.getInstance().allMorphemeIDs()) {
			Pair<String,String[]> splitID = MorphemeHumanReadableDescr.splitMorphID(morphID);
			String[] attrs = splitID.getRight();
			for (int ii=0; ii < attrs.length; ii++) {
				attrPossibleValues.get(ii).add(attrs[ii]);
			}
		}


		// Print the values for each position and find actual length
		if (printValues) {
			System.out.println("Possible values of morpheme ID attributes:");
		}
		int actualLength = 0;
		for (int ii=0; ii < MAX_POS; ii++) {
			if (attrPossibleValues.get(ii).isEmpty()) {
				actualLength = ii+1;
				break;
			}
			if (printValues) {
				System.out.println("\nAt position " + ii+1);
				for (String aValue : attrPossibleValues.get(ii)) {
					System.out.println("  " + aValue);
				}
			}
		}

		// Truncate the list
		attrPossibleValues = attrPossibleValues.subList(0, actualLength-1);

		String[][] expValues = new String[][] {
			// Position 1
			new String[] {
				"a",
				"ad",
				"c",
				"e",
				"n",
				"nn",
				"nv",
				"p",
				"pd",
				"pr",
				"q",
				"rad",
				"rp",
				"rpd",
				"rpr",
				"tad",
				"tn",
				"tpd",
				"tv",
				"v",
				"vn",
				"vv"
			},
			// Position 2
			new String[] {
				"abl",
				"acc",
				"caus",
				"cond",
				"dat",
				"dec",
				"dub",
				"freq",
				"gen",
				"ger",
				"imp",
				"int",
				"loc",
				"ml",
				"mlsc",
				"nom",
				"part",
				"sc",
				"sim",
				"via"
			},
			// Position 3
			new String[] {
				"1d",
				"1p",
				"1s",
				"2d",
				"2p",
				"2s",
				"3d",
				"3p",
				"3s",
				"4d",
				"4p",
				"4s",
				"d",
				"p",
				"s"
			},
			// Position 4
			new String[] {
				"1d",
				"1p",
				"1s",
				"2d",
				"2p",
				"2s",
				"3d",
				"3p",
				"3s",
				"4d",
				"4p",
				"4s",
				"fut",
				"prespas"
			},
			// Position 5
			new String[] {
				"fut",
				"prespas"
			}
		};
		AssertObject.assertDeepEquals(
			"List of possible values for different positions of the morpheme IDs were not as expected",
				expValues, attrPossibleValues);
	}

}
