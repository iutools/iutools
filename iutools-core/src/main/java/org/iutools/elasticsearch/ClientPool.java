package org.iutools.elasticsearch;

import ca.nrc.dtrc.elasticsearch.ESFactory;
import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import ca.nrc.dtrc.elasticsearch.es7.ES7Factory;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.Transport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Generates one ElasticSearch client per thread
 */
public class ClientPool {

	private static ClientPool __singleton = null;

	public static String host() {
		return "localhost";
	}

	public static int port() {
		return 9200;
	}

	public static String baseURL() {
		return "http://"+host()+":"+port()+"/";
	}

	/***
	 * Transport object for current thread.
	 */
	public static Transport transport() {
		Long currThread = Thread.currentThread().getId();
		return singleton().thread2transport.get(currThread);
	}

	public static RestClient restClient() {
		Long currThread = Thread.currentThread().getId();
		return singleton().thread2restClient.get(currThread);
	}

	/**
	 * We are trying to use the high level ElasticsearchClient provided the the ES framework.
	 * But there are certain things we can't figure out how to do with that client. So sometimes
	 * we may need to use NRC's lower-level, homegrown client.
	 */
	public static ESFactory esFactory(String indexName) throws GenericESException {
		try {
			return new ES7Factory(indexName);
		} catch (ElasticSearchException e) {
			throw new GenericESException(e);
		}
	}

	/*
    * ElasticSearch resources that were opened for various threads.
	 */
	private Map<Long, ElasticsearchClient> thread2esClient =
		new HashMap<Long, ElasticsearchClient>();
	private Map<Long, RestClient> thread2restClient =
		new HashMap<Long, RestClient>();
	private Map<Long, ElasticsearchTransport> thread2transport =
		new HashMap<Long, ElasticsearchTransport>();

	// Note: The constructor is private to enforce Singleton design
	// pattern. There will only ever be one instance of ClientPool
	// and it will be created by the class ClientPool.getSingleton();
	private ClientPool() {}

	private static ClientPool singleton() {
		if (__singleton == null) {
			__singleton = new ClientPool();
		}
		return __singleton;
	}

	public static ElasticsearchClient getClient() throws GenericESException {
		Logger logger = LogManager.getLogger("org.iutools.elasticsearch.ClientPool.getConnection");
		logger.trace("invoked");
		singleton();
		ElasticsearchClient client = null;
		// we cleanup the clients index every time we ask for a new client
		cleanupThreadClientIndex();
		Long currThread = Thread.currentThread().getId();
		if (!singleton().hasLiveClient4Thread(currThread)) {
			// We don't have a live client for the current thread.
			// Initialize one and put its related resources in the various
			// thread2* maps
			//
			RestClient restClient = RestClient.builder(
				new HttpHost(host(), port())).build();
			singleton().thread2restClient.put(currThread, restClient);

			// Create the transport with a Jackson mapper
			RestClientTransport transport = new RestClientTransport(
				restClient, new JacksonJsonpMapper());
			singleton().thread2transport.put(currThread, transport);

			// And create the API client
			ElasticsearchClient esClient = new ElasticsearchClient(transport);
			singleton().thread2esClient.put(currThread, esClient);
		}

		logger.trace("exiting");
		return singleton().thread2esClient.get(currThread);
	}

	private boolean hasLiveClient4Thread(Long thrID) {
		boolean answer = thread2esClient.containsKey(thrID);
		return answer;
	}

	public static void closeAll() throws GenericESException {
		singleton().cleanupThreadClientIndex(true);
		return;
	}

//	protected void finalize() throws Throwable {
//		Logger logger = LogManager.getLogger("org.iutools.elasticsearch.ClientPool.finalize");
//		logger.trace("invoked");
//		// When the singleton expires, we need to close all the open reources, even
//		// those associated with the main thread (closeAll=true)
//		cleanupThreadClientIndex(true);
//		logger.trace("exited");
//	}

	private synchronized static void cleanupThreadClientIndex() throws GenericESException {
		cleanupThreadClientIndex((Boolean)null);
	}

	private synchronized static void cleanupThreadClientIndex(Boolean closeAll) throws GenericESException {
		Logger logger = LogManager.getLogger("org.iutools.sql.ConnectionPool.cleanupThreadConnIndex");
		logger.trace("invoked");
		if (closeAll == null) {
			closeAll = false;
		}
		Set<Long> activeThreads = activeThreadIDs();

		// Loop through all the threads in the thread2connIndex.
		//
		// If the thread has terminated, then:
		// - close it's associated ElasticSearch resources
		// - delete it from the index

		// Note: Create a set of all the threads for which we have an entry in thread2clientIndex
		// This is to avoid Conccurent Access exception when we delete entries of
		// thread2connIndex while looping on its keys
		//
		Set<Long> threadsWithOpenedResources = new HashSet<Long>();
		for (Long thr: singleton().thread2esClient.keySet()) {
			threadsWithOpenedResources.add(thr);
		}
		logger.trace("--** threadsWithOpenedResources="+threadsWithOpenedResources);

		// Now loop through the set of threads for which we have a client.
		Set<Long> threads2beDeleted = new HashSet<Long>();
		for (Long thrWithOpenResources: threadsWithOpenedResources) {
			logger.trace("Looking at thread: "+thrWithOpenResources);

			// closeAll --> we want to close ALL resources, even those for which
			//   the thread is still alive.
			if (closeAll || !activeThreads.contains(thrWithOpenResources)) {
				try {
					// Closing the transport will also close the RestClient.
					// No need to close the ElasticsearchClient.
					//
					singleton().thread2transport.get(thrWithOpenResources).close();
				} catch (IOException e) {
					throw new GenericESException(e);
				}
				singleton().thread2esClient.remove(thrWithOpenResources);
			}
		}

		logger.trace("exited");
	}

	protected static synchronized Set<Long> activeThreadIDs() {
		Set<Long> threadIDs = new HashSet<Long>();
		for (Thread thr: Thread.getAllStackTraces().keySet()) {
			threadIDs.add(thr.getId());
		}
		return threadIDs;
	}
}
