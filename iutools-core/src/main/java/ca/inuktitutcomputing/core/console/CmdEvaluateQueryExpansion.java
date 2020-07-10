package ca.inuktitutcomputing.core.console;

import ca.pirurvik.iutools.MorphRelativesFinderEvaluator;

public class CmdEvaluateQueryExpansion extends ConsoleCommand {

	public CmdEvaluateQueryExpansion(String name) {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Evaluate the expansions returned for the words in the Gold Standard.";
	}

	@Override
	public void execute() throws Exception {
		String compilationFilePath = getCorpusSavePath();
		String goldStandardCSVFilePath = getGoldStandardFile();
		boolean statsOverMorphemes = getStatsOverMorphemes()==null? false : true;
		
		MorphRelativesFinderEvaluator evaluator = new MorphRelativesFinderEvaluator(
				compilationFilePath, goldStandardCSVFilePath
				);
		evaluator.setOptionComputeStatsOverSurfaceForms(statsOverMorphemes);
		
		evaluator.run();		

	}
	
	
	
	

}
