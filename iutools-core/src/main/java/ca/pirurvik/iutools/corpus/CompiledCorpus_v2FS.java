package ca.pirurvik.iutools.corpus;

import java.io.File;
import java.io.IOException;

import ca.nrc.datastructure.trie.*;
import com.google.common.io.Files;

/**
 * InFileSystem version of the v2 corpus class.
 */
public class CompiledCorpus_v2FS extends CompiledCorpus_v2 {


	public CompiledCorpus_v2FS(File _corpusDir) {
		super(_corpusDir);
	}

	@Override
	protected Trie makeWordCharTrie() {
		File trieRoot = new File(corpusDir, "wordCharTrie");
		Trie_InFileSystem trie = new Trie_InFileSystem(trieRoot);
		return trie;
	}

	@Override
	protected Trie makeCharNgramsTrie() {
		File trieRoot = new File(corpusDir, "charNgramsTrie");
		Trie_InFileSystem trie = new Trie_InFileSystem(trieRoot);
		return trie;
	}

	@Override
	protected Trie makeMorphNgramsTrie() {
		File trieRoot = new File(corpusDir, "morphNgramsTrie");
		Trie_InFileSystem trie = new Trie_InFileSystem(trieRoot);
		return trie;
	}

	@Override
	public void makeStale(Trie trie) throws CompiledCorpusException {
		try {
			File file = stalenessFile((Trie_InFileSystem) trie);
			file.getParentFile().mkdirs();
			Files.touch(file);
		} catch (IOException e) {
			throw new CompiledCorpusException(e);
		}
		return;
	}

	@Override
	public void makeNotStale(Trie trie) {
		File file = stalenessFile((Trie_InFileSystem) trie);
		if (file.exists()) {
			file.delete();
		}
	}

	@Override
	public boolean isStale(Trie trie) {
		boolean answer = stalenessFile((Trie_InFileSystem) trie).exists();
		return answer;
	}

	private File stalenessFile(Trie_InFileSystem trie) {
		File file = new File(trie.getRootDir().toString()+".stale");
		return file;
	}
}
