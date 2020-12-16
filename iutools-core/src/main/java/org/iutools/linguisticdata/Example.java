//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//
// -----------------------------------------------------------------------
//           (c) Conseil national de recherches Canada, 2002
//           (c) National Research Council of Canada, 2002
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// Document/File:		Example.java
//
// Type/File type:		code Java / Java code
// 
// Auteur/Author:		Benoit Farley
//
// Organisation/Organization:	Conseil national de recherches du Canada/
//				National Research Council Canada
//
// Date de cr�ation/Date of creation:	
//
// Description: Classe pour les exemples d'affixe.
//
// -----------------------------------------------------------------------

//                                  ***

// -------------------//Information RCS Information\\---------------------
// $Id: Example.java,v 1.1 2009/06/19 19:38:07 farleyb Exp $
//
// Commentaires RCS---------------------------------------RCS Log Messages
//
// $Log: Example.java,v $
// Revision 1.1  2009/06/19 19:38:07  farleyb
// Nouvelle version de Inuktitut Juin 2009
//
// Revision 1.1  2003/10/10 06:01:10  desiletsa
// Premi�re sauvegarde
//
// Revision 1.0  2003-06-25 13:19:51-04  farleyb
// Initial revision
//
// Revision 1.0  2003-01-21 17:01:57-05  farleyb
// Initial revision
//
//
// -------------------\\Information RCS Information//---------------------
//
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@


package org.iutools.linguisticdata;

import java.util.*;


public class Example {
    public String term;
    public String nb;
    public String termExSyl;
    public String termExLat;
    public String exampleLat;
    public String exampleSyl;
    public String eng;
    public String fre;


    public Example(Vector<String> v) {
	term = v.elementAt(0);
	nb = v.elementAt(1);
	termExLat = v.elementAt(2);
	termExSyl = v.elementAt(3);
	exampleLat = v.elementAt(4);
	exampleSyl = v.elementAt(5);
	eng = v.elementAt(6);
	fre = v.elementAt(7);
    }

}
