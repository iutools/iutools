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

import ca.inuktitutcomputing.data.LinguisticDataAbstract;
import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.data.Roots;
import ca.inuktitutcomputing.utilities1.Util;


public class DescriptionOfRoot {

    static public void main(String args[]) throws LinguisticDataException {
    	if (args.length==0) {
    		usage("Argument missing");
    	}
    	String source = Util.getArgument(args, "s");
    	boolean res = LinguisticDataAbstract.init(source);
    	if ( ! res ) {
    		usage("'csv' is the only one accepted value for the \"source\" option '-s'");
    	}
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