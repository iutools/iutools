package org.iutools.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class CSVReader {

    protected BufferedReader reader;
    protected String [] fieldNames;

    public CSVReader(BufferedReader _reader) throws IOException {
        this.reader = _reader;
        String firstLine = this.reader.readLine();
        this.fieldNames = firstLine.split(",");
    }

    public Map<String,String> readNext() throws IOException {
        Map<String,String> attributeValuePair = null;
        String line = this.reader.readLine();
        if (line!=null)
            attributeValuePair = csvToMap(line);

        return attributeValuePair;
    }

    public List<Map<String,String>> readAll() throws IOException {
        List<Map<String,String>> allAttributeValuePairs = new ArrayList<Map<String,String>>();
        Map<String,String> attributeValuePairs = null;
        while ( (attributeValuePairs=this.readNext() ) != null) {
            allAttributeValuePairs.add(attributeValuePairs);
        }

        return allAttributeValuePairs;
    }

    protected Map csvToMap(String line) {
        Map<String,String> currentRow = new HashMap<String,String>();
        Vector<String> values = new Vector();
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
        else if (str.charAt(0)=='"') {
            String s = str.replaceFirst("^\"", "");
            String newStr = s.replaceFirst("\"$", "");
            newStr = newStr.replaceAll("\"\"", "\"");
            return newStr;
        } else
            return str;
    }

}