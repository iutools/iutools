/*
 * Conseil national de recherche Canada 2004/
 * National Research Council Canada 2004
 * 
 * Cr�� le / Created on 23-Sep-2004
 * par / by Benoit Farley
 * 
 */
package ca.inuktitutcomputing.data;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.script.Orthography;
//import ca.inuktitutcomputing.data.LinguisticDataAbstract;
import ca.inuktitutcomputing.data.Action;
import ca.inuktitutcomputing.data.Affix;
import ca.inuktitutcomputing.data.Base;
import ca.inuktitutcomputing.data.Demonstrative;
import ca.inuktitutcomputing.data.SurfaceFormOfAffix;
import ca.inuktitutcomputing.data.Pronoun;
import ca.inuktitutcomputing.data.Suffix;
import ca.inuktitutcomputing.data.DemonstrativeEnding;
import ca.inuktitutcomputing.data.NounEnding;
import ca.inuktitutcomputing.data.VerbEnding;
import ca.inuktitutcomputing.data.VerbWord;

public abstract class Data {
    
	//-----Faire les objets des morph�mes------------------------------
	
	@SuppressWarnings("unchecked")
	public static void makeBase(HashMap<String,String> v) throws LinguisticDataException {
        Base x = new Base(v);
        addToHash(x,"base - original");
        if (LinguisticData.getInstance().getIdToBaseTable().containsKey(x.id)) {
        	throw new RuntimeException("Bases ID already contains a key "+x.id+". This one is defined in "+x.tableName+"."+". Check your .csv files in the linguistics data");
//        	System.err.println("Bases ID already contains a key "+x.id+". This one is defined in "+x.tableName+".");
        }
		LinguisticData.getInstance().addEntryToIdToBaseTable(x.id, x);
        // If the root has variant forms, create a root object for each
        // one and link it to the original root.
        if (x.getVariant() != null) {
            StringTokenizer st = new StringTokenizer(x.getVariant());
            while (st.hasMoreTokens()) {
				HashMap<String,String> v2 = (HashMap<String,String>) v.clone();
                v2.put("morpheme", st.nextToken());
                v2.put("variant", null);
                v2.put("nb", "-" + x.getNb());
                v2.put("originalMorpheme",x.id);
                Base x2 = new Base(v2);
                addToHash(x2,"base - variant");
            }
        }
        // If the root has a special root for composition, create such a
        // composition object and link it to the original root.
        if (x.getCompositionRoot() != null) {
            StringTokenizer st = new StringTokenizer(x.getCompositionRoot());
            while (st.hasMoreTokens()) {
                HashMap<String,String> v2 = (HashMap<String,String>) v.clone();
                v2.put("morpheme", st.nextToken());
                v2.put("variant", null);
                v2.put("nb", "-" + x.getNb());
                v2.put("originalMorpheme",x.id);
                v2.put("compositionRoot",null);
                v2.put("subtype","nc");
                Base x2 = new Base(v2);
                addToHash(x2,"base - composition");
            }
        }
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
	public static void makeDemonstrative(HashMap<String,String> v) throws LinguisticDataException {
	    // 1ère forme
        Demonstrative x = new Demonstrative(v);
        addToHash(x);
        if (LinguisticData.getInstance().getIdToBaseTable().containsKey(x.id)) throw new RuntimeException("Key '"+x.id+"' already exists in linguistic data hash");

		LinguisticData.getInstance().addEntryToIdToBaseTable(x.id, x);
        // 2ème forme: créer un nouvel objet pour chaque form de racine
        String roots[] = x.getRoot().split(" ");
        for (int i=0; i<roots.length; i++) {
            HashMap<String,String> v2 = (HashMap<String,String>)v.clone();
            v2.put("morpheme", roots[i]);
            v2.put("root", roots[i]);
            Demonstrative x2 = new Demonstrative(v2, "r");
            addToHash(x2);
            LinguisticData.getInstance().addEntryToIdToBaseTable(x2.id,x2);
        }
}
	
	@SuppressWarnings("unchecked")
	public static void makePronoun(HashMap<String,String> v) throws LinguisticDataException {
	    Pronoun x = new Pronoun(v);
	    addToHash(x);
        if (LinguisticData.getInstance().getIdToBaseTable().containsKey(x.id)) throw new RuntimeException("Key '"+x.id+"' already exists in linguistic data hash");	    
		LinguisticData.getInstance().addEntryToIdToBaseTable(x.id, x);
        if (x.getVariant() != null) {
            StringTokenizer st = new StringTokenizer(x.getVariant());
            while (st.hasMoreTokens()) {
                HashMap<String,String> v2 = (HashMap<String,String>) v.clone();
                v2.put("morpheme", st.nextToken());
                v2.put("variant", null);
                v2.put("nb", "-" + x.getNb());
                v2.put("originalMorpheme",x.id);
                Pronoun x2 = new Pronoun(v2);
                addToHash(x2);
        		LinguisticData.getInstance().addEntryToIdToBaseTable(x2.id, x2);
            }
        }
	}
	
	public static void makeSuffix(HashMap<String,String> v) {
        Suffix x = new Suffix(v);
        if (LinguisticData.getInstance().getIdToAffixTable().containsKey(x.id)) throw new RuntimeException("Key '"+x.id+"' already exists in linguistic data hash");        
        LinguisticData.getInstance().addEntryToIdToAffixTable(x.id,x);
       addToForms(x, x.morpheme);	    
	}
	
	public static void makeNounEnding(HashMap<String,String> v) {
        NounEnding x = new NounEnding(v);
        if (LinguisticData.getInstance().getIdToAffixTable().containsKey(x.id)) throw new RuntimeException("Key '"+x.id+"' already exists in linguistic data hash");        
        LinguisticData.getInstance().addEntryToIdToAffixTable(x.id,x);
        addToForms(x, x.morpheme);	    
 	}
	
	public static void makeVerbEnding(HashMap<String,String> v) {
        VerbEnding x = new VerbEnding(v);
        // This test with a throw is commented out to allow the execution of scripts using the linguistic database.
        // This matter will be attended to shortlty (Benoît Farley, 2019-09-17)
//      if (LinguisticData.getInstance().getIdToAffixTable().containsKey(x.id)) 
//        	throw new RuntimeException("Key '"+x.id+"' already exists in linguistic data hash");        
//    		System.err.println("Key '"+x.id+"' already exists in linguistic data hash");        
        LinguisticData.getInstance().addEntryToIdToAffixTable(x.id,x);
        addToForms(x, x.morpheme);	    
	}
	
	public static void makeDemonstrativeEnding(HashMap<String,String> v) {
        DemonstrativeEnding x = new DemonstrativeEnding(
                v);
        if (LinguisticData.getInstance().getIdToAffixTable().containsKey(x.id)) 
        	throw new RuntimeException("Key already exists in linguistic data hash");
//    		System.err.println("Key already exists in linguistic data hash");
        LinguisticData.getInstance().addEntryToIdToAffixTable(x.id,x);
        addToForms(x, x.morpheme);	    
	}
    
    public static void makeVerbWord(HashMap<String,String> v) {
        VerbWord x = new VerbWord(v);
        LinguisticData.getInstance().addVerbWord(x.verb,x);
    }
	
    public static void makeSource(HashMap<String,String> v) {
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
		String key) {

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
		String key) {

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
		Action[] actions2) {
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

	public static void addToHash(Base x) throws LinguisticDataException {
		addToHash(x,null);
	}
	
	public static void addToHash(Base x, String caller) throws LinguisticDataException {
		Logger logger = Logger.getLogger("Data.addToHash");
		if (x.morpheme.equals("iglu")) {
			logger.debug("--- addToHash(iglu)");
			if (caller != null) logger.debug("    from: "+caller);
		}
		LinguisticData.getInstance().addBaseForCanonicalForm(x.morpheme, x);
    }
}
