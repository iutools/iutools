package ca.pirurvik.iutools.console;

public class InvalidArgumentConsoleException extends ConsoleException {
	public InvalidArgumentConsoleException(String mess) {
		//super(mess, exc);
		super(mess);
	}
}
