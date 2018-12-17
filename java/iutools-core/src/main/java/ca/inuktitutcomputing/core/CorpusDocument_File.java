package ca.inuktitutcomputing.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import ca.inuktitutcomputing.documents.*;

public class CorpusDocument_File extends CorpusDocument {
	
	public String id;
	
	public CorpusDocument_File(String filePathname) {
		this.id = filePathname;
	}
		
	@Override
	public String getContents() throws Exception {
		String fileType = getType();
		if (fileType.equals("pdf"))
			return getPDFContent();
		else if (fileType.equals("doc"))
			return getDOCContent();
		else if (fileType.equals("txt"))
			return getTxtContent();
		else
			return "";
	}

	@Override
	public String getId() {
		return id;
	}
	
	public boolean hasContents() {
		String fileType = getType();
		if (fileType.equals("pdf") || fileType.equals("doc") || fileType.equals("txt"))
			return true;
		return false;
	}
	
	private String getTxtContent() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(id));
		String contents = "";
		String line;
		while ( (line=br.readLine()) != null) {
			contents += " "+line;
		}
		br.close();
		return contents;
	}

	private String getDOCContent() throws IOException {
		NRC_DOCDocument doc = new NRC_DOCDocument("file://"+id);
		return doc.getPageContent();
	}

	private String getPDFContent() throws Exception {
		NRC_PDFDocument doc = new NRC_PDFDocument("file://"+id);
		return doc.getContents();
	}

	private String getType() {
		String lcid = this.id.toLowerCase();
		if (lcid.endsWith(".pdf"))
			return "pdf";
		else if (lcid.endsWith(".doc"))
			return "doc";
		else if (lcid.endsWith(".txt"))
			return "txt";
		else
			return null;
	}


}
