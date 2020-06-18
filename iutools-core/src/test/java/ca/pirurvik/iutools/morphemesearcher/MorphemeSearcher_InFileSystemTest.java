package ca.pirurvik.iutools.morphemesearcher;

import org.junit.Ignore;

import com.google.common.io.Files;

import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InFileSystem;

@Ignore
public class MorphemeSearcher_InFileSystemTest extends MorphemeSearcherTest {

	@Override
	protected CompiledCorpus makeCorpus() {
		return new CompiledCorpus_InFileSystem(Files.createTempDir());
	}
}
