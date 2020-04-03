/*
 * Conseil national de recherche Canada 2006/
 * National Research Council Canada 2006
 * 
 * Cr�� le / Created on Mar 2, 2006
 * par / by Benoit Farley
 * 
 */
package ca.inuktitutcomputing.data.constraints;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.data.Morpheme;

public interface Conditions {

    boolean isMetBy(Morpheme m) throws LinguisticDataException;
    boolean isMetByFullMorphem(Morpheme m) throws LinguisticDataException;
    String toText(String lang);
    Condition expand();
}
