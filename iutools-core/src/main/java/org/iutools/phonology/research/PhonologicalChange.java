/*
 * Conseil national de recherche Canada 2005/ National Research Council Canada
 * 2005
 * 
 * Cr�� le / Created on Apr 20, 2005 par / by Benoit Farley
 *  
 */



/*
 * I hesitate to go too deep with this because for a give dialect, the rules 
 * may not be systematically applied. As Dorais says: "It should also be noticed
 * that in North Baffin, the bilC clusters have been assimilated only about
 * a generation ago."  This means that we might be wrong in blindly applying
 * North Baffin rules to a word because we might miss something. 
 */




package org.iutools.phonology.research;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class PhonologicalChange {
	
	static char consonants[] = { 'g', 'j', 'k', 'l', 'm', 'n', 'p', 'r', 's', 't', 'v'};
	
	static char vowels[] = {'i', 'u', 'a' };

    static char alveolars[] = { 't', 's', '&', 'l', 'n' };

    static char labials[] = { 'p', 'b', 'v', 'm' };

    static char palatals[] = { 'j' };

    static char velars[] = { 'k', 'g', 'N' };

    static char uvulars[] = { 'q', 'r' };

    static String listOfDialects[] = { "aivilik", "northbaffin",
            "southeastbaffin", "southwestbaffin", "nunavik-northlabrador" };

    static Hashtable<String,Transformation[]> transfs = new Hashtable<String,Transformation[]>();

    static Hashtable<String,DialectalChangesSet> dialects = new Hashtable<String,DialectalChangesSet>();
    
    static {
    	final String alveolarsRegex = "["+new String(alveolars)+"]";
    	final String labialsRegex = "["+new String(labials)+"]";
    	final String palatalsRegex = "["+new String(palatals)+"]";
    	final String velarsRegex = "["+new String(velars)+"]";
    	final String uvularsRegex = "["+new String(uvulars)+"]";
    	final String consonantsRegex = "["+new String(consonants)+"]";
    	final String vowelsRegex = "["+new String(vowels)+"]";
    }
    
    /*
     * Starting with the stand that both consonants of a consonant clusters
     * must be of the same manner of articulation (voiceless, voiced, nasal).
     * 
     * A word about labials:
     * 
     * Mallon gives 'v' as the voicing of 'p', but perhaps it should be 'b', 
     * as 'v' is rather a fricative. This would be confirmed by the presence
     * in Spalding's dictionary of words with 'bl': ablaartuq; ablurtuq; ibluktuq;
     * itibliq; niblirtuq; qablu; qiblirtuq; 
     */
    
    /*
     * Transformations:
     * 
     *   - apr : assimilation de position régressive
     *   - afr : assimilation _ fricative régressive (?)
     *   - afp : assimilation _ fricative p?
     *   - af  : assimilation _ fricative non-régressive (2ème consonne est assimilée par 1ère consonne)
     *   - am
     *   - pal
     *   - nas
     */

    static {
    	// Note: ts is not in this list
        transfs.put("alvC.CC", new Transformation[] {
                new Transformation("tp", "pp", "apr"),
                new Transformation("tk", "kk", "apr"),
                new Transformation("tq", "qq", "apr"),
                new Transformation("tl", "ll", "apr"),
//				new Transformation("t&", "&&", "apr"), //*** see below specific transformation t&.&&
                new Transformation("tj", "jj", "apr"),
                new Transformation("lv", "vv", "apr"),
                new Transformation("lg", "gg", "apr"),
                new Transformation("lr", "rr", "apr"),
                new Transformation("jg", "gg", "apr"),
				new Transformation("nm", "mm", "apr")
				// double ng (ng+ng) is written nng in the Roman alphabet, which cannot be distinguished from n+ng
//				,new Transformation("nng", "nng", "apr")
		});

        // Note: ps is not in this list
        transfs.put("labC.CC", new Transformation[] {
                new Transformation("pt", "tt", "apr"),
                new Transformation("pk", "kk", "apr"),
                new Transformation("pq", "qq", "apr"),
                new Transformation("bv", "vv", "apr"), // should not be here: no 'b' in syllabics
                new Transformation("pv", "vv", "apr"),
                new Transformation("bl", "ll", "apr"), // should not be here: no 'b' in syllabics
                new Transformation("pl", "ll", "apr"),
                new Transformation("bj", "jj", "apr"), // should not be here: no 'b' in syllabics
                new Transformation("pj", "jj", "apr"),
                new Transformation("bg", "gg", "apr"), // should not be here: no 'b' in syllabics
                new Transformation("pg", "gg", "apr"),
                new Transformation("mn", "nn", "apr"),
                new Transformation("mng", "nng", "apr"),
                new Transformation("vg", "gg", "apr") });


        transfs.put("velC.CC", new Transformation[] {
                new Transformation("kp", "pp", "apr"),
                new Transformation("kt", "tt", "apr"),
                new Transformation("ks", "ss", "apr+afr"),
                new Transformation("gv", "vv", "apr"),
                new Transformation("gl", "ll", "apr"),
                new Transformation("gj", "jj", "apr"),
                new Transformation("ngm", "mm", "apr"),
                new Transformation("ngn", "nn", "apr"),
                });

		transfs.put("ps.ss", new Transformation[] { new Transformation("ps", "ss",
				"apr+afr") });
		transfs.put("ps.ts", new Transformation[] { new Transformation("ps", "ts",
				"apr") });
        transfs.put("ts.tt", new Transformation[] { new Transformation("ts", "tt",
                "afp") });
		transfs.put("ks.ts", new Transformation[] { new Transformation("ks", "ts",
				"apr") });

        transfs.put("t&.&&", new Transformation[] { new Transformation("t&", "&&",
                "afp") });
		transfs.put("&&.ts", new Transformation[] { new Transformation("&&", "ts",
				"afp") });
		transfs.put("&&.tt", new Transformation[] { new Transformation("&&", "tt",
				"afp") });
		transfs.put("&.s", new Transformation[] { new Transformation("&", "s",
                "afp") });
        transfs.put("nr.rng", new Transformation[] { new Transformation("nr", "rN",
                "") });
        transfs.put("mr.rng", new Transformation[] { new Transformation("mr", "rN",
                "") });

        transfs.put("itV.isV", new Transformation[] {
                new Transformation("^(?!iti)iti", "isi", "pal"),
                new Transformation("^(?!itu)itu", "isu", "pal"),
                new Transformation("^(?!ita)ita", "isa", "pal") });
        transfs.put("^sitV.tisV", new Transformation[] {
                new Transformation("^siti", "tisi", "pal"),
                new Transformation("^situ", "tisu", "pal"),
                new Transformation("^sita", "tisa", "pal") });


        transfs.put("vg.gg", new Transformation[] { new Transformation("vg", "gg",
                "apr") });
        transfs.put("vr.rr", new Transformation[] { new Transformation("vr", "rr",
                "apr") });
        transfs.put("nasal$", new Transformation[] {
                new Transformation("t$", "n", "nas"),
                new Transformation("p$", "m", "nas"),
                new Transformation("k$", "ng", "nas"),
                new Transformation("q$", "r", "nas") });
        transfs.put("^tVs.sVs", new Transformation[] {
                new Transformation("^tis", "sis", "pal"),
                new Transformation("^tus", "sus", "pal"),
                new Transformation("^tas", "sas", "pal") });
        transfs.put("C&.Cs", new Transformation[] {
                new Transformation("p&", "ps", "af"),
                new Transformation("t&", "ts", "af"),
                new Transformation("k&", "ks", "af"),
                new Transformation("q&", "qs", "af"),
                new Transformation("&&", "ts", "af") });
        transfs.put("C&.Ct", new Transformation[] {
                new Transformation("p&", "pt", "af"),
                new Transformation("t&", "tt", "af"),
                new Transformation("k&", "kt", "af"),
                new Transformation("q&", "qt", "af"),
                new Transformation("&&", "tt", "af") });
        transfs.put("V&V.VsV", new Transformation[] {
                new Transformation("i&i", "isi", "af"),
                new Transformation("i&u", "isu", "af"),
                new Transformation("i&a", "isa", "af"),
                new Transformation("u&i", "usi", "af"),
                new Transformation("u&u", "usu", "af"),
                new Transformation("u&a", "usa", "af"),
                new Transformation("a&i", "asi", "af"),
                new Transformation("a&u", "asu", "af"),
                new Transformation("a&a", "asa", "af") });
        transfs.put("V&V.VlV", new Transformation[] {
                new Transformation("i&i", "ili", "am"),
                new Transformation("i&u", "ilu", "am"),
                new Transformation("i&a", "ila", "am"),
                new Transformation("u&i", "uli", "am"),
                new Transformation("u&u", "ulu", "am"),
                new Transformation("u&a", "ula", "am"),
                new Transformation("a&i", "ali", "am"),
                new Transformation("a&u", "alu", "am"),
                new Transformation("a&a", "ala", "am") });
        transfs.put("V&V.VslV", new Transformation[] {
                new Transformation("i&i", "isi", "am"),
                new Transformation("i&u", "isu", "am"),
                new Transformation("i&a", "isa", "am"),
                new Transformation("u&i", "usi", "am"),
                new Transformation("u&u", "usu", "am"),
                new Transformation("u&a", "usa", "am"),
                new Transformation("a&i", "asi", "am"),
                new Transformation("a&u", "asu", "am"),
                new Transformation("a&a", "asa", "am"),
                new Transformation("i&i", "ili", "am"),
                new Transformation("i&u", "ilu", "am"),
                new Transformation("i&a", "ila", "am"),
                new Transformation("u&i", "uli", "am"),
                new Transformation("u&u", "ulu", "am"),
                new Transformation("u&a", "ula", "am"),
                new Transformation("a&i", "ali", "am"),
                new Transformation("a&u", "alu", "am"),
                new Transformation("a&a", "ala", "am") });


        // A 'true' as second argument to DialectalChange indicates that the
        // change may or may not take place in the dialect.
        
        /*
         * Aivilik   --- considered as a North Baffin subdialect
         * 
         * bil-C: many remain
         * api-api with optional exception: ts > tt ; otherwise api-C : assimilation > C-C with exceptions: nr > rng ; ts > tt
         */
        dialects.put("aivilik", 
                new DialectalChangesSet("aivilik",
                        new DialectalChange[] {
                		},
                        new DialectalChange[] {
                        	new DialectalChange("ts.tt", true),
                        	new DialectalChange("t&.&&"), // should not be necessary because alvC.CC does it
                        	new DialectalChange("nr.rng"),
                        	new DialectalChange("alvC.CC"),
                        	new DialectalChange("ps.ss",true),
                        	new DialectalChange("vg.gg"),
                        	new DialectalChange("vr.rr"),
                }));
        dialects.put("northbaffin",
                new DialectalChangesSet("northbaffin",
                        new DialectalChange[] {
                        	// The two next changes are not truly optional. They are
                        	// identified here as optional because since it is not
                        	// possible to tell whether the 'i' of '^sit' and the 'i'
                        	// of 'itV' are "strong" i's or not, which is the condition
                        	// for these changes taking place, we have to allow for
                        	// both possibilities.
                        	new DialectalChange("^sitV.tisV", true),
                        	new DialectalChange("itV.isV", true),
//                        	new DialectalChange("nasal$", true) // leave this for analyzer when failure, because obsolete now
                        	}, 
                		new DialectalChange[] {
                        	new DialectalChange("ts.tt"), 
                        	new DialectalChange("t&.&&"), // ??? à vérifier car = alvC.CC
                        	new DialectalChange("nr.rng"), 
                        	new DialectalChange("alvC.CC"),
                        	new DialectalChange("ps.ss"), // ??? to be checked because labC.CC takes care of it
                        	new DialectalChange("mr.rng"), 
                        	new DialectalChange("labC.CC"),
                        	new DialectalChange("velC.CC",true)
                }));
        dialects.put("southeastbaffin",
                new DialectalChangesSet("southeastbaffin",
                        new DialectalChange[] {
                        	new DialectalChange("^tVs.sVs",true), // should be ^t_s.s_s, _ standing for syllable
                        	new DialectalChange("itV.isV", true),
							new DialectalChange("&&.tt"),
							new DialectalChange("C&.Ct"),
							new DialectalChange("V&V.VslV"),
//                        	new DialectalChange("nasal$", true)
                        	},
                        new DialectalChange[] { 
                        	new DialectalChange("nr.rng"),
                        	new DialectalChange("alvC.CC"),
                        	new DialectalChange("ps.ts"),
                        	new DialectalChange("mr.rng"),
                        	new DialectalChange("labC.CC"),
                        	new DialectalChange("velC.CC",true)
                })); // *
        dialects.put("southwestbaffin",
                new DialectalChangesSet("southwestbaffin",
                        new DialectalChange[] {
							new DialectalChange("&&.ts"),
							new DialectalChange("C&.Cs"),
                        	new DialectalChange("V&V.VsV") },
                        new DialectalChange[] {
                        	new DialectalChange("nr.rng"), 
                        	new DialectalChange("alvC.CC"), 
                        	new DialectalChange("ps.ts"), 
                        	new DialectalChange("mr.rng"),
                        	new DialectalChange("labC.CC"),
                        	new DialectalChange("velC.CC",true)
                        	}));  // *
        dialects.put("nunavik-northlabrador",
                new DialectalChangesSet("nunavik-northlabrador",
                        new DialectalChange[] {
                        	new DialectalChange("^sitV.tisV", true),  // should be removed
                        	new DialectalChange("itV.isV", true), // should be removed
                        	new DialectalChange("&&.ts"), 
                        	new DialectalChange("&.s"), 
//                        	new DialectalChange("nasal$", true)
                        	}, 
                        new DialectalChange[] {
                        	new DialectalChange("nr.rng"), 
                        	new DialectalChange("alvC.CC"), 
                        	new DialectalChange("ps.ts"),
                        	new DialectalChange("ks.ts"),
                        	new DialectalChange("mr.rng"),
                        	new DialectalChange("labC.CC"),
                        	new DialectalChange("velC.CC"),
//                        	new DialectalChange("uvuC.CC",true) // North Labrador
                        	})); // *
    }
    
    
    public static Set<String> formsInAllDialects(String morpheme) {
    	Set<String> allForms = new HashSet<String>();
    	for (int idial=0; idial<listOfDialects.length; idial++) {
    		List<String> formsInDialect = formsInDialect(morpheme,listOfDialects[idial]);
    		allForms.addAll(formsInDialect);
    	}
    	
    	return allForms;
    }
    
    
    public static List<String> formsInDialect(String morpheme, String dialect) {
        DialectalChangesSet setOfChanges = (DialectalChangesSet) dialects.get(dialect);
        List<String> formsInDialect = applySetOfChanges(morpheme,setOfChanges);
        
    	return formsInDialect;
    }
    
    

    
    
    private static List<String> applySetOfChanges(String morpheme, DialectalChangesSet setOfChanges) {
    	Logger logger = LogManager.getLogger("PhonologicalChange.applySetOfChanges");
    	logger.debug("setOfChanges: "+setOfChanges.dialect);
    	DialectalChange firstSubsetOfChanges[] = setOfChanges.firstSubsetOfChanges;
    	DialectalChange secondSubsetOfChanges[] = setOfChanges.secondSubsetOfChanges;
    	List<DialectalChange> allChanges = new ArrayList<DialectalChange>();
    	allChanges.addAll(Arrays.asList(firstSubsetOfChanges));
    	allChanges.addAll(Arrays.asList(secondSubsetOfChanges));
    	List<String> formsInDialect = applyChanges(morpheme,allChanges);
		
    	return formsInDialect;
	}



    /*
     * Apply each change one after the other to the result of the last change,
     * starting with the original string. If a change is optional, there are
     * 2 results: the string before the change and the string after the change.
     * Processing must continue with both results instead of the one.
     *
     * This goes only 1 way. For example, it will return allak from aglak,
     * but it will not return aglak from allak.
     */

	private static List<String> applyChanges(String morpheme, List<DialectalChange> listOfChanges) {
    	Logger logger = LogManager.getLogger("PhonologicalChange.applyChanges");
		List<String> formsToProcessWithNextChange = new ArrayList<String>();
		formsToProcessWithNextChange.add(morpheme);
		for (int ichange = 0; ichange < listOfChanges.size(); ichange++) {
			DialectalChange change = listOfChanges.get(ichange);
	    	logger.debug("change: "+change.name);
			Transformation transformations[] = change.getTransformations();
			if (transformations==null) logger.debug("transformations = null");
			List<String> resultsOfChange = new ArrayList<String>();
			for (int inextform = 0; inextform < formsToProcessWithNextChange.size(); inextform++) {
				String formToProcess = formsToProcessWithNextChange.get(inextform);
				if (change.optional) {
					resultsOfChange.add(formToProcess);
				} 
				for (int itransf = 0; itransf < transformations.length; itransf++) {
					formToProcess = transformations[itransf].applyTo(formToProcess);
				}
				if (!resultsOfChange.contains(formToProcess))
					resultsOfChange.add(formToProcess);
			}
			formsToProcessWithNextChange = resultsOfChange;
		}

		return formsToProcessWithNextChange;
	}




    
    //------------------------------- CLASSES -------------------------------

    static class Transformation {
        public String from;
        public String to;
        public String method;

        public Transformation(String from, String to, String method) {
            this.from = from;
            this.to = to;
            this.method = method;
        }

		/**
		 * Apply the transformation of the regex 'from' to the string
		 *
		 * @param string
		 * @return
		 */
		public String applyTo(String string) {
			String transformedString = string.replaceAll(from, to);

        	return transformedString;
        }
    }

    static class DialectalChange {
        public String name;

        public boolean optional;

        public String dialect;

        public DialectalChange(String name, boolean optional) {
            this.name = name;
            this.optional = optional;
        }

        public DialectalChange(String name) {
            this.name = name;
            optional = false;
        }
        
        public Transformation[] getTransformations() {
            return (Transformation[])transfs.get(name);
        }
    }

}


class DialectalChangesSet {
    
    public String dialect;
    public PhonologicalChange.DialectalChange firstSubsetOfChanges[];
    public PhonologicalChange.DialectalChange secondSubsetOfChanges[];
    
    public DialectalChangesSet(String dialect,
            PhonologicalChange.DialectalChange _firstSubsetOfChanges[],
            PhonologicalChange.DialectalChange _secondSubsetOfChanges[]) {
        this.dialect = dialect;
        this.firstSubsetOfChanges = _firstSubsetOfChanges;
        this.secondSubsetOfChanges = _secondSubsetOfChanges;
    }
}


//class Match implements Comparable {
//    public Integer position;
//
//    String operation;
//
//    public PhonologicalChange.Transformation changes;
//
//    public Boolean optional;
//
//    /**
//     * @param oper
//     * @param integer
//     * @param objects
//     * @param boolean1
//     */
//    public Match(Integer pos, String oper, PhonologicalChange.Transformation chgs,
//            Boolean opt) {
//        position = pos;
//        operation = oper;
//        changes = chgs;
//        optional = opt;
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see java.lang.Comparable#compareTo(java.lang.Object)
//     */
//    public int compareTo(Object arg0) {
//        Match obj = (Match) arg0;
//        String x = "?????????? ????????? ???????";
//        return position.compareTo(obj.position);
//    }
//
//}