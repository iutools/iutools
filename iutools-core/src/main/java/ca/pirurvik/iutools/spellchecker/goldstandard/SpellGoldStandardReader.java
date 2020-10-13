package ca.pirurvik.iutools.spellchecker.goldstandard;

import ca.nrc.data.file.CSVReader;
import ca.nrc.string.StringUtils;
import ca.pirurvik.iutools.spellchecker.SpellCheckerException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.log4j.helpers.LogLog.warn;

/**
 * Read a Spelling Gold Standard from a bunch of files produced by human
 * evaluators.
 */
public class SpellGoldStandardReader {

    public static enum GSReaderOption {WARN_CORRECTIONS_WITH_NO_COMMENT}

    public static class CSVConsumer implements Consumer<Path> {

        private Set<GSReaderOption> options = null;

        private SpellGoldStandard goldStandard;

        List<Path> processedFiles = new ArrayList<Path>();

        public CSVConsumer(SpellGoldStandard _gs) {
            init_CSVConsumer(_gs, null);
        }

        public CSVConsumer(SpellGoldStandard _gs, GSReaderOption[] _options) {
            init_CSVConsumer(_gs, _options);
        }

        private void init_CSVConsumer(SpellGoldStandard _gs, GSReaderOption[] _options) {
            if (_options == null) {
                _options = new GSReaderOption[0];
            }
            options = new HashSet<GSReaderOption>();
            Collections.addAll(options, _options);
            this.goldStandard = _gs;
        }

        @Override
        public void accept(Path aPath) {
            if (isCSVFile(aPath)) {
                try {
                    this.processCSVFile(aPath);
                } catch (SpellCheckerException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("accept: processing file " + aPath);
            }
        }

        protected void processCSVFile(Path csvFile) throws SpellCheckerException {
            Pair<String,String> fileID_andRevisor = idAndRevisorForFile(csvFile);
            String docID = fileID_andRevisor.getLeft();
            String revisor = fileID_andRevisor.getRight();
            List<List<String>> lines = null;
            try {
                lines = new CSVReader().read(csvFile.toString());
            } catch (IOException e) {
                throw new SpellCheckerException("Unable to parse human revision file "+csvFile, e);
            }
            int lineCounter = 0;
            for (List<String> aLine: lines) {
                lineCounter++;
                if (lineCounter == 1 || aLine.size() == 0) {
                    // Skip the first line as well as empty lines
                    continue;
                }

                if (aLine.size() < 3) {
                    throw new SpellCheckerException(
                        "Wrong number of fields in a CSV file line\n"+
                        "  In file      : "+csvFile+"\n"+
                        "  At Line      :"+lineCounter+"\n" +
                        "  Got #fields  : "+aLine.size()+"\n" +
                        "  Exp at least : 3\n" +
                        "  Line fields were: \n    "+String.join("\n    ", aLine)
                    );
                }

                fillCSVOptionalFields(aLine);
                String wordID = aLine.get(0);
                String origWord = aLine.get(2).toLowerCase();
                String correctedWord = aLine.get(3).toLowerCase();
                String comment = null;
                if (aLine.size() > 4) {
                    comment = aLine.get(4);
                }
                if (!origWord.equals(correctedWord) &&
                    (comment == null || comment.isEmpty())) {
                    this.warn(
                        "Invalid line in human revision file: "+csvFile+"\n"+
                        "The original word differed from the corrected one, but there was no comment\n"+
                        "Line: "+ StringUtils.join(aLine.iterator(), ","));
                } else if (origWord.equals(correctedWord) &&
                    (comment != null && !comment.isEmpty())) {
                    this.warn(
                            "Invalid line in human revision file: "+csvFile+"\n"+
                            "The original word was the same as the corrected one, but there was a comment\n"+
                                    "Line: "+ StringUtils.join(aLine.iterator(), ","));
                } else {
                    // Valid line
                    goldStandard.addCase(origWord, correctedWord, docID, revisor);
                }
            }
        }

        private void fillCSVOptionalFields(List<String> csvLine) {
            int lengthDiff = 6 - csvLine.size();
            for (int ii=0; ii < lengthDiff; ii++) {
                csvLine.add("");
            }
        }

        private void warn(String mess) {
            if (options.contains(GSReaderOption.WARN_CORRECTIONS_WITH_NO_COMMENT)) {
                System.out.println("WARNING: " + mess);
            }
        }

        protected Pair<String,String> idAndRevisorForFile(Path csvFile) {
            Pair<String,String> info = null;

            Matcher matcher = Pattern.compile("([^\\.]*)\\.([^\\.]*)\\.csv").matcher(csvFile.toString());
            if (matcher.matches()) {
                info = Pair.of(matcher.group(1), matcher.group(2));
            }
            return info;
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
        return read(gsRootDir, new GSReaderOption[0]);
    }


    public static SpellGoldStandard read(
        File gsRootDir, GSReaderOption... options) throws IOException {
        SpellGoldStandard gs = new SpellGoldStandard();

        CSVConsumer consumer = new CSVConsumer(gs, options);

        gsRootDir.toPath();
        Files.walk(Paths.get(gsRootDir.toString(), new String[]{}))
                .forEach(consumer);

        return gs;
    }

    private static void processCSVFile(Path csvPath) {
        System.out.println("Process csv file: "+csvPath);
    }
}
