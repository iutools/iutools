package ca.inuktitutcomputing.utilbin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.data.Base;
import ca.inuktitutcomputing.data.LinguisticDataAbstract;
import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.nrc.json.PrettyPrinter;

public class rootsWithSameNeutralForm {
	
	static HashMap<String,List<String>> rootMorphemes;
	static HashMap<String,List<String>> duplicateRootMorphemes;
		
	public static void main(String[] args) throws Exception {
		rootMorphemes = new HashMap<String,List<String>>();
		duplicateRootMorphemes = new HashMap<String,List<String>>();
		LinguisticDataSingleton.getInstance("csv");
		String rootIDs[] = LinguisticDataAbstract.getAllBasesIds();
		for (int iroot=0; iroot<rootIDs.length; iroot++) {
			Base root = LinguisticDataAbstract.getBase(rootIDs[iroot]);
			String rootMorpheme = root.morpheme;
			String rootTableName = root.getTableName();
			List<String> tableNamesForRoot = rootMorphemes.get(rootMorpheme);
			if (tableNamesForRoot!=null) {
				addToDuplicates(rootMorpheme,rootTableName);
			}
			addToRoots(rootMorpheme,rootTableName);
		}
		
		Iterator<String> iter = duplicateRootMorphemes.keySet().iterator();
		while (iter.hasNext()) {
			String morpheme = iter.next();
			String tables = String.join("; ",duplicateRootMorphemes.get(morpheme).toArray(new String[] {}));
			System.out.println(morpheme+" = "+tables);
		}
				
	}
	
	public static void addToRoots(String morpheme, String tableName) {
		List<String> list = rootMorphemes.get(morpheme);
		if (list==null)
			list = new ArrayList<String>();
		list.add(tableName);
		rootMorphemes.put(morpheme, list);
	}

	public static void addToDuplicates(String morpheme, String tableName) {
		List<String> listForDuplicate = duplicateRootMorphemes.get(morpheme);
		if (listForDuplicate==null)
			listForDuplicate = new ArrayList<String>(rootMorphemes.get(morpheme));
		listForDuplicate.add(tableName);
		duplicateRootMorphemes.put(morpheme, listForDuplicate);
	}

	
}


