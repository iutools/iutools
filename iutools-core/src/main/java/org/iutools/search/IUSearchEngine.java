package org.iutools.search;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	public IUSearchEngine(String _bingKey) throws IOException, SearchEngineException {
		super(_bingKey);
	}
	
	
	@Override
	public SearchResults search(Query query) throws SearchEngineException, IOException {
		Logger tLogger = LogManager.getLogger("org.iutools.search.IUSearchEngine.search");
		
		if (query.lang != null && !query.lang.equals("iu")) {
			throw new SearchEngineException(
					this.getClass().getName()+
					" cannot process a query for language="+query.lang+
					".\nThis class of search engine can only process Inuktut queries (iu)");
		}
		return super.search(query);
	}
}
