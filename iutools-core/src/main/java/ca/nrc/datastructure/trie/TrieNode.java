package ca.nrc.datastructure.trie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TrieNode {
    public String[] keys = new String[] {};
    
    // If not null, it means this node is a "terminal" node, i.e.
    // it corresponds to an complete expression that cannot have 
    // children.
    //
    public String surfaceForm = null;
    
    // TODO: This attribute it redundant with (surfaceForm != null)
    //   getRid of it once we have converted the Trie from an InMemory 
    //   version to a InFileSystem version
    //
    public boolean isWord = false;
    
    protected long frequency = 0;
    protected Map<String,TrieNode> children = new HashMap<String,TrieNode>();
    protected TrieNode mostFrequentTerminal;
    protected Map<String,Object> stats = new HashMap<String,Object>();
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
    	return surfaceForm != null;
    }
    
    public void setTerminalSurfaceForm(String _form) {
    	this.surfaceForm = _form;
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
//    	this.surfaceForm = form;
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
        return String.join(" ", this.keys);
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
}