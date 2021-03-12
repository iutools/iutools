package org.iutools.linguisticdata.dataCSV;

import ca.nrc.json.PrettyPrinter;
import org.iutools.linguisticdata.Action;
import org.iutools.linguisticdata.Affix;
import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.linguisticdata.dataCSV.LinguisticDataCSV;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

//	@Test
//	public void test_getBases() throws Exception {
////		LinguisticDataCSV data = new LinguisticDataCSV("r");
////		Vector<Morpheme> baseObjects = data.getBasesForCanonicalForm("iglu");
//		Vector<Morpheme> baseObjects = LinguisticData.getInstance().getBasesForCanonicalForm("iglu");
//		Assert.assertEquals("The number of Base objects for the form 'iglu' is not as expected.", 1, baseObjects.size());
//		Assert.assertEquals("Base for the form 'iglu' was not as expected.", "iglu", baseObjects.get(0).morpheme);
//	}
//
//	@Test
//	public void test_getBaseWithId() throws Exception {
////		LinguisticDataCSV data = new LinguisticDataCSV("r");
////		Base gotBase = data.getBaseWithId("iglu/1n");
//		Base gotBase = LinguisticData.getInstance().getBaseWithId("iglu/1n");
//		Assert.assertEquals("Morpheme for the base 'iglu/1n' was not as expected.", "iglu", gotBase.morpheme);
//	}
//
//	@Test
//	public void test_getIdToRootTable() throws Exception {
////		LinguisticDataCSV data = new LinguisticDataCSV("r");
////		Hashtable<String,Morpheme> gotTable = data.getIdToRootTable();
//		Hashtable<String,Morpheme> gotTable = LinguisticData.getInstance().getIdToRootTable();
//		Morpheme morpheme = gotTable.get("iglu/1n");
//		Assert.assertEquals("Morpheme for the base 'iglu/1n' was not as expected.", "iglu", morpheme.morpheme);
//	}
//
//	@Test
//	public void test_getgetIdToGiVerbsTable() throws Exception {
////		LinguisticDataCSV data = new LinguisticDataCSV();
////		Hashtable<String,Base> gotTable = data.getIdToGiVerbsTable();
//		Hashtable<String,Base> gotTable = LinguisticData.getInstance().getIdToGiVerbsTable();
//		Base giVerb = gotTable.get("naglik/1v");
//		Assert.assertEquals("The base 'naglik/1v' was not found in the giverbs table.", "naglik", giVerb.morpheme);
//	}
//
//	@Test
//	public void test_getIdToDemonstrativeTable() throws Exception {
////		LinguisticDataCSV data = new LinguisticDataCSV();
////		Hashtable<String,Demonstrative> gotTable = data.getIdToDemonstrativeTable();
//		Hashtable<String,Demonstrative> gotTable = LinguisticData.getInstance().getIdToDemonstrativeTable();
//		Demonstrative demBase = gotTable.get("makua/pd-ml-p");
//		Assert.assertEquals("The demonstrative [base] 'makua/pd-ml-p' was not found in the demonstrative table.", "makua", demBase.morpheme);
//	}
//
//	@Test
//	public void test_check_compositionRoot() {
//		LinguisticData.init();
//		Hashtable<String,Base> idToBaseTable = LinguisticData.getInstance().idToBaseTable;
//		Base arviat = idToBaseTable.get("arviat/1n");
////		System.out.println("ARVIAT/1N:\n"+PrettyPrinter.print(arviat));
//		Assert.assertEquals("'morpheme' of original base is incorrect.","arviat",arviat.morpheme);
//	}
//
	@Test
	public void test__readLinguisticDataCSV__original_csv_suffix_file() throws IOException, LinguisticDataException {
		String dbName = "Inuktitut";
		String tableName = "Suffix";
		String typeOfObject = null;
		String csv = "morpheme,nb,type,function,position,transitivity,nature,plural,antipassive,V-form,V-action1,V-action2,t-form,t-action1,t-action2,k-form,k-action1,k-action2,q-form,q-action1,q-action2,engMean,freMean,condPrec,condPrecTrans,condOnNext,sources,combination,dialect,mobility\n"
//				+ "allak,1,sv,vv,m,n,,,,ala,n,i(ra),* ala,s,i(ra),* ala,s,i(ra),* ala,s,i(ra),\"ease, simpleness of action; 'easily'; 'just'\",\"action facile, simple; 'facilement'; 'juste'\",,,,H1 A1,,,m\n";
				+ "allak,1,sv,vv,m,n,,,,allak ala,n,i(ra),allak ala,s,i(ra),allak ala,s,i(ra),allak ala,s,i(ra),\"ease, simpleness of action; 'easily'; 'just'\",\"action facile, simple; 'facilement'; 'juste'\",,,,H1 A1,,,m\n";
		InputStream is = new ByteArrayInputStream(csv.getBytes("UTF-8"));
		LinguisticDataCSV ldcsv = new LinguisticDataCSV();
		LinguisticData lingData = new LinguisticData();
		ldcsv.readLinguisticDataCSV(is,dbName,tableName,typeOfObject,lingData);
		Affix affix = lingData.getAffixWithId("allak/1vv");
		String[] vforms = affix.getForm('V');
		Assert.assertEquals("The number of V forms is not correct.",2,vforms.length);
		Assert.assertArrayEquals("The forms in V-form are not as expected.",new String[]{"allak","ala"},vforms);
		Action[] vactions1 = affix.getAction1('V');
		Assert.assertEquals("The number of V actions1 is not correct.",2,vactions1.length);
		Assert.assertArrayEquals("The actions in V-action1 are not as expected.",new Integer[]{Action.NEUTRAL,Action.NEUTRAL},typesOfActions(vactions1));
		Action[] vactions2 = affix.getAction2('V');
		Assert.assertEquals("The number of V actions2 is not correct.",2,vactions2.length);
		Assert.assertArrayEquals("The actions in V-action2 are not as expected.",new Integer[]{Action.INSERTION,Action.INSERTION},typesOfActions(vactions2));

		String[] tforms = affix.getForm('t');
		Assert.assertEquals("The number of T forms is not correct.",2,tforms.length);
		Assert.assertArrayEquals("The forms in T-form are not as expected.",new String[]{"allak","ala"},tforms);
		Action[] tactions1 = affix.getAction1('t');
		Assert.assertEquals("The number of T actions1 is not correct.",2,tactions1.length);
		Assert.assertArrayEquals("The actions in T-action1 are not as expected.",new Integer[]{Action.DELETION,Action.DELETION},typesOfActions(tactions1));
		Action[] tactions2 = affix.getAction2('t');
		Assert.assertEquals("The number of T actions2 is not correct.",2,tactions2.length);
		Assert.assertArrayEquals("The actions in T-action2 are not as expected.",new Integer[]{Action.INSERTION,Action.INSERTION},typesOfActions(tactions2));

		String[] kforms = affix.getForm('k');
		Assert.assertEquals("The number of K forms is not correct.",2,kforms.length);
		Assert.assertArrayEquals("The forms in K-form are not as expected.",new String[]{"allak","ala"},kforms);
		Action[] kactions1 = affix.getAction1('k');
		Assert.assertEquals("The number of K actions1 is not correct.",2,kactions1.length);
		Assert.assertArrayEquals("The actions in K-action1 are not as expected.",new Integer[]{Action.DELETION,Action.DELETION},typesOfActions(kactions1));
		Action[] kactions2 = affix.getAction2('k');
		Assert.assertEquals("The number of K actions2 is not correct.",2,kactions2.length);
		Assert.assertArrayEquals("The actions in K-action2 are not as expected.",new Integer[]{Action.INSERTION,Action.INSERTION},typesOfActions(kactions2));

		String[] qforms = affix.getForm('q');
		Assert.assertEquals("The number of Q forms is not correct.",2,qforms.length);
		Assert.assertArrayEquals("The forms in Q-form are not as expected.",new String[]{"allak","ala"},qforms);
		Action[] qactions1 = affix.getAction1('q');
		Assert.assertEquals("The number of Q actions1 is not correct.",2,qactions1.length);
		Assert.assertArrayEquals("The actions in Q-action1 are not as expected.",new Integer[]{Action.DELETION,Action.DELETION},typesOfActions(qactions1));
		Action[] qactions2 = affix.getAction2('q');
		Assert.assertEquals("The number of Q actions2 is not correct.",2,qactions2.length);
		Assert.assertArrayEquals("The actions in Q-action2 are not as expected.",new Integer[]{Action.INSERTION,Action.INSERTION},typesOfActions(qactions2));
	}

	private Integer[] typesOfActions(Action[] actions) {
		Integer[] types = new Integer[actions.length];
		for (int i=0; i<actions.length; i++) {
			types[i] = actions[i].type;
		}

		return types;
	}

}
