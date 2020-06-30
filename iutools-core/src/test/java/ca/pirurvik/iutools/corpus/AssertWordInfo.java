package ca.pirurvik.iutools.corpus;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;
import org.junit.Assert;

public class AssertWordInfo extends Asserter<WordInfo> {

	public AssertWordInfo(WordInfo gotWinfo, String mess) {
		super(gotWinfo, mess);
	}

	protected WordInfo winfo() {
		return gotObject;
	}

	public AssertWordInfo frequencyIs(long expFreq) {
		Assert.assertEquals(
			baseMessage+"\nFrequency not as expected", 
			expFreq, winfo().frequency);
		// TODO Auto-generated method stub
		return this;
	}

	public AssertWordInfo topDecompIs(String[] expTopDecomp) throws Exception {
		AssertObject.assertDeepEquals(
			baseMessage+"\nTop decomposition was not as expected", 
			expTopDecomp, winfo().topDecomposition());
		return this;
	}
}
