package ca.pirurvik.iutools.concordancer;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.file.ResourceGetter;
import ca.nrc.testing.AssertHelpers;
import ca.nrc.testing.AssertObject;

public class Aligner_MalignaTest {
	
	@Test
	public void test__FixMalignaIssues() throws Exception {
		Assert.fail(
			"\n\nIGNORE THIS FAILURE!\n\n"+
			"It is just a reminder to deal with some failing (currently @Ignored) tests.\n"+
			"These tests started failing on 2020-06-03\n" + 
			"It seems the content of the www.gov.nu.ca home pages has changed\n" + 
			"in a way that makes Maligna crash.\n"+
			"Will need to fix the Maligna bug and submit the fix to its maintainers.");
	}
	
	
	/////////////////////////////
	// DOCUMENTATION TESTS
	/////////////////////////////

	@Test
	public void test__Aligner_Maligna__Synopsis() throws Exception {
		// Use this class to align sentences from two documents which 
		// are translations of each other in different languages.
		//
		String[] enSents = new String[] {
			"Hello World.",
			"Hi.",
			"Take me to your leader!"
		};
		String[] frSents = new String[] {
			"Bonjour le monde.",
			"Menez moi à votre chef!"
		};
		
		Aligner_Maligna aligner = new Aligner_Maligna();
		List<Pair<String,String>> alignments 
				= aligner.align(enSents, frSents);
		for (Pair<String,String> anAlignment: alignments) {
			String enSentences = anAlignment.getFirst();
			String frSentences = anAlignment.getSecond();
		}
	}	
	
	////////////////////////
	// VERIFICATION TESTS
	////////////////////////
	
	
	// This test started failing on 2020-06-03
	// It seems the content of the www.gov.nu.ca home pages has changed
	// in a way that makes Maligna crash.
	//
	@Test @Ignore
	public void test__align__HappyPath() throws Exception {
		String[] enSents = new String[] {
			"Hello World.",
			"Hi.",
			"Take me to your leader!"
		};
		String[] frSents = new String[] {
			"Bonjour le monde.",
			"Menez moi à votre chef!"
		};
		
		Aligner_Maligna aligner = new Aligner_Maligna();
		List<Pair<String,String>> gotAlignments = aligner.align(enSents, frSents);
		
		List<Pair<String,String>> expAlignments 
			= new ArrayList<Pair<String,String>>();
		{
			expAlignments.add(
				Pair.of("Hello World.Hi.", "Bonjour le monde."));
			expAlignments.add(
				Pair.of("Take me to your leader!", "Menez moi à votre chef!"));					
		}
		
		AssertObject.assertDeepEquals(
				"Alignments were not as expected", 
				expAlignments, gotAlignments);
	}
	
	// This test started failing on 2020-06-03
	// It seems the content of the www.gov.nu.ca home pages has changed
	// in a way that makes Maligna crash.
	//
	@Test @Ignore
	public void test__Aligner_Maligna__CallAsMain() throws Exception {
		String f1Path = ResourceGetter.getResourcePath("ca/pirurvik/iutools/concordancer/l1_sents.txt");
		String f2Path = ResourceGetter.getResourcePath("ca/pirurvik/iutools/concordancer/l2_sents.txt");

		Aligner_Maligna.main(new String[] {f1Path, f2Path});
		
		String expContent = "Hello World.Hi.\nTake me to your leader!";
		AssertHelpers.assertFileContentIs("", expContent, f1Path+".al");

		expContent = "Bonjour le monde.\nMenez moi à votre chef!";
		AssertHelpers.assertFileContentIs("", expContent, f2Path+".al");
	
		expContent = "[1,2]\n[3]";
		AssertHelpers.assertFileContentIs("", expContent, f1Path+".al.nums");

		expContent = "[1]\n[2]";
		AssertHelpers.assertFileContentIs("", expContent, f2Path+".al.nums");
	}
}
