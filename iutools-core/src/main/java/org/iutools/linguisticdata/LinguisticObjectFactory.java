/*
 * Conseil national de recherche Canada 2004/
 * National Research Council Canada 2004
 * 
 * Cr�� le / Created on 23-Sep-2004
 * par / by Benoit Farley
 * 
 */
package org.iutools.linguisticdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;


public abstract class LinguisticObjectFactory {


	/**
	 * Make an object of class Base with the passed attribute-value pairs and
	 * add an entry to a mapped set of canonical forms.
	 *
	 * @param linguisticDataMap a HashMap of attribute to value
	 */
	@SuppressWarnings("unchecked")
	public static synchronized List<Base> makeBase(HashMap<String,String> linguisticDataMap) throws LinguisticDataException {
		List<Base> objects = new ArrayList<Base>();
        Base base = new Base(linguisticDataMap);
        objects.add(base);
        // If the root has variant forms, create a root object for each one and link it to the original root.
        if (base.getVariant() != null) {
			List<String> variants = tokenize(base.getVariant());
			for (String variant : variants) {
				Base baseVariant = _makeBaseVariant(base,linguisticDataMap,variant);
				objects.add(baseVariant);
			}
        }
        // If the root has a special root for composition, create such a composition object and link it to the original root.
        if (base.getCompositionRoot() != null) {
        	Base baseComp = _makeBaseCompositionRoot(base,linguisticDataMap,base.getCompositionRoot());
        	objects.add(baseComp);
       }

        return objects;
	}

	protected static Base _makeBaseCompositionRoot(Base base, HashMap<String, String> linguisticDataMap, String compositionRoot) throws LinguisticDataException {
		HashMap<String, String> linguisticDataMap_clone = (HashMap<String, String>) linguisticDataMap.clone();
		linguisticDataMap_clone.put("morpheme", compositionRoot);
		linguisticDataMap_clone.put("variant", null);
		linguisticDataMap_clone.put("originalMorpheme", base.id);
		linguisticDataMap_clone.put("compositionRoot", null);
		linguisticDataMap_clone.put("subtype", "nc");
		linguisticDataMap_clone.put("nb", base.nb);
		Base baseComp = new Base(linguisticDataMap_clone);
		return baseComp;
	}

	private static Base _makeBaseVariant(Base base, HashMap<String,String> linguisticDataMap, String variant) throws LinguisticDataException {
		HashMap<String,String> linguisticDataMap_clone = (HashMap<String,String>) linguisticDataMap.clone();
		linguisticDataMap_clone.put("morpheme",variant);
		linguisticDataMap_clone.put("variant",null);
		linguisticDataMap_clone.put("originalMorpheme",base.id);
		Base baseVariant = new Base(linguisticDataMap_clone);
		return baseVariant;
	}


	/*
	 * Demonstratives actually produce 2 roots: the first one is used alone, as is;
	 * the second one is used with demonstrative endings. These 2 roots are defined in the CSV file
	 * in 2 fields:
     * 1st root (stand alone): field "morpheme"
     * 2nd root: field "racine" (French for 'root')
     * The field 'racine' may actually contain more than 1 value.
     */
	@SuppressWarnings("unchecked")
	public static synchronized List<Demonstrative> makeDemonstrative(HashMap<String,String> v) throws LinguisticDataException {
		List<Demonstrative> demonstratives = new ArrayList<Demonstrative>();
	    // 1st form
        Demonstrative x = new Demonstrative(v);
        demonstratives.add(x);

        // 2nd form: create a new object for each form of the root
        String roots[] = x.getRoot().split(" ");
        for (int i=0; i<roots.length; i++) {
            HashMap<String,String> v2 = (HashMap<String,String>)v.clone();
            v2.put("morpheme", roots[i]);
            v2.put("root", roots[i]);
            Demonstrative x2 = new Demonstrative(v2, "r");
            demonstratives.add(x2);
        }

        return demonstratives;
}
	
	@SuppressWarnings("unchecked")
	public static synchronized List<Pronoun> makePronoun(HashMap<String,String> v) throws LinguisticDataException {
		List<Pronoun> pronouns = new ArrayList<Pronoun>();
	    Pronoun x = new Pronoun(v);
	    pronouns.add(x);
        if (x.getVariant() != null) {
            StringTokenizer st = new StringTokenizer(x.getVariant());
            while (st.hasMoreTokens()) {
                HashMap<String,String> v2 = (HashMap<String,String>) v.clone();
                v2.put("morpheme", st.nextToken());
                v2.put("variant", null);
				v2.put("nb", x.getNb());
                v2.put("originalMorpheme",x.id);
                Pronoun x2 = new Pronoun(v2);
                pronouns.add(x2);
            }
        }

        return pronouns;
	}
	
	public static synchronized Suffix makeSuffix(HashMap<String,String> v) throws LinguisticDataException {
        Suffix suffix = new Suffix(v);
        return suffix;
	}
	
	public static synchronized NounEnding makeNounEnding(HashMap<String,String> v) throws LinguisticDataException {
        NounEnding ending = new NounEnding(v);
        return ending;
 	}
	
	public static synchronized VerbEnding makeVerbEnding(HashMap<String,String> v) throws LinguisticDataException {
        VerbEnding ending = new VerbEnding(v);
        return ending;
	}
	
	public static synchronized DemonstrativeEnding makeDemonstrativeEnding(HashMap<String,String> v) throws LinguisticDataException {
        DemonstrativeEnding ending = new DemonstrativeEnding(v);
        return ending;
	}
    
    public static synchronized VerbWord makeVerbWord(HashMap<String,String> v) throws LinguisticDataException {
        VerbWord x = new VerbWord(v);
        return x;
    }
	
    public static synchronized Source makeSource(HashMap<String,String> v) throws LinguisticDataException {
        Source s = new Source(v);
        return s;
    }


	//-----------------------------------------------------------------

	private static List<String> tokenize(String string) {
		List<String> tokens = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(string);
		while (st.hasMoreTokens()) {
			tokens.add(st.nextToken());
		}

		return tokens;
	}


}
