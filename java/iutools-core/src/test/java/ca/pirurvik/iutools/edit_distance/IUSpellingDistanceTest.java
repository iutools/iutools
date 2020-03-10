package ca.pirurvik.iutools.edit_distance;


import org.junit.Before;
import org.junit.Test;

import ca.nrc.string.diff.DiffCosting;
import ca.nrc.testing.AssertNumber;

import org.junit.Assert;

public class IUSpellingDistanceTest {
	
	IUSpellingDistance distCalculator = null;
	
	
	/**
	 * These test examples provide 3 words. 
	 * Distance of the 1st word to each of the 
	 * two is computed, and it is expected that 
	 * the distance to the 2nd word is smaller 
	 * than the distance to the 3rd word
	 * 
	 * @author desilets
	 *
	 */
	public static class Example3Way {
		String descr = null;
		String baseWord = null;
		String closerWord = null;
		String furtherWord = null;
		
		public Example3Way(String base, String close, String far) {
			super();
			this.baseWord = base;
			this.closerWord = close;
			this.furtherWord = far;
		}
		
		public Example3Way setDescr(String _descr) {
			this.descr = _descr;
			return this;
		}
		
		public String description() {
			String fullDescr =  "Case: ";
			if (descr != null) {
				fullDescr += descr+"\n";
			}
			fullDescr += "words=["+baseWord+","+closerWord+","+furtherWord+"]";
			
			return fullDescr;
		}
		
		public String toString() {
			return baseWord+"/"+closerWord+"/"+furtherWord;
		}
	}
	
	/**
	 * These test examples provide two words as 
	 * well as min and max distance we expect to 
	 * have between the two.
	 * 
	 * @author desilets
	 *
	 */
	public static class Example2Way {
		private String descr = null;
		public String word1 = null;
		public String word2 = null;
		public Double minDist = null;
		public Double maxDist = null;
		
		public Example2Way(String _word1, String _word2) {
			this.word1 = _word1;
			this.word2 = _word2;
		}
		
		public Example2Way setMinDist(Double _minDist) {
			this.minDist = _minDist;
			return this;
		}
		
		public Example2Way setMaxDist(Double _maxDist) {
			this.maxDist = _maxDist;
			return this;
		}
		
		public Example2Way setDescr(String _descr) {
			this.descr = _descr;
			return this;
		}
		
		public String description() {
			String fullDescr =  "Case: ";
			if (descr != null) {
				fullDescr += descr+"\n";
			}
			fullDescr += "words=["+word1+","+word2+"]";
			
			return fullDescr;
		}
		
		public String toString() {
			return word1+"/"+word2;
		}
	}
	
	Example2Way[] examples2Way = new Example2Way[] {
			
			new Example2Way("tanna", "taanna").setMaxDist(DiffCosting.SMALL_COST)
				.setDescr("Doubling a vowel in first morpheme should be small cost"),
				
			new Example2Way("tanna", "sunainna").setMinDist(DiffCosting.INFINITE)
					.setDescr("Changes in first morpheme, should have INFINITE when one of the words has more than one morpheme."),
	};

	Example3Way[] examples3Way = new Example3Way[] {
			new Example3Way("tanna", "taanna", "sunainna"),
			new Example3Way("tanna", "taanna", "taunna")
					.setDescr("First word should be closer because it is just a doubling of vowel")
	};
	
	@Before
	public void setUp() {
		distCalculator = new IUSpellingDistance();
	}

	////////////////////////
	// VERIFICATION TESTS
	////////////////////////
	
	@Test
	public void test__distance__Examples3Way() throws Exception {
		
		// Set this to the first word of an example if you 
		// only want to run the test on that one example.
		//
		String focusOnExample = null;
//		String focusOnExample = "tanna/taanna/taunna";
		
		for (Example3Way anExample: examples3Way) {
			if (focusOnExample == null || 
				focusOnExample.equals(anExample.toString())) {
				double gotDistClose = distCalculator.distance(anExample.baseWord, anExample.closerWord);
				double gotDistFar = distCalculator.distance(anExample.baseWord, anExample.furtherWord);
				AssertNumber.isLessOrEqualTo(
						anExample.description()+"Failed 3-way example.", 
						gotDistClose, gotDistFar);	
				
				if (focusOnExample != null) {
					System.out.println(
							anExample.description()+
							"\n  Dist to "+anExample.closerWord+"="+gotDistClose+
							"; to "+anExample.furtherWord+"="+gotDistFar);
				}
			}
			
		}
		
		if (focusOnExample != null) {
			Assert.fail("WARNING: Test was run on only one example: "+focusOnExample);
		}
	}
	
	@Test
	public void test__distance__Examples2Way() throws Exception {
		
		// Set this to the first word of an example if you 
		// only want to run the test on that one example.
		//
		String focusOnExample = null;
//		String focusOnExample = "tanna/taanna";
		
		for (Example2Way anExample: examples2Way) {
			
			if (focusOnExample != null && 
					!focusOnExample.equals(anExample.toString())) {
				continue;
			}
			
			double gotDist = distCalculator.distance(anExample.word1, anExample.word2);
			if (anExample.minDist != null) {
				AssertNumber.isGreaterOrEqualTo(
						anExample.description()+
						"\n  Distance was too small for word pair: ["+
						anExample.word1+", "+anExample.word2+"]",
						new Double(gotDist), anExample.minDist);
			}
			if (anExample.maxDist != null) {
				AssertNumber.isLessOrEqualTo(
						anExample.description()+
						"\n  Distance was too large for word pair: ["+
						anExample.word1+", "+anExample.word2+"]",
						new Double(gotDist), anExample.maxDist);
			}
			
			if (focusOnExample != null) {
				System.out.println(
						anExample.description()+
						"\n  distance("+anExample.word1+","+anExample.word2+
						") = "+gotDist);
			}
			
		}
		
		if (focusOnExample != null) {
			Assert.fail("WARNING: Test was run on only one example: "+focusOnExample);
		}
	}
	
	
	@Test
	public void test__distance__HappyPath() throws Exception {
		String word1 = "tamaini";
		String word2 = "tamainni";
		double gotDist = distCalculator.distance(word1, word2);
		double expDist = 0.1;
		Assert.assertEquals("Spelling distance not as expected", 
				expDist, gotDist, 0.01);
	}

	@Test
	public void test__distance__WordsDifferInFirst3Chars__InfiniteCost() throws Exception {
		String word1 = "qallunaatitut";
		String word2 = "alputatitut";
		double gotDist = distCalculator.distance(word1, word2);
		AssertNumber.isGreaterOrEqualTo("Changing leading 3 chars of a word should have yielded an 'infinite' cost.", 
				gotDist, DiffCosting.INFINITE);
	}

	@Test
	public void test__distance__anniaq__AllowCertainTransfInFirst3Chars() throws Exception {
		String word1 = "nniaqamangittulirijiit";
		String word2 = "anniaqarnangittulirijiit";
		double gotDist = distCalculator.distance(word1, word2);
		AssertNumber.isLessOrEqualTo("Removing leading character 'a' 'anniaq*' should NOT have yielded an 'infinite' cost.",
				gotDist, DiffCosting.INFINITE-1);
	}
	
	@Test
	public void test__distance__DoublingCharacter__HasSmallCost() throws Exception {
		String word1 = "nakumi";
		String word2 = "nakumii";
		double gotDist = distCalculator.distance(word1, word2);
		AssertNumber.isLessOrEqualTo("Doubling a character: 'i' --> 'ii' should have had a small cost.",
				gotDist, DiffCosting.SMALL_COST);
	}
	
}
