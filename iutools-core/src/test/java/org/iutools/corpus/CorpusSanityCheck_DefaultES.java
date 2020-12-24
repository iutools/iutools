package org.iutools.corpus;

import java.util.HashMap;
import java.util.Map;

public class CorpusSanityCheck_DefaultES extends CorpusSanityCheck {
    @Override
    protected CompiledCorpus corpusToCheck() throws Exception {
        CompiledCorpus corpus =
            new CompiledCorpusRegistry().getCorpus();
        return corpus;
    }

    @Override
    protected Map<String, Object> expectations() {
        Map<String,Object> exp = new HashMap<String,Object>();

        exp.put("totalWords", new Long(407219));

        exp.put("inuktut:freq", new Long(5));
        exp.put("inuktut:totDecomps", new Integer(1));
        exp.put("inuktut:sampleDecomps",
                new String[][] {
                        new String[] {"{inuk/1n}", "{tut/tn-sim-s}", "\\"}
                }
        );

        exp.put("nuna:freq", new Long(-1));
        exp.put("nuna:totalWords", new Long(5011));

        return exp;
    }


}
