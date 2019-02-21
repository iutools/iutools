package ca.inuktitutcomputing.core;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;

public class QueryExpanderEvaluatorTest {

	@Test(expected=FileNotFoundException.class)
	public void test__QueryExpanderEvaluator__Synopsis() throws Exception {
		
		String compiledCorpusTrieFilePath = "/path/to/json/file/of/compiled/corpus";
		String goldStandardCSVFilePath = "/path/to/gold/standard/csv/file";
		QueryExpanderEvaluator evaluator = 
			new QueryExpanderEvaluator(compiledCorpusTrieFilePath,goldStandardCSVFilePath);
		// if statistics are to be computed over morphemes instead of words:
		evaluator.setOptionComputeStatsOverSurfaceForms(true);
		evaluator.run();
	}
	
	@Test
	public void test__run__stats_compiled_over_surface_forms_of_words() throws Exception {
		// compile some corpus
		CompiledCorpus compiledCorpus =
			getACompiledCorpus(new String[] {
			"takujumaguvit:1",
			"takujumajunga:16","takujumammata:11","takugumagatta:11","takugumavugut:10",
			
			"qaujisaqtikkut:2",
			"qaujisaqtiit:36","qaujisaqtiujut:21","qaujisaqti:16","qaujisaqtinu:15",
			
			"mikinniqsanut:38",
			"mikinniqsanulluunniit:2","mikinniqsanulli:1","mikinniqsait:58","mikiniqsani:40",
	        
	        "akigilirajaqtuniullu:1","akigijangit:29","akigijaujut:12",
	        "akigijanginnit:10","akigijanga:10",
	        
	        "tussiraummut:6","tussirautiit:212","tutsirautinik:87","tussiraut:59",
	        "tuksirautaujut:47",
	        
	        "atuqtauniaqput:12","atuqtauniaqtut:60","atuqtauniaqtunik:39",
	        "atuqtauniarmangaata:34","atuqtauniarninginnut:22"		
		});
		String[] csvGoldStandardLines = new String[] {
"Mot original (fréquence Google du mot en syllabique),"+
"Mot original en syllabique,"+
"Traduction,"+
"\"Reformulations trouvées\","+
"\"Meilleures Reformulations\","+
"\"Syllabique des refromulations avec #hits Google\","+
"\"Traduction des meilleures Reformulations\","+
"Notes",

"takujumaguvit (191),ᑕᑯᔪᒪᒍᕕᑦ,if you would want/like to see,1,"+
"\"takujumajuq;takujumajut\",\"ᑕᑯᔪᒪᔪᖅ (1);" + 
"ᑕᑯᔪᒪᔪᑦ (49)\",,\"used for « for more information », i.e. if you want to see more information…\"",

"qaujisaqtikkut (98),"+
"ᖃᐅᔨᓴᖅᑎᒃᑯᑦ,"+
"research institute/council,"+
"0,"+
","+
","+
","+
"X:qauji/to know s.t. – Y:saq/to work through prolonged action to achieve X – ti/he who Y – kkut/group",

"mikinniqsanut (1660),"+
"ᒥᑭᓐᓂᖅᓴᓄᑦ,"+
"to/for elementary (school) / smaller (communities),"+
"1,"+
"\"mikinniqsaq;mikinniqsait;mikiniqsaq;mikiniqsait\","+
"\"ᒥᑭᓐᓂᖅᓴᖅ (2);ᒥᑭᓐᓂᖅᓴᐃᑦ (27);ᒥᑭᓂᖅᓴᖅ (19);ᒥᑭᓂᖅᓴᐃᑦ (30)\","+
","+
"\"-nut\nX-niqsaq: comparison; more X\nmikit- to be small\"",

"akigiliutinajaqtuq (1),ᐊᑭᒋᓕᐅᑎᓇᔭᖅᑐᖅ,cost (what it would cost),1,akinga,ᐊᑭᖓ (781),its cost,exchange value-to have as-on going-action for s.o.-would-it",

"tuksiraummut (9),ᑐᒃᓯᕋᐅᒻᒧᑦ,proposal,1,\"tuksiraut;tuksirautiit\",\"ᑐᒃᓯᕋᐅᑦ (1380);" + 
"ᑐᒃᓯᕋᐅᑏᑦ (406)\",application (as in application form)/request/proposal/,\"-mut tuksiraummut : to ask for something–action made by steps or prolonged action–object, means for\"",

"atuqtauniarput (2),ᐊᑐᖅᑕᐅᓂᐊᕐᐳᑦ,they will be used,1,\"atuqtaujut;atuqtauvut\",\"ᐊᑐᖅᑕᐅᔪᑦ (856);" + 
"ᐊᑐᖅᑕᐅᕗᑦ (6)\",resources/use/usage"

		};
		QueryExpanderEvaluator evaluator = new QueryExpanderEvaluator();
		evaluator.setCompiledCorpus(compiledCorpus);
		evaluator.setGoldStandard(csvGoldStandardLines);
		evaluator.run();
		
		int nbTotalExpansionsFromCorpus = 30;
		int nbTotalExpansionsNotInGSAlternatives = 26;
		int nbTotalGoldStandardAlternatives = 17;
		
        int nbTotalGoodExpansions = nbTotalExpansionsFromCorpus - nbTotalExpansionsNotInGSAlternatives;
        
        float precision_expected = (float)nbTotalGoodExpansions / (float)nbTotalExpansionsFromCorpus;
        float recall_expected = (float)nbTotalGoodExpansions / (float)nbTotalGoldStandardAlternatives;
        float fmeasure_expected = 2 * precision_expected * recall_expected / (precision_expected + recall_expected);
        
        assertEquals(precision_expected,evaluator.precision,"The precision is not correct.");
        assertEquals(recall_expected,evaluator.recall,"The recall is not correct.");
        assertEquals(fmeasure_expected,evaluator.fmeasure,"The f-measure is not correct.");
	}

	
	@Test
	public void test__run__stats_compiled_over_morphemes() throws Exception {
		// compile some corpus
		CompiledCorpus compiledCorpus =
			getACompiledCorpus(new String[] {
			"takujumaguvit:1",
			"takujumajunga:16","takujumammata:11","takugumagatta:11","takugumavugut:10",
			
			"qaujisaqtikkut:2",
			"qaujisaqtiit:36","qaujisaqtiujut:21","qaujisaqti:16","qaujisaqtinu:15",
			
			"mikinniqsanut:38",
			"mikinniqsanulluunniit:2","mikinniqsanulli:1","mikinniqsait:58","mikiniqsani:40",
	        
	        "akigilirajaqtuniullu:1","akigijangit:29","akigijaujut:12",
	        "akigijanginnit:10","akigijanga:10",
	        
	        "tussiraummut:6","tussirautiit:212","tutsirautinik:87","tussiraut:59",
	        "tuksirautaujut:47",
	        
	        "atuqtauniaqput:12","atuqtauniaqtut:60","atuqtauniaqtunik:39",
	        "atuqtauniarmangaata:34","atuqtauniarninginnut:22"		
		});
		String[] csvGoldStandardLines = new String[] {
"Mot original (fréquence Google du mot en syllabique),"+
"Mot original en syllabique,"+
"Traduction,"+
"\"Reformulations trouvées\","+
"\"Meilleures Reformulations\","+
"\"Syllabique des refromulations avec #hits Google\","+
"\"Traduction des meilleures Reformulations\","+
"Notes",

"takujumaguvit (191),ᑕᑯᔪᒪᒍᕕᑦ,if you would want/like to see,1,"+
"\"takujumajuq;takujumajut\",\"ᑕᑯᔪᒪᔪᖅ (1);" + 
"ᑕᑯᔪᒪᔪᑦ (49)\",,\"used for « for more information », i.e. if you want to see more information…\"",

"qaujisaqtikkut (98),"+
"ᖃᐅᔨᓴᖅᑎᒃᑯᑦ,"+
"research institute/council,"+
"0,"+
","+
","+
","+
"X:qauji/to know s.t. – Y:saq/to work through prolonged action to achieve X – ti/he who Y – kkut/group",

"mikinniqsanut (1660),"+
"ᒥᑭᓐᓂᖅᓴᓄᑦ,"+
"to/for elementary (school) / smaller (communities),"+
"1,"+
"\"mikinniqsaq;mikinniqsait;mikiniqsaq;mikiniqsait\","+
"\"ᒥᑭᓐᓂᖅᓴᖅ (2);ᒥᑭᓐᓂᖅᓴᐃᑦ (27);ᒥᑭᓂᖅᓴᖅ (19);ᒥᑭᓂᖅᓴᐃᑦ (30)\","+
","+
"\"-nut\nX-niqsaq: comparison; more X\nmikit- to be small\"",

"akigiliutinajaqtuq (1),ᐊᑭᒋᓕᐅᑎᓇᔭᖅᑐᖅ,cost (what it would cost),1,akinga,ᐊᑭᖓ (781),its cost,exchange value-to have as-on going-action for s.o.-would-it",

"tuksiraummut (9),ᑐᒃᓯᕋᐅᒻᒧᑦ,proposal,1,\"tuksiraut;tuksirautiit\",\"ᑐᒃᓯᕋᐅᑦ (1380);" + 
"ᑐᒃᓯᕋᐅᑏᑦ (406)\",application (as in application form)/request/proposal/,\"-mut tuksiraummut : to ask for something–action made by steps or prolonged action–object, means for\"",

"atuqtauniarput (2),ᐊᑐᖅᑕᐅᓂᐊᕐᐳᑦ,they will be used,1,\"atuqtaujut;atuqtauvut\",\"ᐊᑐᖅᑕᐅᔪᑦ (856);" + 
"ᐊᑐᖅᑕᐅᕗᑦ (6)\",resources/use/usage"

		};
		QueryExpanderEvaluator evaluator = new QueryExpanderEvaluator();
		evaluator.setCompiledCorpus(compiledCorpus);
		evaluator.setGoldStandard(csvGoldStandardLines);
        evaluator.setOptionComputeStatsOverSurfaceForms(false);
		evaluator.run();
		
		int nbTotalExpansionsFromCorpus = 30;
		int nbTotalExpansionsNotInGSAlternatives = 22;
		int nbTotalGoldStandardAlternatives = 17;
		
        int nbTotalGoodExpansions = nbTotalExpansionsFromCorpus - nbTotalExpansionsNotInGSAlternatives;
        
        float precision_expected = (float)nbTotalGoodExpansions / (float)nbTotalExpansionsFromCorpus;
        float recall_expected = (float)nbTotalGoodExpansions / (float)nbTotalGoldStandardAlternatives;
        float fmeasure_expected = 2 * precision_expected * recall_expected / (precision_expected + recall_expected);
        
        assertEquals(precision_expected,evaluator.precision,"The precision is not correct.");
        assertEquals(recall_expected,evaluator.recall,"The recall is not correct.");
        assertEquals(fmeasure_expected,evaluator.fmeasure,"The f-measure is not correct.");
 	}

	

	// ---------------
	
	private CompiledCorpus getACompiledCorpus(String[] entries) throws Exception {
		File dir = new File(IUConfig.getIUDataPath()+"src/test/temp");
		dir.mkdir();
		String corpusDir = dir.getAbsolutePath();
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				new File(corpusDir+"/corpusText.txt")));
		for (String entry : entries) {
			String[] wordFreq = entry.split(":");
			for (int i=0; i<Integer.parseInt(wordFreq[1]); i++)
				bw.write(wordFreq[0]+" ");
		}
		bw.close();
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.compileCorpusFromScratch(corpusDir);
        FileUtils.deleteDirectory(dir);
        return compiledCorpus;
	}

}
