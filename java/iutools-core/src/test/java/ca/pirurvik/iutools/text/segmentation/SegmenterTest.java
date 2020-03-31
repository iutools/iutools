package ca.pirurvik.iutools.text.segmentation;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertObject;
import ca.pirurvik.iutools.text.segmentation.Segmenter;

public class SegmenterTest {

	//////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////

	@Test
	public void test__Segmenter__Synopsis() {
		//
		// Use this class to segment text into sentences
		//
		
		// You can use a generic segmenter which does an OK job for 
		// all languages
		Segmenter segmenter = new Segmenter_Generic();
		
		// Or, you can ask for a segmenter that is specific for a language
		segmenter = Segmenter.makeSegmenter("iu");
		
		// You can use the above Inuktut specific segmenter to split 
		// text into sentences.
		//
		String text = "";
		List<String> sentences = segmenter.segment(text);
		
		// You can also ask to have the sentences be split into tokens
		//
		List<String[]> tokenizedSents = segmenter.segmentTokenized(text);
	}
	
	//////////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////////

	@Test
	public void test__segment__EnglishText() throws Exception {
		Segmenter segmenter = Segmenter.makeSegmenter("en");
		String text = "Hello world. Take me to your leader\nRight now!";
		List<String> gotSentences = segmenter.segment(text);
		String[] expSentences = new String[] {
			"Hello world. ", "Take me to your leader\n", "Right now!"
		};
		AssertObject.assertDeepEquals("Sentences not as expected", 
			expSentences, gotSentences);
	}

	@Test
	public void test__segment__InuktutText() throws Exception {
		Segmenter segmenter = Segmenter.makeSegmenter("iu");
		String text = 
			"ᔫ ᓴᕕᑲᑖᖅ\n" + 
			"ᓯᕗᓕᖅᑎ ᓄᓇᕗᒻᒥ\n" + 
			"ᒪᓕᒐᓕᐅᖅᑎ ᔫ ᓴᕕᑲᑖᖅ ᓂᕈᐊᖅᑕᐅᒃᑲᓐᓂᓚᐅᖅᐳᖅ ᓂᕈᐊᕕᒡᔪᐊᕐᓇᐅᑎᓪᓗᒍ ᐅᑐᐱᕆ 30, 2017-ᒥ.";
		List<String> gotSentences = segmenter.segment(text);
		String[] expSentences = new String[] {
				"ᔫ ᓴᕕᑲᑖᖅ\n", "ᓯᕗᓕᖅᑎ ᓄᓇᕗᒻᒥ\n", 
				"ᒪᓕᒐᓕᐅᖅᑎ ᔫ ᓴᕕᑲᑖᖅ ᓂᕈᐊᖅᑕᐅᒃᑲᓐᓂᓚᐅᖅᐳᖅ ᓂᕈᐊᕕᒡᔪᐊᕐᓇᐅᑎᓪᓗᒍ ᐅᑐᐱᕆ 30, 2017-ᒥ."
		};
		AssertObject.assertDeepEquals("Sentences not as expected", 
			expSentences, gotSentences);
	}
	
	@Test
	public void test__segment__PlainTextRenderedFromHTML() throws Exception {
		// When rendering a web page as plain text, it's quite common for 
		// divs to be rendered into a single line, but with multiple spaces 
		// between each div.
		//
		// These divs should be deemed separate sentences.
		//
		
		String text = "Hello world    Take me to your leader.";	
		Segmenter segmenter = Segmenter.makeSegmenter();
		
		List<String> gotSentences = segmenter.segment(text);
		String[] expSentences = new String[] {
				"Hello world    ", "Take me to your leader."
		};
		AssertObject.assertDeepEquals("Sentences not as expected", 
			expSentences, gotSentences);
	}
	@Test
	public void test__segmentTokenized__HappyPath() throws Exception {
		Segmenter segmenter = Segmenter.makeSegmenter("en");
		String text = "Hello world. Greetings universe!";
		List<String[]> gotSentences = segmenter.segmentTokenized(text);
		
		String[][] expSentences = new String[][] {
			new String[] {"Hello", " ", "world", ". "},
			new String[] {"Greetings", " ", "universe", "!"}
		};
		AssertObject.assertDeepEquals("Tokenized Sentences not as expected", 
			expSentences, gotSentences);
	}
	
}
