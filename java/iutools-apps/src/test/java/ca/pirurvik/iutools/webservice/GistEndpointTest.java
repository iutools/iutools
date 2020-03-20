package ca.pirurvik.iutools.webservice;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.inuktitutcomputing.utilities.Alignment;
import ca.nrc.ui.web.testing.MockHttpServletResponse;

public class GistEndpointTest {

	GistEndpoint endPoint = null;
	
	@Before
	public void setUp() throws Exception {
		endPoint = new GistEndpoint();
	}
	
	/***********************
	 * VERIFICATION TESTS
	 ***********************/	
	
	@Test
	public void test__GistEndpoint__Roman__HappyPath() throws Exception {
		GistInputs gistInputs = new GistInputs("takujaujuq");
		
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.GIST,
					gistInputs
				);
		
		// Benoit, pour le moment, GistEndpoint retourne un array de 
		// decomps vide. Mais éventuellement, du devras changer cette 
		// expectation.
		String[] expDecompsAsStrings = new String[] {
			"{taku:taku/1v}{ja:jaq/1vn}{u:u/1nv}{juq:juq/1vn}",
			"{taku:taku/1v}{ja:jaq/1vn}{u:u/1nv}{juq:juq/tv-ger-3s}"
		};
		Alignment[] expSentencePairs = new Alignment[] {
			new Alignment(
				"en","Mr. Chairman, and this wording and the reason I was asking the question at that time was that you know one really looked that, you can see that, if the community doesnât feel that it is fair or right whether they are right or not, that the government is obligated to act and that could be anywhere.",
				"iu","iksivautaq, ukualu uqarsimaningit tainnalu apiqqutigiluaqqauvara taiksumani tamanna <span class='highlighted'>takujaujuq</span>, takuksaujuq, nunaliujut nammaktutiqanngippata tamarutik tammangikkutik, gavamakkut qanuiligiariaqaqput tamanna namituinnaujunnaqtuni."
				)
		};
		IUTServiceTestHelpers.assertGistResponseIsOK(response, 
				expDecompsAsStrings, expSentencePairs);
	}
	
	@Test
	public void test__computeSentencePair() {
		String alignmentStr = "123:: inuktitut sentence@----@english sentence";
		Alignment alignment = endPoint.computeSentencePair(alignmentStr);
		String expectedInuktitutSentence = "inuktitut sentence";
		String expectedEnglishSentence = "english sentence";
		String gotInuktitut = alignment.sentences.get("iu");
		String gotEnglish = alignment.sentences.get("en");
		Assert.assertEquals("", expectedInuktitutSentence,gotInuktitut);
		Assert.assertEquals("", expectedEnglishSentence,gotEnglish);
		
		alignmentStr = "19990601::@----@ Thank you.";
		alignment = endPoint.computeSentencePair(alignmentStr);
		expectedInuktitutSentence = "";
		expectedEnglishSentence = "Thank you.";
		gotInuktitut = alignment.sentences.get("iu");
		gotEnglish = alignment.sentences.get("en");
		Assert.assertEquals("", expectedInuktitutSentence,gotInuktitut);
		Assert.assertEquals("", expectedEnglishSentence,gotEnglish);
		
		alignmentStr = "19990401:: amisut inuit ilauqataulauqtuit taikani ullunganni ilauqataugivut maanna unnusaujuq.@----@ I believe that you will all agree that the artwork which surrounds us is beautiful.";
		alignment = endPoint.computeSentencePair(alignmentStr);
		expectedInuktitutSentence = "amisut inuit ilauqataulauqtuit taikani ullunganni ilauqataugivut maanna unnusaujuq.";
		expectedEnglishSentence = "I believe that you will all agree that the artwork which surrounds us is beautiful.";
		gotInuktitut = alignment.sentences.get("iu");
		gotEnglish = alignment.sentences.get("en");
		Assert.assertEquals("", expectedInuktitutSentence,gotInuktitut);
		Assert.assertEquals("", expectedEnglishSentence,gotEnglish);
		
		
	}
}
