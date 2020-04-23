package ca.inuktitutcomputing.morph;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;


import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.Decomposition.DecompositionExpression;

public class DecompositionTest {

	@Test
	public void test__containsMorpheme() throws Exception {
		String word = "makpigaq";
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		Decomposition[] decompositions = analyzer.decomposeWord(word,false);
		boolean gotResult = decompositions[0].containsMorpheme("makpiq/1v");
		Assert.assertTrue("", gotResult);
		gotResult = decompositions[0].containsMorpheme("liuq/4nv");
		Assert.assertFalse("", gotResult);
	}
	
	@Test
	public void test__DecompositionExpression_toStringWithoutSurfaceForms() {
		DecompositionExpression expr = new DecompositionExpression("{makpi:makpiq/1v}{gar:gaq/1vn}{ni:ni/tn-loc-p}");
		String exprWithoutSurfaceForms = expr.toStringWithoutSurfaceForms();
		String expected = "{makpiq/1v} {gaq/1vn} {ni/tn-loc-p}";
		Assert.assertEquals("",expected, exprWithoutSurfaceForms);
	}
	
	@Test
	public void test__getSurfaceForms() throws Exception {
		String word = "makpigaq";
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		Decomposition[] decompositions = analyzer.decomposeWord(word,false);
		String surfaceForms = String.join(";", decompositions[0].getSurfaceForms().toArray(new String[] {}));
		Assert.assertEquals("",  "makpi;gaq", surfaceForms);
		
		word = "umiarjualiuqti";
		decompositions = analyzer.decomposeWord(word,false);
		surfaceForms = String.join(";", decompositions[0].getSurfaceForms().toArray(new String[] {}));
		Assert.assertEquals("",  "umiar;jua;liuq;ti", surfaceForms);
	}

}
