package ca.pirurvik.iutools.spellchecker;

import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.pirurvik.iutools.corpus.CompiledCorpusException;
import ca.pirurvik.iutools.corpus.CompiledCorpus_ES;

public class SpellChecker_ES extends SpellChecker {

    private static final String DEFAULT_CHECKER_INDEX = "spell_checker";

    protected String esIndexNameRoot = null;

    public SpellChecker_ES() throws StringSegmenterException, SpellCheckerException {
        super();
        init_SpellChecker_ES(null);
    }

    public SpellChecker_ES(String _checkerIndexName) throws StringSegmenterException, SpellCheckerException {
        super();
        init_SpellChecker_ES(_checkerIndexName);
    }

    private void init_SpellChecker_ES(String _checkerIndexName) throws SpellCheckerException {
        if (_checkerIndexName == null) {
            _checkerIndexName = DEFAULT_CHECKER_INDEX;
        }

        esIndexNameRoot = _checkerIndexName;

        try {
            corpus = new CompiledCorpus_ES(corpusIndexName());
            explicitlyCorrectWords =
                new CompiledCorpus_ES(
                    explicitlyCorrectWordsIndexName());
        } catch (CompiledCorpusException e) {
            throw new SpellCheckerException(e);
        }

        return;
    }

    protected String explicitlyCorrectWordsIndexName() {
        return esIndexNameRoot+"_EXPLICLTY_CORRECT";
    }

    protected String corpusIndexName() {
        return esIndexNameRoot;
    }

//    private void loadExplicitlyCorrectNumericalWords() throws SpellCheckerException{
//        File jsonFile = null;
//        try {
//            jsonFile =
//                new File(
//                    IUConfig.getIUDataPath("data/numericTermsCorpus.json"));
//
//            String jsonContent =
//                new String(
//                    Files.readAllBytes(jsonFile.toPath()));
//            Map<String,Object> obj = new HashMap<String, Object>();
//            obj = new ObjectMapper().readValue(jsonContent, obj.getClass());
//            Map<String,Long> expressions = (Map<String, Long>) obj.get("expressions");
//
//            int count = 0;
//            boolean oneExprWasAlreadyInIndex = false;
//
//            for (Map.Entry<String,Long> anExpression: expressions.entrySet()) {
//                String word = anExpression.getKey();
//                String[][] sampleDecomps = new String[][]{new String[]{word}};
//                Integer totalDecomps = 1;
//                Object freqTypeUnknown = anExpression.getValue();
//                Long freq = new Long(1);
//                if (freqTypeUnknown != null) {
//                    if (freqTypeUnknown instanceof Long) {
//                        freq = (Long) freqTypeUnknown;
//                    } else if (freqTypeUnknown instanceof Integer) {
//                        freq = Long.valueOf(((Integer) freqTypeUnknown).intValue());
//                    }
//                }
//                System.out.println("--** loadExplicitlyCorrectNumericalWords: looking at numerical word="+word+"("+count+" of "+expressions.size()+")");
//                WordInfo winfo = corpus.info4word(word);
//                if (winfo != null) {
//                    if (count != 0) {
//                        throw new SpellCheckerException(
//                            "The ElasticSearch index contains some, but not all of the known numerical expressions");
//                    }
//
//                    // TODO-Oct2020: We should have a better way of checking if
//                    // the numerical expressions have already been loaded
//                    //
//
//                    // The very first numerical expression we tried to add was
//                    // already in the ES index. That PROBABLY means that they
//                    // all have already been loaded
//                    break;
//                }
//                System.out.println("--** loadExplicitlyCorrectNumericalWords: LOADING absent numerical word="+word);
//                corpus.addWordOccurence(word, sampleDecomps, totalDecomps, freq);
//                count++;
//            }
//        } catch (ConfigException | IOException | CompiledCorpusException e) {
//            throw new SpellCheckerException(e);
//        }
//
//        return;
//    }

}
