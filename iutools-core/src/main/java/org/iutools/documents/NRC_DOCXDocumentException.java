package org.iutools.documents;

public class NRC_DOCXDocumentException extends Exception {
	
	public NRC_DOCXDocumentException(Exception e) { super(e); }

	public NRC_DOCXDocumentException(String mess) { super (mess); }

	public NRC_DOCXDocumentException(String mess, Exception e) { super(mess, e); }

}
