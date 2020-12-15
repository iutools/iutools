//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//
// -----------------------------------------------------------------------
//           (c) Conseil national de recherches Canada, 2003
//           (c) National Research Council of Canada, 2003
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// Document/File:		DefinitionDeSuffixe.java
//
// Type/File type:		code Java / Java code
// 
// Auteur/Author:		Benoit Farley
//
// Organisation/Organization:	Conseil national de recherches du Canada/
//				National Research Council Canada
//
// Date de création/Date of creation:	
//
// Description: La méthode statique 'getDef' retourne dans un format HTML
//              l'information sur un suffixe.
//
// -----------------------------------------------------------------------


package org.iutools.bin;

import java.io.*;

import ca.inuktitutcomputing.data.LinguisticData;
import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.data.Suffixes;


public class DescriptionOfSuffix {
	
    static public void main(String args[]) throws LinguisticDataException {
    	LinguisticData.getInstance();
        try {
			Suffixes.getDef(args,new PrintStream(System.out,true,"utf-8"));
		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
		}
    }
    
    static void usage(String mess) {
		System.err.println("args: [-s csv] -m <suffix id>");
		System.err.println(mess);
		System.exit(1);    		
    }

}
