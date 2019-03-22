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

public class TrieNode {
    public String[] keys = new String[] {};
    protected boolean isWord = false;
    protected long frequency = 0;
    protected HashMap<String,TrieNode> children;
    protected TrieNode mostFrequentTerminal;
    protected Map<String,Object> stats = new HashMap<String,Object>();
    protected String surfaceForm = null;
    protected HashMap<String,Long> surfaceForms = new HashMap<String,Long>();
    
    
    public String key() {
    	return keys[keys.length-1];
    }
    
	public HashMap<String,TrieNode> getChildren() {
		return children;
	}
	
	public TrieNode[] getChildrenNodes() {
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
    	return this.hasChild("\\");
    }
    public TrieNode getChildTerminalNode() {
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
// Commented out temporarily because with the coming changes, the compilation will have to be done again.
//    	mostFrequentTerminal = null;
    }
    
    /**
     * 
     * @return TrieNode The most frequent terminal from this node. If more than 1 with the same frequency, returns the first one.
     */
    public TrieNode getMostFrequentTerminal() {
    	// TODO: sauvegarder également la plage (N most frequent); actuellement, c'est 1 (THE most frequent)
// Commented out temporarily because with the coming changes, the compilation will have to be done again.
//    	if ( mostFrequentTerminal==null ) { 
    	TrieNode[] mostFrequentTerminals = getMostFrequentTerminals(1);
//    	}
    	return mostFrequentTerminals[0];
    }
    
    public TrieNode[] getMostFrequentTerminals(int n) {
    	TrieNode[] mostFrequentTerminals = getMostFrequentTerminals(n, new TrieNode[] {});
    	return mostFrequentTerminals;
    }
    
    public TrieNode[] getMostFrequentTerminals(int n, TrieNode[] exclusions) {
    	return _getMostFrequentTerminals(n, exclusions);
    }

    /*
     * Returns the n most frequent terminals of this node. 
     * If this node has less than n terminals
     */
	private TrieNode[] _getMostFrequentTerminals(int n, TrieNode[] exclusions) {
		TrieNode[] terminals = this.getAllTerminals();
		for (TrieNode nodeToExclude : exclusions)
			terminals = (TrieNode[]) ArrayUtils.removeElement(terminals, nodeToExclude);
	    Arrays.sort(terminals, new Comparator<TrieNode>() {
	        @Override
	        public int compare(TrieNode o1, TrieNode o2) {
	        	if (o1.getFrequency() == o2.getFrequency())
	        		return 0;
	            return o1.getFrequency() < o2.getFrequency()? 1 : -1;
	        }
	    });
	    TrieNode[] mostFrequentTerminals;
	    if (n > terminals.length) {
	    	mostFrequentTerminals = Arrays.copyOfRange(terminals, 0, terminals.length);
	    } else {
	    	mostFrequentTerminals = Arrays.copyOfRange(terminals, 0, n);
	    }
		return mostFrequentTerminals;
	}
	
    
    /**
     * 
     * @return TrieNode[] Array of the terminal nodes from this node.
     */
	public TrieNode[] getAllTerminals() {
		Vector<TrieNode> list = new Vector<TrieNode>();
		_getAllTerminals(list);
		return list.toArray(new TrieNode[] {});
	}
	
	private void _getAllTerminals(Vector<TrieNode> entryList) {
		// Add this node if it is a terminal node
		if (this.isWord())
			entryList.add(this);
		// Add terminals of children of this node, if any
		else {
			Vector<TrieNode> list = new Vector<TrieNode>();
			HashMap<String, TrieNode> children = this.getChildren();
			String[] keys = children.keySet().toArray(new String[] {});
			for (int i = 0; i < keys.length; i++) {
				TrieNode childNode = children.get(keys[i]);
				TrieNode[] terminals = childNode.getAllTerminals();
				list.addAll(Arrays.asList(terminals));
			}
			entryList.addAll(list);
		}
	}
	
    @Override
    public String toString() {
    	String str = "[TrieNode:\n" +
        		"    segments = "+this.getKeysAsString()+"\n"+
        		"    frequency = "+this.frequency+"\n"+
        		"    isWord = "+this.isWord+"\n";
    	if (this.isWord()) {
    		if (this.surfaceForms.size()==0)
    			str += "    surfaceForm = "+this.surfaceForm;
    		else {
    			Object[][] formsFreqs = this.getOrderedSurfaceForms();
				str += "    surfaceForms = "+formsFreqs[0][0]+" ("+formsFreqs[0][1]+")\n";
				
    			for (int i=1; i<formsFreqs.length; i++)
    				str += "                   "+formsFreqs[i][0]+" ("+formsFreqs[i][1]+")\n";
    		}
    	}
        str += "    ]";
        return str;
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