package ca.inuktitutcomputing.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.dataCSV.LinguisticDataCSV;
import ca.inuktitutcomputing.phonology.Dialect;
import ca.inuktitutcomputing.script.Orthography;
import ca.inuktitutcomputing.script.Roman;
import ca.pirurvik.iutools.text.segmentation.MyStringTokenizer;

public class LinguisticData {
	
	private static LinguisticData singleton = null;
	
	// Note: We keep info as both Vector<Base> and Vector<Morpheme> because for
	//  some unknown reason, we cannot cast from Vector<Base> to Vector<Morpheme>
	//  and some clients expect to receive the info in the later type.
    protected Map<String,Vector<Base>> basesForCanonicalForm = new HashMap<String,Vector<Base>>();
    protected Hashtable<String,Vector<Morpheme>> morphemesForCanonicalForm = new Hashtable<String,Vector<Morpheme>>();
    protected Hashtable<String,Base> idToBaseTable = new Hashtable<String,Base>();
    protected Hashtable<String,Affix> idToAffixTable = new Hashtable<String,Affix>();
    protected Hashtable<String,VerbWord> words = new Hashtable<String,VerbWord>();
    protected Hashtable<String,Source> sources = new Hashtable<String,Source>();
    protected Hashtable<String,Vector<Example>> examples = new Hashtable<String,Vector<Example>>();
    protected Hashtable<String,String[]> textualRenderings = new Hashtable<String,String[]>();
    protected Hashtable<String,Vector<SurfaceFormOfAffix>> surfaceFormsOfAffixes = new Hashtable<String,Vector<SurfaceFormOfAffix>>();
    protected Hashtable<Character,Vector<String>> groupsOfConsonants = new Hashtable<Character,Vector<String>>();

    static public void init() {
    	singleton = null;
    }
    public static LinguisticData getInstance() {
    	if (singleton == null) {
    		singleton = new LinguisticData();
    		try {
				LinguisticDataCSV.createLinguisticDataCSV(null);
		        singleton.makeGroupsOfConsonants();
			} catch (LinguisticDataException e) {
				e.printStackTrace();
				System.exit(1);
			}
    	}
    	return singleton;
    }
    
    public LinguisticData() {
    	reinitializeData();
    	makeHashtableOfExamples();
    	makeHashtableOfTextualRenderings();
    }
    
    
    //--------------------------------------------------------------------------
    public void addBaseForCanonicalForm(String canonicalForm, Base base) throws LinguisticDataException {
    	if (!basesForCanonicalForm.containsKey(canonicalForm)) {
    		basesForCanonicalForm.put(canonicalForm, new Vector<Base>());
    		morphemesForCanonicalForm.put(canonicalForm, new Vector<Morpheme>());
    	}
    	
    	basesForCanonicalForm.get(canonicalForm).add(base);
    	morphemesForCanonicalForm.get(canonicalForm).add((Morpheme)base);
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
    
    public Morpheme getMorpheme(String morphId) {
    	Logger logger = Logger.getLogger("LinguisticData.getMorpheme");
        Morpheme morph;
        morph = getBaseWithId(morphId);
        if (morph==null) {
            morph = getAffixWithId(morphId);
            if (morph==null) logger.debug("null affix: "+morphId);
        }
        return morph;
    }
    
 

    //--------------------------------------------------------------------------
	public void addEntryToIdToBaseTable(String baseId, Base baseObject) {
		idToBaseTable.put(baseId, baseObject);
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
		// OK... 
//		if (surfaceFormsOfAffixes.containsKey(simplifiedForm)) 
//			throw new RuntimeException("Key already exists in linguistic data hash");
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

// APPELÉE NULLE PART
//    public String[] getAllSuffixesIds() {
//    	Hashtable<String,Morpheme> suffixes = getId2SuffixTable();
//    	String [] suffixesIds = new String[suffixes.size()];
//    	int i=0;
//    	for (Enumeration<String> keys = suffixes.keys(); keys.hasMoreElements();) {
//    		Suffix suf = (Suffix)suffixes.get(keys.nextElement());
//    		suffixesIds[i++] = suf.id;
//    	}
//    	return suffixesIds;
//    }


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

    public Vector<Example> getExample(String key) {
        return examples.get(key);
    }

    public String[] getAllExamplesIds() {
    	return (String[])examples.keySet().toArray(new String[0]);
    }
    
    //--------------------------------------------------------------------------

    public String getTextualRendering(String key, String lang) {
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
    private  void makeHashtableOfExamples() {
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

	
}
