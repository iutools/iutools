package ca.inuktitutcomputing.dataCSV; 

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.data.Affix;
import ca.inuktitutcomputing.data.Base;
import ca.inuktitutcomputing.data.Data;
import ca.inuktitutcomputing.data.LinguisticDataAbstract;
import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.data.Morpheme;
import ca.inuktitutcomputing.data.Source;
import ca.inuktitutcomputing.data.SurfaceFormOfAffix;
import ca.inuktitutcomputing.data.VerbWord;
import ca.nrc.json.PrettyPrinter;

public final class LinguisticDatabaseCSV {
	
	static public int OK = 0;
	static public int FILE_NOT_FOUND = 1;
	static public int IOEXCEPTION = 2;
	
	public static String[][] dataTables = {
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
		{"VerbWord","Inuktitut","Passives_French"},
		{"VerbWord","Inuktitut","Passives_English"},
		{"Source","Inuktitut","Sources"}
	};
	static Class thisClass;
	
    protected Hashtable<String,Vector<Morpheme>> bases;
    protected Hashtable<String,Base> basesId;
    public Hashtable<String,Vector<SurfaceFormOfAffix>> surfaceFormsOfAffixes;
    protected Hashtable<String,Affix> affixesId;
    protected Hashtable<String,VerbWord> words;
    protected Hashtable<String,Source> sources;


	public LinguisticDatabaseCSV() throws LinguisticDataException {
		bases = new Hashtable<String,Vector<Morpheme>>();
		basesId = new Hashtable<String,Base>();
		words = new Hashtable<String,VerbWord>();
		surfaceFormsOfAffixes = new Hashtable<String,Vector<SurfaceFormOfAffix>>();
		affixesId = new Hashtable<String,Affix>();
		sources = new Hashtable<String,Source>();
		for (int i = 0; i < dataTables.length; i++) {
			readLinguisticDataCSV(dataTables[i]);
		}
	}
	
	public Hashtable<String,Vector<Morpheme>> getBases() {
		return bases;
	}
	
    public String[] getAllAffixesSurfaceStrings() {
    	return (String[]) surfaceFormsOfAffixes.keySet().toArray(new String[] {});
    }
    

	
    public static int readLinguisticDataCSV(String [] data) throws LinguisticDataException {
//    	System.out.println("--- Start reading linguistic data for data= "+String.join("; ", data));
    	Logger logger = Logger.getLogger("LinguisticDatabaseCSV.readLinguisticDataCSV");
    	String type = data[0];
    	String dbName = data[1];
    	String tableName = data[2];    	
    	BufferedReader f;
        try {
        	String fileName = tableName+".csv";
            InputStream is = LinguisticDatabaseCSV.class.getResourceAsStream(fileName);
            f =  new BufferedReader(new InputStreamReader(is));
        	String line;
            
            String firstLine = f.readLine();
            String [] fieldNames = firstLine.split(",");
        	while ( (line=f.readLine()) != null) {
                    HashMap<String,String> nextRow = getNextRow(line,fieldNames);
                    // ajouter le nom de la base de donn�es et le nom de la table
                    nextRow.put("dbName", dbName);
                    nextRow.put("tableName", tableName);
                    if (type.equals("Base")) {
                    	logger.debug("nextRow: "+PrettyPrinter.print(nextRow));
                        Data.makeBase(nextRow);
                    } else if (type.equals("Suffix")) {
                        Data.makeSuffix(nextRow);
                    } else if (type.equals("NounEnding")) {
                        Data.makeNounEnding(nextRow);
                    } else if (type.equals("VerbEnding")) {
                        Data.makeVerbEnding(nextRow);
                    } else if (type.equals("Demonstrative")) {
                        Data.makeDemonstrative(nextRow);
                    } else if (type.equals("DemonstrativeEnding")) {
                        Data.makeDemonstrativeEnding(nextRow);
                    } else if (type.equals("Pronoun")) {
                        Data.makePronoun(nextRow);
                    } else if (type.equals("VerbWord")) {
                        Data.makeVerbWord(nextRow);
                    } else if (type.equals("Source")) {
                        Data.makeSource(nextRow);
                    }
                }
        } catch (FileNotFoundException e) {
//			e.printStackTrace();
//        	System.out.println("--- End after FileNotFoundExcerption reading linguistic data for data= "+String.join("; ", data));
        	return FILE_NOT_FOUND;
		} catch (IOException e) {
//			e.printStackTrace();
//	    	System.out.println("--- End after IOException reading linguistic data for data= "+String.join("; ", data));
			return IOEXCEPTION;
		}
//    	System.out.println("--- End reading linguistic data for data= "+String.join("; ", data));
		return OK;
    }

    public static HashMap<String,String> getNextRow(String line, String [] fieldNames) {
        HashMap<String,String> currentRow = new HashMap<String,String>();
        Vector<String> values = new Vector<String>();
        boolean inString = false;
        int pos = 0;
        char c = 0;
        for (int i=0; i<line.length(); i++) {
        	c=line.charAt(i);
        	if (c=='"')
        		if (inString)
        			if (i<line.length()-1)
        				if (line.charAt(i+1)!='"')
        					inString = false;
        				else
        					++i;
        			else
        				;
        		else
        			inString = true;
        	else if (c==',')
        		if (!inString) {
        			values.add(line.substring(pos, i));
        			pos = i+1;
        		}
        }
        if (c==',')
        	values.add("");
        else
        	values.add(line.substring(pos));
        String [] valuesStr = (String[])values.toArray(new String[0]);
        for (int i = 0; i < valuesStr.length; i++) {
        	try {
            currentRow.put(fieldNames[i], removeQuotesAndNull(valuesStr[i]));
        	} catch (ArrayIndexOutOfBoundsException e) {
        		System.err.println("line: "+line);
        	}
        }
        return currentRow;
    }

	static String removeQuotesAndNull(String str) {
		if (str.equals(""))
			return null;
		else if (str.charAt(0) == '"') {
			String s = str.replaceFirst("^\"", "");
			String newStr = s.replaceFirst("\"$", "");
			newStr = newStr.replaceAll("\"\"", "\"");
			return newStr;
		} else
			return str;
	}

}
