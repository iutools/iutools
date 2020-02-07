package ca.pirurvik.iutools.search;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import ca.nrc.data.harvesting.BingSearchEngine;
import ca.nrc.data.harvesting.SearchEngine;
import ca.nrc.data.harvesting.SearchEngine.Query;
import ca.nrc.data.harvesting.SearchEngine.SearchEngineException;
import ca.nrc.data.harvesting.SearchEngineMultiQuery;
import ca.nrc.data.harvesting.SearchResults;

/**
 * A BingSearchEngine that is specifically configured to deal better 
 * with Inuktut searches
 * 
 * @author desilets
 *
 */
public class IUSearchEngine extends SearchEngineMultiQuery {

	public IUSearchEngine() throws IOException, SearchEngineException {
		super();
	}
	
	
	@Override
	public SearchResults search(Query query) throws SearchEngineException, IOException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.search.IUSearchEngine.search");
		
		if (query.lang != null && !query.lang.equals("iu")) {
			throw new SearchEngineException(
					this.getClass().getName()+
					" cannot process a query for language="+query.lang+
					".\nThis class of search engine can only process Inuktut queries (iu)");
		}
		return super.search(query);
	}
}
