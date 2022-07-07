package org.iutools.linguisticdata.dataCSV;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.csv.CSVReader;
import ca.nrc.file.ResourceGetter;

public final class LinguisticDataCSV { //extends LinguisticDataAbstract {
	
	static public int OK = 0;
	static public int FILE_NOT_FOUND = 1;
	static public int IOEXCEPTION = 2;
	
	public static String[][] dataTables = {
			// element 0: class of the objects created from data in data file
			// element 1: name of the database
			// element 2: name of the CSV data file (without the extension)
			// element 3: if present, 'type' to be used when creating the objects
			//            only for non-linguistic data
		{"Base","Inuktitut","RootsSpalding"},
		{"Base","Inuktitut","RootsSchneider"},
		{"Base","Inuktitut","WordsRelatedToRoots"},
		{"Base","Inuktitut","UndecomposableCompositeWords"},
		{"Base","Inuktitut","CommonCompositeWords"},
		{"Base","Inuktitut","Locations"},
		{"Base","Inuktitut","LoanWords"},
		{"Suffix","Inuktitut","Suffixes"},
		{"NounEnding","Inuktitut","Endings_noun"},
		{"VerbEnding","Inuktitut","Endings_verb"},
		{"VerbEnding","Inuktitut","Endings_verb_participle"},
		{"Demonstrative","Inuktitut","Demonstratives"},
		{"DemonstrativeEnding","Inuktitut","Endings_demonstrative"},
		{"Pronoun","Inuktitut","Pronouns"},
			// The following files do not describe linguistic objects. They do not contain a field 'type'; it is provided here as the fourth element
			// so that the objects can be created.
			// vw: verb-word objects; src: source objects
		{"VerbWord","Inuktitut","Passives_French","vw"},
		{"VerbWord","Inuktitut","Passives_English","vw"},
		{"Source","Inuktitut","Sources","src"},
			// The next files contain additional morphemes not in the original database
		{"Suffix","Inuktitut","Suffixes_additional"},

	};
	static Class thisClass;

	public LinguisticDataCSV() {
	}

	/*
	 * Read all the linguistic data files into linguistic objects and register them in
	 * the LinguisticData singleton passed in the argument linguisticDataRegister.
	 */
	public void readAndRegisterLinguisticDataCSV(LinguisticData linguisticDataRegister) throws LinguisticDataException {
		for (int i=0; i < dataTables.length; i++) {
				_readLinguisticDataCSV(dataTables[i],linguisticDataRegister);
		}
	}

	protected void _readLinguisticDataCSV(String [] data,LinguisticData linguisticDataRegister)
		throws LinguisticDataException {
		Logger logger = LogManager.getLogger("LinguisticDataCSV.readLinguisticDataCSV");
		String classOfObject = data[0];
		String dbName = data[1];
		String tableName = data[2];
		String typeOfObject = data.length==4 ? data[3] : null;
		logger.trace("Reading CSV data with type="+classOfObject+", dbName="+dbName+", tableName="+tableName);
		String fileName = tableName+".csv";
		logger.trace("fileName="+fileName);
		String tablePath = "org/iutools/linguisticdata/dataCSV/"+fileName;
		try {
			InputStream is = ResourceGetter.getResourceAsStream(tablePath);
			readLinguisticDataCSV(is,dbName,tableName,typeOfObject,linguisticDataRegister);
		} catch (IOException e) {
			throw new LinguisticDataException(
				"Could not read linguistic data file "+tablePath);
		}

    	logger.trace("Done reading the CSV file");
	}

	public void readLinguisticDataCSV(InputStream is,String dbName,String tableName,String typeOfObject,LinguisticData linguisticDataRegister) throws IOException, LinguisticDataException {
		BufferedReader f =  new BufferedReader(new InputStreamReader(is));
		CSVReader csvReader = new CSVReader(f);
		boolean endOfFile = false;
		while ( !endOfFile ) {
			Map<String,String> linguisticDataAttributeValuePairs = csvReader.readNext();
			if (linguisticDataAttributeValuePairs==null)
				endOfFile = true;
			else {
				linguisticDataAttributeValuePairs.put("dbName", dbName);
				linguisticDataAttributeValuePairs.put("tableName", tableName);
				if (typeOfObject!=null) {
					// non-linguistic objects must be added a field "type"
					linguisticDataAttributeValuePairs.put("type",typeOfObject);
				}
				linguisticDataRegister.makeAndRegisterLinguisticObject((HashMap) linguisticDataAttributeValuePairs);
			}
		}
	}
}
