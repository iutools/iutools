package org.iutools.morph.failureanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.nrc.datastructure.Pair;
import ca.nrc.string.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProblematicNGram {
	
	public static final int MAX_EXAMPLES = 20;
	public static enum SortBy {FS_RATIO, N_FAILURES};
	
	public String ngram = null;
	public long numFailures = 0;
	public long numSuccesses = 0;
	public long failureMass = -1;
	
	private Map<String,Long> failureExampleFreq = new HashMap<String,Long>();
	private Pair<String,Long> failureExampleWithSmallestFreq = 
			Pair.of("", new Long(-2)); 
	
	private Map<String,Long> successExampleFreq = new HashMap<String,Long>();
	private Pair<String,Long> successExampleWithSmallestFreq = 
			Pair.of("", new Long(-2)); 
	
	private Double failureSuccessRatio = null;

	ObjectMapper mapper = new ObjectMapper();
	{

	}

	public ProblematicNGram(String _ngram) {
		initProblematicNGram(_ngram);
	}

	private void initProblematicNGram(String _ngram) {
		this.ngram = _ngram;
	}

	public void addFailureExample(String word) {
		addFailureExample(word, null);
	}
	
	public void addFailureExample(String word, Long freq) {
		if (freq == null) {
			freq = new Long(-1);
		}
		if (failureExampleFreq.keySet().size() < MAX_EXAMPLES) {
			failureExampleFreq.put(word, freq);
		} else {
			// Too many examples. Insert this one if its frequency 
			// is higher than the lowes-frequency example
			//
			if (freq > failureExampleWithSmallestFreq.getSecond()) {
				failureExampleFreq.remove(
					failureExampleWithSmallestFreq.getFirst());
				failureExampleFreq.put(word, freq);
				failureExampleWithSmallestFreq = 
					exampleWithSmallestFreq(failureExampleFreq);
			}
		}
	}

	private Pair<String, Long> exampleWithSmallestFreq(Map<String, Long> examples) {
		Pair<String,Long> example = null;
		for (Entry<String, Long> entry: examples.entrySet()) {
			if (example == null || entry.getValue() < example.getSecond()) {
				example = Pair.of(entry.getKey(), entry.getValue());
			}
		}
		return example;
	}

	public void addSuccessExample(String word) {
		addSuccessExample(word, null);
	}
	
	public void addSuccessExample(String word, Long freq) {
		if (freq == null) {
			freq = new Long(-1);
		}
		if (successExampleFreq.keySet().size() < MAX_EXAMPLES) {
			successExampleFreq.put(word, freq);
		} else {
			// Too many examples. Insert this one if its frequency 
			// is higher than the lowest-frequency example
			//
			if (freq > successExampleWithSmallestFreq.getSecond()) {
				successExampleFreq.remove(
					successExampleWithSmallestFreq.getFirst());
				successExampleFreq.put(word, freq);
				successExampleWithSmallestFreq = 
					exampleWithSmallestFreq(successExampleFreq);
			}
		}
		
	}
	
	public Long getNumFailures() {
		return numFailures;
	}
	
	public void setFailureSuccessRatio(Double ratio) {
		this.failureSuccessRatio = ratio;
	}	
	
	public Double getFailSucceedRatio() {
		return this.failureSuccessRatio;
	}	

	public String toCSV() {
		String csv = 
			ngram+","+
			getFailSucceedRatio()+","+getNumFailures()+","+failureMass+","+
			StringUtils.join(failureExamples().iterator(), "; ")+","+
			StringUtils.join(successExamples().iterator(), "; ")
			;
		return csv;
	}

	public String toJSON() {
		Map<String,Object> jsonMap = new HashMap<String,Object>();
		jsonMap.put("1_ngram", this.ngram);
		jsonMap.put("2_fs-ratio", this.getFailSucceedRatio());
		jsonMap.put("3_total-failures", failureMass);
		jsonMap.put("4_total-successes", numSuccesses);
		jsonMap.put("5_failure-examples", failureExamples());
		jsonMap.put("6_success-examples", failureExamples());

		String json = null;
//			MapperFactory.mapper(MapperFactory.Options.SORT_FIELDS)
//			.writeValueAsString(jsonMap);

		return json;
	}

	public static String csvHeaders() {
		String headers = 
			"ngram,fs_ratio,n_failures,failure_mass,failure_examples,success_examples";
		return headers;
	}

	public List<String> failureExamples() {
		List<Pair<String,Long>> examplesWithFreq = 
				failureExamplesWithFreq();
		List<String> examples = new ArrayList<String>();
		for (Pair<String,Long> anExample: examplesWithFreq) {
			examples.add(anExample.getFirst());
		}
		return examples;
	}
	
	public List<Pair<String, Long>> failureExamplesWithFreq() {
		List<Pair<String,Long>> examples = 
			new ArrayList<Pair<String,Long>>();
		for (String word: failureExampleFreq.keySet()) {
			examples.add(Pair.of(word, failureExampleFreq.get(word)));
		}
		
		sortExamplesByFreq(examples);
				
		return examples;
	}
	

	private void sortExamplesByFreq(List<Pair<String, Long>> examples) {
		Collections.sort(examples, 
			(Pair<String,Long> e1, Pair<String,Long> e2) -> {
				Long e1Freq = e1.getSecond();
				Long e2Freq = e2.getSecond();
				int compare = 0;
				if (e1Freq != null && e2Freq != null) {
					compare = e1.getSecond().compareTo(e2.getSecond());
				}
				return compare;
			});
	}

	public List<String> successExamples() {
		List<Pair<String,Long>> examplesWithFreq = 
				successExamplesWithFreq();
		List<String> examples = new ArrayList<String>();
		for (Pair<String,Long> anExample: examplesWithFreq) {
			examples.add(anExample.getFirst());
		}
		return examples;
	}


	public List<Pair<String, Long>> successExamplesWithFreq() {
		List<Pair<String,Long>> examples = 
				new ArrayList<Pair<String,Long>>();
			for (String word: successExampleFreq.keySet()) {
				examples.add(Pair.of(word, failureExampleFreq.get(word)));
			}
			
			sortExamplesByFreq(examples);
					
			return examples;
	}
}
