package ca.inuktitutcomputing.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ca.nrc.datastructure.Pair;

public class IUTokenizer {
	
	static Pattern pParan = Pattern.compile("(\\((.*?)\\))");
	// TODO: add other signs that might be used in the stead of "dash"
	static Pattern pPunct = Pattern.compile("((\\p{Punct}|\u2212|\u2013)+)");

	private List<String> words;
	private List<Pair<String,Boolean>> allTokens;
	private String text;
	
	public IUTokenizer() {
	}
	
	
	public List<String> run(String text) {
		this.text = text;
		words = new ArrayList<String>();
		allTokens = new ArrayList<Pair<String,Boolean>>();
		Pattern pSpace = Pattern.compile("(\\s+)");
		Matcher mSpace = pSpace.matcher(text);
		
		int pos = 0;
		while (mSpace.find()) {
			String token = text.substring(pos,mSpace.start(1));
			if (token.length()!=0)
				__processToken(token);
			allTokens.add(new Pair<>(mSpace.group(1),false));
			pos = mSpace.end(1);
		}
		if (text.substring(pos).length()!=0)
			__processToken(text.substring(pos));

		return onlyWords();
	}
	
	public List<Pair<String,Boolean>> getAllTokens() {
		return allTokens;
	}
	
	
	public void __processToken(String token) {
		Logger logger = Logger.getLogger("IUTokenizer.__processToken");
		logger.debug("token= " + token);
		token = token.replaceAll("\\s","");
		Pattern pacr = Pattern.compile("^([^\\.]\\.)+$");
		Matcher macr = pacr.matcher(token);
		if (macr.matches()) {
			words.add(token);
			allTokens.add(new Pair<>(token, true));
		} else {
			Pattern pInitialPunct = Pattern.compile("^(\\p{Punct}+)(.+)");
			Matcher mInitialPunct = pInitialPunct.matcher(token);
			if (mInitialPunct.matches()) {
				allTokens.add(new Pair<>(mInitialPunct.group(1),false));
				token = mInitialPunct.group(2);
			}
			Pattern pFinalPunct = Pattern.compile("^(.+[^\\p{Punct}])(\\p{Punct}+)$");
			Matcher mFinalPunct = pFinalPunct.matcher(token);
			if (mFinalPunct.matches()) {
				token = mFinalPunct.group(1);
			}
			
			Matcher mpunct = pPunct.matcher(token);
			int pos = 0;
			while (mpunct.find()) {
				String punctuationMark = mpunct.group(1);
				logger.debug("found punctuation pattern in " + token + " at position " + mpunct.start(1));
				if ( punctuationMark.equals("&")  && mpunct.start(1)!=0 )
					continue;
				if ( isDash(punctuationMark) && mpunct.start(1)!=0 && wordIsNumberWithSuffix(token)!=null )
					continue;
				if ( pos != mpunct.start(1))
					allTokens.add(new Pair<>(token.substring(pos,mpunct.start(1)), true));
				allTokens.add(new Pair<>(punctuationMark, false));
				pos = mpunct.end(1);
			}
			if ( pos != token.length() )
				allTokens.add(new Pair<>(token.substring(pos), true));

			if (mFinalPunct.matches()) {
				allTokens.add(new Pair<>(mFinalPunct.group(2),false));
			}
			
//			Matcher mpunct = pPunct.matcher(token);
//			int pos = 0;
//			while (mpunct.find()) {
//				String punctuationMark = mpunct.group(1);
//				logger.debug("found punctuation pattern in " + token + " at position " + mpunct.start(1));
//				if ( punctuationMark.equals("&")  && mpunct.start(1)!=0 )
//					continue;
//				if ( isDash(punctuationMark) && mpunct.start(1)!=0 && wordIsNumberWithSuffix(token)!=null )
//					continue;
//				if ( pos != mpunct.start(1))
//					allTokens.add(new Pair<>(token.substring(pos,mpunct.start(1)), true));
//				allTokens.add(new Pair<>(punctuationMark, false));
//				pos = mpunct.end(1);
//			}
//			if ( pos != token.length() )
//				allTokens.add(new Pair<>(token.substring(pos), true));
		}

	}	

	protected String[] wordIsNumberWithSuffix(String word) {
		Pattern p = Pattern.compile("^(\\$?\\d+(?:[.,:]\\d+)?(?:[.,:]\\d+)?[\\-\u2013\u2212]?)([agijklmnpqrstuv]+)$");
		Matcher mp = p.matcher(word);
		if (mp.matches())
			return new String[] {mp.group(1),mp.group(2)};
		else
			return null;
	}

	public List<String> onlyWords() {
		List<String> onlyWords = new ArrayList<String>();
		for (int iToken=0; iToken<allTokens.size(); iToken++) {
			Pair<String,Boolean> token = allTokens.get(iToken);
			if (token.getSecond())
				onlyWords.add(token.getFirst());
		}
		
		return onlyWords;
	}
	
	
	public List<Pair<String,Boolean>> getTokens() {
		return allTokens;
	}
	
	
	public String reconstruct() {
		String str = "";
		for (int iTok=0; iTok<allTokens.size(); iTok++) {
			Pair<String,Boolean> tok = allTokens.get(iTok);
			str += tok.getFirst();
		}
		
		return str;
	}
	
	public static boolean isDash(String character) {
		return character.equals("-") || character.equals("\u2212") || character.equals("\u2013");
	}

}
