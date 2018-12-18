package ca.nrc.datastructure.trie;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class TrieNode {
    public String[] keys = new String[] {};
    protected boolean isWord = false;
    protected long frequency = 0;
    protected HashMap<String,TrieNode> children;
    protected TrieNode mostFrequentTerminal;
    protected Map<String,Object> stats = new HashMap<String,Object>();
    
    public void setChildren(HashMap<String,TrieNode> _children) {
    	this.children = _children;
    }

    public void setMostFrequentTerminal(TrieNode _mostFrequentTerminal) {
    	this.mostFrequentTerminal = _mostFrequentTerminal;
    }

    public TrieNode() {
        this.children = new HashMap<String,TrieNode>();
    }

    public TrieNode(String[] _keys) {
        this();
        this.keys = _keys;
    }
    
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
    	mostFrequentTerminal = null;
    }
    
    public TrieNode getMostFrequentTerminal() {
    	// TODO: sauvegarder également la plage (N most frequent); actuellement, c'est 1 (THE most frequent0
    	if ( mostFrequentTerminal==null )
    		mostFrequentTerminal = _getMostFrequentTerminal(0);
    	return mostFrequentTerminal;
    }

    
    // Returns the first of possibly more than 1 possibilities
	public TrieNode[] getAllTerminals() {
		Vector<TrieNode> list = new Vector<TrieNode>();
		return this._getAllTerminals(list).toArray(new TrieNode[]{});
	}
	
	private Vector<TrieNode> _getAllTerminals(Vector<TrieNode> initialList) {
		Vector<TrieNode> list = new Vector<TrieNode>();
		if (!this.isWord) {
			HashMap<String,TrieNode> children = this.getChildren();
			String[] keys = children.keySet().toArray(new String[]{});
			for (int i=0; i<keys.length; i++) {
				TrieNode childNode = children.get(keys[i]);
				Vector<TrieNode> terminals = childNode._getAllTerminals(initialList);
				list.addAll(terminals);
			}
		} else {
			list.add(this);
		}
		return list;
	}
	
    // Returns the first of possibly more than 1 possibilities
	private TrieNode _getMostFrequentTerminal(long max) {
		if (this.isWord())
			return this;
			
		TrieNode[] terminals = this.getAllTerminals();
		long maxFreq = 0;
		TrieNode mostFreqTerm = null;
		for (TrieNode terminal : terminals)
			if (terminal.getFrequency() > maxFreq) {
				maxFreq = terminal.getFrequency();
				mostFreqTerm = terminal;
			}
		return mostFreqTerm;
	}
	
    @Override
    public String toString() {
        return "[TrieNode:\n" +
        		"    segments = "+this.getKeysAsString()+"\n"+
        		"    frequency = "+this.frequency+"\n"+
        		"    isWord = "+this.isWord+"\n"+
        		"    ]";
    }

	public HashMap<String,TrieNode> getChildren() {
		return children;
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