package org.iutools.worddict;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertSet;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Set;

public class SynsDictTest {

	///////////////////////////////////////
	// DOCUMENTATION TEST
	///////////////////////////////////////

	@Test
	public void test__Synset__Synopsis() throws Exception {
		// Use a SynsDict to capture synonymimity relations between words
		SynsDict dict = new SynsDict("en");

		// This is how you add sets of synonyms
		dict.addSynset("hello", "hi", "greetings");
		dict.addSynset("goodbye", "bye");

		// Given a word, you can get a list of synonyms for it
		Set<String> syns = dict.synonymsFor("hello");
		AssertSet.assertEquals(
			"List of synonyms was wrong for hello",
			new String[] {"hi", "greetings"}, syns
		);
	}
}
