package ca.pirurvik.iutools.corpus;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Carries out a sanity check on the InMemory version of the default corpus
 * 
 * @author desilets
 *
 */

public class CorpusSanityCheck_DefaultInMemory extends CorpusSanityCheck {

	@Override
	protected CompiledCorpus corpusToCheck() throws Exception {
		CompiledCorpus corpus = 
			CompiledCorpusRegistry
			.getCorpus();
		return corpus;
	}

	@Override
	protected Map<String, Object> expectations() {
		Map<String,Object> exp = new HashMap<String,Object>();
		exp.put("totalWords", new Long(387303));

		exp.put("inuktut:freq", new Long(5));
		exp.put("inuktut:totDecomps", new Integer(1));
		exp.put("inuktut:sampleDecomps", 
			new String[][] {
				new String[] {"{inuk/1n}", "{tut/tn-sim-s}", "\\"}
			}
		);

		exp.put("nuna:freq", new Long(2823));
		exp.put("nuna:totalWords", new Long(2823));

		return exp;
	}

}
