package ca.pirurvik.iutools.text.segmentation;

import java.util.Arrays;
import java.util.List;

import ca.nrc.datastructure.Pair;

public class Segmenter_IU extends Segmenter {

	public Segmenter_IU() {
		super();
	}
	

	@Override
	protected String[] tokenize(String text) {
		IUTokenizer tokenizer = new IUTokenizer();
		tokenizer.tokenize(text);
		List<Pair<String, Boolean>> allTokens = tokenizer.getAllTokens();
		String[] tokens = new String[allTokens.size()];
		for (int ii=0; ii < allTokens.size(); ii++) {
			tokens[ii] = allTokens.get(ii).getFirst();
		}

		return tokens;
	}

}
