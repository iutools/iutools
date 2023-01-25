package org.iutools.cli;

import ca.nrc.ui.commandline.CommandLineException;

public class CmdGlossariesSummary extends ConsoleCommand {

    public CmdGlossariesSummary(String name) throws CommandLineException {
        super(name);
    }

    @Override
    public String getUsageOverview() {
        return "Summarize the content of various glossaries";
    }

    @Override
    public void execute() throws Exception {
        throw new RuntimeException("This command is not implemented yet.");
    }
}
