package org.iutools.cli;

import ca.nrc.ui.commandline.CommandLineException;
import org.iutools.morphrelatives.MorphRelativesFinderEvaluator;

public class CmdEvaluateQueryExpansion extends ConsoleCommand {

	public CmdEvaluateQueryExpansion(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Evaluate the expansions returned for the words in the Gold Standard.";
	}

	@Override
	public void execute() throws Exception {
		String goldStandardCSVFilePath = getGoldStandardFile();
		boolean statsOverMorphemes = getStatsOverMorphemes()==null? false : true;
		
		MorphRelativesFinderEvaluator evaluator =
			new MorphRelativesFinderEvaluator(goldStandardCSVFilePath);
		evaluator.setOptionComputeStatsOverSurfaceForms(statsOverMorphemes);
		
		evaluator.run();		

	}
	
	
	
	

}
