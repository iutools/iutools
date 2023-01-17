package org.iutools.concordancer.tm;

import static ca.nrc.dtrc.elasticsearch.ESFactory.*;

import ca.nrc.datastructure.CloseableIterator;
import ca.nrc.ui.commandline.UserIO;
import org.iutools.concordancer.Alignment;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.datastructure.CloseableIteratorWrapper;
import org.iutools.datastructure.trie.StringSegmenterException;
import org.iutools.datastructure.trie.StringSegmenter_Word;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.text.segmentation.IUTokenizer;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * A basic Translation Memory that uses ElasticSearch
 */
public abstract class TranslationMemory {
	public abstract void loadFile(Path tmFile, ESOptions... options)
		throws TranslationMemoryException;
	public abstract void addAlignment(Alignment alignment) throws TranslationMemoryException;
	public abstract void removeAligmentsFromDoc(String docID) throws CompiledCorpusException;
	protected abstract CloseableIterator<Alignment>
		search(String sourceLang, String[] sourceExprVariants,
			String targetLang, String[] withTranslation) throws TranslationMemoryException;
	public abstract void delete() throws TranslationMemoryException;

	public static final String DEFAULT_TM_NAME = "iutools_tm";

	protected UserIO userIO = new UserIO().setVerbosity(UserIO.Verbosity.Level0);

	protected String tmName = DEFAULT_TM_NAME;

	public TranslationMemory() {
		init_TranslationMemory(DEFAULT_TM_NAME);
	}

	public TranslationMemory(String _tmName) {
		init_TranslationMemory(_tmName);
	}

	private void init_TranslationMemory(String _tmName) {
		if (_tmName != null) {
			this.tmName = _tmName;
		}
		return;
	}

	public void loadFile(Path tmFile) throws TranslationMemoryException {
		loadFile(tmFile, new ESOptions[0]);
	}

	public TranslationMemory setUserIO(UserIO _userIO) {
		this.userIO = _userIO;
		return this;
	}

	public CloseableIterator<Alignment> search(
		String sourceLang, String sourceExpr, String targetLang) throws TranslationMemoryException {
		return search(sourceLang, sourceExpr, targetLang, (String)null);
	}

	public CloseableIterator<Alignment> search(
		String sourceLang, String sourceExpr, String targetLang, String withTranslation)
		throws TranslationMemoryException {

		CloseableIterator<Alignment> iter = new CloseableIteratorWrapper<Alignment>(Collections.emptyIterator());
		String[] sourceExprVariants = new String[]{sourceExpr};
		String[] translationVariants = null;
		try {
			if (sourceLang.equals("iu")) {
				// For iu, search the source expression in either scripts.
				// Some of the TMs use roman while others use syllabic
				sourceExprVariants = new String[]{
					TransCoder.ensureScript(TransCoder.Script.SYLLABIC, sourceExpr),
					TransCoder.ensureScript(TransCoder.Script.ROMAN, sourceExpr),
				};
			}
			if (withTranslation != null) {
				translationVariants = new String[] {withTranslation};
				if (targetLang.equals("iu")) {
					// For iu, search the target expression in either scripts.
					// Some of the TMs use roman while others use syllabic
					translationVariants = new String[]{
						TransCoder.ensureScript(TransCoder.Script.SYLLABIC, withTranslation),
						TransCoder.ensureScript(TransCoder.Script.ROMAN, withTranslation),
					};
				}
			}
		} catch (TransCoderException e) {
			throw new TranslationMemoryException(e);
		}
		iter = search(sourceLang, sourceExprVariants, targetLang, translationVariants);

		return iter;
	}

	private boolean isSingleWord(String word, String lang) throws TranslationMemoryException {
		boolean singleWord = true;
		List<String> words = null;
		if (lang.equals("iu")) {
				words = new IUTokenizer().tokenize(word);
		} else {
			try {
				words = Arrays.asList(new StringSegmenter_Word().segment(word));
			} catch (TimeoutException | StringSegmenterException e) {
				throw new TranslationMemoryException(e);
			}
		}
		singleWord = words.size() == 1;
		return singleWord;
	}


}
