package ca.inuktitutcomputing.applications;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import ca.inuktitutcomputing.fonts.Font;
import ca.inuktitutcomputing.script.Syllabics;
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
			doc = replaceText(doc);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static HWPFDocument replaceText(HWPFDocument doc) {
		Range r1 = doc.getRange();

		for (int i = 0; i < r1.numSections(); ++i) {
			Section s = r1.getSection(i);
			for (int x = 0; x < s.numParagraphs(); x++) {
				Paragraph p = s.getParagraph(x);
				String unicodeText = processParagraph(p).trim();
				if (unicodeText.length()!=0)
					System.out.println(unicodeText);
			}
		}
		return doc;
	}

	private static String processParagraph(Paragraph p) {
        int nbRuns = p.numCharacterRuns();
        String font = "";
        String previousFont = "";
        String text = null;
        String totalText = "";
        for (int i=0; i<nbRuns; i++) {
            CharacterRun cr = p.getCharacterRun(i);
            previousFont = font;
            font = cr.getFontName();
            String piece = null;
            try {
                piece = cr.text();
            } catch (Exception e) {
                // Sometimes, an ArrayOutOfBoundsException arises.
                piece = "";
            }
            if (font.equals(previousFont))
                /* The font for this piece of text is the same as for the
                 * preceeding piece of text. Just append this text 'text'.
                 */
                text += piece;
            else if (text==null)
                // Very first piece of text. Just initialize 'text' to it.
                text = piece;
            else {
                /*
                 * The font for this piece of text is different than for the
                 * preceeding piece of text. If that previous font is a known
                 * legacy or unicode inuktitut font, convert the text in unicode
                 * and add it to the total text. Otherwise, just add the text as
                 * is.
                 */
                if (text.replaceAll("\\s", "").equals("")) {
                   	;
                } else if (Font.isLegacy(previousFont)) {
                	String iutext = TransCoder.legacyToUnicode(text,previousFont);
                    totalText += " "+iutext;
                }
                else if (Font.isUnicodeFont(previousFont)) {
                    totalText += " "+text;
                }
                else if (Syllabics.containsInuktitut(text)) {
                    totalText += " "+text;
                }
                else {
                    totalText += " "+text;
                }
                // Re-init 'text' to the contents of the current piece of text
                text = piece;
            }
        }
        return totalText;
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