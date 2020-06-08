package ca.nrc.datastructure.trie;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TrieNode {
	
	// TODO: Rename to keySequence???
	//
    public String[] keys = new String[] {};
    
    // TODO: I *THINK* this corresponds to the most frequent surface form
    //   for the node. That's the only possible explanation nodes (even 
    //   terminal ones) cannot be garanteed to have just one surface form. 
    //
    //   If that is indeed the correct meaning of this attribute, then it realy 
    //   should be replaced by a method mostFrequentSurfaceForm().
    //   This method would look in the surfaceForms attribute and find the most 
    //   frequent one. The value could be cached so we don't need to recompute 
    //   everytime (unles a call to addSurfaceForm() has been made, in which 
    //   case the cached value should be set to null to indicate that it needs 
    //   to be recomputed. 
    //
    public String surfaceForm = null;
    
    // TODO: Eventually get rid of this and replace by method 
    //   isTerminal(), which will check if the last entry in 
    //   keys is equal to either $ or \
    //
    public boolean isWord = false;
    
    protected long frequency = 0;
    
    
    // TODO: Eventually, remplace to 
    //
    //      Set<String> extensions
    //
    //   Each element in extensions is a key that can be used to 
    //   extend the 'keys' sequence.
    //
    //   This will avoid creating references between nodes 
    //
    protected Map<String,TrieNode> children = new HashMap<String,TrieNode>();
    
    protected TrieNode mostFrequentTerminal;
    
    // TODO: When we have gotten rid of Trie_InMemory, get rid of 
    //   this attribute. The 'data' attribute is filling the role 
    //   that 'stats' used to fill
    //
    protected Map<String,Object> stats = new HashMap<String,Object>();
    
    protected Map<String,Object> data = new HashMap<String,Object>();
    
    protected String cachedMostFrequentSurfaceForm = null;
    
    // TODO: The name of this attribute is confusing. It's not 
    //   the surface forms, but rather their frequencies.
    //   
    //   Eventually, rename it surfaceForms --> surfaceFormFreqs and
    //   implement a method surfaceForms() that will return its keys.
    //
    //   Can't do that until we get rid of the InMemory trie because 
    //   it stores the nodes in a large JSON file and the JSON of nodes
    //   uses the name .surfaceForms.
    protected HashMap<String,Long> surfaceForms = new HashMap<String,Long>();
    
    public TrieNode() {
    	init_TrieNode(null, null, null);
    }

    public TrieNode(String[] _keys) {
    	init_TrieNode(_keys, null, null);
    }
    
    public TrieNode(String[] _keys, String _surfaceForm) {
    	init_TrieNode(_keys, _surfaceForm, null);
    }

    public TrieNode(String[] _keys, String _surfaceForm, boolean _isTerminal) {
    	init_TrieNode(_keys, _surfaceForm, _isTerminal);
    }

    private void init_TrieNode(String[] _keys, String _surfaceForm, 
    		Boolean _isTerminal) {
        if (_keys != null) {
        	this.keys = _keys;
        	if (_keys.length > 0) {
            	String lastKey = _keys[_keys.length-1];
            	if (lastKey.equals("\\") || lastKey.equals("$")) {
            		this.isWord = true;
            	}
        		
        	}
        }
        
        if (_surfaceForm != null) {
        	this.addSurfaceForm(_surfaceForm);
        }
        
        if (_isTerminal != null) {
        	this.surfaceForm = _surfaceForm;
        }
    }
    
    @JsonIgnore
    public boolean isTerminal() {
    	boolean answer = false;
    	String last = lastKey();
    	if (last!= null) {
    		if (last.equals("$") || last.equals("\\")) {
    			answer = true;
    		}
    	}
    	
    	return answer;
    }
    
    public void setTerminalSurfaceForm(String _form) {
    	this.surfaceForm = _form;
    }    
    
    protected String lastKey() {
    	String last = null;
    	if (keys != null && keys.length > 0) {
    		last = keys[keys.length-1];
    	}
    	
    	return last;
    }
    
    public String getTerminalSurfaceForm() {
    	return this.surfaceForm;
    }
    
    public String key() {
    	return keys[keys.length-1];
    }
    
	public Map<String,TrieNode> getChildren() {
		return children;
	}
	
	public TrieNode[] childrenNodes() {
		Collection<TrieNode> childrenNodes = (Collection<TrieNode>) children.values();
		return childrenNodes.toArray(new TrieNode[] {});
	}
	
	public void addChild(String key, TrieNode node) {
		this.getChildren().put(key, node);
	}
	
	public boolean hasChild(String key) {
		return this.getChildren().containsKey(key);
	}
	
    public void setChildren(HashMap<String,TrieNode> _children) {
    	this.children = _children;
    }
    
    public boolean hasTerminalNode() {
    	return this.hasChild("$") || this.hasChild("\\");
    }
    
    public TrieNode childTerminalNode() {
    	TrieNode terminal = this.getChildren().get("\\");
    	if (terminal == null) {
    		terminal = this.getChildren().get("$");
    	}
    	
    	return terminal;
    }
        
    public HashMap<String,Long> getSurfaceForms() {
    	return surfaceForms;
    }
    
    public Object[][] orderedSurfaceForms() {
    	ArrayList<Object> listObjects = new ArrayList<Object>();
    	for (String key : this.surfaceForms.keySet()) {
    		listObjects.add(new Object[] {key,this.surfaceForms.get(key)});
    	}
    	Object[][] objects = listObjects.toArray(new Object[][] {});
    	Arrays.sort(objects, (Object[] o1, Object[] o2) ->  {
    		return ((Long)o2[1]).compareTo(((Long)o1[1]));
    	});
    	return objects;
    }
    
    public void addSurfaceForm(String form) {
    	this.surfaceForm = form;
    	cachedMostFrequentSurfaceForm = null;
    	
    	if (this.surfaceForms.containsKey(form)) {
    		this.surfaceForms.put(form, new Long(this.surfaceForms.get(form).longValue()+1));
    	} else {
    		this.surfaceForms.put(form, new Long(1));
    	}
    }

    /*public void setMostFrequentTerminal(TrieNode _mostFrequentTerminal) {
    	this.mostFrequentTerminal = _mostFrequentTerminal;
    }*/


    public String keysAsString() {
    	return keysAsString(null);
    }
    
    public String keysAsString(Boolean chopTerminalChar) {
    	if (chopTerminalChar == null) {
    		chopTerminalChar = false;
    	}
    	
    	String[] keysToPrint = keys;
    	if (chopTerminalChar) {
    		keysToPrint = Arrays.copyOfRange(keys, 0, keys.length-1);
    	}
    	String keyStr = String.join(" ", keysToPrint);
    	return keyStr;
    }
    
    
    
    public long getFrequency() {
    	return frequency;
    }
    
    public void incrementFrequency() {
    	frequency++;
    }
	
	@Override
    public String toString() {
		String toS = "[";
		toS += 
			"TrieNode:\n"+
		    "    segments = "+this.keysAsString()+"\n"+
			"    surfaceForm = "+surfaceForm+"\n"+
        	"    frequency = "+this.frequency+"\n";
        toS += "    ]";
        
        return toS;
    }

	// Stats
	
	public void defineStat(String statName) {
		if (! stats.containsKey(statName)) {
			stats.put(statName, null);
		}
	}
	
	public void ensureStatIsDefined(String statName) throws TrieNodeException {
		if (! stats.containsKey(statName)) {
			throw new TrieNodeException("No statistic with name "+statName+" have been defined");
		}
	}
	
	public void incrementStat(String statName, Number incr) throws TrieNodeException {
		ensureStatIsDefined(statName);
		if (getStat(statName) == null) {
			setStat(statName, incr);
		} else {
			if (incr instanceof Long) {
				setStat(statName, (Long) getStat(statName) + (Long) incr);
			} else if (incr instanceof Integer) {
				setStat(statName, (Integer) getStat(statName) + (Integer) incr);								
			} else if (incr instanceof Float) {
				setStat(statName, (Float) getStat(statName) + (Float) incr);								
			} else if (incr instanceof Double) {
				setStat(statName, (Double) getStat(statName) + (Double) incr);								
			}
		}
	}
	
	public Object getStat(String statName) throws TrieNodeException {
		ensureStatIsDefined(statName);
		Object val = stats.get(statName);
		
		return val;
	}
	
	public void setStat(String statName, Object val) throws TrieNodeException {
		ensureStatIsDefined(statName);
		stats.put(statName, val);
	}

	public Set<String> childrenSegments() {
		return children.keySet();
	}

	@Transient
	public void setData(String fldName, Object fldValue) {
		data.put(fldName, fldValue);
	}
	
	@Transient
	public Object getData(String fldName) {
		return data.get(fldName);
	}
}