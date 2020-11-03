package ca.pirurvik.iutools.corpus;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ca.nrc.ui.commandline.UserIO;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class RW_CompiledCorpus_InMemory extends RW_CompiledCorpus {

    public RW_CompiledCorpus_InMemory(UserIO io) {
        super(io);
    }

    public RW_CompiledCorpus_InMemory() {
        super();
    }

    @Override
    protected CompiledCorpus newCorpus(File savePath) {
        return new CompiledCorpus_InMemory();
    }

    @Override
    public CompiledCorpus readCorpus(File savePath)
            throws CompiledCorpusException {

        CompiledCorpus_InMemory corpus = null;
        try {
            JsonReader reader = new JsonReader(new FileReader(savePath));
            corpus = gson.fromJson(reader, CompiledCorpus_InMemory.class);
        } catch (IOException e) {
            throw new CompiledCorpusException(e);
        }
        return corpus;
    }

    @Override
    public void writeCorpus(CompiledCorpus corpus, File savePath)
            throws CompiledCorpusException {
        try {
            FileWriter fw = new FileWriter(savePath);
            new Gson().toJson(corpus, fw);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            throw new CompiledCorpusException(e);
        }

        return;
    }
}
