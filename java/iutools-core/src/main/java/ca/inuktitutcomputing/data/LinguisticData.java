package ca.inuktitutcomputing.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class LinguisticData {
	
	private static LinguisticData singleton = null;
	
    protected static Map<String,Vector<Base>> basesForCanonicalForm = new HashMap<String,Vector<Base>>();
    
    public static LinguisticData getInstance() {
    	if (singleton == null) {
    		singleton = new LinguisticData();
    	}
    	return singleton;
    }
    
    public void addBaseForCanonicalForm(String canonicalForm, Base base) throws LinguisticDataException {
    	if (!basesForCanonicalForm.containsKey(canonicalForm)) {
    		basesForCanonicalForm.put(canonicalForm, new Vector<Base>());
    	}
    	
    	basesForCanonicalForm.get(canonicalForm).add(base);
    }
    
    public Vector<Base> getBasesForCanonicalForm(String canonicalForm) {
    	Vector<Base> bases = basesForCanonicalForm.get(canonicalForm);
    	return bases;
    }
	
}
