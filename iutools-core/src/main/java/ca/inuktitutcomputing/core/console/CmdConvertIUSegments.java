package ca.inuktitutcomputing.core.console;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmdConvertIUSegments extends ConsoleCommand {

	public CmdConvertIUSegments(String name) {
		super(name);
	} 

	@Override
	public String getUsageOverview() {
		return "Convert an Inuktitut Morphological Analyzer analysis into a sequence of morphemes compatible with the Trie structure.";
	}

	@Override
	public void execute() throws Exception {
		String imaAnalysis = getWord(false); // ima: Inuktitut Morphological Analyzer
		String converted = null;
		
		boolean interactive = false;
		if (imaAnalysis == null) {
			interactive = true;
		} else {
			converted = convert(imaAnalysis);
		}

		while (true) {
			if (interactive) {
				imaAnalysis = prompt("Enter IMA analysis");
				if (imaAnalysis == null) 
					break;
				converted = convert(imaAnalysis);
			}
			echo(converted);
			
			if (!interactive) break;				
		}

	}

	protected String convert(String imaAnalysis) {
		Pattern p = Pattern.compile("\\{.+?\\:(.+?)\\}");
		Matcher m = p.matcher(imaAnalysis);
		Vector<String> parts = new Vector<String>();
        while (m.find()) {
            String part = "{" + m.group(1) + "}";
            parts.add(part);
        }
        String res = String.join(" ", parts.toArray(new String[] {}));
        return res;
    }

}
