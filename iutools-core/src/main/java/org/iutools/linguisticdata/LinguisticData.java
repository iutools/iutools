package org.iutools.linguisticdata;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

import ca.nrc.debug.Debug;
import org.apache.log4j.Logger;

import org.iutools.linguisticdata.dataCSV.LinguisticDataCSV;
import org.iutools.phonology.Dialect;
import org.iutools.script.Orthography;
import org.iutools.script.Roman;
import org.iutools.text.segmentation.MyStringTokenizer;

public class LinguisticData {
	
	private static LinguisticData singleton = null;

	public static Map<String,String> type2class = new HashMap<>();
	static {
        type2class.put("n","Base");
        type2class.put("v","Base");
        type2class.put("a","Base");
        type2class.put("c","Base");
        type2class.put("e","Base");
        type2class.put("sv","Suffix");
        type2class.put("sn","Suffix");
        type2class.put("q","Suffix");
        type2class.put("tn","NounEnding");
        type2class.put("tv","VerbEnding");
        type2class.put("ad","Demonstrative");
        type2class.put("pd","Demonstrative");
        type2class.put("tad","DemonstrativeEnding");
        type2class.put("tpd","DemonstrativeEnding");
        type2class.put("p","Pronoun");
        // TODO: only 3 occurrences in RootsSpalding.csv; must be changed to "p"
        type2class.put("pr","Pronoun");
        type2class.put("rp","Pronoun");
        type2class.put("rpr","Pronoun");
        type2class.put("vw","VerbWord");
        type2class.put("src","Source");
    }
	
	// For bases:
    // We keep info as both Vector<Base> and Vector<Morpheme>
    // because for some unknown reason, we cannot cast from Vector<Base> to Vector<Morpheme>
	// and some clients expect to receive the info in the later type.
    // SURFACE FORMS TO OBJECTS
    protected Map<String,Vector<Base>> basesForCanonicalForm = new HashMap<String,Vector<Base>>();
    protected Hashtable<String,Vector<Morpheme>> morphemesForCanonicalForm = new Hashtable<String,Vector<Morpheme>>();
    protected Hashtable<String,Vector<SurfaceFormOfAffix>> surfaceFormsOfAffixes = new Hashtable<String,Vector<SurfaceFormOfAffix>>();
    // MORPHEME IDS TO MORPHEMES
    protected Hashtable<String,Base> idToBaseTable = new Hashtable<String,Base>();
    protected Hashtable<String,Affix> idToAffixTable = new Hashtable<String,Affix>();
    // OTHERS
    protected Hashtable<String,VerbWord> words = new Hashtable<String,VerbWord>();
    protected Hashtable<String,Source> sources = new Hashtable<String,Source>();
    protected Hashtable<String,Vector<Example>> examples = new Hashtable<String,Vector<Example>>();
    protected Hashtable<String,String[]> textualRenderings = new Hashtable<String,String[]>();
    protected Hashtable<Character,Vector<String>> groupsOfConsonants = new Hashtable<Character,Vector<String>>();

    static public void init() {
    	singleton = null;
    }

    public static synchronized LinguisticData getInstance() {
        Logger logger = Logger.getLogger("LinguisticData.getInstance");
        logger.debug("singleton == null ? " + (singleton==null));
    	if (singleton == null) {
    		singleton = new LinguisticData();
    		try {
                singleton.readLinguisticDataCSV();
		        singleton.makeGroupsOfConsonants(); // used in Dialect.java
			} catch (LinguisticDataException e) {
				e.printStackTrace();
				System.exit(1);
			}
    	}
    	return singleton;
    }
    
    public LinguisticData() {
    	reinitializeData();
    }

    /*
     * Read the data stored in the CSV files into linguistic objects and register them in the singleton (this).
     */
    protected void readLinguisticDataCSV() throws LinguisticDataException {
        LinguisticDataCSV ldcsv = new LinguisticDataCSV();
        ldcsv.readAndRegisterLinguisticDataCSV(this);
    }


    
    
    //--------------------------------------------------------------------------
    public void add2basesForCanonicalForm(String canonicalForm, Base base) throws LinguisticDataException {
    	if (!basesForCanonicalForm.containsKey(canonicalForm)) {
    		basesForCanonicalForm.put(canonicalForm, new Vector<Base>());
    		morphemesForCanonicalForm.put(canonicalForm, new Vector<Morpheme>());
    	}
    	
    	basesForCanonicalForm.get(canonicalForm).add(base);
    	morphemesForCanonicalForm.get(canonicalForm).add((Morpheme)base);
    }

    //-----------------------------------------------------------------

    /*
     * These methods create SurfaceFormOfAffix objects that represent forms and actions
     * of the affixes: suffixes and endings. Those objects are registered in a hash table.
     */
    public void addToForms(
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


    public void addToForms(
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

    /*
     * Si l'une des surfaceFormsOfAffixes ou des actions est inconnue, on ne
     * place pas de forme dans la table de hachage des surfaceFormsOfAffixes.
     */
    private void addToForms1(
            String[] altForms,
            String key,
            String type,
            String id,
            String context,
            Action[] actions1,
            Action[] actions2) throws LinguisticDataException {

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
                    // Simplification of the form (ng > N ; nng > NN).
                    // Because the method looking for an affix is passed a simplified inuktitut text.
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
                        addToSurfaceFormsOfAffixes(form1, newForm);
                    }

                    /*
                     * Certain action2 may also produce a special form, like self-decapitation for example.
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
                        addToSurfaceFormsOfAffixes(form2, otherForm);
                    }
                }
            }
    }

    // -----------------------------------------------------------------------------------------------------------------
    public void addEntryToIdToBaseTable(String baseId, Base baseObject) {
        idToBaseTable.put(baseId, baseObject);
    }

    public Vector<Morpheme> getBasesForCanonicalForm(String canonicalForm) {
    	Vector<Morpheme> bases = morphemesForCanonicalForm.get(canonicalForm);
    	return bases;
    }

	public String[] getCanonicalFormsForAllBases() {
		String[] forms = basesForCanonicalForm.keySet().toArray(new String[0]);;
		return forms;
	}
    
    public Hashtable<String, Vector<Morpheme>> getBasesForAllCanonicalForms_hashtable() {
    	return morphemesForCanonicalForm;
    }

    /*
     * Look for the morpheme identified by its id.
     * First check in the roots; if not found, check in the affixes.
     */
    public Morpheme getMorpheme(String morphId) {
        Morpheme morph;
        morph = getBaseWithId(morphId);
        if (morph==null) {
            morph = getAffixWithId(morphId);
        }
        return morph;
    }

    public Base getBaseWithId(String morphId) {
    	Base b = null;
    	if (idToBaseTable != null)
    		b = idToBaseTable.get(morphId);
        return b;
    }

    public Base getBaseFromMorphemeIdObject(Morpheme.Id morphId) {
    	Base b = null;
    	if (idToBaseTable != null)
    		b = idToBaseTable.get(morphId.id);
        return b;
    }
    
    public Hashtable<String, Base> getIdToBaseTable() {
    	return idToBaseTable;
    }
    
    public String[] getAllBasesIds() {
    	return (String[])idToBaseTable.keySet().toArray(new String[0]);
    }

	public Hashtable<String, Morpheme> getIdToRootTable() {
		Hashtable<String, Morpheme> table = new Hashtable<String, Morpheme>();
		String clazz = Base.class.getName();
		for (Enumeration<String> rootIds = idToBaseTable.keys(); rootIds.hasMoreElements();) {
			String rootId = rootIds.nextElement();
			Object obj = idToBaseTable.get(rootId);
			if (obj.getClass().getName() == clazz) {
				Base root = idToBaseTable.get(rootId);
				table.put(rootId, root);
			}
		}
		return table;
	}
	

    //--------------------------------------------------------------------------
	public void addEntryToIdToAffixTable(String affixId, Affix affixObject) {
		idToAffixTable.put(affixId, affixObject);
	}
	
    public Affix getAffixWithId(String uniqueId) {
        Affix aff = idToAffixTable.get(uniqueId);
        return aff;
    }

    public Suffix getSuffixWithId(String uniqueId) {
        Suffix afs = (Suffix)idToAffixTable.get(uniqueId);
        return afs;
    }

    public Hashtable<String, Affix>getIdToAffixTable() {
    	return idToAffixTable;
    }
    
    public String[] getAllAffixesIds() {
    	return (String[])idToAffixTable.keySet().toArray(new String[0]);
    }
    
    
    //--------------------------------------------------------------------------
    public void addToSurfaceFormsOfAffixes(String str, SurfaceFormOfAffix form) {
        String simplifiedForm = Orthography.simplifiedOrthographyLat(str);
		Vector<SurfaceFormOfAffix> v = surfaceFormsOfAffixes.get(simplifiedForm);
		if (v == null)
			v = new Vector<SurfaceFormOfAffix>();
		v.add(form);
		surfaceFormsOfAffixes.put(simplifiedForm, v);
    }

    public Hashtable<String,Vector<SurfaceFormOfAffix>> getFormToSurfaceFormsOfAffixesTable() {
		return surfaceFormsOfAffixes;
	}

    public Vector<SurfaceFormOfAffix> getSurfaceForms(String form) {
        // Simplify the spelling
        String simplifiedForm = Orthography.simplifiedOrthographyLat(form);
        return surfaceFormsOfAffixes.get(simplifiedForm);
    }
    
    public SurfaceFormOfAffix getForm(String morph) {
    	return (SurfaceFormOfAffix)getSurfaceForms(morph).elementAt(0);
    }
    
    public String[] getAllAffixesSurfaceFormsKeys() {
    	return (String[]) surfaceFormsOfAffixes.keySet().toArray(new String[] {});
    }

    //--------------------------------------------------------------------------
	public Hashtable<String, Morpheme> getId2SuffixTable() {
		Hashtable<String, Morpheme> table = new Hashtable<String, Morpheme>();
		for (Enumeration<String> affixIds = idToAffixTable.keys(); affixIds.hasMoreElements();) {
			String affixId = affixIds.nextElement();
			Affix aff = (Affix) idToAffixTable.get(affixId);
			if (aff.getClass().getName().equals("Suffix"))
				table.put(affixId, (Morpheme)aff);
		}
		return table;
	}


    //--------------------------------------------------------------------------
	public Hashtable<String, Demonstrative> getIdToDemonstrativeTable() {
		Hashtable<String, Demonstrative> table = new Hashtable<String, Demonstrative>();
		String clazz = Demonstrative.class.getName();
		for (Enumeration<String> demonstrativeIds = idToBaseTable.keys(); demonstrativeIds.hasMoreElements();) {
			String demonstrativeId = demonstrativeIds.nextElement();
			Object obj = idToBaseTable.get(demonstrativeId);
			if (obj.getClass().getName() == clazz) {
				Demonstrative demonstrativeObject = (Demonstrative) idToBaseTable.get(demonstrativeId);
				table.put(demonstrativeId, demonstrativeObject);
			}
		}
		return table;
	}

    //--------------------------------------------------------------------------
	public Hashtable<String,Base> getIdToGiVerbsTable() {
    	Hashtable<String,Base> giverbsHash = new Hashtable<String,Base>();
    	Hashtable<String,Morpheme> bases = getIdToRootTable();
    	for (Enumeration<String> keys = bases.keys(); keys.hasMoreElements();) {
    		String key = keys.nextElement();
    		Base base = (Base)bases.get(key);
    		if (base.isGiVerb())
    			giverbsHash.put(key, base);
    	}
    	return giverbsHash;
    }

    public Base [] getGiVerbs() {
    	Hashtable<String,Base> giverbsHash = getIdToGiVerbsTable();
    	String [] giverbsKeysArray = (String[])giverbsHash.keySet().toArray(new String[]{});
    	Arrays.sort(giverbsKeysArray);
    	Base [] giverbs = new Base[giverbsKeysArray.length];
    	for (int i=0; i<giverbsKeysArray.length; i++)
    		giverbs[i] = (Base)giverbsHash.get(giverbsKeysArray[i]);
    	return giverbs;
    }


    //--------------------------------------------------------------------------
    public void addVerbWord(String verbWordForm, VerbWord wordObject) {
    	words.put(verbWordForm, wordObject);
    }
	
    public Hashtable<String,VerbWord> getWords() {
    	return words;
    }
    
    public String[] getAllVerbWordsForms() {
    	return (String[])words.keySet().toArray(new String[0]);
    }
    
    public VerbWord getVerbWord(String term) {
        VerbWord mv = words.get(term);
        return mv;
    }


    //--------------------------------------------------------------------------
    public void addSource(String sourceId, Source sourceObject) {
    	sources.put(sourceId,sourceObject);
    }

    public Hashtable<String,Source> getIdToSourceTable() {
    	return sources;
    }
   
    public String[] getAllSourceIds() {
    	return (String[])sources.keySet().toArray(new String[0]);
    }
    
    public Source getSource(String sourceId) {
        Source s = sources.get(sourceId);
        return s;
    }

    //--------------------------------------------------------------------------

    // These 2 tables are not used by the morphological analyzer. They are used only by
    // applications that display information and examples about morphemes. This should
    // be left to those applications to create the tables.
    public void prepareNaturalLanguageElements() {
        makeHashtableOfExamples();
        makeHashtableOfTextualRenderings();
    }


    //--------------------------------------------------------------------------

    public Vector<Example> getExample(String key) {
        if (examples==null)
            makeHashtableOfExamples();
        return examples.get(key);
    }

    public String[] getAllExamplesIds() {
        if (examples==null)
            makeHashtableOfExamples();
    	return (String[])examples.keySet().toArray(new String[0]);
    }
    
    //--------------------------------------------------------------------------

    public String getTextualRendering(String key, String lang) {
        if (textualRenderings==null)
            makeHashtableOfTextualRenderings();
        return _getTextualRendering(key,lang);
    }
    
//    public String getTextualRendering(String key, String lang, 
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

    private  String _getTextualRendering(String key, String lang) {
        Object val = textualRenderings.get(key);
        String res = null;
        if (val == null)
            res = "";
        else {
            String[] txt = (String[]) val;
            res = lang.equals("en") ? txt[0] : txt[1];
        }
        
        return res;
    }

    
    //--------------------------------------------------------------------------
    public Hashtable<Character,Vector<String>> getGroupsOfConsonants() {
    	return groupsOfConsonants;
    }

	public void addToGroupsOfConsonants(String str) {
        char chars[] = str.toCharArray();
        for (int i=0; i<chars.length-1; i++) {
            if (Roman.isConsonant(chars[i]) &&
                    Roman.isConsonant(chars[i+1])) {
                Character charac = new Character(chars[i+1]);
                Vector<String> grCons;
                if (groupsOfConsonants.containsKey(charac))
                    grCons = groupsOfConsonants.get(charac);
                else
                    grCons = new Vector<String>();
                String newGr = new String(new char[]{chars[i],chars[i+1]});
                if (!grCons.contains(newGr))
                    grCons.add(newGr);
                groupsOfConsonants.put(charac,grCons);
            }
        }
    }

    
    //--------------------------------------------------------------------------
	public void reinitializeData() {
	    examples = new Hashtable<String,Vector<Example>>();
	    textualRenderings = new Hashtable<String,String[]>();
	    surfaceFormsOfAffixes = new Hashtable<String,Vector<SurfaceFormOfAffix>>();
		basesForCanonicalForm = new Hashtable<String,Vector<Base>>();
	    morphemesForCanonicalForm = new Hashtable<String,Vector<Morpheme>>();
	    idToBaseTable = new Hashtable<String,Base>();
	    idToAffixTable = new Hashtable<String,Affix>();
	    words = new Hashtable<String,VerbWord>();
	    sources = new Hashtable<String,Source>();
	    groupsOfConsonants = new Hashtable<Character,Vector<String>>();
	}
    //--------------------------------------------------------------------------

    private void makeGroupsOfConsonants() {
    	if (getBasesForAllCanonicalForms_hashtable() != null) {
    		String [] keys = getCanonicalFormsForAllBases();
    		for (int i=0; i<keys.length; i++) {
    			String key = Orthography.simplifiedOrthographyLat(keys[i]);
                addToGroupsOfConsonants(key);
    		}
    	}
    	if (getFormToSurfaceFormsOfAffixesTable() != null) {
    		String [] forms = getAllAffixesSurfaceFormsKeys();
    		for (int i=0; i<forms.length; i++)
                addToGroupsOfConsonants(forms[i]);
    	}
    	String keys[] = Dialect.getKeys();
    	for (int i=0; i<keys.length; i++)
    		addToGroupsOfConsonants(keys[i]);
    }
    


    //--------------------------------------------------------------------------
    // TEXTUAL (NATURAL LANGUAGE) RENDERING OF LINGUISTIC INFORMATION
	private void makeHashtableOfTextualRenderings() {
		textualRenderings = TextualRendering.makeHashOfTextualRenderings();
	}

	//--------------------------------------------------------------------------
    // EXAMPLES
    private void makeHashtableOfExamples() {
        try {
            String line;
            boolean eof;
            
            BufferedReader examplesReader = getExamplesFile();

            examples = new Hashtable<String,Vector<Example>>();
            eof = false;

            while (examplesReader != null && !eof) {
                // Lire une ligne du fichier.
                // Chaque ligne contient, s�par�s par un espace ou par un tab:
                // <term> <id> <ex. lat.> <ex. syll.> <trad. angl.> <trad.
                // fran.>
                // * * <ex. lat.> ...
                //
                // Un * * est un exemple pour le m�me terme.
                // <id> est la signature unique d'un suffixe = numero + function
                // ou numero + 'q' si le suffixe est un suffixe de queue.
                line = examplesReader.readLine();
                if (line == null)
                    eof = true;
                else {
                    MyStringTokenizer mst = new MyStringTokenizer(line, ' ',
                            '"');
                    Vector<String> v = new Vector<String>();
                    Vector<Example> current;
                    Example ex;
                    String term = null;
                    String id = null;
                    String key;
                    //					st.slashSlashComments(true); // reconnaissance de '//'
                    // pour ligne de commentaire
                    //					st.quoteChar((int) '"'); // cha�ne de caract�res entre
                    // deux "
                    //					st.wordChars(33, (int) '"' - 1);
                    //					st.wordChars((int) '"' + 1, (int) '/' - 1);
                    //					st.wordChars((int) '/' + 1, 5760);
                    //					typeToken = StreamTokenizer.TT_EOL; // initialisation �
                    // int -= TT_EOF
                    //					while (typeToken != StreamTokenizer.TT_EOF) {
                    //						st.nextToken();
                    //						if (st.ttype == StreamTokenizer.TT_EOF)
                    //							typeToken = st.ttype;
                    //						else if (st.ttype == StreamTokenizer.TT_NUMBER)
                    //							v.add(Integer.toString((int) st.nval));
                    //						else
                    //							v.add(st.sval);
                    //					}
                    while (mst.hasMoreTokens()) {
                        v.add((String)mst.nextToken());
                    }
                    if (v.size() != 0) {
                        if (((String) v.elementAt(0)).equals("*")) {
                            v.setElementAt(term, 0);
                            v.setElementAt(id, 1);
                        } else {
                            term = (String) v.elementAt(0);
                            id = (String) v.elementAt(1);
                        }
                        ex = new Example(v);
                        // Ajouter � la table de hachage.
                        // Chaque cl� contient un vecteur, parce qu'un
                        // mot peut avoir plus d'un exemple.
                        // On ajoute � ce vecteur.
                        key = term + id;
                        current = (Vector<Example>) examples.get(key);
                        if (current == null)
                            current = new Vector<Example>();
                        current.add(ex);
                		if (examples.containsKey(key)) 
                			throw new RuntimeException("Key already exists in linguistic data hash");
                        examples.put(key, current);
                    }
                } // else
            } // while (!eof)
            if (examplesReader!=null)
                examplesReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    } // makeHashOfExamples

	private static BufferedReader getExamplesFile() {
        // Le "UTF-8" assume que le fichier lexiqueSyl.dat contient des
        // caract�res cod�s de cette fa�on, et assure que les caract�res
        // lus seront en unicode.
		BufferedReader reader = null;
        InputStream is = new Examples().getExampleStream();
        if (is != null) {
            InputStreamReader isr = null;
            try {
                isr = new InputStreamReader(is, "utf-8");
            } catch (UnsupportedEncodingException e) {
                isr = new InputStreamReader(is);
            }
            reader = new BufferedReader(isr);
        }
        return reader;
	}

    //----- MAKE OBJECTS FOR THE MORPHEMES ------------------------------

    public void makeAndRegisterLinguisticObject(HashMap<String,String> linguisticDataMap) throws LinguisticDataException {
        Logger logger = Logger.getLogger("LinguisticDataCSV.makeLinguisticObject");
        String morphemeTypeInLinguisticData = linguisticDataMap.get("type");
        logger.debug("morphemeTypeInLinguisticData: "+morphemeTypeInLinguisticData);
        String classOfMorpheme = LinguisticData.type2class.get(morphemeTypeInLinguisticData);
        // original 'originalClassOfMorpheme' variable in the if-statements below has been replaced by 'classOfMorpheme'
        if (classOfMorpheme.equals("Base")) {
            makeAndRegisterBase(linguisticDataMap);
        } else if (classOfMorpheme.equals("Suffix")) {
            makeAndRegisterSuffix(linguisticDataMap);
        } else if (classOfMorpheme.equals("NounEnding")) {
            makeAndRegisterNounEnding(linguisticDataMap);
        } else if (classOfMorpheme.equals("VerbEnding")) {
            makeAndRegisterVerbEnding(linguisticDataMap);
        } else if (classOfMorpheme.equals("Demonstrative")) {
            makeAndRegisterDemonstrative(linguisticDataMap);
        } else if (classOfMorpheme.equals("DemonstrativeEnding")) {
            makeAndRegisterDemonstrativeEnding(linguisticDataMap);
        } else if (classOfMorpheme.equals("Pronoun")) {
            makeAndRegisterPronoun(linguisticDataMap);
        } else if (classOfMorpheme.equals("VerbWord")) {
            makeAndRegisterVerbWord(linguisticDataMap);
        } else if (classOfMorpheme.equals("Source")) {
            makeAndRegisterSource(linguisticDataMap);
        }
    }


    public void makeAndRegisterBase(HashMap<String,String> linguisticData) throws LinguisticDataException {
        List<Base> bases = LinguisticObjectFactory.makeBase(linguisticData);
        for (Base base : bases) {
            if (base.originalMorpheme==null // not a variant
                    && getIdToBaseTable().containsKey(base.id)) {
                String callStack = Debug.printCallStack();
                throw new RuntimeException(
                        "Bases ID already contains a key "+base.id+". This one is defined in "+
                                base.tableName+"."+". Check your .csv files in the linguistics data\n"+
                                "\nCall stack was:\n"+callStack);
            } else {
                add2basesForCanonicalForm(base.morpheme, base);
                if (base.subtype==null) { // not equal to "nc" (noun composite)
                    addEntryToIdToBaseTable(base.id, base);
                }
            }
        }
    }

    public void makeAndRegisterSuffix(HashMap<String,String> linguisticData) throws LinguisticDataException {
        Suffix suffix = LinguisticObjectFactory.makeSuffix(linguisticData);
        if (getIdToAffixTable().containsKey(suffix.id))
            throw new RuntimeException("Key '"+suffix.id+"' already exists in linguistic data hash");
        addEntryToIdToAffixTable(suffix.id,suffix);
        addToForms(suffix, suffix.morpheme);
    }

    public void makeAndRegisterNounEnding(HashMap<String,String> linguisticData) throws LinguisticDataException {
        NounEnding ending = LinguisticObjectFactory.makeNounEnding(linguisticData);
        if (getIdToAffixTable().containsKey(ending.id))
            throw new RuntimeException("Key '"+ending.id+"' already exists in linguistic data hash");
        addEntryToIdToAffixTable(ending.id,ending);
        addToForms(ending, ending.morpheme);
    }

    public void makeAndRegisterVerbEnding(HashMap<String,String> linguisticData) throws LinguisticDataException {
        VerbEnding ending = LinguisticObjectFactory.makeVerbEnding(linguisticData);
        // This test with a throw is commented out to allow the execution of scripts using the linguistic database.
        // This matter will be attended to shortly (Benoît Farley, 2019-09-17)
//      if (getIdToAffixTable().containsKey(ending.id))
//        	throw new RuntimeException("Key '"+ending.id+"' already exists in linguistic data hash");
        addEntryToIdToAffixTable(ending.id,ending);
        addToForms(ending, ending.morpheme);
    }

    public void makeAndRegisterDemonstrative(HashMap<String,String> linguisticData) throws LinguisticDataException {
        List<Demonstrative> demonstratives = LinguisticObjectFactory.makeDemonstrative(linguisticData);
        for (Demonstrative demonstrative : demonstratives) {
            if (getIdToBaseTable().containsKey(demonstrative.id))
                throw new RuntimeException("Key '" + demonstrative.id + "' already exists in linguistic data hash");
            add2basesForCanonicalForm(demonstrative.morpheme,demonstrative);
            addEntryToIdToBaseTable(demonstrative.id, demonstrative);
        }
    }

    public void makeAndRegisterDemonstrativeEnding(HashMap<String,String> linguisticData) throws LinguisticDataException {
        DemonstrativeEnding ending = LinguisticObjectFactory.makeDemonstrativeEnding(linguisticData);
        if (getIdToAffixTable().containsKey(ending.id))
            throw new RuntimeException("Key '" + ending.id + "' already exists in linguistic data hash");
        addEntryToIdToAffixTable(ending.id,ending);
        addToForms(ending, ending.morpheme);
    }

    public void makeAndRegisterPronoun(HashMap<String,String> linguisticData) throws LinguisticDataException {
        List<Pronoun> pronouns = LinguisticObjectFactory.makePronoun(linguisticData);
        for (Pronoun pronoun : pronouns) {
            if (getIdToBaseTable().containsKey(pronoun.id))
                throw new RuntimeException("Key '" + pronoun.id + "' already exists in linguistic data hash");
            add2basesForCanonicalForm(pronoun.morpheme,pronoun);
            addEntryToIdToBaseTable(pronoun.id, pronoun);
        }
    }

    public void makeAndRegisterVerbWord(HashMap<String,String> linguisticData) throws LinguisticDataException {
        VerbWord verbWord = LinguisticObjectFactory.makeVerbWord(linguisticData);
        addVerbWord(verbWord.verb,verbWord);
    }

    public void makeAndRegisterSource(HashMap<String,String> linguisticData) throws LinguisticDataException {
        Source source = LinguisticObjectFactory.makeSource(linguisticData);
        addSource(source.id,source);
    }

	public String[] allMorphemeIDs() {
    	List<String> idsList = new ArrayList<String>();
    	Collections.addAll(idsList, getAllAffixesIds());
    	Collections.addAll(idsList, getAllBasesIds());
    	return idsList.toArray(new String[0]);
	}
}
