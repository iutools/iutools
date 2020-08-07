package ca.pirurvik.iutools.spellchecker.goldstandard;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Read a Spelling Gold Standard from a bunch of files produced by human
 * evaluators.
 */
public class SpellGoldStandardReader {

    public static class CSVConsumer implements Consumer<Path> {

        private final SpellGoldStandard goldStandard;
        List<Path> processedFiles = new ArrayList<Path>();

        public CSVConsumer(SpellGoldStandard _gs) {
            this.goldStandard = _gs;
        }

        @Override
        public void accept(Path aPath) {
            if (isCSVFile(aPath)) {
                this.processCSVFile(aPath);
                System.out.println("accept: processing file " + aPath);
            }
        }

        private void processCSVFile(Path csvFile) {


        }

        private static boolean isCSVFile(Path aPath) {
            boolean isCSV = false;
            File aFile = aPath.toFile();
            if (FilenameUtils.getExtension(aPath.toString()).equals("csv")) {
                isCSV = true;
            }
            return isCSV;
        }

    }

    public static SpellGoldStandard read(File gsRootDir) throws IOException {
        SpellGoldStandard gs = new SpellGoldStandard();

        CSVConsumer consumer = new CSVConsumer(gs);

        gsRootDir.toPath();
        Files.walk(Paths.get(gsRootDir.toString(), new String[]{}))
//                .filter(SpellGoldStandardReader::isCSVFile)
                .forEach(consumer);

        return gs;
    }



    private static void processCSVFile(Path csvPath) {
        System.out.println("Process csv file: "+csvPath);
    }
}
