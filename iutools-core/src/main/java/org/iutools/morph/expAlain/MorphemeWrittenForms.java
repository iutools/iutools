package org.iutools.morph.expAlain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.iutools.morph.MorphologicalAnalyzerException;

public class MorphemeWrittenForms {
	
	private static MorphemeWrittenForms _singleton = null;
	
	private List<String> allSurfRegexps = new ArrayList<String>();
	private Pattern _allSurfFormsPattern = null;
	
	Map<String,Set<WrittenMorpheme>> surf2MatchedMorpheme = 
			new HashMap<String,Set<WrittenMorpheme>>();
	
	private MorphemeWrittenForms() throws MorphologicalAnalyzerException {
		
		// TODO: For now, we hard code a small set of forms.
		//   Eventually, those should be computed from the linguistic DB
		WrittenMorpheme[] forms = new WrittenMorpheme[] {
				
			new WrittenMorpheme("inuk/1n", "inuk"),
			new WrittenMorpheme("inuk/1n", "inu"),
			
			new WrittenMorpheme("titut/nn", "titut"),
			new WrittenMorpheme("titut/nn", "titu"),
		};
		
		for (WrittenMorpheme aForm: forms) {
			addForm(aForm);
		}
		
		doneAddingForms();
	}

	private void doneAddingForms() {
	}

	public static synchronized MorphemeWrittenForms getInstance() 
			throws MorphologicalAnalyzerException {
		if (_singleton == null) {
			generateSingleton();
		}
		return _singleton;
	}

	/**
	 * Generate the singleton instance.
	 * 
	 * Note: The method is synchronized to prevent the possibility that two 
	 * threads will try to instantiate it at the same time.
	 * 
	 * @return
	 * @throws MorphologicalAnalyzerException
	 */
	private synchronized static void generateSingleton() 
			throws MorphologicalAnalyzerException {
		Logger tLogger = 
			Logger.getLogger("ca.inukitutcomputing.morph.expAlain.MorphemeWrittenForms.generateSingleton");
		
		// Make sure that singleton has not already been created by another 
		// thread while we were waiting for the method 'sync'
		//
		if (_singleton == null) {
			_singleton = new MorphemeWrittenForms();
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("Regexp for all written forms is:\n"+
						_singleton.allWrittenFormsPattern());
			}
			
		}
	}

	private void addForm(WrittenMorpheme aForm) 
			throws MorphologicalAnalyzerException {
		String morphID = aForm.morphID;
		String morphAttachesTo = aForm.atachesTo();
		updateAllSurfFormsRegex(aForm);
		updateSurf2MatchedMorphemeMap(aForm);
	}

	private void updateAllSurfFormsRegex(WrittenMorpheme aForm) throws MorphologicalAnalyzerException {
		allSurfRegexps.add(aForm.regex());
	}
	

	private void updateSurf2MatchedMorphemeMap(WrittenMorpheme aForm) {
		if (!surf2MatchedMorpheme.containsKey(aForm.writtenForm)) {
			surf2MatchedMorpheme
				.put(aForm.writtenForm, new HashSet<WrittenMorpheme>());
		}
		surf2MatchedMorpheme.get(aForm.writtenForm).add(aForm);
	}
	
	/**
	 * Find all morphemes that can attach to a particular morpheme, and 
	 * whose surface form can match the start of a string.
	 * 
	 * @param attachTo
	 * @param matchSurfForm
	 * @return
	 * @throws MorphologicalAnalyzerException
	 */
	public List<WrittenMorpheme> morphemesThatCanFollow(
			WrittenMorpheme attachTo, 
			String matchSurfForm) throws MorphologicalAnalyzerException {
		
		String matchSurfFormExtended = attachTo.type() + matchSurfForm;		
		
		Matcher matcher = allWrittenFormsPattern().matcher(matchSurfFormExtended);
		List<WrittenMorpheme> morphemes = new ArrayList<WrittenMorpheme>();
		if (matcher.find()) {
			String surfForm = matcher.group(1);
			surfForm = surfForm.substring(1); 
			boolean keepGoing = true;
			while (keepGoing) {
				if (!surf2MatchedMorpheme.containsKey(surfForm)) {
					keepGoing = false;
				} else {
					morphemes.addAll(surf2MatchedMorpheme.get(surfForm));
					surfForm = surfForm.substring(0, surfForm.length()-1);
				}
			}
		}
		
		return morphemes;
	}
	
	/**
	 * Generates a long Pattern to find the longest surface form that matches 
	 * the start of a string (where the first character of that string indicates 
	 * the type of the previous morpheme in the sequence)
	 * 
	 * @return
	 */
	Pattern allWrittenFormsPattern() {
		if (_allSurfFormsPattern == null) {
			//  First sort the list of allSurfRegexps so that the longest
			//  forms come first. That way, the resulting regexp will match
			//  the LONGEST possible form
			//			
	        Collections.sort(allSurfRegexps, 
	        		Comparator.comparing(String::length));
	        Collections.reverse(allSurfRegexps);
			 
			String regex = "("+String.join("|", allSurfRegexps)+")";
			_allSurfFormsPattern = Pattern.compile(regex);
		}
		
		return _allSurfFormsPattern;
	}
	
	
	
}
