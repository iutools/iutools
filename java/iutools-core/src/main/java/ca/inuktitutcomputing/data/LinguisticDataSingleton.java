package ca.inuktitutcomputing.data;

public class LinguisticDataSingleton {
	
		private static String dataSource;

	    /** Constructeur privé */  
	    private LinguisticDataSingleton() {
			LinguisticDataAbstract.init(dataSource);
	    }
	     
	    /** Holder */
	    private static class SingletonHolder
	    {       
	        /** Instance unique non préinitialisée */
	        private final static LinguisticDataSingleton instance = new LinguisticDataSingleton();
	    }
	 
	    /** Point d'accès pour l'instance unique du singleton */
	    public static LinguisticDataSingleton getInstance(String _dataSource)
	    {
	    	dataSource = _dataSource;
	        return SingletonHolder.instance;
	    }
	}
