/*
 * Conseil national de recherche Canada 2006/
 * National Research Council Canada 2006
 * 
 * Cr�� le / Created on Jun 26, 2006
 * par / by Benoit Farley
 * 
 */

/*
 * Parsing Microsoft Word documents.
 * Two tools can be used: Jakarta POI and the TextMining.org text extractors.
 * API.
 */
package org.iutools.documents;

import java.io.InputStream;
import java.net.URLConnection;
import java.util.Date;
import java.util.Hashtable;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;



public class NRC_DOCXDocument implements NRC_Document {

    public XWPFDocument document = null;

    private String urlName = null;
    private String contentType = null;
    private Date date = null;
    private String title = null;
    private String preferredFont = null;
    private Hashtable fonts = null;
    private String pageContent = null;
    private WebPageReader wpr = null;
    private boolean containsInuktitut = false;
    private long lengthOfInuktitutContent = 0;
    private long lengthOfTotalContent = 0;

    public NRC_DOCXDocument (String urlName) throws NRC_DOCXDocumentException {
    	try {
        this.urlName = urlName;
        wpr = new WebPageReader(urlName);
        contentType = wpr.contentType;
        URLConnection conn = wpr.connection;
        date = new Date(conn.getLastModified());
        InputStream fis = conn.getInputStream();
	    document = new XWPFDocument(OPCPackage.open(fis));
        title = "";
    	} catch(Exception e) {
    		throw new NRC_DOCXDocumentException(e);
    	}
    }
    
    /*
     * Retourne le contenu d'un fichier DOC. Le texte inuktitut est converti �
     * Unicode. Si une exception survient, la cha�ne vide est retourn�e.
     */
    public String getContents() {
		try {
			XWPFWordExtractor extractor = new XWPFWordExtractor(document);
			return extractor.getText();
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public String getPageContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrlName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getInuktitutPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object[][] getInuktitutFontsAndPercentages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] highlight(String[] x) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] transliterate() throws OutOfMemoryError, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebPageReader getWpr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getInuktitutFonts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getAllFontsNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPreferredFont() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object[] getAllFonts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsInuktitut() {
		// TODO Auto-generated method stub
		return false;
	}
	

	public static void main(String[] args) throws Exception {
		
		String filepathname = args[0];
		NRC_DOCXDocument document = new NRC_DOCXDocument("file://"+filepathname);
		String text = document.getContents();
		System.out.println(text);
	}


}
