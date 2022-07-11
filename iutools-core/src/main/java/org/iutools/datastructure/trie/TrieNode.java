package org.iutools.datastructure.trie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TrieNode {
	
	public static final String TERMINAL_SEG = "_$";
	public static final String NULL_SEG = "_NULL";

	/**
	 * Time at which we last made sure that the node's aggregate stats
	 * (ex: frequency) were up to date with those of its descendants.
	 */
	public long statsRefeshedOn = 0;
	
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
    
    @JsonProperty
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
            	if (lastKey.equals("\\") || lastKey.equals(TERMINAL_SEG)) {
            		this.isWord = true;
            	}
        		
        	}
        }
        
        if (_surfaceForm != null) {
        	this.updateSurfaceForms(_surfaceForm);
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
    		if (last.equals(TERMINAL_SEG) || last.equals("\\")) {
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

    // TODO-2020: Get rid of this. We already have TrieNode[] childrenNodes()
	//   and we should add String[] childrenSegments()
	//   - getChildrenNodes
	//
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
    	return this.hasChild(TERMINAL_SEG) || this.hasChild("\\");
    }
    
    public TrieNode childTerminalNode() {
    	TrieNode terminal = this.getChildren().get("\\");
    	if (terminal == null) {
    		terminal = this.getChildren().get(TERMINAL_SEG);
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
    
    public void updateSurfaceForms(String form) {
    	updateSurfaceForms(form, 1);
    }

	public void updateSurfaceForms(String form, long freqIncr) {
    	this.surfaceForm = form;
    	cachedMostFrequentSurfaceForm = null;
    	
    	if (this.surfaceForms.containsKey(form)) {
    		this.surfaceForms.put(form, new Long(this.surfaceForms.get(form).longValue()+freqIncr));
    	} else {
    		this.surfaceForms.put(form, new Long(freqIncr));
    	}    	
	}
    
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
    	frequency++;    }

    public void incrementFrequency(long freqIncr) {
    	frequency += freqIncr;
    }
	
	@Override
    public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		builder.append("  TrieNode:\n");
		builder.append("    segments = "+this.keysAsString()+"\n");
		builder.append("    surfaceForm = "+surfaceForm+"\n");
		builder.append("    frequency = "+this.frequency+"\n");
		builder.append("    children = "+String.join(",", children.keySet())+"\n");
		builder.append("}");
        
        return builder.toString();
    }

	// TODO: When we have gotten rid of Trie_InMemory, get rid of
	//   this method. The 'data' attribute is filling the role
	//   that 'stats' used to fill
	//
	public void defineStat(String statName) {
		if (! stats.containsKey(statName)) {
			stats.put(statName, null);
		}
	}

	// TODO: When we have gotten rid of Trie_InMemory, get rid of
	//   this method. The 'data' attribute is filling the role
	//   that 'stats' used to fill
	//
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

	// TODO: When we have gotten rid of Trie_InMemory, get rid of
	//   this method. The 'data' attribute is filling the role
	//   that 'stats' used to fill
	//
	public Object getStat(String statName) throws TrieNodeException {
		ensureStatIsDefined(statName);
		Object val = stats.get(statName);
		
		return val;
	}

	// TODO: When we have gotten rid of Trie_InMemory, get rid of
	//   this method. The 'data' attribute is filling the role
	//   that 'stats' used to fill
	//
	public void setStat(String statName, Object val) throws TrieNodeException {
		ensureStatIsDefined(statName);
		stats.put(statName, val);
	}

	public Set<String> childrenSegments() {
		return children.keySet();
	}


	@JsonIgnore
	public void setField(String fldName, Object fldValue) {
		data.put(fldName, fldValue);
	}
	
	@JsonIgnore
	public Object getField(String fldName) {
		return data.get(fldName);
	}
	
	@JsonIgnore
	public  <T> T getField(String fldName, T newEntry) {
		Logger tLogger = LogManager.getLogger("ca.nrc.datastructure.trie.TrieNode.getField");
		tLogger.trace("For fldName="+fldName+", default entry is of class "+((newEntry == null) ? null: newEntry.getClass()));
		T value = (T)data.get(fldName);
		if (value == null) {
			data.put(fldName, newEntry);
			value = newEntry;
		}

		tLogger.trace("For fldName="+fldName+", returning value  "+((value == null) ? "": value.getClass())+"="+value);
		return value;
	}

	public String[] keysNoTerminal() {
		String[] keysNoTerm = keys;
		if (keys != null && keys.length > 0 &&
				keys[keys.length-1].equals(TERMINAL_SEG)) {
			// Remove the terminal segment
			keysNoTerm = Arrays.copyOf(keys, keys.length-1);
		}
		return keysNoTerm;
	}

	public boolean statsMayNeedRefreshing(long lastTerminalChangeTime) {
    	boolean answer =
			(statsRefeshedOn == 0 ||
				statsRefeshedOn < lastTerminalChangeTime);
    	return answer;
	}
}
