package ca.nrc.datastructure.trie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;

public class TrieNode_InMemory {
    public String[] keys = new String[] {};
    protected boolean isWord = false;
    protected long frequency = 0;
    protected Map<String,TrieNode_InMemory> children = new HashMap<String,TrieNode_InMemory>();
    protected TrieNode_InMemory mostFrequentTerminal;
    protected Map<String,Object> stats = new HashMap<String,Object>();
    protected String surfaceForm = null;
    protected HashMap<String,Long> surfaceForms = new HashMap<String,Long>();
    
    public TrieNode_InMemory() {
    	init_TrieNode(null, null);
    }

    public TrieNode_InMemory(String[] _keys) {
    	init_TrieNode(_keys, null);
    }
    
    public TrieNode_InMemory(String[] _keys, boolean _isWord) {
    	init_TrieNode(_keys, _isWord);
    }

    private void init_TrieNode(String[] _keys, Boolean _isWord) {
        if (_keys != null) {
        	this.keys = _keys;
        }
        if (_isWord != null) {
        	this.isWord = _isWord;
        }
    }
    
    
    public String key() {
    	return keys[keys.length-1];
    }
    
	public Map<String,TrieNode_InMemory> getChildren() {
		return children;
	}
	
	public TrieNode_InMemory[] getChildrenNodes() {
		Collection<TrieNode_InMemory> childrenNodes = (Collection<TrieNode_InMemory>) children.values();
		return childrenNodes.toArray(new TrieNode_InMemory[] {});
	}
	
	public void addChild(String key, TrieNode_InMemory node) {
		this.getChildren().put(key, node);
	}
	
	public boolean hasChild(String key) {
		return this.getChildren().containsKey(key);
	}
	
    public void setChildren(HashMap<String,TrieNode_InMemory> _children) {
    	this.children = _children;
    }
    
    public boolean hasTerminalNode() {
    	return this.hasChild("\\");
    }
    public TrieNode_InMemory getChildTerminalNode() {
    	return this.getChildren().get("\\");
    }
    
    public String getSurfaceForm() {
    	return surfaceForm;
    }
    
    public HashMap<String,Long> getSurfaceForms() {
    	return surfaceForms;
    }
    
    public Object[][] getOrderedSurfaceForms() {
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
    	if (this.surfaceForms.containsKey(form)) {
    		this.surfaceForms.put(form, new Long(this.surfaceForms.get(form).longValue()+1));
    	} else {
    		this.surfaceForms.put(form, new Long(1));
    	}
    }

    /*public void setMostFrequentTerminal(TrieNode _mostFrequentTerminal) {
    	this.mostFrequentTerminal = _mostFrequentTerminal;
    }*/

    
    public String getKeysAsString() {
        return String.join(" ",this.keys);
    }

    public boolean isWord() {
        return isWord;
    }

    public void setIsWord(boolean word) {
        isWord = word;
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
		if (isWord) {
			toS += "TrieNode:";			
		} else {
			toS += "TrieTerminalNode:";
		}
		toS += 
			"\n"+
        	"    segments = "+this.getKeysAsString()+"\n"+
        	"    frequency = "+this.frequency+"\n"+
        	"    isWord = "+this.isWord+"\n";
        if (isWord) {
        	toS += "    surfaceForm = "+this.surfaceForm+"\n"; 
        }
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

}