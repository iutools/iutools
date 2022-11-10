package org.iutools.concordancer.tm;

import ca.nrc.config.ConfigException;
import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import ca.nrc.json.PrettyPrinter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iutools.concordancer.Alignment;
import org.iutools.config.IUConfig;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DELETEME_AddDateToAlignments {

	private static PrettyPrinter prettyPrinter = new PrettyPrinter();
	private static ObjectMapper mapper = new ObjectMapper();
	static Pattern pattFromDoc = Pattern.compile("^Hansard_(\\d\\d\\d\\d)(\\d\\d)(\\d\\d)$");
	static String[] ignoreFields = new String[] {
		"longDescription", "additionalFields", "_detect_language",
		"shortDescription", "creationDate", "content", "idWithoutType", "id",
		"lang",
	};

	public static void main(String[] args) throws ConfigException, IOException, ObjectStreamReaderException, ClassNotFoundException {
		String[] tmFiles = new String[]{
			IUConfig.getIUDataPath("data/translation-memories/nrc-nunavut-hansard.tm.json"),
			IUConfig.getIUDataPath("data/translation-memories/gov-nu-ca.tm.json"),
		};

		for (String tmFile: tmFiles) {
			System.out.println("Adding date to alignments in file: "+tmFile);
			try (PrintWriter writer = openConvertedFileWriter(tmFile)) {
				ObjectStreamReader reader = new ObjectStreamReader(new File(tmFile));
				Alignment algnmt = null;
				int alignNum = 0;
				//			String currAlignDescr = null;
				//			StopWatch sw = new StopWatch().start();
				while ((algnmt = (Alignment) reader.readObject()) != null) {
					alignNum++;
					if (alignNum % 1000 == 0) {
						System.out.println("  Now at alignment #"+alignNum);
					}
					JSONObject jObj = toJsonObject(algnmt);
					addDate(jObj);
					writeAlignment(jObj, writer);
				}
			}
		}
	}

	private static PrintWriter openConvertedFileWriter(String tmFilePath) throws IOException {
		File tmFile = new File(tmFilePath+".NEW");
		tmFile.createNewFile();
		return new PrintWriter(tmFile);
	}

	private static void writeAlignment(JSONObject jObj, PrintWriter writer) throws JsonProcessingException {
		String jsonString = jObj.toString();
		Map<String,Object> asMap = mapper.readValue(jsonString, Map.class);
		jsonString = prettyPrinter.pprint(asMap, ignoreFields);
		writer.println(jsonString);
		return;
	}

	private static void addDate(JSONObject jObj) {
		String fromDoc = jObj.getString("from_doc");
		Matcher matcher = pattFromDoc.matcher(fromDoc);
		if (matcher.matches()) {
			String date = matcher.group(1)+"-"+matcher.group(2)+
				"-"+matcher.group(3);
			jObj.put("publishedOn", date);
		}
		return;
	}

	private static JSONObject toJsonObject(Alignment algnmt) throws JsonProcessingException {
		String jsonStr = mapper.writeValueAsString(algnmt);
		return new JSONObject(jsonStr);
	}
}
