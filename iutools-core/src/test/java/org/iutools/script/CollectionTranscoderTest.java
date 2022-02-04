package org.iutools.script;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;

import org.iutools.script.TransCoder.Script;

public class CollectionTranscoderTest {

	////////////////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////////////////

	@Test
	public void test__CollectionTranscoder__Synopsis() throws TransCoderException {
		// Use this class to transcode strings in a collection.
		//
		// For example, say you have a map whose keys are inuktitut text
		Map<String,String> iuMap = new HashMap<String,String>();
		iuMap.put("ᓄᓇᕗᑦ", "nunavut");
		iuMap.put("ᐃᓄᒃᓱᒃ", "inuksuk");

		// You can convert the script of the keys and values as follows
		HashMap<String,Object> blah;
		CollectionTranscoder.transcodeKeys(Script.ROMAN, iuMap);
		CollectionTranscoder.transcodeStrValues(Script.SYLLABIC, iuMap);
	}

	////////////////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////////////////

	@Test
	public void test__transcodeKeys__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("syll2roman",
				Script.ROMAN,
				new Pair[] {
					Pair.of("ᓄᓇᕗᑦ", "nunavut"), Pair.of("ᐃᓄᒃᓱᒃ", "inuksuk")
				},
				new Pair[] {
					Pair.of("nunavut", "nunavut"), Pair.of("inuksuk", "inuksuk")
				}),
			new Case("syll2syll",
				Script.SYLLABIC,
				new Pair[] {
					Pair.of("ᓄᓇᕗᑦ", "nunavut"), Pair.of("ᐃᓄᒃᓱᒃ", "inuksuk")
				},
				new Pair[] {
					Pair.of("ᓄᓇᕗᑦ", "nunavut"), Pair.of("ᐃᓄᒃᓱᒃ", "inuksuk")
				}),
			new Case("roman2syll",
				Script.SYLLABIC,
				new Pair[] {
					Pair.of("nunavut", "nunavut"), Pair.of("inuksuk", "inuksuk")
				},
				new Pair[] {
					Pair.of("ᓄᓇᕗᑦ", "nunavut"), Pair.of("ᐃᓄᒃᓱᒃ", "inuksuk")
				}),
			new Case("roman2syll",
				Script.ROMAN,
				new Pair[] {
					Pair.of("nunavut", "nunavut"), Pair.of("inuksuk", "inuksuk")
				},
				new Pair[] {
					Pair.of("nunavut", "nunavut"), Pair.of("inuksuk", "inuksuk")
				}),
		};

		Consumer<Case> runner = (aCase) -> {
			Script targetScript = (Script) aCase.data[0];
			Pair[] origEntries = (Pair[]) aCase.data[1];
			Pair[] expEntries = (Pair[]) aCase.data[2];

			Map<String,Object> mapToTranscode = new HashMap<String,Object>();
			for (Pair<String,String> anEntry: origEntries) {
				mapToTranscode.put(anEntry.getKey(), anEntry.getValue());
			}

			try {
				CollectionTranscoder.transcodeKeys(targetScript, mapToTranscode);

				Map<String,String> expMap = new HashMap<String,String>();
				for (Pair<String,String> anEntry: expEntries) {
					expMap.put(anEntry.getKey(), anEntry.getValue());
				}

				AssertObject.assertDeepEquals(
					"Keys not converted as expected",
				expMap, mapToTranscode
				);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			return;
		};

		new RunOnCases(cases, runner)
			.run();

	}


	@Test
	public void test__transcodeStrValues__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("syll2roman",
				Script.ROMAN,
				new Pair[] {
					Pair.of("nunavut", "ᓄᓇᕗᑦ"), Pair.of("inuksuk", "ᐃᓄᒃᓱᒃ")
				},
				new Pair[] {
					Pair.of("nunavut", "nunavut"), Pair.of("inuksuk", "inuksuk")
				}),
			new Case("syll2syll",
				Script.SYLLABIC,
				new Pair[] {
					Pair.of("nunavut", "ᓄᓇᕗᑦ"), Pair.of("inuksuk", "ᐃᓄᒃᓱᒃ")
				},
				new Pair[] {
					Pair.of("nunavut", "ᓄᓇᕗᑦ"), Pair.of("inuksuk", "ᐃᓄᒃᓱᒃ")
				}),
			new Case("roman2syll",
				Script.SYLLABIC,
				new Pair[] {
					Pair.of("nunavut", "nunavut"), Pair.of("inuksuk", "inuksuk")
				},
				new Pair[] {
					Pair.of("nunavut", "ᓄᓇᕗᑦ"), Pair.of("inuksuk", "ᐃᓄᒃᓱᒃ")
				}),
			new Case("roman2roman",
				Script.ROMAN,
				new Pair[] {
					Pair.of("nunavut", "nunavut"), Pair.of("inuksuk", "inuksuk")
				},
				new Pair[] {
					Pair.of("nunavut", "nunavut"), Pair.of("inuksuk", "inuksuk")
				}),
		};

		Consumer<Case> runner = (aCase) -> {
			Script targetScript = (Script) aCase.data[0];
			Pair[] origEntries = (Pair[]) aCase.data[1];
			Pair[] expEntries = (Pair[]) aCase.data[2];

			Map<String,String> mapToTranscode = new HashMap<String,String>();
			for (Pair<String,String> anEntry: origEntries) {
				mapToTranscode.put(anEntry.getKey(), anEntry.getValue());
			}

			try {
				CollectionTranscoder.transcodeStrValues(targetScript, mapToTranscode);

				Map<String,String> expMap = new HashMap<String,String>();
				for (Pair<String,String> anEntry: expEntries) {
					expMap.put(anEntry.getKey(), anEntry.getValue());
				}

				AssertObject.assertDeepEquals(
					"Keys not converted as expected",
				expMap, mapToTranscode
				);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			return;
		};

		new RunOnCases(cases, runner)
			.run();
	}

	@Test
	public void test__transcodeCollValues__VariousCases() throws Exception {
		Case[] cases = new Case[] {

			new Case("syll2roman",
				Script.ROMAN,
				new Pair[] {
					Pair.of("somekey", new String[] {"ᓄᓇᕗᑦ", "ᐃᓄᒃᓱᒃ"}),
				},
				new Pair[] {
					Pair.of("somekey", new String[] {"nunavut", "inuksuk"})
				}),
			new Case("syll2syll",
				Script.SYLLABIC,
				new Pair[] {
					Pair.of("somekey", new String[] {"ᓄᓇᕗᑦ", "ᐃᓄᒃᓱᒃ"}),
				},
				new Pair[] {
					Pair.of("somekey", new String[] {"ᓄᓇᕗᑦ", "ᐃᓄᒃᓱᒃ"})
				}),

			new Case("roman2syll",
				Script.SYLLABIC,
				new Pair[] {
					Pair.of("somekey", new String[] {"nunavut", "inuksuk"}),
				},
				new Pair[] {
					Pair.of("somekey", new String[] {"ᓄᓇᕗᑦ", "ᐃᓄᒃᓱᒃ"})
				}),

			new Case("roman2roman",
				Script.ROMAN,
				new Pair[] {
					Pair.of("somekey", new String[] {"nunavut", "inuksuk"}),
				},
				new Pair[] {
					Pair.of("somekey", new String[] {"nunavut", "inuksuk"})
				}),
		};

		Consumer<Case> runner = (aCase) -> {
			Script targetScript = (Script) aCase.data[0];
			Pair[] origEntries = (Pair[]) aCase.data[1];
			Pair[] expEntries = (Pair[]) aCase.data[2];

			Map<String, List<String>> mapToTranscode = new HashMap<String,List<String>>();
			for (Pair<String,String[]> anEntry: origEntries) {
				List<String> origValList = new ArrayList<String>();
				Collections.addAll(origValList, anEntry.getValue());
				mapToTranscode.put(anEntry.getKey(), origValList);
			}

			try {
				CollectionTranscoder.transcodeCollValues(targetScript, mapToTranscode);

				Map<String,List<String>> expMap = new HashMap<String,List<String>>();
				for (Pair<String,String[]> anEntry: expEntries) {
					List<String> expValList = new ArrayList<String>();
					Collections.addAll(expValList, anEntry.getValue());
					expMap.put(anEntry.getKey(), expValList);
				}

				AssertObject.assertDeepEquals(
					"Keys not converted as expected",
				expMap, mapToTranscode
				);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			return;
		};

		new RunOnCases(cases, runner)
			.run();

	}
}


