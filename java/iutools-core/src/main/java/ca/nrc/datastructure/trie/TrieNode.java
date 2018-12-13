package ca.nrc.datastructure.trie;

import java.util.Arrays;
import java.util.Collection;
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
    
    public String getKeys() {
        return String.join("",this.keys);
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
		HashMap<String,TrieNode> children = this.getChildren();
		if (children.size()==0)
			if (!this.isWord())
				return null;
			else
				return this;
		String[] keys = children.keySet().toArray(new String[]{});
		TrieNode childNodeWithMaxFrequency = null;
		for (int i=0; i<keys.length; i++) {
			TrieNode childNode = children.get(keys[i]);
			TrieNode childNodeMax = childNode._getMostFrequentTerminal(max);
			if (childNodeMax!=null && childNodeMax.getFrequency()>max) {
				max = childNodeMax.getFrequency();
				childNodeWithMaxFrequency = childNodeMax;
			}
		}
		return childNodeWithMaxFrequency;
	}
	
    @Override
    public String toString() {
    	TrieNode mostFrequentTerminal = null;
    	if (!this.isWord)
    		mostFrequentTerminal = this.getMostFrequentTerminal();
        return "[TrieNode:\n" +
        		"    segments = "+this.getKeys()+"\n"+
        		"    frequency = "+this.frequency+"\n"+
        		"    isWord = "+this.isWord+"\n"+
        		(mostFrequentTerminal!=null ?
        				"    mostFrequentTerminal = [TrieNode:\n" +
        				"                                 segments = "+mostFrequentTerminal.getKeys()+"\n"+
        				"                                 frequency = "+mostFrequentTerminal.getFrequency()+"\n"+
                		"                                 isWord = "+mostFrequentTerminal.isWord+"\n"+
        				"                                 ]\n" : "")+
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