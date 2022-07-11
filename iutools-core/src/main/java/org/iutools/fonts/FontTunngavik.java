//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//
// -----------------------------------------------------------------------
//           (c) Conseil national de recherches Canada, 2002
//           (c) National Research Council of Canada, 2002
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// Document/File:		PoliceTunngavik.java
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
// Description: 
//
// -----------------------------------------------------------------------

//                                  ***

// -------------------//Information RCS Information\\---------------------
// $Id: PoliceTunngavik.java,v 1.1 2009/06/19 19:38:37 farleyb Exp $
//
// Commentaires RCS---------------------------------------RCS Log Messages
//
// $Log: PoliceTunngavik.java,v $
// Revision 1.1  2009/06/19 19:38:37  farleyb
// Nouvelle version de Inuktitut Juin 2009
//
// Revision 1.11  2009/02/10 15:22:30  farleyb
// *** empty log message ***
//
// Revision 1.10  2006/11/01 15:29:57  farleyb
// *** empty log message ***
//
// Revision 1.9  2006/10/20 17:01:26  farleyb
// *** empty log message ***
//
// Revision 1.8  2006/10/19 13:33:18  farleyb
// *** empty log message ***
//
// Revision 1.7  2006/07/18 21:11:43  farleyb
// *** empty log message ***
//
// Revision 1.6  2006/06/19 17:46:27  farleyb
// *** empty log message ***
//
// Revision 1.5  2006/05/03 17:41:02  farleyb
// Ajout de wordChars aux polices, n�cessaire entre autres pour le surlignage, o� il faut d�terminer la d�limitation des mots.
//
// Revision 1.4  2006/03/09 18:01:53  farleyb
// Diverses modifications re tables de conversion � unicode
//
// Revision 1.3  2005/02/14 17:22:21  stojanovim
// Small changes to Police*
//
// Revision 1.2  2004/12/07 20:49:49  farleyb
// *** empty log message ***
//
// Revision 1.1  2003/10/10 06:01:10  desiletsa
// Premi�re sauvegarde
//
// Revision 1.0  2003-06-25 13:19:58-04  farleyb
// Initial revision
//
// Revision 1.0  2002-12-03 12:40:41-05  farleyb
// Initial revision
//
//
// -------------------\\Information RCS Information//---------------------
//
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@


package org.iutools.fonts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iutools.script.TransCoder;
import org.iutools.utilities.Counter;

// Remplacement de codes d'une police donn�e 
// en codes unicode �quivalents repr�sentant
// les m�mes caract�res

public class FontTunngavik {


    public static String unicodesICI2Codes[][] = {

        // i
        { "\u1403", "w"  }, // i
        { "\u1431", "W"  }, // pi
        { "\u144E", "t"  }, // ti
        { "\u146D", "r"  }, // ki
        { "\u148B", "Q"  }, // gi
        { "\u14A5", "u"  }, // mi
        { "\u14C2", "i"  }, // ni
        { "\u14EF", "y"  }, // si
        { "\u14D5", "o"  }, // li
        { "\u1528", "p"  }, // ji
        { "\u1555", "="  }, // vi
        { "\u1546", "E"  }, // ri
        { "\u157F", "e"  }, // qi
        { "\u158F", "q"  }, // Ni
        { "\u15A0", "O"  }, // &i

        // ii
        { "\u1404", "`w" }, // ii
        { "\u1432", "`W" }, // pii
        { "\u144F", "`t" }, // tii
        { "\u146E", "`r" }, // kii
        { "\u148C", "`Q" }, // gii
        { "\u14A6", "`u" }, // mii
        { "\u14C3", "~i" }, // nii
        { "\u14F0", "<y" }, // sii
        { "\u14D6", "~o" }, // lii
        { "\u1529", ">p" }, // jii
        { "\u1556", "`=" }, // vii
        { "\u1547", "~E" }, // rii
        { "\u1580", "`e" }, // qii
        { "\u1590", "<q" }, // Nii
        { "\u15A1", "~O" }, // &ii

        { "\u1404", "\u0192" }, // ii
        { "\u1432", "\u2020" }, // pii
        { "\u144F", "\u2030" }, // tii
        { "\u146E", "\u0152" }, // kii
        { "\u148C", "\u0161" }, // gii
        { "\u14A6", "\u0178" }, // mii
        { "\u14C3", "\u00a3" }, // nii
        { "\u14F0", "\u00a6" }, // sii
        { "\u14D6", "\u00a9" }, // lii
        { "\u1529", "\u00ac" }, // jii
        { "\u1556", "\u00b0" }, // vii
        { "\u1547", "\u00b3" }, // rii
        { "\u1580", "\u00b6" }, // qii
        { "\u1590", "\u00c2" }, // Nii
        { "\u15A1", "\u00c5" }, // &ii

        // u
        { "\u1405", "s"  }, // u
        { "\u1433", "S" }, // pu
        { "\u1450", "g"  }, // tu
        { "\u146F", "f"  }, // ku
        { "\u148D", "A"  }, // gu
        { "\u14A7", "j"  }, // mu
        { "\u14C4", "k"  }, // nu
        { "\u14F1", "h"  }, // su
        { "\u14D7", "l" }, // lu
        { "\u152A", "J"  }, // ju
        { "\u1557", "K"  }, // vu
        { "\u1548", "D"  }, // ru
        { "\u1581", "d"  }, // qu
        { "\u1591", "a"  }, // Nu
        { "\u15A2", "L"  }, // &u

        // uu
        { "\u1406", ">s" }, // uu
        { "\u1434", ">S" }, // puu
        { "\u1451", ">g" }, // tuu
        { "\u1470", "]f" }, // kuu
        { "\u148E", "]A" }, // guu
        { "\u14A8", "<j" }, // muu
        { "\u14C5", "~k" }, // nuu
        { "\u14F2", "<h" }, // suu
        { "\u14D8", "~l" }, // luu
        { "\u152B", "<J" }, // juu
        { "\u1558", ">K" }, // vuu
        { "\u1549", ">D" }, // ruu
        { "\u1582", "<d" }, // quu
        { "\u1592", "<a" }, // Nuu
        { "\u15A3", "~L" }, // &uu

        { "\u1406", "\u201e" }, // uu
        { "\u1434", "\u2021" }, // puu
        { "\u1451", "\u0160" }, // tuu
        { "\u1470", "\u02dc" }, // kuu
        { "\u148E", "\u203a" }, // guu
        { "\u14A8", "\u00a1" }, // muu
        { "\u14C5", "\u00a4" }, // nuu
        { "\u14F2", "\u00a7" }, // suu
        { "\u14D8", "\u00aa" }, // luu
        { "\u152B", "\u00ad" }, // juu
        { "\u1558", "\u00b1" }, // vuu
        { "\u1549", "\u00b4" }, // ruu
        { "\u1582", "\u00c0" }, // quu
        { "\u1592", "\u00c3" }, // Nuu
        { "\u15A3", "\u00c6" }, // &uu

        // a
        { "\u140A", "x" }, // a
        { "\u1438", "X" }, // pa
        { "\u1455", "b" }, // ta
        { "\u1472", "v" }, // ka
        { "\u1490", "Z" }, // ga
        { "\u14AA", "m" }, // ma
        { "\u14C7", "N" }, // na
        { "\u14F4", "n" }, // sa
        { "\u14DA", "M" }, // la
        { "\u152D", "/" }, // ja
        { "\u1559", "?" }, // va
        { "\u154B", "C" }, // ra
        { "\u1583", "c" }, // qa
        { "\u1593", "z" }, // Na
        { "\u15A4", "I" }, // &a

        // aa
        { "\u140B", "<x" }, // aa
        { "\u1439", "<X" }, // paa
        { "\u1456", "]b" }, // taa
        { "\u1473", ">v" }, // kaa
        { "\u1491", ">Z" }, // gaa
        { "\u14AB", ">m" }, // maa
        { "\u14C8", "~N" }, // naa
        { "\u14F5", ">n" }, // saa
        { "\u14DB", "~M" }, // laa
        { "\u152E", ">/" }, // jaa
        { "\u155A", "<?" }, // vaa
        { "\u154C", ">C" }, // raa
        { "\u1584", "`c" }, // qaa
        { "\u1594", "<z" }, // Naa
        { "\u15A5", "~I" }, // &aa

        { "\u140B", "\u2026" }, // aa
        { "\u1439", "\u02c6" }, // paa
        { "\u1456", "\u2039" }, // taa
        { "\u1473", "\u2122" }, // kaa
        { "\u1491", "\u0153" }, // gaa
        { "\u14AB", "\u00a2" }, // maa
        { "\u14C8", "\u00a5" }, // naa
        { "\u14F5", "\u00a8" }, // saa
        { "\u14DB", "\u00ab" }, // laa
        { "\u152E", "\u00ae" }, // jaa
        { "\u155A", "\u00b2" }, // vaa
        { "\u154C", "\u00b5" }, // raa
        { "\u1584", "\u00c1" }, // qaa
        { "\u1594", "\u00c4" }, // Naa
        { "\u15A5", "\u00c7" }, // &aa

        // consonnes seules
        { "\u1449", "2" }, // p
        { "\u1466", "5" }, // t
        { "\u1483", "4" }, // k
        { "\u14A1", "[" }, // g
        { "\u14BB", "7" }, // m
        { "\u14D0", "8" }, // n
        { "\u1505", "{" }, // s
        { "\u14EA", "9" }, // l
        { "\u153E", "0" }, // j
        { "\u155D", "}" }, // v
        { "\u1550", "3" }, // r
        { "\u1585", "6" }, // q
        { "\u1595", "1" }, // N
        { "\u15A6", "P" }, // &
        { "\u157C", "B" } // H
    };

    public static String unicodesICI2CodesDigits[][] = {

        // chiffres
        { "1", "!"  }, // 1
        { "2", "@"  }, // 2
        { "3", "#"  }, // 3
        { "4", "$"  }, // 4
        { "5", "%"  }, // 5
        { "6", "^"  }, // 6
        { "7", "&"  }, // 7
        { "8", "*"  }, // 8
        { "9", "("  }, // 9
        { "0", ")"  } // 0
    };
        
    public static String unicodesICI2CodesOthers[][] = {

        // autres signes
        {"=","+"}, // =
        {"%","-"}, // %
        {"/","F"}, // /
        { "(","G"  }, // (
        { ")","H"  }, // )
        {"$","R"}, // $
        {"+","T"}, // +
        {"!","U"}, // !
        {"?","V"}, // ?
        {"_","Y"}, // _
        {"}","\\"}, // }
        {"-","_"}, // -
        {"{","|"} // {

    };
    // <  >  ]  `  ~
    public static String dotCodes = "<>]`~";
    
    public static HashMap<String,String> legacy2unicode = new HashMap<String,String>();

    public static TransCoder transcoderToUnicodeICI;
    public static TransCoder transcoderToUnicodeAIPAITAI;
    public static TransCoder transcoderToFontICI;
    public static TransCoder transcoderToFontAIPAITAI;

    public static String wordChars = "";
    public static Set fontChars = new HashSet();
    static {
        for (int i=0; i<unicodesICI2Codes.length; i++) {
            String chars = unicodesICI2Codes[i][1];
            /*
             * If there are more than 1 character, it is a long syllable made of
             * a dot code and a short syllable character, both of which will 
             * already be in the list. So we don't process this long-syllable
             * character.
             */
            if (chars.length()==1) {
                wordChars += unicodesICI2Codes[i][1];
                for (int j=0; j<unicodesICI2Codes[i][1].length(); j++)
                    fontChars.add(new Character(unicodesICI2Codes[i][1].charAt(j)));
                legacy2unicode.put(unicodesICI2Codes[i][1], unicodesICI2Codes[i][0]);
            }
        }
        for (int i=0; i<unicodesICI2CodesDigits.length; i++) {
            wordChars += unicodesICI2CodesDigits[i][1];
            for (int j=0; j<unicodesICI2CodesDigits[i][1].length(); j++)
                fontChars.add(new Character(unicodesICI2CodesDigits[i][1].charAt(j)));
        }
        for (int i=0; i<unicodesICI2CodesOthers.length; i++) {
            wordChars += unicodesICI2CodesOthers[i][1];
        }
        wordChars += dotCodes;
        for (int j=0; j<dotCodes.length(); j++)
            fontChars.add(new Character(dotCodes.charAt(j)));
    }

    static public String transcodeToUnicode(String s) {
        return transcodeToUnicode(s,null); // null = no aipaitai
    }
    
    /*
     * aipaitaiMode: String -  "aipaitai" : convertir a+i au caract�re unicode AI
     */
    static public String transcodeToUnicode(String s, String aipaitaiMode) {
    	Logger logger = LogManager.getLogger("FontTunngavik.transcodeToUnicode");
    	logger.debug("s: "+s);
        int aipaitai = aipaitaiMode==null? 0 : aipaitaiMode.equals("aipaitai")? 1 : 0;
        boolean dot = false;
        int i=0,j;
        Counter ic = new Counter(i);
        int l=s.length();
        char c,d,e;
        int sbl;
        StringBuffer sb = new StringBuffer();
        while (ic.n < l) {
            sbl = sb.length();
            c = s.charAt(ic.n);
            switch (c) {
            case '`':
            case '~':
            case '<':
            case '>':
            case ']':
            	dot = true;
            	ic.n++;
            	continue;
            
            /*case 'w': d = '\u1403'; if (dot) {d++;} break; // i
            case 'W': d = '\u1431'; if (dot) {d++;} break;  // pi
            case 't': d = '\u144E'; if (dot) {d++;} break;  // ti
            case 'u': d = '\u14A5'; if (dot) {d++;} break; // mi
            case 'i': d = '\u14C2'; if (dot) {d++;} break; // ni
            case 'y': d = '\u14EF'; if (dot) {d++;} break; // si
            case 'o': d = '\u14D5'; if (dot) {d++;} break;  // li
            case 'p': d = '\u1528'; if (dot) {d++;} break;  // ji
            case '=': d = '\u1555'; if (dot) {d++;} break;  // vi
            case 'O': d = '\u15A0'; if (dot) {d++;} break; // &i
            */
            case 'w': // i
            case 'W': // pi
            case 't': // ti
            case 'u': // mi
            case 'i': // ni
            case 'y': // si
            case 'o': // li
            case 'p': // ji
            case '=': // vi
            case 'O': // &i
            	d=legacy2unicode.get(Character.toString(c)).charAt(0); if (dot) {d++;} break;

            case 'r': // ki
                //d = '\u146D'; 
                d=legacy2unicode.get(Character.toString(c)).charAt(0);
                if (sbl > 1 && sb.charAt(sbl-1)=='\u1550' && sb.charAt(sbl-2)=='\u1550') {
                    sb.deleteCharAt(sbl-1);
                    sb.deleteCharAt(sbl-2);
                    sb.append('\u1585');
                } else if (sbl > 1 && sb.charAt(sbl-1)=='\u1550' && sb.charAt(sbl-2)=='\u1585') {
                    sb.deleteCharAt(sbl-1);
                } else if (sbl > 0 && sb.charAt(sbl-1)=='\u1550') {
                    sb.deleteCharAt(sbl-1);
                    d = '\u157f';
                }
                if (dot) {d++;}
                break;
            case 'Q': // gi
                d = '\u148B'; 
                if (sbl != 0) {
                    e = sb.charAt(sbl-1);
                    switch (e) {
                    case '\u1595': //ng+gi
                        sb.deleteCharAt(sbl-1);
                        d = '\u158f'; // ngi
                        break;
                    case '\u1596': //nng+gi
                        sb.deleteCharAt(sbl-1);
                        d = '\u1671'; // nngi
                        break;
                    }
                }
                if (dot) {d++;} 
                break;
            case 'E': // ri
                d = '\u1546'; 
                if (sbl != 0 && sb.charAt(sbl-1)=='\u1585') { // q+ri = r+ri (rri)
                    sb.setCharAt(sbl-1,'\u1550');
                }
                if (dot) {d++;} 
                break;
            case 'e': // qi
                d = '\u157F';
                if (sbl != 0 && (sb.charAt(sbl-1)=='\u1550' || sb.charAt(sbl-1)=='\u1585')) { // r+qi = q+ki (qqi)
                    sb.deleteCharAt(sbl-1);
                    sb.append('\u1585');
                    d = '\u146d';
                    }
                if (dot) {d++;} 
                break; 
            case 'q': // ngi
                d = '\u158F';
                if (sbl != 0) {
                    e = sb.charAt(sbl-1);
                    switch (e) {
                    case '\u14d0': //n
                    case '\u1595': //ng
                        sb.deleteCharAt(sbl-1);
                        d = '\u1671'; // nngi
                        break;
                    }
                }
                if (dot) {d++;} 
                break;
                
            /*case '\u0192': d = '\u1404'; break; // ii
            case '\u2020': d = '\u1432'; break; // pii
            case '\u2030': d = '\u144F'; break; // tii
            case '\u0178': d = '\u14A6'; break; // mii
            case '\u00a3': d = '\u14C3'; break; // nii
            case '\u00a6': d = '\u14F0'; break; // sii
            case '\u00a9': d = '\u14D6'; break; // lii
            case '\u00ac': d = '\u1529'; break; // jii
            case '\u00b0': d = '\u1556'; break; // vii
            case '\u00c5': d = '\u15A1'; break; // &ii
            */
            case '\u0192': // ii
            case '\u2020': // pii
            case '\u2030': // tii
            case '\u0178': // mii
            case '\u00a3': // nii
            case '\u00a6': // sii
            case '\u00a9': // lii
            case '\u00ac': // jii
            case '\u00b0': // vii
            case '\u00c5': // &ii
            	d=legacy2unicode.get(Character.toString(c)).charAt(0); break;

            case '\u0152': // kii
                d = '\u146E'; 
                if (sbl > 1 && sb.charAt(sbl-1)=='\u1550' && sb.charAt(sbl-2)=='\u1550') {
                    sb.deleteCharAt(sbl-1);
                    sb.deleteCharAt(sbl-2);
                    sb.append('\u1585');
                } else if (sbl > 1 && sb.charAt(sbl-1)=='\u1550' && sb.charAt(sbl-2)=='\u1585') {
                    sb.deleteCharAt(sbl-1);
                } else if (sbl > 0 && sb.charAt(sbl-1)=='\u1550') {
                    sb.deleteCharAt(sbl-1);
                    d = '\u1580';
                }
                break;
            case '\u0161': // gi
                d = '\u148C'; 
                if (sbl != 0) {
                    e = sb.charAt(sbl-1);
                    switch (e) {
                    case '\u1595': //ng+gii
                        sb.deleteCharAt(sbl-1);
                        d = '\u1590'; // ngii
                        break;
                    case '\u1596': //nng+gii
                        sb.deleteCharAt(sbl-1);
                        d = '\u1672'; // nngii
                        break;
                    }
                }
                break;
            case '\u00b3': // rii
                if (ic.n != 0 && s.charAt(ic.n-1)=='6') { // q+rii = r+rii (rrii)
                    sb.setCharAt(sb.length()-1,'\u1550');
                }
                d = '\u1547'; 
                break; // rii
            case '\u00b6': // qii
                d = '\u1580'; // qii
                if (ic.n != 0 && (s.charAt(ic.n-1)=='3' || s.charAt(ic.n-1)=='6')) { // r+qii= q+kii (qqii)
                    sb.deleteCharAt(sb.length()-1);
                    sb.append('\u1585');
                    d = '\u146e';
                }
                break; 
            case '\u00c2': // ngii
                d = '\u1590'; // ngii
                if (ic.n != 0) {
                    e = s.charAt(ic.n-1);
                    switch (e) {
                    case '8': //n
                    case '1': //ng
                        sb.deleteCharAt(sb.length()-1);
                        d = '\u1672'; // nngii
                        break;
                    }
                }
            break;
            
            
            /*
            case 's': d = '\u1405'; if (dot) {d++;} break; // u
            case 'S': d = '\u1433'; if (dot) {d++;} break;  // pu
            case 'g': d = '\u1450'; if (dot) {d++;} break;  // tu
            case 'j': d = '\u14A7'; if (dot) {d++;} break; // mu
            case 'k': d = '\u14C4'; if (dot) {d++;} break;  // nu
            case 'h': d = '\u14F1'; if (dot) {d++;} break; // su
            case 'l': d = '\u14D7'; if (dot) {d++;} break; // lu
            case 'J': d = '\u152A'; if (dot) {d++;} break; // ju
            case 'K': d = '\u1557'; if (dot) {d++;} break; // vu
            case 'L': d = '\u15A2'; if (dot) {d++;} break; // &u
            */
            case 's': // u
            case 'S': // pu
            case 'g': // tu
            case 'j': // mu
            case 'k': // nu
            case 'h': // su
            case 'l': // lu
            case 'J': // ju
            case 'K': // vu
            case 'L': // &u
            	d=legacy2unicode.get(Character.toString(c)).charAt(0); if (dot) {d++;} break;

            case 'f': // ku
                d = '\u146F'; 
                if (sbl > 1 && sb.charAt(sbl-1)=='\u1550' && sb.charAt(sbl-2)=='\u1550') {
                    sb.deleteCharAt(sbl-1);
                    sb.deleteCharAt(sbl-2);
                    sb.append('\u1585');
                } else if (sbl > 1 && sb.charAt(sbl-1)=='\u1550' && sb.charAt(sbl-2)=='\u1585') {
                    sb.deleteCharAt(sbl-1);
                } else if (sbl > 0 && sb.charAt(sbl-1)=='\u1550') {
                    sb.deleteCharAt(sbl-1);
                    d = '\u1581';
                }
                if (dot) {d++;} 
                break;
            case 'A': // gu
                d = '\u148D'; 
                if (sbl != 0) {
                    e = sb.charAt(sbl-1);
                    switch (e) {
                    case '\u1595': //ng+gu
                        sb.deleteCharAt(sbl-1);
                        d = '\u1591'; // ngu
                        break;
                    case '\u1596': //nng+gu
                        sb.deleteCharAt(sbl-1);
                        d = '\u1673'; // nngu
                        break;
                    }
                }
                if (dot) {d++;} 
                break;
             case 'D': // ru
                 d = '\u1548'; 
                 if (sbl!= 0 && sb.charAt(sbl-1)=='\u1585') { // q+ru = r+ru (rru)
                     sb.setCharAt(sbl-1,'\u1550');
                 }
                 if (dot) {d++;} 
                 break; 
             case 'd': // qu
                 d = '\u1581';
                 if (sbl != 0 && (sb.charAt(sbl-1)=='\u1550' || sb.charAt(sbl-1)=='\u1585')) { // r+qu = q+ku (qqu)
                     sb.deleteCharAt(sbl-1);
                     sb.append('\u1585');
                     d = '\u146f';
                     }
                 if (dot) {d++;} 
                 break; 
             case 'a': // ngu
                 d = '\u1591';
                 if (sbl != 0 && (sb.charAt(sbl-1)=='\u14d0' || sb.charAt(sbl-1)=='\u1595')) {
                         sb.deleteCharAt(sbl-1);
                         d = '\u1673'; // nngu
                 }
                 if (dot) {d++;} 
                 break;

            /*
            case '\u201e': d = '\u1406'; break; // uu
            case '\u2021': d = '\u1434'; break; // puu
            case '\u0160': d = '\u1451'; break; // tuu
            case '\u00a1': d = '\u14A8'; break; // muu
            case '\u00a4': d = '\u14C5'; break; // nuu
            case '\u00a7': d = '\u14F2'; break; // suu
            case '\u00aa': d = '\u14D8'; break; // luu
            case '\u00ad': d = '\u152B'; break; // juu
            case '\u00b1': d = '\u1558'; break; // vuu
            case '\u00c6': d = '\u15A3'; break; // &uu
            */
             case '\u201e': // uu
             case '\u2021': // puu
             case '\u0160': // tuu
             case '\u00a1': // muu
             case '\u00a4': // nuu
             case '\u00a7': // suu
             case '\u00aa': // luu
             case '\u00ad': // juu
             case '\u00b1': // vuu
             case '\u00c6': // &uu
            	 d=legacy2unicode.get(Character.toString(c)).charAt(0); break;

            case '\u02dc': // kuu
                d = '\u1470'; 
                if (sbl > 1 && sb.charAt(sbl-1)=='\u1550' && sb.charAt(sbl-2)=='\u1550') {
                    sb.deleteCharAt(sbl-1);
                    sb.deleteCharAt(sbl-2);
                    sb.append('\u1585');
                } else if (sbl > 1 && sb.charAt(sbl-1)=='\u1550' && sb.charAt(sbl-2)=='\u1585') {
                    sb.deleteCharAt(sbl-1);
                } else if (sbl > 0 && sb.charAt(sbl-1)=='\u1550') {
                    sb.deleteCharAt(sbl-1);
                    d = '\u1582';
                }
                break;
            case '\u203a': // guu
                d = '\u148E'; 
                if (sbl != 0) {
                    e = sb.charAt(sbl-1);
                    switch (e) {
                    case '\u1595': //ng+guu
                        sb.deleteCharAt(sbl-1);
                        d = '\u1592'; // nguu
                        break;
                    case '\u1596': //nng+guu
                        sb.deleteCharAt(sbl-1);
                        d = '\u1674'; // nnguu
                        break;
                    }
                }
                break;
            case '\u00b4': // ruu
                if (ic.n != 0 && s.charAt(ic.n-1)=='6') { // q+ruu= r+ruu (rruu)
                    sb.setCharAt(sb.length()-1,'\u1550');
                }
                d = '\u1549'; break;
            case '\u00c0': // quu
                d = '\u1582'; // quu
                if (ic.n != 0 && (s.charAt(ic.n-1)=='3' || s.charAt(ic.n-1)=='6')) { // r+quu = q+kuu (qquu)
                    sb.deleteCharAt(sb.length()-1);
                    sb.append('\u1585');
                    d = '\u1470';
                }
                break; 
            case '\u00c3': // nguu
                d = '\u1592'; // nguu
                if (ic.n != 0) {
                    e = s.charAt(ic.n-1);
                    switch (e) {
                    case '8': //n
                    case '1': //ng
                        sb.deleteCharAt(sb.length()-1);
                        d = '\u1674'; // nnguu
                        break;
                    }
                }
                break;
            
            
            case 'x':  // a
            	d = __transliterateA('\u140a', dot, aipaitai, '\u1401', ic, s);
                break;
            case 'X': // pa
            	d = __transliterateA('\u1438', dot, aipaitai, '\u142F', ic, s);
                break;
            case 'b': // ta
            	d = __transliterateA('\u1455', dot, aipaitai, '\u144C', ic, s);
                break;
            case 'm': // ma
            	d = __transliterateA('\u14AA', dot, aipaitai, '\u14A3', ic, s);
                break;
            case 'N': // na
            	d = __transliterateA('\u14C7', dot, aipaitai, '\u14C0', ic, s);
                break;
            case 'n': // sa
            	d = __transliterateA('\u14F4', dot, aipaitai, '\u14ED', ic, s);
                break;
            case 'M': // la
            	d = __transliterateA('\u14DA', dot, aipaitai, '\u14D3', ic, s);
                break;
            case '/': // ja
            	d = __transliterateA('\u152d', dot, aipaitai, '\u1526', ic, s);
                break;
            case '?': // va
            	d = __transliterateA('\u1559', dot, aipaitai, '\u1553', ic, s);
                break;
            case 'I': d = '\u15A4'; if (dot) {d++;} break; // &a (pas de &ai en Unicode)
            case 'v': // ka
                d = '\u1472';
                if (sbl > 1 && sb.charAt(sbl-1)=='\u1550' && sb.charAt(sbl-2)=='\u1550') { // rr..ka > q..ka
                    sb.deleteCharAt(sbl-1);
                    sb.deleteCharAt(sbl-2);
                    sb.append('\u1585');
                } else if (sbl > 1 && sb.charAt(sbl-1)=='\u1550' && sb.charAt(sbl-2)=='\u1585') { // qr..ka > q..ka
                    sb.deleteCharAt(sbl-1);
                } else if (sbl > 0 && sb.charAt(sbl-1)=='\u1550') { // r..ka > qa
                    sb.deleteCharAt(sbl-1);
                    d = '\u1583';
                }
                if (d=='\u1472')
                	d = __transliterateA('\u1472', dot, aipaitai, '\u146B', ic, s);
                else
                	d = __transliterateA('\u1583', dot, aipaitai, '\u166f', ic, s);
                break;
            case 'Z': // ga
                d = '\u1490';
                // remplacer : ng..ga > nga
                //             nng..ga > nnga
                if (sbl != 0) {
                	if (sb.charAt(sbl-1)=='\u1595') {
                		sb.deleteCharAt(sbl-1);
                		d = '\u1593';
                	} else if (sb.charAt(sbl-1)=='\u1596') {
                		sb.deleteCharAt(sbl-1);
                		d = '\u1675';
                	} 
                }
                if (d=='\u1490')
                	d = __transliterateA('\u1490', dot, aipaitai, '\u1489', ic, s);
                else if (d=='\u1593')
                	d = __transliterateA('\u1593', dot, aipaitai, '\u1670', ic, s);
                else {
                	d = __transliterateA('\u1675', dot, aipaitai, '\u166f', ic, s, '\u1596', sb);
                }
               break;
            case 'C': // ra
                d = '\u154b';
                if (sbl!= 0 && sb.charAt(sbl-1)=='\u1585') { // q+r_ = r+r_ (rr_)
                    sb.setCharAt(sbl-1,'\u1550');
                }
            	d = __transliterateA('\u154b', dot, aipaitai, '\u1542', ic, s);
                break;
           case 'c': // qa
               d = '\u1583';
               if (sbl != 0 && (sb.charAt(sbl-1)=='\u1550' || sb.charAt(sbl-1)=='\u1585')) { // r+qa = q+ka (qqa)
                   sb.deleteCharAt(sbl-1);
                   sb.append('\u1585');
                   d = '\u1472';
                   }
               if (d=='\u1583')
               	d = __transliterateA('\u1583', dot, aipaitai, '\u166f', ic, s);
               else
               	d = __transliterateA('\u1472', dot, aipaitai, '\u146b', ic, s);
               break;
            case 'z': // nga
                d = '\u1593';
                if (sbl != 0) {
                    e = sb.charAt(sbl-1);
                    switch (e) {
                    case '\u14d0': //n
                    case '\u1595': //ng
                        sb.deleteCharAt(sbl-1);
                        d = '\u1675'; // nnga
                        break;
                    }
                }
                if (d=='\u1593')
                   	d = __transliterateA('\u1593', dot, aipaitai, '\u1670', ic, s);
                else
                	d = __transliterateA('\u1675', dot, aipaitai, '\u1489', ic, s, '\u1596', sb);
                break;
            
            /*
            case '\u2026': d = '\u140B'; break; // aa
            case '\u02c6': d = '\u1439'; break; // paa
            case '\u2039': d = '\u1456'; break; // taa
            case '\u00a2': d = '\u14AB'; break; // maa
            case '\u00a5': d = '\u14C8'; break; // naa
            case '\u00a8': d = '\u14F5'; break; // saa
            case '\u00ab': d = '\u14DB'; break; // laa
            case '\u00ae': d = '\u152E'; break; // jaa
            case '\u00b2': d = '\u155A'; break; // vaa
            case '\u00c7': d = '\u15A5'; break; // &aa
            */
            case '\u2026': // aa
            case '\u02c6': // paa
            case '\u2039': // taa
            case '\u00a2': // maa
            case '\u00a5': // naa
            case '\u00a8': // saa
            case '\u00ab': // laa
            case '\u00ae': // jaa
            case '\u00b2': // vaa
            case '\u00c7': // &aa
            	d=legacy2unicode.get(Character.toString(c)).charAt(0); break;

            case '\u2122': // kaa
                d = '\u1473'; 
                if (sbl > 1 && sb.charAt(sbl-1)=='\u1550' && sb.charAt(sbl-2)=='\u1550') {
                    sb.deleteCharAt(sbl-1);
                    sb.deleteCharAt(sbl-2);
                    sb.append('\u1585');
                } else if (sbl > 1 && sb.charAt(sbl-1)=='\u1550' && sb.charAt(sbl-2)=='\u1585') {
                    sb.deleteCharAt(sbl-1);
                } else if (sbl > 0 && sb.charAt(sbl-1)=='\u1550') {
                    sb.deleteCharAt(sbl-1);
                    d = '\u1584';
                }
                break;
            case '\u0153': // gaa
                d = '\u1491'; 
                if (sbl != 0) {
                    e = sb.charAt(sbl-1);
                    switch (e) {
                    case '\u1595': //ng+gaa
                        sb.deleteCharAt(sbl-1);
                        d = '\u1594'; // ngaa
                        break;
                    case '\u1596': //nng+gaa
                        sb.deleteCharAt(sbl-1);
                        d = '\u1676'; // nngaa
                        break;
                    }
                }
                break;
            case '\u00b5': // raa
                if (ic.n != 0 && s.charAt(ic.n-1)=='6') { // q+raa = r+raa (rraa)
                    sb.setCharAt(sb.length()-1,'\u1550');
                }
                d = '\u154C'; 
                break;
            case '\u00c1': // qaa
                d = '\u1584'; // qaa
                if (ic.n != 0 && (s.charAt(ic.n-1)=='3' || s.charAt(ic.n-1)=='6')) { // r+qaa = q+kaa (qqaa)
                    sb.deleteCharAt(sb.length()-1);
                    sb.append('\u1585');
                    d = '\u1473';
                }
                break; 
            case '\u00c4': // ngaa
                d = '\u1594';
                if (ic.n != 0) {
                    e = s.charAt(ic.n-1);
                    switch (e) {
                    case '8': //n
                    case '1': //ng
                        sb.deleteCharAt(sb.length()-1);
                        d = '\u1676'; // nngaa
                        break;
                    }
                }
                break; 
            
            
            case '2': d = '\u1449';  break; // p
            case '5': d = '\u1466';  break; // t
            case '4': d = '\u1483';  break; // k
            case '[': d = '\u14A1';  break; // g
            case '7': d = '\u14BB';  break; // m
            case '8': d = '\u14D0';  break; // n
            case '{': d = '\u1505';  break; // s
            case '9': d = '\u14EA';  break; // l
            case '0': d = '\u153E';  break; // j
            case '}': d = '\u155D';  break; // v
            case '3': d = '\u1550';  break; // r
            case '6': d = '\u1585';  break; // q
            case '1': // ng
                d = '\u1595';
                if (sbl != 0 && (sb.charAt(sbl-1)=='\u14d0' || sb.charAt(sbl-1)=='\u1595')) {
                    sb.deleteCharAt(sbl-1);
                    d='\u1596';
                }
                break;
            case 'P': d = '\u15A6';  break; // &
            case 'B': d = '\u157C';  break;// H
            
            
            case '!': d = '1';  break; // 1
            case '@': d = '2';  break; // 2
            case '#': d = '3';  break; // 3
            case '$': d = '4';  break; // 4
            case '%': d = '5';  break; // 5
            case '^': d = '6';  break; // 6
            case '&': d = '7';  break; // 7
            case '*': d = '8';  break; // 8
            case '(': d = '9';  break; // 9
            case ')': d = '0';  break;// 0
            case '+': d = '=';  break; // =
            case '-': d = '%';  break; // %
            case 'F': d = '/';  break; // /
            case 'G': d = '(';  break; // (
            case 'H': d = ')';  break; // )
            case 'R': d = '$';  break; // $
            case 'T': d = '+';  break; // +
            case 'U': d = '!';  break; // !
            case 'V': d = '?';  break; // ?
            case 'Y': d = '_';  break; // _
            case '\\': d = '}';  break; // }
            case '_': d = '-';  break; // -
            case '|': d = '{';  break;// {
            
            
            default: 
            	d=c;
            	break;
            } // switch   
            
            sb.append(d);
            dot = false;
            ic.n++;
        }
        return sb.toString();
    }
    
    static private char __transliterateA(char c, boolean dot, int aipaitai, char aipaichar, Counter ic, String s) {
        char d = c;  // a
        if (dot) {d++;}  // aa
        else if (aipaitai==1) {
        	if (ic.n+1 < s.length()) {
        		char e = s.charAt(ic.n+1);
        		if (e=='w') { // a..i
        			d = aipaichar; // ai
        			ic.n++;
        		}
        	}
        }
        return d;
    }

    static private char __transliterateA(char c, boolean dot, int aipaitai, char aipaichar, Counter ic, String s, char appendCharToSB, StringBuffer sb) {
        char d = c;  // a
        if (dot) {d++;}  // aa
        else if (aipaitai==1) {
        	if (ic.n+1 < s.length()) {
        		char e = s.charAt(ic.n+1);
        		if (e=='w') { // a..i
        			d = aipaichar; // ai
        			sb.append(appendCharToSB);
        			ic.n++;
        		}
        	}
        }
        return d;
    }
    
    
    static public String transcodeFromUnicode(String s) {
        int i=0;
        int l=s.length();
        char c,d;
        StringBuffer sb = new StringBuffer();
        while (i < l) {
            c = s.charAt(i);
            switch (c) {
            case '\u1403': d = 'w'; break; // i
            case '\u1431': d = 'W'; break; // pi
            case '\u144E': d = 't'; break; // ti
            case '\u146D': d = 'r'; break; // ki
            case '\u148B': d = 'Q'; break; // gi
            case '\u14A5': d = 'u'; break; // mi
            case '\u14C2': d = 'i'; break; // ni
            case '\u14EF': d = 'y'; break; // si
            case '\u14D5': d = 'o'; break; // li
            case '\u1528': d = 'p'; break; // ji
            case '\u1555': d = '='; break; // vi
            case '\u1546': d = 'E'; break; // ri
            case '\u157F': d = 'e'; break; // qi
            case '\u158F': d = 'q'; break; // Ni
            case '\u15A0': d = 'O'; break; // &i
            case '\u1404': sb.append('`'); d = 'w'; break; // ii
            case '\u1432': sb.append('`'); d = 'W'; break; // pii
            case '\u144F': sb.append('`'); d = 't'; break; // tii
            case '\u146E': sb.append('`'); d = 'r'; break; // kii
            case '\u148C': sb.append('`'); d = 'Q'; break; // gii
            case '\u14A6': sb.append('`'); d = 'u'; break; // mii
            case '\u14C3': sb.append('~'); d = 'i'; break; // nii
            case '\u14F0': sb.append('<'); d = 'y'; break; // sii
            case '\u14D6': sb.append('~'); d = 'o'; break; // lii
            case '\u1529': sb.append('>'); d = 'p'; break; // jii
            case '\u1556': sb.append('`'); d = '='; break; // vii
            case '\u1547': sb.append('~'); d = 'E'; break; // rii
            case '\u1580': sb.append('`'); d = 'e'; break; // qii
            case '\u1590': sb.append('<'); d = 'q'; break; // Nii
            case '\u15A1': sb.append('~'); d = 'O'; break; // &ii
//          case '\u1404': d = '\u0192'; break; // ii
//          case '\u1432': d = '\u2020'; break; // pii
//          case '\u144F': d = '\u2030'; break; // tii
//          case '\u146E': d = '\u0152'; break; // kii
//          case '\u148C': d = '\u0161'; break; // gii
//          case '\u14A6': d = '\u0178'; break; // mii
//          case '\u14C3': d = '\u00a3'; break; // nii
//          case '\u14F0': d = '\u00a6'; break; // sii
//          case '\u14D6': d = '\u00a9'; break; // lii
//          case '\u1529': d = '\u00ac'; break; // jii
//          case '\u1556': d = '\u00b0'; break; // vii
//          case '\u1547': d = '\u00b3'; break; // rii
//          case '\u1580': d = '\u00b6'; break; // qii
//          case '\u1590': d = '\u00c2'; break; // Nii
//          case '\u15A1': d = '\u00c5'; break; // &ii
            case '\u1405': d = 's'; break; // u
            case '\u1433': d = 'S'; break; // pu
            case '\u1450': d = 'g'; break; // tu
            case '\u146F': d = 'f'; break; // ku
            case '\u148D': d = 'A'; break; // gu
            case '\u14A7': d = 'j'; break; // mu
            case '\u14C4': d = 'k'; break; // nu
            case '\u14F1': d = 'h'; break; // su
            case '\u14D7': d = 'l'; break; // lu
            case '\u152A': d = 'J'; break; // ju
            case '\u1557': d = 'K'; break; // vu
            case '\u1548': d = 'D'; break; // ru
            case '\u1581': d = 'd'; break; // qu
            case '\u1591': d = 'a'; break; // Nu
            case '\u15A2': d = 'L'; break; // &u
            case '\u1406': sb.append('>'); d = 's'; break; // uu
            case '\u1434': sb.append('>'); d = 'S'; break; // puu
            case '\u1451': sb.append('>'); d = 'g'; break; // tuu
            case '\u1470': sb.append(']'); d = 'f'; break; // kuu
            case '\u148E': sb.append(']'); d = 'A'; break; // guu
            case '\u14A8': sb.append('<'); d = 'j'; break; // muu
            case '\u14C5': sb.append('~'); d = 'k'; break; // nuu
            case '\u14F2': sb.append('<'); d = 'h'; break; // suu
            case '\u14D8': sb.append('~'); d = 'l'; break; // luu
            case '\u152B': sb.append('<'); d = 'J'; break; // juu
            case '\u1558': sb.append('>'); d = 'K'; break; // vuu
            case '\u1549': sb.append('>'); d = 'D'; break; // ruu
            case '\u1582': sb.append('<'); d = 'd'; break; // quu
            case '\u1592': sb.append('<'); d = 'a'; break; // Nuu
            case '\u15A3': sb.append('~'); d = 'L'; break; // &uu
//          case '\u1406': d = '\u201e'; break; // uu
//          case '\u1434': d = '\u2021'; break; // puu
//          case '\u1451': d = '\u0160'; break; // tuu
//          case '\u1470': d = '\u02dc'; break; // kuu
//          case '\u148E': d = '\u203a'; break; // guu
//          case '\u14A8': d = '\u00a1'; break; // muu
//          case '\u14C5': d = '\u00a4'; break; // nuu
//          case '\u14F2': d = '\u00a7'; break; // suu
//          case '\u14D8': d = '\u00aa'; break; // luu
//          case '\u152B': d = '\u00ad'; break; // juu
//          case '\u1558': d = '\u00b1'; break; // vuu
//          case '\u1549': d = '\u00b4'; break; // ruu
//          case '\u1582': d = '\u00c0'; break; // quu
//          case '\u1592': d = '\u00c3'; break; // Nuu
//          case '\u15A3': d = '\u00c6'; break; // &uu
            case '\u140A': d = 'x'; break; // a
            case '\u1438': d = 'X'; break; // pa
            case '\u1455': d = 'b'; break; // ta
            case '\u1472': d = 'v'; break; // ka
            case '\u1490': d = 'Z'; break; // ga
            case '\u14AA': d = 'm'; break; // ma
            case '\u14C7': d = 'N'; break; // na
            case '\u14F4': d = 'n'; break; // sa
            case '\u14DA': d = 'M'; break; // la
            case '\u152D': d = '/'; break; // ja
            case '\u1559': d = '?'; break; // va
            case '\u154B': d = 'C'; break; // ra
            case '\u1583': d = 'c'; break; // qa
            case '\u1593': d = 'z'; break; // Na
            case '\u15A4': d = 'I'; break; // &a
            case '\u1401': sb.append('x'); d = 'w'; break; // ai
            case '\u142f': sb.append('X'); d = 'w'; break; // pai
            case '\u144c': sb.append('b'); d = 'w'; break; // tai
            case '\u146b': sb.append('v'); d = 'w'; break; // kai
            case '\u1489': sb.append('Z'); d = 'w'; break; // gai
            case '\u14a3': sb.append('m'); d = 'w'; break; // mai
            case '\u14c0': sb.append('N'); d = 'w'; break; // nai
            case '\u14ed': sb.append('n'); d = 'w'; break; // sai
            case '\u14d3': sb.append('M'); d = 'w'; break; // lai
            case '\u1526': sb.append('/'); d = 'w'; break; // jai
            case '\u1553': sb.append('?'); d = 'w'; break; // vai
            case '\u1543': sb.append('C'); d = 'w'; break; // rai
            case '\u166f': sb.append('c'); d = 'w'; break; // qai
            case '\u1670': sb.append('z'); d = 'w'; break;  // ngai  
            case '\u140B': sb.append('<'); d = 'x'; break; // aa
            case '\u1439': sb.append('<'); d = 'X'; break; // paa
            case '\u1456': sb.append(']'); d = 'b'; break; // taa
            case '\u1473': sb.append('>'); d = 'v'; break; // kaa
            case '\u1491': sb.append('>'); d = 'Z'; break; // gaa
            case '\u14AB': sb.append('>'); d = 'm'; break; // maa
            case '\u14C8': sb.append('~'); d = 'N'; break; // naa
            case '\u14F5': sb.append('>'); d = 'n'; break; // saa
            case '\u14DB': sb.append('~'); d = 'M'; break; // laa
            case '\u152E': sb.append('>'); d = '/'; break; // jaa
            case '\u155A': sb.append('<'); d = '?'; break; // vaa
            case '\u154C': sb.append('>'); d = 'C'; break; // raa
            case '\u1584': sb.append('`'); d = 'c'; break; // qaa
            case '\u1594': sb.append('<'); d = 'z'; break; // Naa
            case '\u15A5': sb.append('~'); d = 'I'; break; // &aa
//          case '\u140B': d = '\u2026'; break; // aa
//          case '\u1439': d = '\u02c6'; break; // paa
//          case '\u1456': d = '\u2039'; break; // taa
//          case '\u1473': d = '\u2122'; break; // kaa
//          case '\u1491': d = '\u0153'; break; // gaa
//          case '\u14AB': d = '\u00a2'; break; // maa
//          case '\u14C8': d = '\u00a5'; break; // naa
//          case '\u14F5': d = '\u00a8'; break; // saa
//          case '\u14DB': d = '\u00ab'; break; // laa
//          case '\u152E': d = '\u00ae'; break; // jaa
//          case '\u155A': d = '\u00b2'; break; // vaa
//          case '\u154C': d = '\u00b5'; break; // raa
//          case '\u1584': d = '\u00c1'; break; // qaa
//          case '\u1594': d = '\u00c4'; break; // Naa
//          case '\u15A5': d = '\u00c7'; break; // &aa
            case '\u1449': d = '2'; break; // p
            case '\u1466': d = '5'; break; // t
            case '\u1483': d = '4'; break; // k
            case '\u14A1': d = '['; break; // g
            case '\u14BB': d = '7'; break; // m
            case '\u14D0': d = '8'; break; // n
            case '\u1505': d = '{'; break; // s
            case '\u14EA': d = '9'; break; // l
            case '\u153E': d = '0'; break; // j
            case '\u155D': d = '}'; break; // v
            case '\u1550': d = '3'; break; // r
            case '\u1585': d = '6'; break; // q
            case '\u1595': d = '1'; break; // N
            case '\u15A6': d = 'P'; break; // &
            case '\u157C': d = 'B'; break;// H
            case '1': d = '!'; break; // 1
            case '2': d = '@'; break; // 2
            case '3': d = '#'; break; // 3
            case '4': d = '$'; break; // 4
            case '5': d = '%'; break; // 5
            case '6': d = '^'; break; // 6
            case '7': d = '&'; break; // 7
            case '8': d = '*'; break; // 8
            case '9': d = '('; break; // 9
            case '0': d = ')'; break;// 0
            case '=': d = '+'; break; // =
            case '%': d = '-'; break; // %
            case '/': d = 'F'; break; // /
            case '(': d = 'G'; break; // (
            case ')': d = 'H'; break; // )
            case '$': d = 'R'; break; // $
            case '+': d = 'T'; break; // +
            case '!': d = 'U'; break; // !
            case '?': d = 'V'; break; // ?
            case '_': d = 'Y'; break; // _
            case '}': d = '\\'; break; // }
            case '-': d = '_'; break; // -
            case '{': d = '|'; break;// {
            default: d = c; break;
            }
            i++;
            sb.append(d);
        }
        return sb.toString();
    }

}
