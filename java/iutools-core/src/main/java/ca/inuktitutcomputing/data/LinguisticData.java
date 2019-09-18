package ca.inuktitutcomputing.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class LinguisticData {
	
	private static LinguisticData singleton = null;
	
    protected static Map<String,Vector<Base>> basesId = new HashMap<String,Vector<Base>>();
    
    public static LinguisticData getInstance() {
    	if (singleton == null) {
    		singleton = new LinguisticData();
    	}
    	return singleton;
    }
    
    public void addBase(String baseID, Base base) throws LinguisticDataException {
    	if (!basesId.containsKey(baseID)) {
    		basesId.put(baseID, new Vector<Base>());
    	}
    	
    	basesId.get(baseID).add(base);
    }
	
}
