/*
 * Conseil national de recherche Canada 2006/
 * National Research Council Canada 2006
 * 
 * Cr�� le / Created on Nov 22, 2006
 * par / by Benoit Farley
 * 
 */
package org.iutools.linguisticdata;

import java.util.HashMap;
import java.util.Hashtable;

public class Source {

   public String id;
   public String authorSurName;
   public String authorMidName;
   public String authorFirstName;
    public String title;
    public String subtitle;
    public String publisher;
    public String publisherMisc;
    public String location;
    public String year;

    static public Hashtable<String,Source> hash = new Hashtable<String,Source>();

    public Source(HashMap<String,String> v) {
        id = v.get("id");
        authorSurName = v.get("authorSurName");
        authorMidName = v.get("authorMidName");
        authorFirstName = v.get("authorFirstName");
        title = v.get("title");
        subtitle = v.get("subtitle");
        publisher = v.get("publisher");
        publisherMisc = v.get("publisherMisc");
        location = v.get("city/country");
        year = v.get("year");
    }
    
    static public void addToHash(String key, Object obj) {
        hash.put(key,(Source)obj);
    }
        
}
