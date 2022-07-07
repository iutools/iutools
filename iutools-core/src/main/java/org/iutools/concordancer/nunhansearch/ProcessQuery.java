package org.iutools.concordancer.nunhansearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unix4j.Unix4j;
import org.unix4j.io.StringOutput;

import org.iutools.config.IUConfig;
import ca.nrc.config.ConfigException;

public class ProcessQuery {
	
	String inuktutWordIndexFilePath = "data/NunHanSearch/1999-2007/InuktitutWordsIndex.txt";
	File inuktutWordIndexFile = null;
	String alignedSentencesFilePath = "data/NunHanSearch/1999-2007/SingleLineAligned.txt";
	File alignedSentencesFile = null;
	
	public ProcessQuery() throws ConfigException {
		Logger tLogger = LogManager.getLogger("ca.inuktitutcomputing.nunhansearch.ProcessQuery.constructor");
		inuktutWordIndexFile = new File(IUConfig.getIUDataPath()+"/"+inuktutWordIndexFilePath);
		alignedSentencesFile = new File(IUConfig.getIUDataPath()+"/"+alignedSentencesFilePath);
		tLogger.trace("inuktutWordIndexFile="+inuktutWordIndexFile);;
	}
	
	public void setInuktutWordIndexFile(String path) {
		inuktutWordIndexFile = new File(path);
	}
	public void setAlignedSentencesFile(String path) {
		alignedSentencesFile = new File(path);
	}
	
	public String[] run(String query) throws ConfigException, IOException {
		Logger tLogger = LogManager.getLogger("ca.inuktitutcomputing.nunhansearch.ProcessQuery.run");
		query = "^"+query+":";
		tLogger.trace("query='"+query+"'");
		TermDistribution distribution = getDistribution(query);
		String[] alignments = getAlignments(distribution.variantsDistributions);
		
		
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("returning alignments=\n[\n   "+String.join("\n  ", alignments)+"\n]");
		}
		
		return alignments;
	}
	
	/**
	 * 
	 * @param variantsDistributions
	 * @return String[] array of String in the format 19990401:: jak anaruaq@----@ Mr. Jack Anawak
	 * @throws IOException
	 */
	
	public String[] getAlignments(HashMap<String, Long[]> variantsDistributions) throws IOException {
		Logger logger = LogManager.getLogger("ca.inuktitutcomputing.nunhansearch.ProcessQuery.getAlignments");
		logger.trace("getAlignments invoked");
		logger.trace("alignedSentencesFile= "+alignedSentencesFile.getAbsolutePath());
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(alignedSentencesFile));
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException("Could not find aligned sentence file: "+alignedSentencesFile);
		}
		String l;
		try {
			while ( (l=br.readLine()) != null) {
//				logger.trace("l: '"+l+"'");
			}
		} catch (IOException e) {
			throw new IOException("Problem reading line from aligned sentence file: "+alignedSentencesFile);
			
		}
		br.close();
		RandomAccessFile raf = new RandomAccessFile(alignedSentencesFile.getAbsolutePath(), "r");
		List<String> listAlignedSentences = new ArrayList<String>();
		Set<String> variants = variantsDistributions.keySet();
		Iterator<String> itVar = variants.iterator();
		while (itVar.hasNext()) {
			String variant = itVar.next();
			logger.trace("variant='"+variant+"'");
			Long[] positions = variantsDistributions.get(variant);
			for (int ip=0; ip<positions.length; ip++) {
				long position = positions[ip].longValue();
				logger.trace("position= "+position);
				raf.seek(position);
				String line = raf.readLine();
				listAlignedSentences.add(line);
			}
		}
		raf.close();
		
		String[] alignmentsArray = 
				listAlignedSentences
					.toArray(new String[listAlignedSentences.size()]);
		if (logger.isTraceEnabled()) {
			logger.trace("returning alignmentsArray=[\n"+String.join("\n  ", alignmentsArray)+"\n]");
		}
		return alignmentsArray;
		
	}

	public TermDistribution getDistribution(String query) 
			throws ConfigException, FileNotFoundException {
		Logger tLogger = LogManager.getLogger("ca.inuktitutcomputing.nunhansearch.ProcessQuery.getDistribution");
		tLogger.trace("inuktutWordIndexFile="+inuktutWordIndexFile);
		
		String grepResult = grep(query,inuktutWordIndexFile);
		String[] matchingLines = grepResult.split("\n");
		Pattern pat = Pattern.compile("^([^:]+):([0-9]+):(.*)$");
		List<Long> listAllVariantsIndices = new ArrayList<Long>();
		HashMap<String,Long[]> dist = new HashMap<String,Long[]>();
		for (String line : matchingLines) {
			Matcher matcher = pat.matcher(line);
			if (matcher.matches()) {
				String variant = matcher.group(1);
				Long freq = Long.parseLong(matcher.group(2));
				String[] indicesStr = matcher.group(3).split(":");
				ArrayList<Long> variantIndices = new ArrayList<Long>();
				for (int i=0; i<indicesStr.length; i++) {
					Long index = Long.parseLong(indicesStr[i]);
					if ( !variantIndices.contains(index) )
						variantIndices.add(index);
				}
				dist.put(variant,variantIndices.toArray(new Long[] {}));
				listAllVariantsIndices.addAll(variantIndices);
			}
		}
		Long[] allVariantsIndices = listAllVariantsIndices.stream() 
                .distinct() 
                .collect(Collectors.toList())
                .toArray(new Long[] {}); 
		Arrays.sort(allVariantsIndices);
		TermDistribution distribution = new TermDistribution(allVariantsIndices,dist);
		return distribution;
	}
	
	public String grep(String query, File file) throws FileNotFoundException {
		Logger tLogger = LogManager.getLogger("ca.inuktitutcomputing.nunhansearch.ProcessQuery.grep");
		if (!file.exists()) {
			throw new FileNotFoundException("Could not find file "+file+".\nIt might be that you need to install the NunHanSearch data directory.");
		}
		String queryRegexp = query.replace("*", "\\S*?");
		StringOutput output = new StringOutput();
		tLogger.trace("queryRegexp= "+queryRegexp);
		Unix4j.grep(queryRegexp, file).toOutput(output);
		

		tLogger.trace("returning output="+output.toString());
		
		return output.toString();
	}

}
