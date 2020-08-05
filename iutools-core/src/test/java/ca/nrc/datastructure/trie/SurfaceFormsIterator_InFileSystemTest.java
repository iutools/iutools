package ca.nrc.datastructure.trie;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SurfaceFormsIterator_InFileSystemTest extends SurfaceFormsIteratorTest{
    @Override
    protected Trie makeTrie() throws IOException {
        File tempDir = Files.createTempDirectory("xxx").toFile();
        tempDir.deleteOnExit();
        return new Trie_InFileSystem(tempDir);
    }
}
