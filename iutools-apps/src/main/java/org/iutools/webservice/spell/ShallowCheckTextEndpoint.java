package org.iutools.webservice.spell;

import org.iutools.datastructure.trie.StringSegmenterException;
import org.iutools.spellchecker.SpellChecker;
import org.iutools.spellchecker.SpellCheckerException;
import org.iutools.spellchecker.SpellingCorrection;
import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.text.segmentation.Token;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;

import java.util.*;

/**
 * End point for doing a SHALLOW spell check on MULTI-WORD text.
 *
 * By SHALLOW spell check, we mean one that only checks for sequences of
 * characters that are NEVER allowed to exist in valid Inuktitut.
 *
 * The advantage of a SHALLOW check over a DEEP one is that it's much faster.
 * The disadvantage is that it may miss some deeper spelling mistakes that violate
 * morpheme composition rules.
 */
public class ShallowCheckTextEndpoint extends Endpoint<ShallowCheckTextInputs, ShallowCheckTextResult> {

	SpellChecker checker = null;

	public ShallowCheckTextEndpoint() throws StringSegmenterException, SpellCheckerException {
		checker = new SpellChecker().setCheckLevel(1);
	}

	@Override
	protected ShallowCheckTextInputs requestInputs(String jsonRequestBody) throws ServiceException {
		return jsonInputs(jsonRequestBody, ShallowCheckTextInputs.class);
	}

	@Override
	public EndpointResult execute(ShallowCheckTextInputs inputs) throws ServiceException {
		String correctedText = "";
		Map<String,String> misspelledWords = new HashMap<String,String>();
		List<Token> tokens = tokenizeText(inputs.origText);
		for (Token aToken: tokens) {
			if (!aToken.isWord) {
				// Just append punctuation as is
				correctedText += aToken.text;
			} else {
				// For word, do a shallow correction of the original word
				try {
					SpellingCorrection tokenCorrection = checker.correctWord(aToken.text);
					String topSuggestion = tokenCorrection.topSuggestion();
					correctedText += topSuggestion;
					if (tokenCorrection.wasMispelled) {
						String correctionMade = topSuggestion;
						if (topSuggestion.equals(aToken.text)) {
							// If the top suggestion is the same as the original text,
							// it means the only CorrectionRules that fired were ones
							// that can only IDENTIFY a mistake WITHOUT correcting it.
							correctionMade = null;
						}
						misspelledWords.put(aToken.text, correctionMade);
					}
				} catch (SpellCheckerException e) {
					throw new ServiceException(e);
				}
			}
		}

		ShallowCheckTextResult results =
			new ShallowCheckTextResult(inputs.origText, correctedText,misspelledWords);

		return results;
	}

	private List<Token> tokenizeText(String origText) {
		IUTokenizer tokenizer = new IUTokenizer();

		tokenizer.tokenize(origText);
		List<Token> tokens = tokenizer.getAllTokens();
		return tokens;
	}
}
