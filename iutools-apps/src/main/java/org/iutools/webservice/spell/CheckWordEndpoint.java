package org.iutools.webservice.spell;

import ca.nrc.config.ConfigException;
import ca.nrc.json.PrettyPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.spellchecker.SpellChecker;
import org.iutools.spellchecker.SpellCheckerException;
import org.iutools.spellchecker.SpellingCorrection;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * End point for doing a DEEP but SLOW spell check on a SINGLE word, and optionally provide
 * some suggested correctins (if the word was deemed mis-spelled).
 *
 * By DEEP check, we mean one that checks morpheme composition rules. In
 * particular, it checks that:
 *
 * - word is composed of a sequence of valid morphemes that are allowed to
 *   follow each other
 * - the written form of each morpheme takes into account the morpheme that
 *   precede and follow it.
 *
 * The advantage of a DEEP check is that it catches deeper mistakes that violate
 * morphological composition rules.
 * The disadvantage is that it's much slower than a SHALLOW one.
 */
public class CheckWordEndpoint extends Endpoint<CheckWordInputs, CheckWordResult> {

	SpellChecker checker = null;

	public CheckWordEndpoint() throws ServiceException {
		try {
			init_SpellEndpoint();
		} catch (SpellCheckerException  | FileNotFoundException  |
			ConfigException e) {
			throw new ServiceException(e);
		}
	}

	private void init_SpellEndpoint() throws SpellCheckerException, FileNotFoundException, ConfigException {
		ensureCheckerIsInstantiated();
	}

	private synchronized void ensureCheckerIsInstantiated() throws SpellCheckerException, FileNotFoundException, ConfigException {
		if (checker == null) {
			try {
				checker =
					new SpellChecker(CompiledCorpusRegistry.defaultCorpusName)
						.setCheckLevel(3);
			} catch (Exception e) {
				throw new SpellCheckerException(e);
			}
		}
	}

	@Override
	protected CheckWordInputs requestInputs(String jsonRequestBody) throws ServiceException {
		return jsonInputs(jsonRequestBody, CheckWordInputs.class);
	}

	@Override
	public EndpointResult execute(CheckWordInputs inputs) throws ServiceException {
		Logger tLogger = LogManager.getLogger("org.iutools.webservice.spell.CheckWordEndpoint.execute");

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("inputs="+PrettyPrinter.print(inputs));
		}

		CheckWordResult result = new CheckWordResult();
		result.providesSuggestions = inputs.suggestCorrections;

		if (inputs.text == null || inputs.text.isEmpty()) {
			throw new ServiceException("Query was empty or null");
		}

		try {
			checker.setCheckLevel(inputs.checkLevel);
		} catch (SpellCheckerException e) {
			throw new ServiceException(e);
		}
		List<SpellingCorrection> corrections = null;
		Integer maxCorrections = 0;
		if (inputs.suggestCorrections) {
			maxCorrections = null;
		}

		if (tLogger.isTraceEnabled()) {

		}
		try {
			if (inputs.suggestCorrections) {
				result.correction = checker.correctWord(inputs.text, maxCorrections);
			} else {
				boolean wasMisspelled = checker.isMispelled(inputs.text);
				result.correction = new SpellingCorrection(inputs.text, new String[0], wasMisspelled);
			}
		} catch (SpellCheckerException e) {
			throw new ServiceException(e);
		}

		tLogger.trace("result.correction= "+ PrettyPrinter.print(result.correction));

		tLogger.trace("Returning");

		return result;
	}
}