package org.iutools.webservice.spell;

import ca.nrc.config.ConfigException;
import ca.nrc.json.PrettyPrinter;
import org.apache.log4j.Logger;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.spellchecker.SpellChecker;
import org.iutools.spellchecker.SpellCheckerException;
import org.iutools.spellchecker.SpellingCorrection;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.util.List;

public class SpellEndpoint extends Endpoint<SpellInputs, SpellResult> {

	SpellChecker checker = null;

	public SpellEndpoint() throws ServiceException {
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
						.enablePartialCorrections();
			} catch (Exception e) {
				throw new SpellCheckerException(e);
			}
		}
	}

	@Override
	protected SpellInputs requestInputs(String jsonRequestBody) throws ServiceException {
		return jsonInputs(jsonRequestBody, SpellInputs.class);
	}

	@Override
	public EndpointResult execute(SpellInputs inputs) throws ServiceException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.spell.SpellEndpoint.execute");

		tLogger.trace("inputs.text= "+inputs.text);
		tLogger.trace("Spell checker has base ES index name = \n"+checker.corpusIndexName());

		SpellResult result = new SpellResult();

		if (inputs.text == null || inputs.text.isEmpty()) {
			throw new ServiceException("Query was empty or null");
		}

		checker.setPartialCorrectionEnabled(inputs.includePartiallyCorrect);
		List<SpellingCorrection> corrections = null;
		try {
			corrections = checker.correctText(inputs.text);
		} catch (SpellCheckerException e) {
			throw new ServiceException(e);
		}

		tLogger.trace("corrections= "+ PrettyPrinter.print(corrections));

		result.correction = corrections;

		tLogger.trace("Returning");

		return result;
	}
}