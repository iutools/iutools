package ca.inuktitutcomputing.core.console;

import ca.pirurvik.iutools.QueryExpanderEvaluator;

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
		String compilationFilePath = getCompilationFile();
		String goldStandardCSVFilePath = getGoldStandardFile();
		boolean statsOverMorphemes = getStatsOverMorphemes()==null? false : true;
		
		QueryExpanderEvaluator evaluator = new QueryExpanderEvaluator(
				compilationFilePath, goldStandardCSVFilePath
				);
		evaluator.setOptionComputeStatsOverSurfaceForms(statsOverMorphemes);
		
		evaluator.run();		

	}
	
	
	
	

}
