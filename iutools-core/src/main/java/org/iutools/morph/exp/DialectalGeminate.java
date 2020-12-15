package org.iutools.morph.exp;

import java.util.Arrays;
import java.util.HashSet;

public class DialectalGeminate {
	
    private static String[][] groups = new String[][] {
        // C: g, j, k, l, m, n, ng, p, q, r, s, t, v
        // labC > CC : p(b), v, m
        { "bl", "ll" }, { "bj", "jj" }, { "bg", "gg" }, { "bv", "vv" },
        { "pl", "ll" }, { "pk", "kk" }, { "pg", "gg" }, { "pv", "vv" }, { "pq", "qq" },
        { "ps", "ts", "ss"},
        { "pt", "tt"},
        { "mng", "nng" }, {"mn", "nn"}, {"mp","pp"},
        // alvC > CC : t, s, &, l, n
        { "tp", "pp" }, { "tk", "kk" }, { "tj", "jj" }, { "ts", "ss", "tt" }, 
                {"t&", "ts", "tt"},
        { "&&", "tt", "ts" }, 
        { "lv", "vv" },
        {"nm","mm"},
        // velC > CC : k, g, ng
        { "kt", "tt" }, { "ks", "ss", "ts" }, { "kp", "pp" }, { "kv", "vv" },
                { "k&", "ss", "ts", "kt" },
        { "gl", "ll" }, { "gv", "vv" }, 
        { "ngm", "mm" }, { "ngn", "nn" },
        // uvuC > CC : q, r
        { "qt", "tt", "rt" }, { "q&", "r&","qs", "qt" }, {"ql", "rl"}, {"qp", "rp"}, {"qs", "rs"},
        { "rq", "qq" },
    };

	public static HashSet<String> formsWithGeminate(String morpheme) {
		HashSet<String> alternateForms = new HashSet<String>();
		String alternateForm = morpheme;
		for (int ig=0; ig<groups.length; ig++) {
			String[] group = groups[ig];
			String consonantCluster = group[0];
			String replacementCluster = group[1];
			// TODO: consider that there may be more than 1 possible replacement
			String[] alternativeConsonantClusters = Arrays.copyOfRange(group, 1, group.length);
			for (int ial=0; ial<alternativeConsonantClusters.length; ial++) {
				if (alternateForm.contains(consonantCluster)) {
					alternateForm = alternateForm.replaceAll(consonantCluster, replacementCluster);
				}
			}
		}
		return null;
	}
    
    

        
}
