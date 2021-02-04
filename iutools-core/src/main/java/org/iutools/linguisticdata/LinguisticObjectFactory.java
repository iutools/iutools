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

import org.apache.log4j.Logger;

import org.iutools.script.Orthography;
import ca.nrc.debug.Debug;
//import ca.inuktitutcomputing.data.LinguisticDataAbstract;


public abstract class LinguisticObjectFactory {


	/**
	 * Make an object of class Base with the passed attribute-value pairs and
	 * add an entry to a mapped set of canonical forms.
	 *
	 * @param linguisticDataMap a HashMap of attribute to value
	 */
	@SuppressWarnings("unchecked")
	public static synchronized void makeBase(HashMap<String,String> linguisticDataMap) throws LinguisticDataException {
		Logger logger = Logger.getLogger("ca.inuktitutcomputing.data.makeBase");
        Base base = new Base(linguisticDataMap);
        add2basesForCanonicalForm(base,"base - original");
        if (LinguisticData.getInstance().getIdToBaseTable().containsKey(base.id)) {
        	String callStack = Debug.printCallStack();
        	throw new RuntimeException(
        		"Bases ID already contains a key "+base.id+". This one is defined in "+
        		base.tableName+"."+". Check your .csv files in the linguistics data\n"+
        		"\nCall stack was:\n"+callStack);
        } else {
			LinguisticData.getInstance().addEntryToIdToBaseTable(base.id, base);
		}
        // If the root has variant forms, create a root object for each
        // one and link it to the original root.
        if (base.getVariant() != null) {
			List<String> variants = tokenize(base.getVariant());
			for (String variant : variants) {
				Base baseVariant = _makeBaseVariant(base,linguisticDataMap,variant);
				add2basesForCanonicalForm(baseVariant, "base - variant");
				LinguisticData.getInstance().addEntryToIdToBaseTable(baseVariant.id, baseVariant);
			}
        }
        // If the root has a special root for composition, create such a
        // composition object and link it to the original root.
        if (base.getCompositionRoot() != null) {
        	String compositionRoot = base.getCompositionRoot();
//        	List<String> compositionRoots = tokenize(base.getCompositionRoot());
//        	for (String compositionRoot : compositionRoots) {
				Base baseComp = _makeBaseCompositionRoot(base,linguisticDataMap,compositionRoot);
				add2basesForCanonicalForm(baseComp, "base - composition");
				// Since this object has the same morphemeid, it would replace the right object in the hash, so we don't do it
//				LinguisticData.getInstance().addEntryToIdToBaseTable(baseComp.id, baseComp);
//			}
       }
	}

	private static List<String> tokenize(String string) {
		List<String> tokens = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(string);
		while (st.hasMoreTokens()) {
			tokens.add(st.nextToken());
		}

		return tokens;
	}

	protected static Base _makeBaseCompositionRoot(Base base, HashMap<String,String> linguisticDataMap, String compositionRoot) throws LinguisticDataException {
			HashMap<String,String> linguisticDataMap_clone = (HashMap<String,String>) linguisticDataMap.clone();
			linguisticDataMap_clone.put("morpheme",compositionRoot);
			linguisticDataMap_clone.put("variant",null);
			linguisticDataMap_clone.put("originalMorpheme",base.id);
			linguisticDataMap_clone.put("compositionRoot",null);
			linguisticDataMap_clone.put("subtype","nc");
			linguisticDataMap_clone.put("nb",base.nb);
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
     * Les démonstratifs ont en fait deux surfaceFormsOfAffixes de racine: la première
     * forme est utilisée seule; la seconde forme est utilisée avec des
     * terminaisons démonstratives. Ces deux surfaceFormsOfAffixes sont définies dans
     * les enregistrements de la table Demonstratifs de la façon suivante: 
     * 1ère forme: champ "morpheme" 
     * 2ème forme: champ "racine"
     * Le champ 'racine' peut en fait contenir plus d'une valeur.
     */
	@SuppressWarnings("unchecked")
	public static synchronized void makeDemonstrative(HashMap<String,String> v) throws LinguisticDataException {
	    // 1ère forme
        Demonstrative x = new Demonstrative(v);
        add2basesForCanonicalForm(x);
        if (LinguisticData.getInstance().getIdToBaseTable().containsKey(x.id)) throw new RuntimeException("Key '"+x.id+"' already exists in linguistic data hash");

		LinguisticData.getInstance().addEntryToIdToBaseTable(x.id, x);
        // 2ème forme: créer un nouvel objet pour chaque form de racine
        String roots[] = x.getRoot().split(" ");
        for (int i=0; i<roots.length; i++) {
            HashMap<String,String> v2 = (HashMap<String,String>)v.clone();
            v2.put("morpheme", roots[i]);
            v2.put("root", roots[i]);
            Demonstrative x2 = new Demonstrative(v2, "r");
            add2basesForCanonicalForm(x2);
            LinguisticData.getInstance().addEntryToIdToBaseTable(x2.id,x2);
        }
}
	
	@SuppressWarnings("unchecked")
	public static synchronized void makePronoun(HashMap<String,String> v) throws LinguisticDataException {
	    Pronoun x = new Pronoun(v);
	    add2basesForCanonicalForm(x);
        if (LinguisticData.getInstance().getIdToBaseTable().containsKey(x.id)) throw new RuntimeException("Key '"+x.id+"' already exists in linguistic data hash");	    
		LinguisticData.getInstance().addEntryToIdToBaseTable(x.id, x);
        if (x.getVariant() != null) {
            StringTokenizer st = new StringTokenizer(x.getVariant());
            while (st.hasMoreTokens()) {
                HashMap<String,String> v2 = (HashMap<String,String>) v.clone();
                v2.put("morpheme", st.nextToken());
                v2.put("variant", null);
//				v2.put("nb", "-" + x.getNb());
				v2.put("nb", x.getNb());
                v2.put("originalMorpheme",x.id);
                Pronoun x2 = new Pronoun(v2);
                add2basesForCanonicalForm(x2);
        		LinguisticData.getInstance().addEntryToIdToBaseTable(x2.id, x2);
            }
        }
	}
	
	public static synchronized void makeSuffix(HashMap<String,String> v) throws LinguisticDataException {
        Suffix x = new Suffix(v);
        if (LinguisticData.getInstance().getIdToAffixTable().containsKey(x.id)) throw new RuntimeException("Key '"+x.id+"' already exists in linguistic data hash");        
        LinguisticData.getInstance().addEntryToIdToAffixTable(x.id,x);
       addToForms(x, x.morpheme);	    
	}
	
	public static synchronized void makeNounEnding(HashMap<String,String> v) throws LinguisticDataException {
        NounEnding x = new NounEnding(v);
        if (LinguisticData.getInstance().getIdToAffixTable().containsKey(x.id)) throw new RuntimeException("Key '"+x.id+"' already exists in linguistic data hash");        
        LinguisticData.getInstance().addEntryToIdToAffixTable(x.id,x);
        addToForms(x, x.morpheme);	    
 	}
	
	public static synchronized void makeVerbEnding(HashMap<String,String> v) throws LinguisticDataException {
        VerbEnding x = new VerbEnding(v);
        // This test with a throw is commented out to allow the execution of scripts using the linguistic database.
        // This matter will be attended to shortlty (Benoît Farley, 2019-09-17)
//      if (LinguisticData.getInstance().getIdToAffixTable().containsKey(x.id)) 
//        	throw new RuntimeException("Key '"+x.id+"' already exists in linguistic data hash");        
//    		System.err.println("Key '"+x.id+"' already exists in linguistic data hash");        
        LinguisticData.getInstance().addEntryToIdToAffixTable(x.id,x);
        addToForms(x, x.morpheme);	    
	}
	
	public static synchronized void makeDemonstrativeEnding(HashMap<String,String> v) throws LinguisticDataException {
        DemonstrativeEnding x = new DemonstrativeEnding(
                v);
        if (LinguisticData.getInstance().getIdToAffixTable().containsKey(x.id)) 
        	throw new RuntimeException("Key already exists in linguistic data hash");
//    		System.err.println("Key already exists in linguistic data hash");
        LinguisticData.getInstance().addEntryToIdToAffixTable(x.id,x);
        addToForms(x, x.morpheme);	    
	}
    
    public static synchronized void makeVerbWord(HashMap<String,String> v) throws LinguisticDataException {
        VerbWord x = new VerbWord(v);
        LinguisticData.getInstance().addVerbWord(x.verb,x);
    }
	
    public static synchronized void makeSource(HashMap<String,String> v) throws LinguisticDataException {
        Source s = new Source(v);
        LinguisticData.getInstance().addSource(s.id,s);
    }


	//-----------------------------------------------------------------

	// Les diverses surfaceFormsOfAffixes des suffixes, des terminaisons verbales et
	// nominales selon la finale du radical auquel ils se rattachent
	// et leurs comportements morphophonologiques, sont plac�es dans
	// la table de hachage des surfaceFormsOfAffixes de surface.

	public static void addToForms(
		DemonstrativeEnding ending,
		String key) throws LinguisticDataException {

		addToForms1(
			new String[] { ending.morpheme },
			key,
			ending.type,
			ending.id,
			null,
			new Action[] {Action.makeAction("neutre")},
			new Action[] {Action.makeAction(null)});
	}
	
	
	public static void addToForms(
		Affix affix,
		String key) throws LinguisticDataException {

		addToForms1(
			affix.vform,
			key,
			affix.type,
			affix.id,
			"V",
			affix.vaction1,
			affix.vaction2);
		addToForms1(
			affix.tform,
			key,
			affix.type,
			affix.id,
			"t",
			affix.taction1,
			affix.taction2);
		addToForms1(
			affix.kform,
			key,
			affix.type,
			affix.id,
			"k",
			affix.kaction1,
			affix.kaction2);
		addToForms1(
			affix.qform,
			key,
			affix.type,
			affix.id,
			"q",
			affix.qaction1,
			affix.qaction2);
	}
	

	// Si l'une des surfaceFormsOfAffixes ou des actions est inconnue, on ne
	// place pas de forme dans la table de hachage des surfaceFormsOfAffixes.

	private static void addToForms1(
		String[] altForms,
		String key,
		String type,
		String id,
		String context,
		Action[] actions1,
		Action[] actions2) throws LinguisticDataException {
//	    if (altForms != null)
//	        for (int i=0; i<altForms.length; i++)
//	            System.out.println("altForms["+i+"]= "+altForms[i]);
	    if (altForms != null)
		for (int i = 0; i < altForms.length; i++) {
			String form;
			if (!altForms[i].equals("?")
				&& actions1[i].getType() != Action.UNKNOWN
				&& actions2[i].getType() != Action.UNKNOWN) {

				if (altForms[i].equals("*"))
					form = key;
				else
					form = altForms[i];
				// Simplification de la forme (ng > N ; nng > NN).
				// La m�thode 'chercherAffixe' cherche un affixe correspondant
				// � son argument, qui a une orthographe simplifi�e.
				form = Orthography.simplifiedOrthographyLat(form);
				String form1 = actions1[i].surfaceForm(form);
				if (form1 != null) {
				    form1 = Orthography.simplifiedOrthographyLat(form1);
				    SurfaceFormOfAffix newForm =
				        new SurfaceFormOfAffix(
				                form1,
				                key,
				                id,
				                type,
				                context,
				                actions1[i],
				                actions2[i]);
				    LinguisticData.getInstance().addToSurfaceFormsOfAffixes(form1, newForm);
				}

				/*
				 * Certaines action2 peuvent aussi g�n�rer une forme sp�ciale,
				 * comme l'autod�capitation, par exemple.
				 */
				String form2 = actions2[i].surfaceForm(form);
				if (form2 != null) {
				    form2 = Orthography.simplifiedOrthographyLat(form2);
					SurfaceFormOfAffix otherForm =
						new SurfaceFormOfAffix(
						    form2,
							key,
							id,
							type,
							context,
							actions1[i],
							actions2[i]);
					LinguisticData.getInstance().addToSurfaceFormsOfAffixes(form2, otherForm);
				}
}
		}
	}

	public static void add2basesForCanonicalForm(Base x) throws LinguisticDataException {
		add2basesForCanonicalForm(x,null);
	}
	
	public static void add2basesForCanonicalForm(Base x, String caller) throws LinguisticDataException {
		Logger logger = Logger.getLogger("Data.add2basesForCanonicalForm");
		if ( x.id.equals("salliit/1n") ) {
			logger.debug("--- add2basesForCanonicalForm("+x.morpheme+") --- "+x.id);
			if (caller != null) logger.debug("    from: "+caller);
		}
		LinguisticData.getInstance().add2basesForCanonicalForm(x.morpheme, x);
    }
}
