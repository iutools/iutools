/*
 * Conseil national de recherche Canada 2003
 * 
 * Cr�� le 25-Nov-2003 par Benoit Farley
 *  
 */
package ca.inuktitutcomputing.data;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.phonology.Dialect;
import ca.inuktitutcomputing.dataCSV.LinguisticDataCSV;
//import ca.inuktitutcomputing.dataCompiled.LinguisticDataCompiled;
import ca.inuktitutcomputing.script.Orthography;
import ca.inuktitutcomputing.script.Roman;
import ca.pirurvik.iutools.text.segmentation.MyStringTokenizer;

public abstract class LinguisticDataAbstract {

//    static Hashtable<String,String[]> textualRenderings;
//    static Hashtable<String,Vector<Example>> examples;

//    protected static Hashtable<String,Vector<SurfaceFormOfAffix>> surfaceFormsOfAffixes = new Hashtable<String,Vector<SurfaceFormOfAffix>>();
//    protected static Hashtable<String,Vector<Morpheme>> morphemesForCanonicalForm = new Hashtable<String,Vector<Morpheme>>();  // MOVED TO LINGUISTICDATA
//    protected static Hashtable<String,Base> idToBaseTable = new Hashtable<String,Base>(); // MOVED TO LINGUISTICDATA
//    protected Hashtable demonstrativesId;
//    protected static Hashtable<String,Affix> idToAffixTable = new Hashtable<String,Affix>();

    // RENDU ICI AD
//    protected static Hashtable<String,VerbWord> words = new Hashtable<String,VerbWord>();
//    protected static Hashtable<String,Source> sources;
    
//    static public Hashtable<Character,Vector<String>> groupsOfConsonants;

    /*
     * source: "csv" - from the .csv files
     *         null - from the dumped database
     * type:   "r" - roots
     *         "s" - suffixes
     */
    public static boolean init() throws LinguisticDataException {
    	return init(null, null);
    }
    
    public static boolean init(String source) throws LinguisticDataException {
    	return init(source, null);
    }
    
    public static boolean init(String source, String type) throws LinguisticDataException {
    	initializeData();
    	if (source != null && !source.equals("csv") && !source.equals("compiled"))
    		return false;
    	if (type != null && !type.equals("r") && !type.equals("s"))
    		return false;
//        makeHashOfTextualRenderings();
//        makeHashOfExamples();
//        if (source==null || source.equals("compiled"))
//            database = new LinguisticDataCompiled(type);
//        else if (source.equals("csv"))
//        LinguisticDataCSV database = new LinguisticDataCSV(type);
        LinguisticDataCSV.createLinguisticDataCSV(type);
        if (type==null || type.equals("s")) {
            // Ajouter le suffixe d'inchoativité.
            // 13 mars 2006: pour le moment, on décide de ne pas l'utiliser, mais
            // de plutôt placer dans la table des racines celles qui résultent de
            // ce processus, comme ikummaq- < ikuma-
//            Suffix spec = new Inchoative();
//            if (LinguisticDataAbstract.getAffixesIds().containsKey(spec.id)) {
//            	throw new RuntimeException("Key '"+spec.id+"' already exists in linguistic data hash");  
//            }
//            affixesId.put(spec.id, spec);
//            Data.addToForms(spec, spec.morpheme);
        }
        makeGroupsOfConsonants();
        return true;
    }
    
    public static void initializeData() {
//    	examples = new Hashtable<String,Vector<Example>>();
//    	textualRenderings = new Hashtable<String,String[]>();
//    	surfaceFormsOfAffixes = new Hashtable<String,Vector<SurfaceFormOfAffix>>();
//    	morphemesForCanonicalForm = new Hashtable<String,Vector<Morpheme>>();
//    	idToBaseTable = new Hashtable<String,Base>();
//    	idToAffixTable = new Hashtable<String,Affix>();
//        words = new Hashtable<String,VerbWord>();
//        sources = new Hashtable<String,Source>();
//        groupsOfConsonants = new Hashtable<Character,Vector<String>>();
    }
    
    private static void makeGroupsOfConsonants() {
    	if (LinguisticDataAbstract.getFormToBasesTable() != null) {
    		String [] keys = LinguisticDataAbstract.getAllBasesKeys();
    		for (int i=0; i<keys.length; i++) {
    			String key = Orthography.simplifiedOrthographyLat(keys[i]);
                addToGroupsOfConsonants(key);
    		}
    	}
    	if (LinguisticDataAbstract.getFormToSurfaceFormsOfAffixesTable() != null) {
    		String [] forms = getAllAffixesSurfaceFormsKeys();
    		for (int i=0; i<forms.length; i++)
                addToGroupsOfConsonants(forms[i]);
    	}
    	String keys[] = Dialect.getKeys();
    	for (int i=0; i<keys.length; i++)
    		addToGroupsOfConsonants(keys[i]);
    }
    
	private static void addToGroupsOfConsonants(String str) {
//        char chars[] = str.toCharArray();
//        for (int i=0; i<chars.length-1; i++) {
//            if (Roman.isConsonant(chars[i]) &&
//                    Roman.isConsonant(chars[i+1])) {
//                Character charac = new Character(chars[i+1]);
//                Vector<String> grCons;
//                if (groupsOfConsonants.containsKey(charac))
//                    grCons = groupsOfConsonants.get(charac);
//                else
//                    grCons = new Vector<String>();
//                String newGr = new String(new char[]{chars[i],chars[i+1]});
//                if (!grCons.contains(newGr))
//                    grCons.add(newGr);
//                groupsOfConsonants.put(charac,grCons);
//            }
//        }
        
        LinguisticData.getInstance().addToGroupsOfConsonants(str);
    }

    public static Hashtable<Character,Vector<String>> getGroupsOfConsonants() {
//    	return groupsOfConsonants;
    	
    	return LinguisticData.getInstance().getGroupsOfConsonants();
    }
    
    
    // The suffix table has a field where the components of
    // composite suffixes are defined.  This method adds an
    // entry in a hashtable for each such combination, with
    // the first component as the key of the entry.
//    public void addToCombinationTable() {
//        
//    }

    //--------------------------------------------------------------


    public static Morpheme getMorpheme(String morphId) {
    	Logger logger = Logger.getLogger("LinguisticDataAbstract.getMorpheme");
        Morpheme morph;
        morph = getBaseWithId(morphId);
        if (morph==null) {
            morph = getAffixWithId(morphId);
            if (morph==null) logger.debug("null affix: "+morphId);
        }
        return morph;
    }
    
    
    protected static VerbWord getVerbWord(String term) {
//        VerbWord mv = words.get(term);
//        return mv;
        
        return LinguisticData.getInstance().getVerbWord(term);
    }

    protected static Source getSource(String sourceId) {
//        Source s = sources.get(sourceId);
//        return s;
        
        return LinguisticData.getInstance().getSource(sourceId);
    }

    /**
     * Returns a Vector of Base and Demonstrative objects, or null.
     * @param term String string in the ICI (Inuit Cultural Institute) standard
     * @return Vector<Morpheme> a vector of Morpheme objects or null
     */
    @SuppressWarnings("unchecked")
	public static Vector<Morpheme> getBasesForCanonicalForm(String term) {
//        Vector<Morpheme> bs = null;
//        Vector<Morpheme> gets = bases.get(term);
//        if (gets != null)
//            bs = (Vector<Morpheme>) gets.clone();
//        return bs;
    	
    	Vector<Morpheme> bases = LinguisticData.getInstance().getBasesForCanonicalForm(term);
    	return bases;
    }
    
    protected static Vector<Example> getExample(String key) {
//        return examples.get(key);
        
        return LinguisticData.getInstance().getExample(key);
    }
    

    
    public static Base getBaseWithId(String morphId) {
//    	Base b = null;
//    	if (idToBaseTable != null)
//    		b = idToBaseTable.get(morphId);
//        return b;

        Base baseObject = LinguisticData.getInstance().getBaseWithId(morphId);
        return baseObject;
    }
    
    protected static Base getBaseFromMorphemeIdObject(Morpheme.Id morphId) {
//    	Base b = null;
//    	if (idToBaseTable != null)
//    		b = idToBaseTable.get(morphId.id);
//        return b;

        Base baseObject = LinguisticData.getInstance().getBaseFromMorphemeIdObject(morphId);
        return baseObject;
    }
    
    /**
     * 
     * @param form String string to be searched as a possible surface form of an affix
     * @return Vector<SurfaceFormOfAffix> vector of objects that describe surface forms in the context (stem ending and actions) or null
     */
    // The keys of the hashtable 'surfaceFormsOfAffixes' are in the
    // simplified spelling (ng > N).  To search for a form
    // in the ICI spelling, one calls this method.
    protected static void addToSurfaceFormsOfAffixes(String str, SurfaceFormOfAffix form) {
//        String simplifiedForm = Orthography.simplifiedOrthographyLat(str);
//		Vector<SurfaceFormOfAffix> v = surfaceFormsOfAffixes.get(simplifiedForm);
//		if (v == null)
//			v = new Vector<SurfaceFormOfAffix>();
//		v.add(form);
//		// OK... 
////		if (surfaceFormsOfAffixes.containsKey(simplifiedForm)) 
////			throw new RuntimeException("Key already exists in linguistic data hash");
//		surfaceFormsOfAffixes.put(simplifiedForm, v);
		
		LinguisticData.getInstance().addToSurfaceFormsOfAffixes(str, form);
    }
    
    public static Hashtable<String,Vector<SurfaceFormOfAffix>> getFormToSurfaceFormsOfAffixesTable() {
//		return surfaceFormsOfAffixes;
		
		return LinguisticData.getInstance().getFormToSurfaceFormsOfAffixesTable();
	}

    public static Vector<SurfaceFormOfAffix> getSurfaceForms(String form) {
//        // Simplify the spelling
        String simplifiedForm = Orthography.simplifiedOrthographyLat(form);
        return getFormToSurfaceFormsOfAffixesTable().get(simplifiedForm);
        
//        return LinguisticData.getInstance().getSurfaceForms(form);
    }
    
    public static SurfaceFormOfAffix getForm(String morph) {
    	return LinguisticDataAbstract.getSurfaceForms(morph).elementAt(0);
    	
//    	return LinguisticData.getInstance().getForm(morph);
    }
    
    public static String[] getAllAffixesSurfaceFormsKeys() {
    	return (String[]) getFormToSurfaceFormsOfAffixesTable().keySet().toArray(new String[] {});
    	
//    	return LinguisticData.getInstance().getAllAffixesSurfaceFormsKeys();
    }
    
    
    
    
    protected static String[] getAllExamplesIds() {
//    	return (String[])examples.keySet().toArray(new String[0]);
    	
    	return LinguisticData.getInstance().getAllExamplesIds();
    }
    
    public static Hashtable<String, Base> getIdToBaseTable() {
//    	return idToBaseTable;
    	
    	return LinguisticData.getInstance().getIdToBaseTable();
    }
    
    public static String[] getAllBasesIds() {
//    	return (String[])idToBaseTable.keySet().toArray(new String[0]);
    	
    	return LinguisticData.getInstance().getAllBasesIds();
    }

    
    //------------------------ id2AffixTable -----------------------------
    
    public static void addEntryToIdToAffixTable(String idOfAffix, Affix affix) {
//    	getIdToAffixTable().put(idOfAffix,affix);
    	
    	LinguisticData.getInstance().addEntryToIdToAffixTable(idOfAffix, affix);
    }
    
	protected static Hashtable<String, Morpheme> getId2SuffixTable() {
//		Hashtable<String, Morpheme> table = new Hashtable<String, Morpheme>();
//		for (Enumeration<String> affixIds = idToAffixTable.keys(); affixIds.hasMoreElements();) {
//			String affixId = affixIds.nextElement();
//			Affix aff = (Affix) idToAffixTable.get(affixId);
//			if (aff.getClass().getName().equals("Suffix"))
//				table.put(affixId, (Morpheme)aff);
//		}
//		return table;
		
		return LinguisticData.getInstance().getId2SuffixTable();
	}
    

    public static Hashtable<String, Affix> getIdToAffixTable() {
//    	return idToAffixTable;
    	
    	return LinguisticData.getInstance().getIdToAffixTable();
    }
    
    public static String[] getAllAffixesIds() {
//    	return (String[])idToAffixTable.keySet().toArray(new String[0]);
    	
    	return LinguisticData.getInstance().getAllAffixesIds();
    }

// APPELÉE NULLE PART
//    public static String[] getAllSuffixesIds() {
//    	Hashtable<String,Morpheme> suffixes = getId2SuffixTable();
//    	String [] suffixesIds = new String[suffixes.size()];
//    	int i=0;
//    	for (Enumeration<String> keys = suffixes.keys(); keys.hasMoreElements();) {
//    		Suffix suf = (Suffix)suffixes.get(keys.nextElement());
//    		suffixesIds[i++] = suf.id;
//    	}
//    	return suffixesIds;
//    	
////		return LinguisticData.getInstance().getAllSuffixesIds();
//
//    }
    
    public static Affix getAffixWithId(String uniqueId) {
//        Affix aff = (Affix)idToAffixTable.get(uniqueId);
//        return aff;
        
        return LinguisticData.getInstance().getAffixWithId(uniqueId);
    }

    public static Suffix getSuffixWithId(String uniqueId) {
//        Suffix afs = (Suffix)idToAffixTable.get(uniqueId);
//        return afs;
        
        return LinguisticData.getInstance().getSuffixWithId(uniqueId);
   }

    //------------------------ id2AffixTable -----------------------------
    
    protected static Hashtable<String,VerbWord> getWords() {
//    	return words;
    	
    	return LinguisticData.getInstance().getWords();
    }
    
    public static void addVerbWord(String verbWordForm, VerbWord wordObject) {
        LinguisticDataAbstract.getWords().put(verbWordForm,wordObject);
        
        LinguisticData.getInstance().addVerbWord(verbWordForm,wordObject);
    }

    protected static String[] getAllVerbWordsForms() {
//    	return (String[])words.keySet().toArray(new String[0]);
    	
    	return LinguisticData.getInstance().getAllVerbWordsForms();
    }
    
    

    protected static Hashtable<String,Source> getIdToSourceTable() {
//    	return sources;
    	
    	return LinguisticData.getInstance().getIdToSourceTable();
    }
    protected static String[] getAllSourceIds() {
//    	return (String[])sources.keySet().toArray(new String[0]);
    	
    	return LinguisticData.getInstance().getAllSourceIds();
    }
    
    public static void addSource(String sourceId, Source sourceObject) {
//    	sources.put(sourceId,sourceObject);
    	
    	LinguisticData.getInstance().addSource(sourceId, sourceObject);
    }


	protected static Hashtable<String, Morpheme> getIdToRootTable() {
//		Hashtable<String, Morpheme> table = new Hashtable<String, Morpheme>();
//		String clazz = Base.class.getName();
//		for (Enumeration<String> rootIds = idToBaseTable.keys(); rootIds.hasMoreElements();) {
//			String rootId = rootIds.nextElement();
//			Object obj = idToBaseTable.get(rootId);
//			if (obj.getClass().getName() == clazz) {
//				Base root = idToBaseTable.get(rootId);
//				table.put(rootId, root);
//			}
//		}
//		return table;
		
		return LinguisticData.getInstance().getIdToRootTable();
	}
    
    protected static Hashtable<String,Vector<Morpheme>> getFormToBasesTable() {
//    	return bases;
    	return LinguisticData.getInstance().getBasesForAllCanonicalForms_hashtable();
    }
    protected static String [] getAllBasesKeys() {
//    	return (String[]) bases.keySet().toArray(new String[0]); 
    	String[] keys = LinguisticData.getInstance().getCanonicalFormsForAllBases();
    	return keys;
    }
    
    protected static Hashtable<String,Base> getIdToGiVerbsTable() {
    	return LinguisticData.getInstance().getIdToGiVerbsTable();
    }
    
    protected static Base [] getGiVerbs() {
//    	Hashtable<String,Base> giverbsHash = new Hashtable<String,Base>();
//    	Hashtable<String,Morpheme> bases = getIdToRootTable();
//    	for (Enumeration<String> keys = bases.keys(); keys.hasMoreElements();) {
//    		String key = keys.nextElement();
//    		Base base = (Base)bases.get(key);
//    		if (base.isGiVerb())
//    			giverbsHash.put(key, base);
//    	}
//    	String [] giverbsKeysArray = (String[])giverbsHash.keySet().toArray(new String[]{});
//    	Arrays.sort(giverbsKeysArray);
//    	Base [] giverbs = new Base[giverbsKeysArray.length];
//    	for (int i=0; i<giverbsKeysArray.length; i++)
//    		giverbs[i] = (Base)giverbsHash.get(giverbsKeysArray[i]);
//    	return giverbs;
    	
    	return LinguisticData.getInstance().getGiVerbs();
    }

    
    protected static Hashtable<String,Demonstrative> getIdToDemonstrativeTable() {
//        Hashtable<String,Demonstrative> hash = Demonstrative.hash;
//        if (hash.size()==0) {
//            String clazz = Demonstrative.class.getName();
//            for (Enumeration<String> keys = idToBaseTable.keys(); keys.hasMoreElements();) {
//                Object key = keys.nextElement();
//                Object obj = idToBaseTable.get(key);
//                if (obj.getClass().getName()==clazz) {
//                    Demonstrative dem = (Demonstrative)idToBaseTable.get(key);
//                    dem.addToHash((String)key,dem);
//                }
//            }
//        }
//        return Demonstrative.hash;
        
        return LinguisticData.getInstance().getIdToDemonstrativeTable();
    }

    
    protected static String getTextualRendering(String key, String lang) {
//        return _getTextualRendering(key,lang);
        
        return LinguisticData.getInstance().getTextualRendering(key,lang);
    }

    
//    protected static String getTextualRendering(String key, String lang, 
//    		String additionalText,int position) {
//        String res = _getTextualRendering(key,lang);
//        if (additionalText != null) {
//            if (position == 0)
//                res = additionalText + " " + res;
//            else
//                res = res + " " + additionalText;
//        }
//        String keyPlus = key + "+";
//        Object valPlus = textualRenderings.get(keyPlus);
//        if (valPlus != null) {
//            String[] textualRenderingsPlus = (String[]) valPlus;
//            res = res + ".  "
//                    + (lang.equals("en") ? textualRenderingsPlus[0] : textualRenderingsPlus[1]);
//        }
//        return res;
//    }
//
//    protected static String _getTextualRendering(String key, String lang) {
//        Object val = textualRenderings.get(key);
//        String res = null;
//        if (val == null)
//            res = "";
//        else {
//            String[] txt = (String[]) val;
//            res = lang.equals("en") ? txt[0] : txt[1];
//        }
//        return res;
//    }
    
//    private static boolean makeHashOfTextualRenderings() {
//        textualRenderings = new Hashtable<String,String[]>();
//        textualRenderings.put("dec", new String[] { "declarative", "déclaratif" });
//        textualRenderings.put("int", new String[] { "interrogative", "interrogatif" });
//        textualRenderings.put("imp", new String[] { "imperative", "impératif" });
//        textualRenderings.put("part", new String[] { "participle", "participe" });
//        textualRenderings.put("prespas", new String[] { "present and past",
//                "présent et passé" });
//        textualRenderings.put("fut", new String[] { "future", "futur" });
//        textualRenderings.put("pos", new String[] { "positive", "positif" });
//        textualRenderings.put("neg", new String[] { "negative", "négatif" });
//        textualRenderings.put("caus", new String[] { "becausative", "causatif" });
//        textualRenderings.put("freq", new String[] { "frequentative", "fréquentatif" });
//        textualRenderings.put("cond", new String[] { "conditional", "conditionnel" });
//        textualRenderings.put("dub", new String[] { "dubitative", "dubitatif" });
//        textualRenderings.put("tv",
//                new String[] { "verbal ending", "terminaison verbale" });
//        textualRenderings.put("q", new String[] { "tail suffix", "suffixe de queue" });
//        textualRenderings.put("tn",
//                new String[] { "noun ending", "terminaison nominale" });
//        textualRenderings.put("sv", new String[] { "verbal suffix", "suffixe verbal" });
//        textualRenderings.put("sn", new String[] { "noun suffix", "suffixe nominal" });
//        textualRenderings
//                .put("function",
//                        new String[] { "producing a", "produisant un" });
//        textualRenderings.put("vv", new String[] { "verb-to-verb", "verbe-à-verbe" });
//        textualRenderings.put("nv", new String[] { "noun-to-verb", "nom-à-verbe" });
//        textualRenderings.put("vn", new String[] { "verb-to-noun", "verbe-à-nom" });
//        textualRenderings.put("nn", new String[] { "noun-to-noun", "nom-à-nom" });
//        textualRenderings.put("nsp", new String[] { "non-specific", "non-spécifique" });
//        textualRenderings.put("sp", new String[] { "specific", "spécifique" });
//        textualRenderings.put("s", new String[] { "singular", "singulier" });
//        textualRenderings.put("d", new String[] { "dual", "duel" });
//        textualRenderings.put("p", new String[] { "plural", "plural" });
//        textualRenderings.put("n", new String[] { "noun", "nom" });
//        textualRenderings.put("v", new String[] { "verb", "verbe" });
//        textualRenderings.put("a", new String[] { "adverb", "adverbe" });
//        textualRenderings.put("e", new String[] { "expression or exclamation",
//                "expression ou exclamation" });
//        textualRenderings.put("c", new String[] { "conjunction", "conjonction" });
//        textualRenderings.put("pr", new String[] { "pronoun", "pronom" });
//        textualRenderings.put("m", new String[] { "medial", "médiane" });
//        textualRenderings.put("t", new String[] { "terminal", "terminale" });
//        textualRenderings.put("f", new String[] { "final", "finale" });
//        textualRenderings.put("V", new String[] { "vowel", "voyelle" });
//        textualRenderings.put("C", new String[] { "consonant", "consonne" });
//        textualRenderings.put("VV", new String[] { "vowels", "voyelles" });
//        textualRenderings.put("VC", new String[] { "vowel or consonant",
//                "voyelle ou consonne" });
//        textualRenderings.put("1ordinal", new String[] { "1st", "1ère" });
//        textualRenderings.put("2ordinal", new String[] { "2nd", "2ème" });
//        textualRenderings.put("3ordinal", new String[] { "3rd", "3ème" });
//        textualRenderings.put("4ordinal", new String[] { "4th", "4ème" });
//        textualRenderings.put("personne", new String[] { "person", "personne" });
//
//        textualRenderings.put("nom", new String[] { "nominative", "nominatif" });
//        textualRenderings.put("gen", new String[] { "genitive", "génitif" });
//        textualRenderings.put("acc", new String[] { "accusative", "accusatif" });
//        textualRenderings.put("abl", new String[] { "ablative", "ablatif" });
//        textualRenderings.put("dat", new String[] { "dative", "datif" });
//        textualRenderings.put("loc", new String[] { "locative", "locatif" });
//        textualRenderings.put("sim", new String[] { "similaris", "similaris" });
//        textualRenderings.put("via", new String[] { "vialis", "vialis" });
//        textualRenderings.put("possessif", new String[] { "possessive", "possessif" });
//        textualRenderings.put("possesseur", new String[] { "Possessor", "Possesseur" });
//        textualRenderings.put("vt", new String[] { "transtitive", "transitif" });
//        textualRenderings.put("vt1", new String[] { "transtitive", "transitif" });
//        textualRenderings.put("vt2", new String[] { "transtitive", "transitif" });
//        textualRenderings.put("vi", new String[] { "intransitive", "transitive" });
//        textualRenderings.put("va", new String[] { "adjectival", "adjectif" });
//        textualRenderings.put("ve", new String[] { "emotion, feeling",
//                "d'émotion,de sentiment" });
//        textualRenderings.put("vres", new String[] { "result", "de résultat" });
//        textualRenderings
//                .put(
//                        "vres+",
//                        new String[] {
//                                "When this kind of verb is used transitively, the thing upon which the action "
//                                        + "is done is the object of the verb.  When it is used intransitively, that thing "
//                                        + "is the subject of the verb.  Similar verbs in English: to boil, to shatter.",
//                                "Quand ce type de verbe est utilisé transitivement, la chose sur laquelle porte l'action "
//                                        + "est l'objet du verbe.  Quand il est utilisé intransitivement, cette chose "
//                                        + "est le sujet du verbe." });
//        textualRenderings
//                .put(
//                        "m!",
//                        new String[] {
//                                "must be followed by another suffix, i.e. it cannot occur in word-final position",
//                                "doit être suivi d'un autre suffixe, i.e. il ne peut pas se trouver à la fin d'un mot" });
//        textualRenderings
//                .put(
//                        "f!",
//                        new String[] {
//                                "occurs only in word-final position, i.e. it cannot be followed by another suffix",
//                                "ne peut se trouver qu'à la fin d'un mot, i.e. il ne peut pas être suivi d'un autre suffixe" });
//        textualRenderings
//                .put(
//                        "t!",
//                        new String[] {
//                                "may occur in word-final position, but may also be followed by additional suffixes",
//                                "peut se trouver à la fin d'un mot, mais peut cependant être suivi d'autres suffixes" });
//        textualRenderings.put("neutre",
//                new String[] { "does not affect", "n'affecte pas" });
//        textualRenderings.put("suppr", new String[] { "deletes", "supprime" });
//        textualRenderings.put("suppr1", new String[] { "is deleted", "est supprimé" });
//        textualRenderings.put("nasal", new String[] { "nasalizes", "nasalise" });
//        textualRenderings.put("nasal1", new String[] { "is nasalized to",
//                "est nasalisé en" });
//        textualRenderings.put("sonor", new String[] { "vocalizes", "sonorise" });
//        textualRenderings.put("assim", new String[] { "assimilates", "assimile" });
//        textualRenderings.put("assim2", new String[] { "to", "précédent à" });
//        textualRenderings.put("allonge", new String[] { "lengthens", "allonge" });
//        textualRenderings.put("fusion", new String[] { "fusions", "fusionne" });
//        textualRenderings.put("sonor", new String[] { "vocalizes", "sonorise" });
//        textualRenderings.put("sur", new String[] { "on", "sur" });
//        textualRenderings.put("en", new String[] { "into", "en" });
//        textualRenderings.put("au", new String[] { "to the", "au" });
//        textualRenderings.put("du", new String[] { "of the", "du" });
//        textualRenderings.put("à", new String[] { "to", "à" });
//        textualRenderings.put("avec", new String[] { "with", "avec" });
//        textualRenderings.put("voyellefinale", new String[] { "end vowel",
//                "voyelle finale" });
//        textualRenderings.put("finale", new String[] { "final", "finale" });
//        textualRenderings.put("le", new String[] { "the", "le" });
//        textualRenderings.put("la", new String[] { "the", "la" });
//        textualRenderings.put("duradical", new String[] { "of the stem", "du radical" });
//        textualRenderings
//                .put("dusuffixe",
//                        new String[] { "of the suffix", "du suffixe" });
//        textualRenderings.put("ins1", new String[] { "inserts", "insére" });
//        textualRenderings.put("devantsuffixe", new String[] { "in front of the suffix",
//                "devant le suffixe" });
//        textualRenderings.put("derniereVoyelle", new String[] { "last vowel",
//                "dernière voyelle" });
//        textualRenderings.put("casVV", new String[] { "the stem ends with 2 vowels",
//                "le radical se termine par 2 voyelles" });
//        textualRenderings.put("supprv2", new String[] { "the second vowel is deleted",
//                "la seconde voyelle est supprimée" });
//        textualRenderings.put("après", new String[] { "After", "Après" });
//        textualRenderings.put("une", new String[] { "a", "une" });
//        textualRenderings.put("deux", new String[] { "two", "deux" });
//        textualRenderings.put("il", new String[] { "it", "il" });
//        textualRenderings.put("et", new String[] { "and", "et" });
//        textualRenderings.put("si", new String[] { "if", "si" });
//        textualRenderings.put("estSing", new String[] { "is", "est" });
//        textualRenderings.put("l'", new String[] { "the", "l'" });
//        textualRenderings.put("inconnue", new String[] { "unknown", "inconnue" });
//
//        // sources
//        textualRenderings
//        .put(
//                "A1",
//                new String[] {
//                        "Alex Spalding, \"Inuktitut - A Grammar of North Baffin Dialects\", Wuerz Publishing Ltd., Winnipeg, 1992",
//                        "Alex Spalding, \"Inuktitut - A Grammar of North Baffin Dialects\", Wuerz Publishing Ltd., Winnipeg, 1992" });
//        textualRenderings
//                .put(
//                        "A2",
//                        new String[] {
//                                "A. Spalding, \"Inuktitut - A Multi-dialectal Outline Dictionary (with an Aivilingmiutaq base)\", Nunavut Arctic College, 1998",
//                                "A. Spalding, \"Inuktitut - A Multi-dialectal Outline Dictionary (with an Aivilingmiutaq base)\", Nunavut Arctic College, 1998" });
//        textualRenderings
//                .put(
//                        "H1",
//                        new String[] {
//                                "Kenn Harper, \"Suffixes of the Eskimo dialects of Cumberland Peninsula and North Baffin Island\", National Museum of Man, Mercury Series, Canadian Ethnology Service, Paper no. 54, Ottawa, 1979",
//                                "Kenn Harper, \"Suffixes of the Eskimo dialects of Cumberland Peninsula and North Baffin Island\", Musée national de l'Homme, Collecion Mercure, Service canadien d'ethnologie, Dossier no. 54, Ottawa, 1979" });
//        textualRenderings
//                .put(
//                        "H2",
//                        new String[] {
//                                "Kenn Harper, \"Some aspects of the grammar of the Eskimo dialects of Cumberland Peninsula and North Baffin Island\", National Museum of Man, Mercury Series, Ethnology Division, Paper no. 15, Ottawa, 1974",
//                                "Kenn Harper, \"Some aspects of the grammar of the Eskimo dialects of Cumberland Peninsula and North Baffin Island\", Musée national de l'Homme, Collection Mercure, Division d'ethnologie, Dossier no. 15, Ottawa, 1974" });
//        textualRenderings
//                .put(
//                        "M1",
//                        new String[] {
//                                "M. Mallon, \"Introductory Inuktitut Reference Grammar version 2.1\", Nunavut Arctic College, Ittukuluuk Language Programs, Iqaluit & Victoria, 1995",
//                                "M. Mallon, \"Introductory Inuktitut Reference Grammar version 2.1\", Nunavut Arctic College, Ittukuluuk Language Programs, Iqaluit & Victoria, 1995" });
//        textualRenderings.put("Hnsrd", new String[] { "Hansards of Nunavut",
//                "Hansards du Nunavut" });
//        textualRenderings
//                .put(
//                        "S1",
//                        new String[] {
//                                "L. Schneider, \"Dictionnaire des infixes de la langue eskimaude\", Minist�re des Affaires culturelles, Direction générale du Patrimoine, Dossier 43, 1979",
//                                "L. Schneider, \"Dictionnaire des infixes de la langue eskimaude\", Minist�re des Affaires culturelles, Direction générale du Patrimoine, Dossier 43, 1979" });
//        textualRenderings
//                .put(
//                        "S2",
//                        new String[] {
//                                "L. Schneider, \"Ulirnaisigutiit - An Inuktitut-English Dictionary of Northern Quebec, Labrador and Eastern Arctic Dialects\", Les Presses de l'Université Laval, Québec, 1985",
//                                "L. Schneider, \"Ulirnaisigutiit - An Inuktitut-English Dictionary of Northern Quebec, Labrador and Eastern Arctic Dialects\", Les Presses de l'Université Laval, Québec, 1985" });
//        // 	textualRenderings.put("",new String [] {"",""});
//        // 	textualRenderings.put("",new String [] {"",""});
//        // 	textualRenderings.put("",new String [] {"",""});
//        return true;
//    }

    //--------------------------------------------------------------------------------------------------------------
    // EXAMPLES
//    private static boolean makeHashOfExamples() {
//        try {
//            String line;
//            boolean eof;
//            
//            BufferedReader examplesReader = getExamplesFile();
//
//            examples = new Hashtable<String,Vector<Example>>();
//            eof = false;
//
//            while (examplesReader != null && !eof) {
//                // Lire une ligne du fichier.
//                // Chaque ligne contient, s�par�s par un espace ou par un tab:
//                // <term> <id> <ex. lat.> <ex. syll.> <trad. angl.> <trad.
//                // fran.>
//                // * * <ex. lat.> ...
//                //
//                // Un * * est un exemple pour le m�me terme.
//                // <id> est la signature unique d'un suffixe = numero + function
//                // ou numero + 'q' si le suffixe est un suffixe de queue.
//                line = examplesReader.readLine();
//                if (line == null)
//                    eof = true;
//                else {
//                    MyStringTokenizer mst = new MyStringTokenizer(line, ' ',
//                            '"');
//                    Vector<String> v = new Vector<String>();
//                    Vector<Example> current;
//                    Example ex;
//                    String term = null;
//                    String id = null;
//                    String key;
//                    //					st.slashSlashComments(true); // reconnaissance de '//'
//                    // pour ligne de commentaire
//                    //					st.quoteChar((int) '"'); // cha�ne de caract�res entre
//                    // deux "
//                    //					st.wordChars(33, (int) '"' - 1);
//                    //					st.wordChars((int) '"' + 1, (int) '/' - 1);
//                    //					st.wordChars((int) '/' + 1, 5760);
//                    //					typeToken = StreamTokenizer.TT_EOL; // initialisation �
//                    // int -= TT_EOF
//                    //					while (typeToken != StreamTokenizer.TT_EOF) {
//                    //						st.nextToken();
//                    //						if (st.ttype == StreamTokenizer.TT_EOF)
//                    //							typeToken = st.ttype;
//                    //						else if (st.ttype == StreamTokenizer.TT_NUMBER)
//                    //							v.add(Integer.toString((int) st.nval));
//                    //						else
//                    //							v.add(st.sval);
//                    //					}
//                    while (mst.hasMoreTokens()) {
//                        v.add((String)mst.nextToken());
//                    }
//                    if (v.size() != 0) {
//                        if (((String) v.elementAt(0)).equals("*")) {
//                            v.setElementAt(term, 0);
//                            v.setElementAt(id, 1);
//                        } else {
//                            term = (String) v.elementAt(0);
//                            id = (String) v.elementAt(1);
//                        }
//                        ex = new Example(v);
//                        // Ajouter � la table de hachage.
//                        // Chaque cl� contient un vecteur, parce qu'un
//                        // mot peut avoir plus d'un exemple.
//                        // On ajoute � ce vecteur.
//                        key = term + id;
//                        current = (Vector<Example>) examples.get(key);
//                        if (current == null)
//                            current = new Vector<Example>();
//                        current.add(ex);
//                		if (examples.containsKey(key)) 
//                			throw new RuntimeException("Key already exists in linguistic data hash");
//                        examples.put(key, current);
//                    }
//                } // else
//            } // while (!eof)
//            if (examplesReader!=null)
//                examplesReader.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//
//        return true;
//    } // makeHashOfExamples
//
//	private static BufferedReader getExamplesFile() {
//        // Le "UTF-8" assume que le fichier lexiqueSyl.dat contient des
//        // caract�res cod�s de cette fa�on, et assure que les caract�res
//        // lus seront en unicode.
//		BufferedReader reader = null;
//        InputStream is = new Examples().getExampleStream();
//        if (is != null) {
//            InputStreamReader isr = null;
//            try {
//                isr = new InputStreamReader(is, "utf-8");
//            } catch (UnsupportedEncodingException e) {
//                isr = new InputStreamReader(is);
//            }
//            reader = new BufferedReader(isr);
//        }
//        return reader;
//	}

}