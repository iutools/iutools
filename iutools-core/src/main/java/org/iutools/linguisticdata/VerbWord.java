/*
 * Conseil national de recherche Canada 2006/
 * National Research Council Canada 2006
 * 
 * Cr�� le / Created on Nov 2, 2006
 * par / by Benoit Farley
 * 
 */
package org.iutools.linguisticdata;

import java.util.HashMap;
import java.util.Hashtable;

public class VerbWord {

    public String verb;
    public String passive;
    static public Hashtable<String,VerbWord> hash = new Hashtable<String,VerbWord>();

    public VerbWord(HashMap<String,String> v) {
        verb = v.get("verb");
        passive = v.get("passive");
    }

    static public void addToHash(String key, Object obj) {
        hash.put(key,(VerbWord)obj);
    }

}
