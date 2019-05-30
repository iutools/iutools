package ca.inuktitutcomputing.morph;

import java.io.IOException;

import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertHelpers;

public class GistTest {

	@Test
	public void test__Gist_Synopsis() throws IOException {
		String word = "iglumik";
		Gist gist = new Gist(word);
		Pair<String,String>[] components = gist.wordComponents;
		Pair<String,String>[] expected = new Pair[2];
		expected[0] = new Pair<String,String>("iglu","(1) house");
		expected[1] = new Pair<String,String>("mik","accusative: a; the (one)");
		AssertHelpers.assertDeepEquals("", expected, components);
	}

}
