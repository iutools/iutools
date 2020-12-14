package ca.inuktitutcomputing.morph.expAlain;

import java.util.Collection;
import java.util.List;

import org.junit.Assert;

import org.iutools.testing.Asserter;

public class WrittenMorphemeCollectionAsserter extends Asserter {

	public static WrittenMorphemeCollectionAsserter assertThat(
			List<WrittenMorpheme> gotMorphemes, String mess) throws Exception {
		return new WrittenMorphemeCollectionAsserter(gotMorphemes, mess, 
			gotMorphemes.getClass());
		
	}	
	
	public WrittenMorphemeCollectionAsserter(
			Collection<WrittenMorpheme> _gotMorphemes, 
			String mess, Class<?> gotObjectClass) throws Exception {
		super(_gotMorphemes, mess, gotObjectClass);
	}

	public void containsMorpheme(String id, String surfForm) {
		WrittenMorpheme expMorpheme = new WrittenMorpheme(id, surfForm);
		boolean found = false;
		String errMess = "Colection did not contain the expected morpheme: "
			+expMorpheme+"\nGot morphemes\n";
		for (WrittenMorpheme aMorpheme: gotMorphemes()) {
			if (aMorpheme.toString().equals(expMorpheme.toString()) ) {
				found = true;
				break;
			}
			errMess += "  "+aMorpheme.toString()+",\n";
		}
		
		Assert.assertTrue(errMess, found);
	}

	private Collection<WrittenMorpheme> gotMorphemes() {
		return ((Collection<WrittenMorpheme>)gotObject);
	}
}
