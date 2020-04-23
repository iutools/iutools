/*
 * Conseil national de recherche Canada 2005/
 * National Research Council Canada 2005
 * 
 * Cr�� le / Created on Jul 7, 2005
 * par / by Benoit Farley
 * 
 */
package ca.inuktitutcomputing.data;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Hashtable;

import ca.inuktitutcomputing.data.constraints.Conditions;
import ca.inuktitutcomputing.data.constraints.Imacond;
import ca.inuktitutcomputing.data.constraints.ParseException;

public class Pronoun extends Base {
	//
	String person;
	static public Hashtable<String,Pronoun> hash = new Hashtable<String,Pronoun>();
	//
	
    //------------------------------------------------------------------------   
    public Pronoun() {
    }
    
    public Pronoun(HashMap<String,String> v) {
		getAndSetBaseAttributes(v);
		type = v.get("type");
		number = v.get("number");
		variant = v.get("variant");
		nb = v.get("nb");
		if (nb==null || nb.equals(""))
			nb = "1";
		nature = v.get("nature");
		person = v.get("per");
		String comb = v.get("combination");
		if (comb != null) {
			setCombiningParts(comb);
		}
		String cs = v.get("condOnNext");
        if (cs != null && !cs.equals(""))
            try {
                nextCondition = (Conditions) new Imacond(
                        new ByteArrayInputStream(cs.getBytes())).ParseCondition();
            } catch (ParseException e) {
            }
		setAttrs();
    }
    
    //------------------------------------------------------------------------   
	public void addToHash(String key, Object obj) {
	    hash.put(key,(Pronoun)obj);
	}

    //------------------------------------------------------------------------   
	void setAttrs() {
		setAttributes();
		setId();
	}

    void setAttributes() {
    	HashMap<String,Object> prAttrs = new HashMap<String,Object>();
    	prAttrs.put("person", person);
    	super.setAttributes(prAttrs);
    }

	
	boolean isFirstPerson() {
		if ( person.equals("1"))
			return true;
		else
			return false;
	}
	
	boolean isSecondPerson() {
		if ( person.equals("2"))
			return true;
		else
			return false;
	}
	
	boolean isThirdPerson() {
		if ( person.equals("3"))
			return true;
		else
			return false;
	}
	
    //------------------------------------------------------------------------   
	public String showData() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Pronoun: morpheme= " + morpheme + "\n");
		sb.append("id= "+id+"\n");
		sb.append("variant= " + variant + "\n");
		sb.append("nb= " + nb + "\n");
		sb.append("type= " + type + "\n");
		sb.append("nature= " + nature + "\n");
		sb.append("per= " + person + "\n");
		sb.append("number= " + number + "\n");
		sb.append("englishMeaning= " + englishMeaning + "\n");
		sb.append("frenchMeaning= " + frenchMeaning + "\n");
    	sb.append("dbName= "+dbName+"\n");
    	sb.append("tableName= "+tableName+"\n");
    	sb.append("]");
 		return sb.toString();
	}



}
