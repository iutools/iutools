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

package org.iutools.bin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;

import org.iutools.documents.NRC_PDFDocument;


public class TranslitPDFFile {

    public static void main(String[] args) {
        translittererPage(args[0]);
    }

    public static void translittererPage(String fileName) {

        //-------------------------------------------------------
        // Lecture du fichier PDF 
        //-------------------------------------------------------

        //		Debogage.init();

        try {
        	
        	processFile(fileName);

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

    private static void processFile(String fileName) throws MalformedURLException, IOException {
    	NRC_PDFDocument pdfDoc = new NRC_PDFDocument("file:///"+fileName);
    	String[] fontNames = pdfDoc.getAllFontsNames();
    	System.out.println("Fonts: "+Arrays.toString(fontNames));
    	//String contents = pdfDoc.getInuktitutContent();
    	String contents = pdfDoc.getPageContent();
    	System.out.println("Inuktitut contents: "+contents);
	}
    
    


}