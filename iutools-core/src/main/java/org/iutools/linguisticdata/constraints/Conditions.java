/*
 * Conseil national de recherche Canada 2006/
 * National Research Council Canada 2006
 * 
 * Cr�� le / Created on Mar 2, 2006
 * par / by Benoit Farley
 * 
 */
package org.iutools.linguisticdata.constraints;

import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.linguisticdata.Morpheme;

public interface Conditions {

    boolean isMetBy(Morpheme m) throws LinguisticDataException;
    boolean isMetByFullMorphem(Morpheme m) throws LinguisticDataException;
    String toText(String lang);
    Condition expand();
}
