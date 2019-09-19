package ca.inuktitutcomputing.data;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public class LinguisticData {
	
	private static LinguisticData singleton = null;
	
	// Note: We keep info as both Vector<Base> and Vector<Morpheme> because for
	//  some unknown reason, we cannot cast from Vector<Base> to Vector<Morpheme>
	//  and some clients expect to receive the info in the later type.
    protected static Map<String,Vector<Base>> basesForCanonicalForm = new HashMap<String,Vector<Base>>();
    protected static Hashtable<String,Vector<Morpheme>> morphemesForCanonicalForm = new Hashtable<String,Vector<Morpheme>>();
    
    public static LinguisticData getInstance() {
    	if (singleton == null) {
    		singleton = new LinguisticData();
    	}
    	return singleton;
    }
    
    public void addBaseForCanonicalForm(String canonicalForm, Base base) throws LinguisticDataException {
    	if (!basesForCanonicalForm.containsKey(canonicalForm)) {
    		basesForCanonicalForm.put(canonicalForm, new Vector<Base>());
    		morphemesForCanonicalForm.put(canonicalForm, new Vector<Morpheme>());
    	}
    	
    	basesForCanonicalForm.get(canonicalForm).add(base);
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

	
}
