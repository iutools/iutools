package ca.inuktitutcomputing.data;

import org.junit.Test;

import ca.inuktitutcomputing.dataCSV.LinguisticDataCSV;

import java.util.Hashtable;
import java.util.Vector;

import org.junit.*;

/*
 * In reality, for the time being (September 2019), these tests are used to
 * verify that everything is fine while we refactor the database processing
 * from LinguisticDataAbstract static methods to LinguisticData non-static 
 * methods.
 * 
 * When the refactorization has been completed, all calls to LinguisticDataAbstract
 * static methods throughout the code will have to be replaced with calls to
 * LinguisticData.getInstance() –non-static– methods.
 */

public class LinguisticDataCSVTest {
	
	@Test
	public void test_getBases() throws Exception {
//		LinguisticDataCSV data = new LinguisticDataCSV("r");
//		Vector<Morpheme> baseObjects = data.getBasesForCanonicalForm("iglu");
		Vector<Morpheme> baseObjects = LinguisticDataAbstract.getBasesForCanonicalForm("iglu");
		Assert.assertEquals("The number of Base objects for the form 'iglu' is not as expected.", 1, baseObjects.size());
		Assert.assertEquals("Base for the form 'iglu' was not as expected.", "iglu", baseObjects.get(0).morpheme);
	}

	@Test
	public void test_getBaseWithId() throws Exception {
//		LinguisticDataCSV data = new LinguisticDataCSV("r");
//		Base gotBase = data.getBaseWithId("iglu/1n");
		Base gotBase = LinguisticDataAbstract.getBaseWithId("iglu/1n");
		Assert.assertEquals("Morpheme for the base 'iglu/1n' was not as expected.", "iglu", gotBase.morpheme);
	}

	@Test
	public void test_getIdToRootTable() throws Exception {
//		LinguisticDataCSV data = new LinguisticDataCSV("r");
//		Hashtable<String,Morpheme> gotTable = data.getIdToRootTable();
		Hashtable<String,Morpheme> gotTable = LinguisticDataAbstract.getIdToRootTable();
		Morpheme morpheme = gotTable.get("iglu/1n");
		Assert.assertEquals("Morpheme for the base 'iglu/1n' was not as expected.", "iglu", morpheme.morpheme);
	}

	@Test
	public void test_getgetIdToGiVerbsTable() throws Exception {
//		LinguisticDataCSV data = new LinguisticDataCSV();
//		Hashtable<String,Base> gotTable = data.getIdToGiVerbsTable();
		Hashtable<String,Base> gotTable = LinguisticDataAbstract.getIdToGiVerbsTable();
		Base giVerb = gotTable.get("naglik/1v");
		Assert.assertEquals("The base 'naglik/1v' was not found in the giverbs table.", "naglik", giVerb.morpheme);
	}

	@Test
	public void test_getIdToDemonstrativeTable() throws Exception {
//		LinguisticDataCSV data = new LinguisticDataCSV();
//		Hashtable<String,Demonstrative> gotTable = data.getIdToDemonstrativeTable();
		Hashtable<String,Demonstrative> gotTable = LinguisticDataAbstract.getIdToDemonstrativeTable();
		Demonstrative demBase = gotTable.get("makua/pd-ml-p");
		Assert.assertEquals("The demonstrative [base] 'makua/pd-ml-p' was not found in the demonstrative table.", "makua", demBase.morpheme);
	}

}
