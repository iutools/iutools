package ca.pirurvik.iutools.edit_distance;


import org.junit.Before;
import org.junit.Test;

import ca.nrc.datastructure.Cloner;
import ca.nrc.string.diff.DiffCosting;
import ca.nrc.testing.AssertNumber;

import org.junit.Assert;

public class IUSpellingDistanceTest {
	
	IUSpellingDistance distCalculator = null;
	
	/**
	 * These test examples provide two words as 
	 * well as min and max distance we expect to 
	 * have between the two.
	 * 
	 * @author desilets
	 *
	 */
	public static class DistanceExample {
		private String descr = null;
		public String word1 = null;
		public String word2 = null;
		public Double minDist = null;
		public Double maxDist = null;
		public Double expDist = null;
		
		public DistanceExample() {}

		public DistanceExample(String _word1, String _word2) {
			this.word1 = _word1;
			this.word2 = _word2;
		}
		
		public DistanceExample setMinDist(Double _minDist) {
			this.minDist = _minDist;
			this.expDist = null;			
			return this;
		}
		
		public DistanceExample setMaxDist(Double _maxDist) {
			this.maxDist = _maxDist;
			this.expDist = null;
			return this;
		}
		
		public DistanceExample setExpDist(double _expDist) {
			this.expDist = _expDist;
			this.maxDist = null;
			this.minDist = null;
			
			return this;
		}
		
		public DistanceExample setDescr(String _descr) {
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

		public DistanceExample reverse() {
			DistanceExample reversed = new DistanceExample();
			reversed.word1 = word2;
			reversed.word2 = word1;
			reversed.expDist = expDist;
			reversed.maxDist = maxDist;
			reversed.minDist = minDist;
			reversed.descr = descr;
			return reversed;
		}
	}
	
	@Before
	public void setUp() {
		distCalculator = new IUSpellingDistance();
	}

	////////////////////////
	// VERIFICATION TESTS
	////////////////////////
	
	DistanceExample[] distanceExamples = new DistanceExample[] {
			
			new DistanceExample("tanna", "taanna")
				.setExpDist(DiffCosting.TINY_COST)
				.setDescr("Doubling a vowel in first morpheme should encure TINY cost"),
				
			new DistanceExample("tanna", "sunainna")
					.setMinDist(DiffCosting.INFINITE)
					.setDescr("Changing first morpheme should encur INFINITE cost if at least one of the words is multi-morpheme."),

			new DistanceExample("tuna", "suna")
					.setExpDist(2*DiffCosting.SMALL_COST)
					.setDescr("Changing first morpheme should encur SMALL cost if both words are single morphemes."),
					
			new DistanceExample("nigiani", "niggiani")
					.setMaxDist(DiffCosting.SMALL_COST)
					.setDescr("Doubling a consonant shoudl have TINY cost, even in first morpheme"),
				
			new DistanceExample("nakuqmi", "nakurmiik")
					.setExpDist(DiffCosting.TINY_COST + 3*DiffCosting.SMALL_COST)
					.setDescr("(a) Doubling a vowel, (b) substituting a char mid word and (c) appending a consonant at the end"),
			
			new DistanceExample("nniaq", "aanniaq")
					.setExpDist(DiffCosting.TINY_COST)
					.setDescr("Special case: ommitting 'aa' from 'aaniaq', should encur TINY cost"),
					
			new DistanceExample("aanniaq", "nniaq")
					.setExpDist(DiffCosting.TINY_COST)
					.setDescr("Special case: adding 'aa' to 'nniaq', should encur TINY cost"),
										
			new DistanceExample("nunavuumit", "nunavummit")
					.setExpDist(2*DiffCosting.TINY_COST)
					.setDescr("Doubling/de-doubling two consecutive chars"),
					
			new DistanceExample("nigiani", "niruarnaqsigasuarnilimaangani")
					.setMinDist(10 * DiffCosting.SMALL_COST)
					.setDescr("Second string much longer"),					
	};
	
	@Test
	public void test__distance__Examples2Way() throws Exception {
		
		// Set this to the first word of an example if you 
		// only want to run the test on that one example.
		//
		String focusOnExample = null;
//		focusOnExample = "nigiani/niruarnaqsigasuarnilimaangani";
		
		for (DistanceExample anExampleOrig: distanceExamples) {
			
			if (focusOnExample != null && 
					!focusOnExample.equals(anExampleOrig.toString())) {
				continue;
			}
			
			for (boolean reverseDir: new boolean[] {false, true}) {
				
				DistanceExample anExample = null;
				if (reverseDir) {
					anExample = anExampleOrig.reverse();
				} else {
					anExample = anExampleOrig;
				}
				
				double gotDist = distCalculator.distance(anExample.word1, anExample.word2);
				if (anExample.expDist != null) {
					Assert.assertEquals(
							anExample.description()+
							"\n  Distance was not as expected for word pair: ["+
							anExample.word1+", "+anExample.word2+"]",
							anExample.expDist, new Double(gotDist), DiffCosting.TINY_COST);
				}
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
	}
	
	
//	@Test
//	public void test__distance__Examples3Way() throws Exception {
//		
//		// Set this to the first word of an example if you 
//		// only want to run the test on that one example.
//		//
//		String focusOnExample = null;
////		String focusOnExample = "tanna/taanna/taunna";
//		
//		for (Example3Way anExample: examples3Way) {
//			if (focusOnExample == null || 
//				focusOnExample.equals(anExample.toString())) {
//				double gotDistClose = distCalculator.distance(anExample.baseWord, anExample.closerWord);
//				double gotDistFar = distCalculator.distance(anExample.baseWord, anExample.furtherWord);
//				AssertNumber.isLessOrEqualTo(
//						anExample.description()+"Failed 3-way example.", 
//						gotDistClose, gotDistFar);	
//				
//				if (focusOnExample != null) {
//					System.out.println(
//							anExample.description()+
//							"\n  Dist to "+anExample.closerWord+"="+gotDistClose+
//							"; to "+anExample.furtherWord+"="+gotDistFar);
//				}
//			}
//			
//		}
//		
//		if (focusOnExample != null) {
//			Assert.fail("WARNING: Test was run on only one example: "+focusOnExample);
//		}
//	}
	
	@Test
	public void test__distance__HappyPath() throws Exception {
		String word1 = "tamaini";
		String word2 = "tamainni";
		double gotDist = distCalculator.distance(word1, word2);
		double expDist = 0.1;
		Assert.assertEquals("Spelling distance not as expected", 
				expDist, gotDist, DiffCosting.SMALL_COST);
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
	public void test__distance__DoublingCharacter__HasSmallCost() throws Exception {
		String word1 = "nakumi";
		String word2 = "nakumii";
		double gotDist = distCalculator.distance(word1, word2);
		AssertNumber.isLessOrEqualTo("Doubling a character: 'i' --> 'ii' should have had a small cost.",
				gotDist, DiffCosting.SMALL_COST);
	}

}
