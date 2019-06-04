package ca.pirurvik.iutools;

public class SpellCheckerException extends Exception {

	public SpellCheckerException(Exception e) { super(e); }

	public SpellCheckerException(String mess) { super (mess); }

	public SpellCheckerException(String mess, Exception e) { super(mess, e); }
}
