package ca.inuktitutcomputing.data;

import java.util.concurrent.TimeoutException;

public class LinguisticDataSingleton {
	
		private static String dataSource;
		private static LinguisticDataSingleton instance;

	    /** Constructeur privé 
	     * @throws LinguisticDataException 
	     * @throws TimeoutException */  
	    private LinguisticDataSingleton() throws LinguisticDataException {
//	    	System.out.println("initialize linguistic database "+dataSource);
			LinguisticDataAbstract.init(dataSource);
	    }
	     
	    /** Holder 
	     * @throws LinguisticDataException */
	    /* Cette technique du Holder joue sur le fait que la classe interne ne sera chargée 
	     * en mémoire que lorsque l'on y fera référence pour la première fois, c'est-à-dire
	     * lors du premier appel de "getInstance()" sur la classe Singleton. Lors de son 
	     * chargement, le Holder initialisera ses champs statiques et créera donc l'instance 
	     * unique du Singleton. */
//	    private static class SingletonHolder
//	    {       
//	        /** Instance unique non préinitialisée */
//	        private final static LinguisticDataSingleton instance = new LinguisticDataSingleton();
//	    }
//	 
//	    /** Point d'accès pour l'instance unique du singleton */
	    public static LinguisticDataSingleton getInstance(String _dataSource) throws LinguisticDataException
	    {
	    	dataSource = _dataSource;
	    	if (instance == null) {
	    		instance = new LinguisticDataSingleton();
	    	}
	        return instance;
	    }
	}
