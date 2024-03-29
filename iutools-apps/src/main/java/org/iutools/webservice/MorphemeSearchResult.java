package org.iutools.webservice;

import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeException;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;

import java.util.List;


public class MorphemeSearchResult {
	public String morphID;
	public MorphemeHumanReadableDescr morphDescr = null;
	public String  meaning;
	public List<String> words;
	public List<Double> wordScores;


	public MorphemeSearchResult() {
	};
	
	public MorphemeSearchResult(String _morphID, String _meaning, List<String> _words, List<Double> _wordScores) throws ServiceException {
		this.morphID = _morphID;
		try {
			this.morphDescr = new MorphemeHumanReadableDescr(morphID);
		} catch (MorphemeException e) {
			throw new ServiceException(e);
		}
		this.meaning = _meaning;
		this.words = _words;
		this.wordScores = _wordScores;
	}
}

