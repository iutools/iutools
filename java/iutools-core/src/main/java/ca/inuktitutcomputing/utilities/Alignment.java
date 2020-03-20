package ca.inuktitutcomputing.utilities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Alignment implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Map<String,String> sentences = new HashMap<String,String>();
	
	public Alignment() {
		
	}
	
	public Alignment(String _l1, String _s1, String _l2, String _s2) {
		sentences.put(_l1, _s1);
		sentences.put(_l2, _s2);
	}
	
	public Alignment(Map<String,String> _sentences) {
		sentences = _sentences;
	}

}
