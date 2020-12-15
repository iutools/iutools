package org.iutools.bin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import org.iutools.fonts.Font;
import ca.inuktitutcomputing.script.TransCoder;

public class TranslitDOCFileParagraphs {

	public static void main(String[] args) {
		translit(args[0]);
	}

	public static void translit(String filePath) {
		POIFSFileSystem fs = null;
		try {
			fs = new POIFSFileSystem(new FileInputStream(filePath));
			HWPFDocument doc = new HWPFDocument(fs);
			replaceText(doc);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void replaceText(HWPFDocument doc) {
		Logger logger = Logger.getLogger("TranslitDOCFileParagraphs.replaceText");
		Range r1 = doc.getRange();
		
		Pattern toc = Pattern.compile("^(.*)TOC (?:\\\\. )*\".+?\"(.*)$");
		Pattern hyperlink = Pattern.compile("^(.*)HYPERLINK \\\\l \".+?\"(.*)$");
		Pattern pageref = Pattern.compile("^(.*)PAGEREF _Toc\\d+ \\\\h(.*)$");
		String regexp = "^(.*?)PAGE(.+?)[0-9ivxmcld]+(.*)$";
		Pattern page = Pattern.compile(regexp,Pattern.CASE_INSENSITIVE);

		String font = "";
		String previousFont = "";

		for (int i = 0; i < r1.numSections(); ++i) {
			Section s = r1.getSection(i);
			int np = s.numParagraphs();
			logger.debug("section "+i+": "+np+" paragraphs");
			for (int x = 0; x < s.numParagraphs(); x++) {
				logger.debug("s"+i+"  p"+x+" de "+s.numParagraphs());
				Paragraph p = s.getParagraph(x);
				int nbRuns = p.numCharacterRuns();
				String textOfRunsInSameFont = null;
				String totalParagraphTextTransliterated = "";
				logger.debug("\nNEW PARAGRAPH");
				for (int irun = 0; irun < nbRuns; irun++) {
					CharacterRun cr = p.getCharacterRun(irun);
					previousFont = font;
					font = cr.getFontName();
					logger.debug("*** font: " + font);
					if (font.toLowerCase().equals("prosyl"))
						font = "tunngavik";
					String piece = null;
					try {
						piece = cr.text();
						logger.debug("piece: '"+piece+"'"+"["+piece.length()+"]");
						String codes = "";
						for (int ic=0; ic<piece.length(); ic++)
							codes += piece.codePointAt(ic)+" ";
						logger.debug(codes);
						Matcher mtoc = toc.matcher(piece);
						Matcher mhyp = hyperlink.matcher(piece);
						Matcher mpref = pageref.matcher(piece);
						if (mtoc.find())
							piece = String.join(" ", new String[] {mtoc.group(1),mtoc.group(2)});
						else if (mhyp.find())
							piece = String.join(" ", new String[] {mhyp.group(1),mhyp.group(2)});
						else if (mpref.find())
							piece = String.join(" ", new String[] {mpref.group(1),mpref.group(2)});
							
						
						piece = piece.replace("\n", " ").replace("\r", " ");
						logger.debug("piece (without cr): '" + piece + "'");
					} catch (Exception e) {
						// Sometimes, an ArrayOutOfBoundsException arises.
						piece = "";
					}

					if (irun == 0) { // textOfRunsInSameFont==null) {
						// Very first piece of text in the paragraph. Just initialize
						// 'textOfRunsInSameFont' to it.
						textOfRunsInSameFont = piece;
//						logger.debug("first piece of text: '" + textOfRunsInSameFont + "'");
						continue;
					}

					if (font.equals(previousFont)) {
						/*
						 * The font for this piece of text is the same as for the preceeding piece of
						 * text. Just append.
						 */
						textOfRunsInSameFont += piece;
//						logger.debug("same font as previous character run - text: '" + textOfRunsInSameFont + "'");
					} else {
						/*
						 * The font for this piece of text is different than for the preceeding piece of
						 * text. If that previous font is a known legacy font, convert the text in
						 * unicode and add it to the total text. Otherwise, just add the text as is.
						 */
						logger.debug("change of font");
						logger.debug("textOfRunsInSameFont: '"+textOfRunsInSameFont+"'");
						Pattern p07 = Pattern.compile("\\x07$");
						Matcher m07 = p07.matcher(textOfRunsInSameFont);
						if (m07.find())
							textOfRunsInSameFont = "";
						Pattern p192021 = Pattern.compile("^(.*?)\\x13\\x20*PAGE\\x20+\\x14[0-9ivxlcdm]+\\x15(.*)$");
						Matcher m192021 = p192021.matcher(textOfRunsInSameFont);
						while (m192021.matches()) {
							textOfRunsInSameFont = String.join(" ", new String[] {m192021.group(1),m192021.group(2)});
							m192021 = p192021.matcher(textOfRunsInSameFont);
						}
//						Matcher mpage = page.matcher(textOfRunsInSameFont);
//						while (mpage.matches()) {
//							textOfRunsInSameFont = String.join(" ", new String[] {mpage.group(1),mpage.group(2)});
//							mpage = page.matcher(textOfRunsInSameFont);
//						}
						textOfRunsInSameFont = textOfRunsInSameFont.replaceAll("\\s+", " ");
						logger.debug("textOfRunsInSameFont: '"+textOfRunsInSameFont+"'");
						if (textOfRunsInSameFont.replaceAll("\\s", "").equals("")) {
							logger.debug("change of font / text is just white space; add a space");
							totalParagraphTextTransliterated += " ";
						} else if (Font.isLegacy(previousFont)) {
							logger.debug("change of font from Legacy to X / TRANSLIT from " + previousFont + " to Unicode");
							String iutext = TransCoder.legacyToUnicode(textOfRunsInSameFont, previousFont);
							logger.debug("    --- transliterated to " + previousFont + " - iutext: '" + iutext + "'");
							totalParagraphTextTransliterated += " " + iutext;
						} else { // if (Font.isUnicodeFont(previousFont)) {
							logger.debug("change of font from Unicode to X / no need for transliteration");
							totalParagraphTextTransliterated += " " + textOfRunsInSameFont;
						}
						// Re-init 'textOfRunsInSameFont' to the contents of the current piece of text
						textOfRunsInSameFont = piece;
					}
				}
				logger.debug("END OF PARAGRAPH");
				logger.debug("textOfRunsInSameFont: "+textOfRunsInSameFont);
				if (!textOfRunsInSameFont.equals("")) {
					textOfRunsInSameFont = textOfRunsInSameFont.replaceAll("\\s+", " ");
//					logger.debug("textOfRunsInSameFont: '"+textOfRunsInSameFont+"'"+" ==? "+regexp);
					String codes = "";
					for (int ic=0; ic<textOfRunsInSameFont.length(); ic++)
						codes += textOfRunsInSameFont.codePointAt(ic)+" ";
					logger.debug(codes);
					
					Pattern p07 = Pattern.compile("\\x07$");
					Matcher m07 = p07.matcher(textOfRunsInSameFont);
					if (m07.find())
						textOfRunsInSameFont = "";
					Pattern p192021 = Pattern.compile("^(.*?)\\x13\\x20*PAGE\\x20+\\x14[0-9ivxlcdm]+\\x15(.*)$");
					Matcher m192021 = p192021.matcher(textOfRunsInSameFont);
					while (m192021.matches()) {
						textOfRunsInSameFont = String.join(" ", new String[] {m192021.group(1),m192021.group(2)});
						m192021 = p192021.matcher(textOfRunsInSameFont);
					}
					logger.debug("textOfRunsInSameFont: "+textOfRunsInSameFont);
//					Matcher mpage = page.matcher(textOfRunsInSameFont);
//					while (mpage.matches()) {
//						textOfRunsInSameFont = String.join(" ", new String[] {mpage.group(1),mpage.group(3)});
//						mpage = page.matcher(textOfRunsInSameFont);
//						logger.debug("textOfRunsInSameFont *: '"+textOfRunsInSameFont+"'");
//						logger.debug("spaces: "+mpage.group(2).codePointAt(0)+" "+mpage.group(2).codePointAt(1));
//					}
					if (Font.isLegacy(font)) {
						logger.debug("end of paragraph / TRANSLIT '"+textOfRunsInSameFont+"' from " + font + " to Unicode");
						String iutext = TransCoder.legacyToUnicode(textOfRunsInSameFont, font);
						logger.debug("    --- transliterated to " + previousFont + " - iutext: '" + iutext + "'");
						totalParagraphTextTransliterated += " " + iutext;
					} else { // if (Font.isUnicodeFont(previousFont)) {
						logger.debug("end of paragraph / no need for transliteration");
						totalParagraphTextTransliterated += " " + textOfRunsInSameFont;
					}
				}
				totalParagraphTextTransliterated = totalParagraphTextTransliterated.trim();
				if (totalParagraphTextTransliterated.length() != 0) {
					System.out.println(totalParagraphTextTransliterated);
					logger.debug("'"+totalParagraphTextTransliterated+"'");
					String codes = "";
					for (int ic=0; ic<totalParagraphTextTransliterated.length(); ic++)
						codes += totalParagraphTextTransliterated.codePointAt(ic)+" ";
					logger.debug(codes);
				}
			}
		}
	}

	private static void saveWord(String filePath, HWPFDocument doc) throws FileNotFoundException, IOException {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
			doc.write(out);
		} finally {
			out.close();
		}
	}
}