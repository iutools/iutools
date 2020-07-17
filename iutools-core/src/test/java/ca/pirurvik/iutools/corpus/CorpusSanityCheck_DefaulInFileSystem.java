package ca.pirurvik.iutools.corpus;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Carries out a sanity check on the InMemory version of the default corpus
 * 
 * @author desilets
 *
 */

// TODO-June2020: Activate this test Once we have finalized the default 
// InFileSystem corpus 
@Ignore
public class CorpusSanityCheck_DefaulInFileSystem 
	extends CorpusSanityCheck {

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Override
	protected CompiledCorpus corpusToCheck() throws Exception {
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus("HANSARD-1999-2002.v2020-07-19");
		return corpus;
	}

	@Override
	protected Map<String, Object> expectations() {
		Map<String,Object> exp = new HashMap<String,Object>();
		
		exp.put("inuktut:freq", new Long(3));
		exp.put("inuktut:totDecomps", new Integer(0));
		exp.put("inuktut:sampleDecomps", null);
		
		return exp;
	}

}
