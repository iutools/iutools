package ca.pirurvik.iutools.core;

import ca.inuktitutcomputing.data.LinguisticDataAbstract;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphInuk;

/**
 * Hello world!
 *
 */
public class DecomposeInuktutWord 
{
    public static void main( String[] args )
    {
        System.out.println( "Initializing data base..." );
		LinguisticDataAbstract.init("csv");
    	String word = args[0];
        System.out.println( "Decomposing '"+word+"' ..." );
		LinguisticDataAbstract.init("csv");
		Decomposition [] decs = null;
        try {
			decs = MorphInuk.decomposeWord(word);
	        System.out.println("Nb. decompositions: "+decs.length);
	        System.out.println("decs: "+decs.toString());
	        for (int i=0; i<decs.length; i++) {
	        	System.out.println(decs[i].toStr2());
	        }
		} catch (Exception e) {
			System.err.println("Error "+e.getClass().getName());
			System.err.println(e.getMessage());
		}
    }
}
