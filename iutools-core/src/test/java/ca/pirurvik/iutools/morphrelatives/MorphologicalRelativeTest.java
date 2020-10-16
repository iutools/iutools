package ca.pirurvik.iutools.morphrelatives;

import java.util.HashSet;
import java.util.Set;

import ca.pirurvik.iutools.morphrelatives.MorphologicalRelative;
import org.junit.Assert;
import org.junit.Test;


public class MorphologicalRelativeTest {

	@Test
	public void test__Equality() {
		Set<MorphologicalRelative> expansions = new HashSet<MorphologicalRelative>();
		
		// These two should be considered equal
		MorphologicalRelative exp1 = new MorphologicalRelative("hello", null, 100);
		MorphologicalRelative exp2 = new MorphologicalRelative("hello", null, 100);
		
		// This one is distinc from the first two
		MorphologicalRelative exp3 = new MorphologicalRelative("word", null, 87);
		
		Assert.assertTrue(exp1.equals(exp2));
		Assert.assertFalse(exp1.equals(exp3));
		
		Assert.assertEquals(
			"Hash codes should have been the same", 
			exp1.hashCode(), exp2.hashCode());
		
		Assert.assertNotEquals(
			"Hash codes should NOT have been the same", 
			exp1.hashCode(),  exp3.hashCode());
		
		// Test inclusion of equal objects in a set
		for (MorphologicalRelative exp: new MorphologicalRelative[] {exp1, exp2, exp3}) {
			expansions.add(exp);
		}
		Assert.assertEquals(
			"Number of elements in the set was wrong", 
			2, expansions.size());
	}
	
	@Test
	public void test__morphologicalDistance__HappyPath() {
		String origWord = "takujuq";
		String[] oriMorphemes = new String[] {"taku", "juq"};
		String expansionWord = "takulauqtuq";
		String[] expansionMorphemes = new String[] {"taku", "lauq", "tuq"};
		MorphologicalRelative expansion = 
			new MorphologicalRelative(expansionWord, expansionMorphemes, 12, 
				origWord, oriMorphemes);
		
		int gotDistance = expansion.morphologicalDistance();
		int expDistance = 3;
		Assert.assertEquals(
			"Morphological distance was not as expected", 
			expDistance, gotDistance);
	}

	@Test
	public void test__morphologicalDistance__SameListOfMorphemes() {
		String origWord = "takujuq";
		String[] oriMorphemes = new String[] {"taku", "juq"};
		String expansionWord = "takuju";
		String[] expansionMorphemes = new String[] {"taku", "juq"};
		MorphologicalRelative expansion = 
			new MorphologicalRelative(expansionWord, expansionMorphemes, 12, 
				origWord, oriMorphemes);
		
		int gotDistance = expansion.morphologicalDistance();
		int expDistance = 0;
		Assert.assertEquals(
			"Morphological distance was not as expected", 
			expDistance, gotDistance);
	}
}
