package org.iutools.webservice.morphdict;

import org.iutools.linguisticdata.MorphemeException;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;

import java.util.*;

public class MorphemeDictResult extends EndpointResult {

	public List<MorphemeHumanReadableDescr> matchingMorphemes =
		new ArrayList<MorphemeHumanReadableDescr>();

	public Map<String,String[]> examplesForMorpheme = new HashMap<String,String[]>();

	public Set<String> matchingMorphemeIDs() {
		return examplesForMorpheme.keySet();
	}

	public Set<MorphemeHumanReadableDescr> matchingMorphemesDescr() {
		Set<MorphemeHumanReadableDescr> matchingDescrs =
			new HashSet<MorphemeHumanReadableDescr>();
		matchingDescrs.addAll(matchingMorphemes);
		return matchingDescrs;
	}

    public void ensureScript(TransCoder.Script iuAlphabet) throws ServiceException {
		try {
			for (int ii=0; ii < matchingMorphemes.size(); ii++) {
				MorphemeHumanReadableDescr morphemeDescr = matchingMorphemes.get(ii);
				morphemeDescr.ensureScript(iuAlphabet);
			}
			for (Map.Entry<String, String[]> entry: examplesForMorpheme.entrySet()) {
				String[] examples = entry.getValue();
				for (int ii=0; ii < examples.length; ii++) {
					examples[ii] = TransCoder.ensureScript(iuAlphabet, examples[ii]);
				}
				entry.setValue(examples);
			}
		} catch (MorphemeException | TransCoderException e) {
			throw new ServiceException(e);
		}
		return;
	}
}