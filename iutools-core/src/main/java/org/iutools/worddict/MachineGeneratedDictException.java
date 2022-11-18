package org.iutools.worddict;

public class MachineGeneratedDictException extends Exception {
	public MachineGeneratedDictException(String mess, Exception e) {
		super(mess, e);
	}
	public MachineGeneratedDictException(String mess) {
		super(mess);
	}
	public MachineGeneratedDictException(Exception e) {
		super(e);
	}
}
