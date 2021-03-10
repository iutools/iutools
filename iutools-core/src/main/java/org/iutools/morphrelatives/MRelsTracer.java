package org.iutools.morphrelatives;

import ca.nrc.config.Config;
import ca.nrc.config.ConfigException;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.string.StringUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.iutools.corpus.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MRelsTracer {

    protected static String[] _relsToTrack =  null;
    static CompiledCorpus _corpus = null;

    protected static String[] relsToTrack() throws MorphRelativesFinderException {
        if (_relsToTrack == null) {
            String propName = "org.iutools.morphrelatives.relsToTrack";
            String[] defVal = new String[0];
            try {
                _relsToTrack = Config.getConfigProperty(propName, defVal);
            } catch (ConfigException e) {
                throw new MorphRelativesFinderException(e);
            }
        }
        return _relsToTrack;
    }

    public static void traceRelatives(Logger tLogger,
        MorphologicalRelative[] relatives, String message) throws MorphRelativesFinderException {
        if (tLogger.isTraceEnabled()) {
            message += "\n--   Total relatives at this point: "+relatives.length;

            String[] relsToTrack = relsToTrack();
            if (relsToTrack.length == 0) {
                // Print all relatives collected so far
                message += allRelsPrintout(relatives);
            } else {
                // Only print the status of relatives we are tracking
                message += relsBeingTrackedPrintout(relsToTrack, relatives);
            }

            tLogger.trace(message);
        }
    }

    public static void traceRelatives(Logger tLogger, Set<MorphologicalRelative> relatiesSet,
        String message) throws MorphRelativesFinderException {
        if (tLogger.isTraceEnabled()) {
            List<MorphologicalRelative> relatives = new ArrayList<MorphologicalRelative>();
            relatives.addAll(relatiesSet);
            relatives.sort(
                    (MorphologicalRelative e1, MorphologicalRelative e2) -> {
                        return e1.getWord().compareTo(e2.getWord());
                    }
            );
            MorphologicalRelative[] relativesArr = relatives.toArray(new MorphologicalRelative[0]);
            traceRelatives(tLogger, relativesArr, message);
        }
    }

    protected static void traceRelatives(Logger logger,
        List<MorphologicalRelative> relatives, String message) throws MorphRelativesFinderException {
        if (logger.isTraceEnabled()) {
            MorphologicalRelative[] relativesArr = null;
            if (relatives != null) {
                relativesArr = relatives.toArray(new MorphologicalRelative[0]);
            }
            traceRelatives(logger, relativesArr, message);
        }
    }

    private static String allRelsPrintout(MorphologicalRelative[] relatives) {
        String message = "\nRelatives collected so far:\n   ";
        message += printoutRelatives(null, relatives);
        return message;
    }

    private static String relsBeingTrackedPrintout(String[] relsToTrack,
        MorphologicalRelative[] relatives) {
        String printout =
            "\n--   The following tracked relatives are PRESENT in the list of relatives collected so far:"+
            "\n--      ";
        printout += printoutRelatives(relsToTrack, relatives);

        List<String> missingRels = new ArrayList<String>();
        for (String aRelToTrace : relsToTrack) {
            boolean isMissing = true;
            for (MorphologicalRelative aFoundRel : relatives) {
                if (aFoundRel.getWord().equals(aRelToTrace)) {
                    isMissing = false;
                    break;
                }
            }
            if (isMissing) {
                missingRels.add(aRelToTrace);
            }
        }

        if (!missingRels.isEmpty()) {
            printout +=
                "\n--   The list of relatives was missing the following:\n"+
                "--      [" +
                StringUtils.join(missingRels.iterator(), ", ")+"]";
        } else {
            printout +=
                "\n--   NOTHING MISSING from the the list of list of relatives.";
        }
        return printout;
    }

    private static String printoutFoundRelative(MorphologicalRelative aRelative) {
        String printout = aRelative.getWord();
        long freq = aRelative.getFrequency();
        if (freq >= 0) {
            printout += ":f="+freq;
        }

        List<String> commonMorphemes = aRelative.morphemesInCommon();
        if (commonMorphemes != null) {
            printout += ":c="+commonMorphemes.size();
        }

        return printout;
    }

    private static String printoutRelatives(String[] toPrint,
        MorphologicalRelative[] amongRelatives) {
        String printout = "[";
        for (MorphologicalRelative aRelative: amongRelatives) {
            if (toPrint == null ||
                ArrayUtils.contains(toPrint, aRelative.getWord())) {
                printout += printoutFoundRelative(aRelative);
                printout += ", ";
            }
        }
        printout += "]";
        return printout;
    }

    public static void traceRelative(
        Logger tLogger, MorphologicalRelative aRelative, String mess)
        throws MorphRelativesFinderException {
        if (tLogger.isTraceEnabled()) {
            String word = aRelative.getWord();
            if (ArrayUtils.contains(relsToTrack(), word)) {
                tLogger.trace(mess+"\n--    word="+word);
            }
        }
    }

    protected static CompiledCorpus corpus() throws MorphRelativesFinderException {
        if (_corpus == null) {
            try {
                _corpus = new CompiledCorpusRegistry().getCorpus();
            } catch (CompiledCorpusRegistryException |CompiledCorpusException e) {
                throw new MorphRelativesFinderException(e);
            }
        }
        return _corpus;
    }

    public static void traceTrackedRels(Logger logger) throws MorphRelativesFinderException {
        traceTrackedRels(logger, "");
    }

    public static void traceTrackedRels(Logger logger, String mess)
        throws MorphRelativesFinderException {
        if (logger.isTraceEnabled()) {
            mess += "\n--    Relatives being tracked are:";
            // Print the Winfo of all words being tracked.
            String[] words = relsToTrack();
            for (String aWord: words) {
                try {
                    WordInfo winfo = corpus().info4word(aWord);
                    String winfoStr = "\n"+PrettyPrinter.print(winfo);
                    winfoStr =
                        winfoStr.replaceAll("\n", "\n--        ");
                    mess += winfoStr;
                } catch (CompiledCorpusException e) {
                    throw new MorphRelativesFinderException(e);
                }
            }
            logger.trace(mess);
        }
    }
}
