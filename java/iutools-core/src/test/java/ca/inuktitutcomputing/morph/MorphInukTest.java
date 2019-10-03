package ca.inuktitutcomputing.morph;

import org.junit.Assert;
import org.junit.Test;

import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import java.util.concurrent.TimeoutException;

public class MorphInukTest {
	
	@Test(expected=TimeoutException.class)
	public void test__decomposeWord__timeout() throws Exception  {
//		Assert.fail("Fix this test which seems to freeze forever");
		MorphInuk.stpwActive = true;
		MorphInuk.millisTimeout = 3000;
		String word = "ilisaqsitittijunnaqsisimannginnama";
		try {
		MorphInuk.decomposeWord(word);
		} catch(Exception e) {
			//System.err.println(e.getClass().getName()+" --- "+e.getMessage());
			throw e;
		}
	}

	@Test(expected=TimeoutException.class)
	public void test__decomposeWord__timeout_10s() throws Exception  {
//		Assert.fail("Fix this test which seems to freeze forever");
		MorphInuk.stpwActive = true;
		MorphInuk.millisTimeout = 10000;
		String word = "ilisaqsitittijunnaqsisimannginnama";
		try {
		MorphInuk.decomposeWord(word);
		} catch(Exception e) {
			//System.err.println(e.getClass().getName()+" --- "+e.getMessage());
			throw e;
		}
	}

	@Test
	public void test__decomposeWord__maligatigut() throws Exception  {
		String word = "maligatigut";
		try {
			MorphInuk.stpwActive = false;
			Decomposition[] decs = MorphInuk.decomposeWord(word);
			Assert.assertTrue(decs.length==0);
//			for (int i=0; i<decs.length; i++) {
//				System.out.println(decs[i].toStr2());
//			}
		} catch(Exception e) {
			//System.err.println(e.getClass().getName()+" --- "+e.getMessage());
			throw e;
		}
	}

	@Test
	public void test__decomposeWord__uqaqtiup() throws Exception  {
		String word = "uqaqtiup";
		try {
			MorphInuk.stpwActive = false;
			Decomposition[] decs = MorphInuk.decomposeWord(word);
			Assert.assertTrue(decs.length!=0);
			for (int i=0; i<decs.length; i++) {
				System.out.println(decs[i].toStr2());
			}
		} catch(Exception e) {
			//System.err.println(e.getClass().getName()+" --- "+e.getMessage());
			throw e;
		}
	}
	
	
	@Test
	public void test__decomposeWord__maligaliuqtinik() throws Exception  {
		String word = "maligaliuqtinik";
		try {
//			MorphInuk.stpwActive = false;
			Decomposition[] decs = MorphInuk.decomposeWord(word);
			Assert.assertTrue(decs.length!=0);
			for (int i=0; i<decs.length; i++) {
				System.out.println(decs[i].toStr2());
			}
		} catch(Exception e) {
			//System.err.println(e.getClass().getName()+" --- "+e.getMessage());
			throw e;
		}
	}

}
