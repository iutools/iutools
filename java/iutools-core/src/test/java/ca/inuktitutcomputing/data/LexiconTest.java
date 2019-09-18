package ca.inuktitutcomputing.data;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.junit.Test;

import ca.nrc.json.PrettyPrinter;

public class LexiconTest {

	@Test
	public void test_getFormsInContext__Case_ijaq() throws LinguisticDataException {
		LinguisticDataSingleton.getInstance("csv");
		String rootString = "iglu";
		Vector<Morpheme> roots = Lexicon.lookForBase(rootString,false);
		assertEquals("",1,roots.size());
	}

}
