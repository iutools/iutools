package ca.pirurvik.iutools.webservice.gist;

import java.util.ArrayList;
import java.util.List;

import ca.nrc.datastructure.Pair;
import ca.pirurvik.iutools.webservice.ServiceResponse;

public class GistPrepareContentResponse extends ServiceResponse {

	public List<String[]> iuSentences = new ArrayList<String[]>();
	public List<String[]> enSentences = null;
	public boolean wasActualText = true;

	public GistPrepareContentResponse() {
		
	}

	public boolean getAlignmentsAvailable() {
		boolean available = 
			(iuSentences != null && !iuSentences.isEmpty() &&
				enSentences != null && !enSentences.isEmpty());
 		
		return available;
	}
	
	
	public void setAlignmentsAvailable(boolean available) {
		// Do nothing. This method is there just so the Jackson serializer
		// does not raise an UnrecognizedPropertyException
	}
}
