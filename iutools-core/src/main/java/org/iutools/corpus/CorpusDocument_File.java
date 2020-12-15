package org.iutools.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FilenameUtils;

import org.iutools.documents.NRC_DOCDocument;
import org.iutools.documents.NRC_DOCXDocument;
import org.iutools.documents.NRC_DOCXDocumentException;
import org.iutools.documents.NRC_PDFDocument;

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
		else if (fileType.equals("docx"))
			return getDOCXContent();
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
		if (fileType.equals("pdf") || fileType.equals("doc") || fileType.equals("docx") || fileType.equals("txt"))
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
		String contents = doc.getPageContent();
		doc.close();
		return contents;
	}

	private String getDOCXContent() throws NRC_DOCXDocumentException {
		NRC_DOCXDocument doc = new NRC_DOCXDocument("file://"+id);
		String contents = doc.getContents();
		doc.close();
		return contents;
	}

	private String getPDFContent() throws IOException {
		NRC_PDFDocument doc;
		doc = new NRC_PDFDocument("file://"+id);
		String contents = doc.getContents();
		doc.close();
		return contents;
	}

	private String getType() {
		String ext = FilenameUtils.getExtension(this.id).toLowerCase();
		return ext;
	}

	@Override
	public BufferedReader contentsReader() throws CorpusDocumentException {
		BufferedReader reader = null;
		try {
			String fileType = getType();
			if (fileType.equals("txt")) {
				reader = new BufferedReader(new FileReader(id));
			} else {
				String content = null;
				if (fileType.equals("pdf")) {
					content = getPDFContent();
				} else if (fileType.equals("doc")) {
					content = getDOCContent();
				} else if (fileType.equals("docx")) {
					content = getDOCXContent();
				} else if (fileType.equals("txt")) {
					content = getTxtContent();
				} else {
					throw new IOException("Unsupported content type "+fileType+
						" for file "+id);
				}
				File tempFile = File.createTempFile("doc", "txt");
				tempFile.deleteOnExit();
				Files.write(tempFile.toPath(), content.getBytes());
				reader = new BufferedReader(new FileReader(id));
			}
		} catch (Exception e) {
			throw new CorpusDocumentException(e);
		}
		
		return reader;
	}
}
