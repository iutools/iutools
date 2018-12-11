// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//
// -----------------------------------------------------------------------
//           (c) Conseil national de recherches Canada, 2002
//           (c) National Research Council of Canada, 2002
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// Document/File: TranslitInuk.java
//
// Type/File type: code Java / Java code
// 
// Auteur/Author: Benoit Farley
//
// Organisation/Organization: Conseil national de recherches du Canada/
//				National Research Council Canada
//
// Date de cr�ation/Date of creation: 9 avril 2002 / April 9, 2002
//
// Description: Entr�e d'un programme qui fait la translit�ration d'une page
//              HTML du syllabaire inuktitut en caract�res latins.
//
// -----------------------------------------------------------------------

package ca.inuktitutcomputing.applications;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import ca.inuktitutcomputing.script.TransCoder;


public class TranslitTextFile {

    public static void main(String[] args) {
        translittererPage(args[0]);
    }

    public static void translittererPage(String fileName) {

        //-------------------------------------------------------
        // Lecture du fichier HTML et cr�ation d'un document HTML
        // Traitement du document HTML (traitement du texte en inuktitut)
        // Ecriture du document HTML dans un nouveau fichier HTML
        //-------------------------------------------------------

        //		Debogage.init();

        try {
        	BufferedReader bf =
        		new BufferedReader(new FileReader(fileName));
        	Writer pw = new OutputStreamWriter(
        	        new FileOutputStream(fileName+".out.txt"), "UTF-8");
        	
        	processFile(bf,pw);
        	pw.close();

            System.exit(0);

        } catch (Exception e) {
        	e.printStackTrace();
            System.exit(1);
        }
    }


    //-----------------------------------------------------------
    // Traitement du document HTML
    // - Recherche du texte en inuktitut et traitement de ce texte
    //------------------------------------------------------------

    private static void processFile(BufferedReader bf, Writer pw) {
		String text;
		int nLines = 30;
		try {
			while (nLines-- != 0 && (text = bf.readLine()) != null) {
				System.out.println("> "+text);
				String textUni = TransCoder.legacyToUnicode(text,"Naamajut");
				System.out.println("> "+textUni);
				//pw.write(textUni);
				//pw.write("\n");
			}
			//pw.flush();
		} catch (Exception e) {

		}

	}
    
    


}