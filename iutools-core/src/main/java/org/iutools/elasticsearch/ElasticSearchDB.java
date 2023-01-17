package org.iutools.elasticsearch;

import ca.nrc.dtrc.elasticsearch.*;
import ca.nrc.dtrc.elasticsearch.es7.ES7Factory;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import org.json.JSONObject;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/*
 * This class ties a series of ElasticSearch indices into a "database".
 * Each index will store a particular type of document.
 * All indices start with the same database name
 */
public class ElasticSearchDB {

	private static HashMap<String,Boolean> indexExists = new HashMap<>();

	public String dbName = null;
	private DBSchema schema = new DBSchema();

	public ElasticSearchDB(String _dbName) throws GenericESException {
		init__ElasticSearchDB(_dbName, (DBSchema) null);
	}

	public ElasticSearchDB(String _dbName, DBSchema _schema) throws GenericESException {
		init__ElasticSearchDB(_dbName, _schema);
	}

	private void init__ElasticSearchDB(String _dbName, DBSchema _schema) throws GenericESException {
		if (_dbName == null) {
			_dbName = "iutools_db";
		}
		this.dbName = _dbName;
		if (_schema != null) {
			this.schema = _schema;
		}
		ensureIndicesAreDefined();
	}

	/**
	 * Although we are trying to use the more "modern" ElasticsearchClient class provided by the
	 * ElasticSearch framework, there are things we haven't been able to do with that client.
	 *
	 * So sometimes we need to resort to NRC's homegrown StreamlinedClient.
	 */
	public StreamlinedClient nrcClient(String indexName) throws GenericESException {
		StreamlinedClient client = null;
		try {
			client = new ES7Factory(indexName).client();
		} catch (ElasticSearchException e) {
			throw new GenericESException(e);
		}
		return client;
	}

	private void ensureIndicesAreDefined() throws GenericESException {
		for (DocType docType: schema.docTypes()) {
			if (!indexExists4docType(docType)) {
				defineIndexForDocType(docType);
			}
		}
		return;
	}

	private void defineIndexForDocType(DocType docType) throws GenericESException {
		String indexName = docType.indexName(dbName);

		URL url = null;
		try {
			url = new URL(ClientPool.baseURL()+indexName);
		} catch (MalformedURLException e) {
			throw new GenericESException(e);
		}

		JSONObject properties = new JSONObject();
		for (String fldName: docType.fieldNames()) {
			properties.put(fldName, new JSONObject()
					.put("type", docType.typename4field(fldName))
			);
		}
		JSONObject request = new JSONObject()
			.put("mappings", new JSONObject()
				.put("properties", properties)
			)
		;

		Transport transport = ClientPool.esFactory(indexName).transport();
		try {
			final String jsonResult = transport.put(url, request);
		} catch (ElasticSearchException e) {
			throw new GenericESException(e);
		}
	}

	/**
	 * Check if an ES "database" exists.
	 */

	public String index4doc(Document doc) {
		return DocType.index4doc(doc, dbName);
	}

	public String index4docType(DocType type) {
		return type.indexName(dbName);
	}

	public String index4docClass(Class<? extends Document> docClass) {
		return DocType.index4typeName(docClass.getSimpleName(), dbName);
	}

	private synchronized boolean indexExists4docType(DocType doctype) throws GenericESException {
		boolean exists = indexExists(doctype.indexName(dbName));
		return exists;
	}
	private synchronized boolean indexExists(String indexName) throws GenericESException {
		Boolean exists = null;
		if (indexExists.containsKey(indexName)) {
			exists = indexExists.get(indexName);
		}
		if (exists == null) {
			ElasticsearchClient client = ClientPool.getClient();
			try {
				BooleanResponse response = client.indices().exists(e -> e.index(indexName));
				exists = response.value();
				// Remember the answer so we don't have to keep asking the server.
				indexExists.put(indexName, exists);
			} catch (IOException e) {
				throw new GenericESException(e);
			}
		}
		return exists;
	}
	private synchronized void createIndexIfNotExists(String indexName) throws GenericESException {
		if (!indexExists(indexName)) {
			// Create the index
			ElasticsearchClient client = ClientPool.getClient();
			try {
				CreateIndexResponse response = client.indices().create(c -> c.index(indexName));
				// Remember that this index now exists, so we don't have to keep asking the server.
				indexExists.put(indexName, true);
			} catch (IOException e) {
				throw new GenericESException(e);
			}
		}
	}
}
