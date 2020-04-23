package ca.pirurvik.iutools.spellchecker;


/** 
 * This class represents an example used for training or testing
 * a binary classifier.
 * 
 * @author desilets
 *
 */
public class BinaryClassifierExample {
	
	/**
	 * The input to be fed to the classifier.
	 */
	String input = null;
	
	/**
	 * The category we expect the classifier to output for this
	 * example.
	 */
	String corectCategory = null;
	
	/**
	 * The category that the classifier outputed for this example.
	 */
	String predictedCategory = null;
	
	public BinaryClassifierExample(String _input) {
		this.input = _input;
	}
	
}
