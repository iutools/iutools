package org.iutools.cli;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.ui.commandline.CommandLineException;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.tm.TMEvaluationCase;
import org.iutools.concordancer.tm.TMFactory;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.config.IUConfig;
import org.iutools.morphrelatives.MorphRelativesFinder;
import org.iutools.morphrelatives.MorphRelativesFinderException;
import org.iutools.morphrelatives.MorphologicalRelative;
import org.iutools.script.TransCoder;
import org.iutools.text.IUWord;
import org.iutools.text.WordException;
import org.iutools.worddict.Glossary;
import org.iutools.worddict.GlossaryEntry;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.Pattern;

public class CmdGenerateTMEvaluationData extends ConsoleCommand {

    File outputDir = null;
    private File casesFile = null;
    private FileWriter tmCasesWriter = null;
    private File sentencesFile = null;
    private FileWriter sentencesWriter = null;

    ObjectMapper mapper = new ObjectMapper();

    TranslationMemory tm = null;

    MorphRelativesFinder relativesFinder = null;

    Set<String> alreadySeenAlignments = new HashSet<>();
    public CmdGenerateTMEvaluationData(String name) throws CommandLineException {
        super(name);
        try {
            tm = TMFactory.makeTM();
            relativesFinder = new MorphRelativesFinder();
        } catch (TranslationMemoryException | MorphRelativesFinderException e) {
            throw new CommandLineException(e);
        }
    }

    @Override
    public String getUsageOverview() {
        return "Generate data for evaluating the Translation Memory (and the MachineGeneratedDict)";
    }
    @Override
    public void execute() throws Exception {
        openFiles();
        try {
            writeCaseFileHeaders();

            Glossary gloss = loadAllGlossaryFiles();
            echo("Generating TM evaluation data in directory: "+outputDir);
            generateData(gloss);
        } finally {
            closeFiles();
        }
        echo("Evaluation terms printed to: "+casesFile);
        echo("Sentence pairs printed to: "+sentencesFile);
        echo("DONE");
    }

    private void closeFiles() throws Exception {
        tmCasesWriter.close();
        sentencesWriter.close();
    }

    private void generateData(Glossary gloss) throws Exception {
        Set<IUWord> iuSingleWords = allIUWords(gloss);
        generateTermsData(iuSingleWords, gloss);
        generateSentences(iuSingleWords);
    }

    private Set<IUWord> allIUWords(Glossary gloss) throws Exception {
        Set<IUWord> words = new HashSet<>();
        for (String iuTerm: gloss.allTerms("iu")) {
            if (iuTerm.contains(" ")) {
                continue;
            }
            IUWord iuWord = null;
            try {
                iuWord = new IUWord(iuTerm);
            } catch (WordException e) {
                // There was something wrong with the term. Just leave iuWord at null
                // so it won't be added.
            }
            if (iuWord != null) {
                words.add(iuWord);
            }
        }

        return words;
    }

    private void generateTermsData(Set<IUWord> iuWords, Glossary gloss) throws Exception {
        String mess = "Generating data for IU terms";
        echo(mess);
        ProgressMonitor_Terminal progress =
            new ProgressMonitor_Terminal(iuWords.size(), mess, 30);
        for (IUWord word : iuWords) {
//            System.out.println("  Generating data for term="+word);
            writeTMCase(word, gloss);
            progress.stepCompleted();
        }

    }

    private void writeTMCase(IUWord word, Glossary gloss) throws Exception {
        String term = word.inRoman();
        TMEvaluationCase newCase = new TMEvaluationCase(word);
//                System.out.println("--** CmdGenerateTMEvaluationData.execute: term="+term);
        List<GlossaryEntry> entries = gloss.entries4word("iu", term);
        for (GlossaryEntry anEntry : entries) {
            List<String> enTerms = anEntry.termsInLang("en");
            newCase
                .addDialects(anEntry.dialects)
                .addSource(anEntry.source)
                .addEnEquilvalents(anEntry.termsInLang("en"))
            ;
        }
        writeCase(newCase);
    }

    private void generateSentences(Set<IUWord> iuWords) throws Exception {
        Pattern patt = allTermsPattern(iuWords);
        File[] alignmentFiles = allTMFiles();
        long totalAlignments = countAlignmentsInFiles(alignmentFiles);
        ProgressMonitor_Terminal progress = new ProgressMonitor_Terminal(totalAlignments, "Generating evaluation sentence pairs", 30);
        
        for (File aFile: alignmentFiles) {
           ObjectStreamReader reader = new ObjectStreamReader(aFile);
           Alignment alignment = (Alignment) reader.readObject();
           while (alignment != null) {
               progress.stepCompleted();
               possiblyWriteSentencePair(alignment, patt);
               alignment = (Alignment) reader.readObject();
           }
        }
    }

    private long countAlignmentsInFiles(File[] alignmentFiles) throws Exception {
        echo("Counting total number of alignments (may take a while)...");
        long total = 0;
        for (File aFile: alignmentFiles) {
           ObjectStreamReader reader = new ObjectStreamReader(aFile);
           Object line = reader.readObject();
           while (line != null) {
               total++;
               line = reader.readObject();
           }
        }
        return total;
    }

    private File[] allTMFiles() throws Exception {
        File[] files = new File[]{
            new File(IUConfig.dataFilePath("data/translation-memories/nrc-nunavut-hansard.tm.json")),
        };
        return files;
    }

    private Pattern allTermsPattern(Set<IUWord> iuWords) {
        String regex = "";
        boolean first = true;
        for (IUWord aWord: iuWords) {
            String syllWord = aWord.inSyll();
            if (syllWord != null && !syllWord.isEmpty()) {
                if (!first) {
                    regex += "|";
                }
                first = false;
                regex += syllWord;

            }
        }
        regex = "("+regex+")";
//        System.out.println("--** allTermsPattern: regex="+regex);
        Pattern patt = Pattern.compile(regex);
        return patt;
    }

    private void possiblyWriteSentencePair(Alignment alignment, Pattern filter) throws Exception {
        boolean printIt = true;
        System.out.println("--** possiblyWriteSentencePair: alignment="+alignment);
        if (alreadyPrinted(alignment)) {
            System.out.println("--** possiblyWriteSentencePair: already printed");
            printIt = false;
        }

        if (printIt) {
            String iuSentence = alignment.sentence4lang("iu");
            if (!filter.matcher(iuSentence).find()) {
                printIt = false;
            }
        }

        if (printIt) {
//            System.out.println("--** possiblyWriteSentencePair: printing the sentence");
            String iuSent = alignment.sentences.get("iu");
            alignment.sentences.put("iu_roman", TransCoder.ensureRoman(iuSent));
            alignment.sentences.put("iu_syll", TransCoder.ensureSyllabic(iuSent));
            String json = mapper.writeValueAsString(alignment.sentences);
            sentencesWriter.write(json + "\n");
        }
    }


    private boolean alreadyPrinted(Alignment alignment) {
        String alignHash = hash4aligment(alignment);
        boolean answer = alreadySeenAlignments.contains(alignHash);
        alreadySeenAlignments.add(alignHash);
        return answer;
    }

    private String hash4aligment(Alignment alignment) {
        String md5Hex = DigestUtils.md5Hex(alignment.toString()).toUpperCase();
        return md5Hex;
    }

    private String[] variantsForTerm(String term) throws Exception {
        MorphologicalRelative[] relatives = relativesFinder.findRelatives(term);
        String[] variants = new String[relatives.length+1];
        variants[0] = term;
        for (int ii=0; ii < relatives.length; ii++) {
            variants[ii+1] = relatives[ii].getWord();
        }
        return variants;
    }

//    private void generateTermCase(String term, Glossary gloss) throws Exception {
//        TMEvaluationCase newCase = new TMEvaluationCase(term);
////                System.out.println("--** CmdGenerateTMEvaluationData.execute: term="+term);
//        List<GlossaryEntry> entries = gloss.entries4word("iu", term);
//        for (GlossaryEntry anEntry : entries) {
//            List<String> enTerms = anEntry.termsInLang("en");
//            newCase
//                .addDialects(anEntry.dialects)
//                .addSource(anEntry.source)
//                .addEnEquilvalents(anEntry.termsInLang("en"))
//            ;
//        }
//        writeCase(newCase);
//
//    }

    private void writeCaseFileHeaders() throws Exception {
        tmCasesWriter.write(
        "class=org.iutools.concordancer.tm.TMEvaluationCase\n" +
            "bodyEndMarker=NEW_LINE\n\n");

        sentencesWriter.write(
                "class=java.util.HashMap\n" +
                        "bodyEndMarker=NEW_LINE\n\n");
    }

    private void writeCase(TMEvaluationCase newCase) throws Exception {
        if (!newCase.enEquivalents.isEmpty()) {
            String json = mapper.writeValueAsString(newCase);
            tmCasesWriter.write(json+"\n");
        }
    }

    private void openFiles() throws Exception {
        outputDir = new File(new IUConfig().dataFilePath("data/translation-memories/testdata"));
        this.casesFile = new File(outputDir, "tmCases.json");
        this.tmCasesWriter = new FileWriter(casesFile);
        this.sentencesFile = new File(outputDir, "sentencePairs.json");
        this.sentencesWriter = new FileWriter(sentencesFile);
    }

    private Glossary loadAllGlossaryFiles() throws Exception {
        echo("Loading glossary files (this may take a while)...");
        Glossary gloss = new Glossary();
        for (File glossFile: glossFilesToLoad()) {
            gloss.loadFile(glossFile);
        }
        echo("DONE: Loading glossary files");
        return gloss;
    }

    private static File[] glossFilesToLoad() throws Exception {
		String[] fileNames = new String[] {
			"Dorais 1978",
			"EDU 2000 (rev. 2019)",
			"NAC Kadlun-Jone & Angalik (1996)",
			"NAC Kublu (2005)",
			"SCHNEIDER",
			"tusaalanga",
			"iutools-loanWords",
			"iutools-locations",
			"wpGlossary",
		};
		File[] files = new File[fileNames.length];
		for (int ii=0; ii < files.length; ii++) {
            files[ii] = new IUConfig().glossaryFPath(fileNames[ii]+".gloss.json").toFile();
		}
		return files;
	}

}
