package ca.pirurvik.iutools.edit_distance;

import org.junit.Assert;
import org.junit.Test;

import ca.nrc.string.diff.StringTransformation;
import ca.nrc.testing.AssertObject;

public class IUDiffCostingTest {
	
	private static class CharDoublingExample {
		public String origStr = null;
		public String revStr = null;
		String[] expResult = null;
		String descr = null;
		
		public CharDoublingExample(String orig, String rev, 
				String[] _expRes) {
			this.origStr = orig;
			this.revStr = rev;
			this.expResult = _expRes;
		}
		
		public CharDoublingExample setDescr(String _descr) {
			this.descr = _descr;
			return this;
		}
		
		public String toString() {
			String str = "";
			if (descr != null) {
				str += "Case: "+descr+"\n   ";
			}
			str = "["+origStr+"->"+revStr+", exp=";
			if (expResult == null) {
				str += "null";
			} else {
				str += "'"+String.join("', '", expResult)+"'";
			}
			return str;
		}
	}
	
	//
	// Examples for testing isCharacterDoubling1way()
	//
	// Note: NOT same as data for testing isCharacterDoubling2ways
	//
	CharDoublingExample[] isCharacterDoubling1way_Data = new CharDoublingExample[] {
			new CharDoublingExample("i", "ii", new String[] {"", ""})
					.setDescr("Doubled on left, no Xtra chars"),
			new CharDoublingExample("ii", "i", null)
					.setDescr("Doubled on right, no Xtra chars"),
			new CharDoublingExample("iiq", "i", null)
					.setDescr("Doubled on left XtraChars on left"),
			new CharDoublingExample("i", "iiq", new String[] {"", "q"})
					.setDescr("Doubled on right, Xtra chars on right"),
			new CharDoublingExample("iq", "ii", new String[] {"q", ""})
					.setDescr("Douled on right, Xtra chars on left"),
	};
	
	//
	// Examples for testing isCharacterDoubling2ways()
	//
	// Note: NOT same as data for testing isCharacterDoubling1way
	//
	CharDoublingExample[] isCharacterDoubling2ways_Data = new CharDoublingExample[] {
			new CharDoublingExample("i", "ii", new String[] {"", ""})
					.setDescr("Doubled on left, no Xtra chars"),
			new CharDoublingExample("ii", "i", new String[] {"", ""})
					.setDescr("Doubled on right, no Xtra chars"),
			new CharDoublingExample("iiq", "i", new String[] {"q", ""})
					.setDescr("Doubled on left XtraChars on left"),
			new CharDoublingExample("i", "iiq", new String[] {"", "q"})
					.setDescr("Doubled on right, Xtra chars on right"),
			new CharDoublingExample("iq", "ii", new String[] {"q", ""})
					.setDescr("Douled on right, Xtra chars on left"),
	};

	@Test
	public void test__isCharacterDoubling1way__SeveralCases() throws Exception {
		IUDiffCosting costing = new IUDiffCosting();
		
		String focusOnExample = null;
//		String focusOnExample = "iq/ii";
		
		for (int ii=0; ii < isCharacterDoubling1way_Data.length; ii++) {
			CharDoublingExample anExample = isCharacterDoubling1way_Data[ii];
			
			String exampleKey = anExample.origStr+"/"+anExample.revStr;
			if (focusOnExample != null &&
					!focusOnExample.equals(exampleKey)) {
				continue;
			}
			
			
			String[] gotResult = 
				costing.isCharDoubling(anExample.origStr, anExample.revStr);
			AssertObject.assertDeepEquals(
					"Bad results for example: "+anExample, anExample.expResult, gotResult);
		}
		
		if (focusOnExample != null) {
			Assert.fail("WARNING: This test was run on just one example.");
		}		
	}

	@Test
	public void test__isCharacterDoubling2ways__SeveralCases() throws Exception {
		IUDiffCosting costing = new IUDiffCosting();
		
		String focusOnExample = null;
//		String focusOnExample = "iq/ii";
		
		for (int ii=0; ii < isCharacterDoubling2ways_Data.length; ii++) {
			CharDoublingExample anExample = isCharacterDoubling2ways_Data[ii];
			
			String exampleKey = anExample.origStr+"/"+anExample.revStr;
			if (focusOnExample != null &&
					!focusOnExample.equals(exampleKey)) {
				continue;
			}
			
			String[] origTokens = anExample.origStr.split("");
			String[] revTokens = anExample.revStr.split("");
			StringTransformation transf = 
					new StringTransformation(0, origTokens, 0, revTokens);
			
			String[] gotResult = 
				costing.isCharDoubling2ways(transf);
			AssertObject.assertDeepEquals("Bad results for example: "+anExample, anExample.expResult, gotResult);
		}
		
		if (focusOnExample != null) {
			Assert.fail("WARNING: This test was run on just one example.");
		}		
	}
	
}
