// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//
// -----------------------------------------------------------------------
//           (c) Conseil national de recherches Canada, 2003
//           (c) National Research Council of Canada, 2003
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// Document/File: DefinitionDeRacine.java
//
// Type/File type: code Java / Java code
// 
// Auteur/Author: Benoit Farley
//
// Organisation/Organization: Conseil national de recherches du Canada/
//				National Research Council Canada
//
// -----------------------------------------------------------------------

package ca.inuktitutcomputing.applications;

import java.io.*;

import ca.inuktitutcomputing.data.LinguisticData;
import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.data.Roots;


public class DescriptionOfRoot {

    static public void main(String args[]) throws LinguisticDataException {
    	LinguisticData.getInstance();
        try {
			Roots.getDef(args,new PrintStream(System.out,true,"utf-8"));
		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
		}
    }
    
    static void usage(String mess) {
		System.err.println("args: [-s csv] -m <root id>");
		System.err.println(mess);
		System.exit(1);    		
    }

}