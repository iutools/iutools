package org.iutools.concordancer.tm;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.concordancer.SentencePair;
import org.iutools.config.IUConfig;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
//import org.junit.Before;
//import org.junit.Test;

public class WordSpotterTest {

	@BeforeEach
	public void setUp() throws Exception {
		System.out.println("setUp invoked");
		assumeWordSpotterIsEnabled();
	}

	//////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////

	@Test
	public void test__WordSpotter__Synopsis() {
		// Use WordSpotter to spot a word and its equivalent in a list of
		// aligned sentences.
		//
		// For example, say you have a list of aligned sentences
		//
		SentencePair[] sentenceAlignments = {
			new SentencePair(
				"en", "Premier Joe Savikataaq was selected by his colleagues in the 5th Legislative Ass embly of Nunavut to lead Nunavut on June 14, 2018.",
				"iu", "ᓯᕗᓕᖅᑎ ᔫ ᓴᕕᑲᑖᖅ ᓂᕈᐊᖅᑕᐅᓚᐅᖕᒪᑦ ᒪᓕᒐᓕᐅᖅᑎᐅᖃᑎᖏᓐᓄᑦ ᑕᓪᓕᒪᒋᓕᖅᑕᖓᓐᓂᑦ ᒪᓕᕆᓕᐅᕐᕕᖕᒥᑦ ᓄᓇᕗᒻᒥᑦ ᓯᕗᓕᖅᑎᐅᓂᐊ ᓕᖅᖢᓂ ᓄᓇᕗᒻᒧᑦ ᔫᓐ 14, 2018−ᖑᑎᓪᓗᒍ."
			),

			new SentencePair(
				"en", "Prior to that, Premier Savikataaq was first elected in the general election held on October 28, 2013, to represent the constituency of Arviat South in the 4th L egislative Assembly of Nunavut.",
				"iu", "ᓯᕗᙵᓂᑦ, ᓯᕗᓕᖅᑎ ᓴᕕᑲᑖᖅ ᓯᕗᓪᓕᖅᐹᒥᑦ ᓂᕈᐊᖅᑕᐅᓚᐅᖅᓯᒪᔪᖅ ᓂᕈᐊᖕᓇᐅᑎᓪᓗᒍ ᐅᑐᐱᕆ 28, 2013−ᒥᑦ, ᑭᒡᒐᖅᑐᐃᓂᐊᖅ ᖢᓂ ᐊᕐᕕᐊᑦ ᓂᒋᐊᓂᑦ ᑎᓴᒪᒋᓕᖅᑕᖓᓐᓂᑦ ᒪᓕᒐᓕᐅᕐᕕᖕᒥᑦ ᓄᓇᕗᒻᒥᑦ"
			)
		};

		// You want to spot the iu equivalent of en word "Premier" in these sentences
		//
		String enWord = "premier";
		WordSpotter spotter = new WordSpotter();
		WordSpotting[] spottings = spotter.spot("en", enWord, sentenceAlignments);

		// For each aligned sentence, the spottings tell you where to find the
		// English word and its equivalent (if they were present)
		//
		for (WordSpotting aSpotting: spottings) {
			// The aligned sentence in which the words were spotted
			SentencePair sentencePair = aSpotting.sentencePair();

			// The offsets of the english word and its equivalent
			List<Pair<Integer,Integer>> enOffsets = aSpotting.offsets("en");
			List<Pair<Integer,Integer>> frOffsets = aSpotting.offsets("fr");

			// The actual occurences of the english word and its equivalent in iu
			List<String> enOccurences = aSpotting.occurences("en");
			List<String> iuOccurences = aSpotting.occurences("iu");
		}
	}

	//////////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////////

	@Test
	public void test__WordSpotter__HappyPath() {
		SentencePair[] sentenceAlignments = {
			new SentencePair(
				"en", "Premier Joe Savikataaq was selected by his colleagues in the 5th Legislative Ass embly of Nunavut to lead Nunavut on June 14, 2018.",
				"iu", "ᓯᕗᓕᖅᑎ ᔫ ᓴᕕᑲᑖᖅ ᓂᕈᐊᖅᑕᐅᓚᐅᖕᒪᑦ ᒪᓕᒐᓕᐅᖅᑎᐅᖃᑎᖏᓐᓄᑦ ᑕᓪᓕᒪᒋᓕᖅᑕᖓᓐᓂᑦ ᒪᓕᕆᓕᐅᕐᕕᖕᒥᑦ ᓄᓇᕗᒻᒥᑦ ᓯᕗᓕᖅᑎᐅᓂᐊ ᓕᖅᖢᓂ ᓄᓇᕗᒻᒧᑦ ᔫᓐ 14, 2018−ᖑᑎᓪᓗᒍ."
			),

			new SentencePair(
				"en", "Prior to that, Premier Savikataaq was first elected in the general election held on October 28, 2013, to represent the constituency of Arviat South in the 4th L egislative Assembly of Nunavut.",
				"iu", "ᓯᕗᙵᓂᑦ, ᓯᕗᓕᖅᑎ ᓴᕕᑲᑖᖅ ᓯᕗᓪᓕᖅᐹᒥᑦ ᓂᕈᐊᖅᑕᐅᓚᐅᖅᓯᒪᔪᖅ ᓂᕈᐊᖕᓇᐅᑎᓪᓗᒍ ᐅᑐᐱᕆ 28, 2013−ᒥᑦ, ᑭᒡᒐᖅᑐᐃᓂᐊᖅ ᖢᓂ ᐊᕐᕕᐊᑦ ᓂᒋᐊᓂᑦ ᑎᓴᒪᒋᓕᖅᑕᖓᓐᓂᑦ ᒪᓕᒐᓕᐅᕐᕕᖕᒥᑦ ᓄᓇᕗᒻᒥᑦ"
			)
		};

		String enWord = "premier";
		WordSpotter spotter = new WordSpotter();
		WordSpotting[] spottings = spotter.spot("en", enWord, sentenceAlignments);
//		new AssertWordSpottings(spottings, "")
//			.wasSpottedInAllSentences();

		for (WordSpotting aSpotting: spottings) {
			// The aligned sentence in which the words were spotted
			SentencePair sentencePair = aSpotting.sentencePair();

			// The offsets of the english word and its equivalent
			List<Pair<Integer,Integer>> enOffsets = aSpotting.offsets("en");
			List<Pair<Integer,Integer>> frOffsets = aSpotting.offsets("fr");

			// The actual occurences of the english word and its equivalent in iu
			List<String> enOccurences = aSpotting.occurences("en");
			List<String> iuOccurences = aSpotting.occurences("iu");
		}

	}


	//////////////////////////////////
	// TEST HELPERS
	//////////////////////////////////

	protected static void assumeWordSpotterIsEnabled() throws Exception {
		Boolean enabled = IUConfig.wordSpottingEnabled();
		Assume.assumeTrue(
			"Word Spotting is diabled, therefore that functionality will not be tested.\n"+
			"If you want to test this funcitonaly, define config property "+
			IUConfig.propName_wordSpotting+"=true.",
			enabled); ;
	}

}
