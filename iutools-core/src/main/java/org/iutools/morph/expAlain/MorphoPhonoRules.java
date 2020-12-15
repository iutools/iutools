package org.iutools.morph.expAlain;

/**
 * This class defines the morpho-phonolical rules that transform the characters 
 * at the edges of two consecutive morphemes.
 * 
 * @author desilets
 *
 */
public class MorphoPhonoRules {

	private static MorphoPhonoRules _singleton = null;
	
	public static MorphoPhonoRules getInstance() {
		if (_singleton == null) {
			generateSingleton();
		}
		return _singleton;		
	}

	// Note: This method is synchronized to avoid two threads trying to 
	//  generate the singleton at the same time
	//
	private static synchronized void generateSingleton() {
		// Make sure another thread has not generated the singleton while we 
		// were waiting for this synchronized method to be 'released' by the 
		// other thread.
		//
		if (_singleton == null) {
			_singleton = new MorphoPhonoRules();
			
			// TODO: Benoit, this is where you would create the rules 
			//   Not sure what form those rules should take. If you 
			//   want we can pair program to figure that out.
			{
				
			}
		}
		
	}

	public boolean canJoin(WrittenMorpheme morph1, WrittenMorpheme morph2) {
		// TODO For now, we assume that anything can join with anythin
		return true;
	}

}
