package ca.pirurvik.iutools.text.segmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.nrc.datastructure.Pair;
import ca.nrc.string.SimpleTokenizer;
import ca.pirurvik.iutools.text.segmentation.Segmenter;

public class Segmenter_Generic extends Segmenter {

	@Override
	protected String[] tokenize(String text) {
		String[] tokens = new SimpleTokenizer().tokenize(text, true);
		return tokens;
	}
	
}
