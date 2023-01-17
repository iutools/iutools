package org.iutools.elasticsearch;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;

/*
 * Use this instead of ElasticSearchException when you want to throw
 * an exception for which you don't have the endpoint name or the
 * ErrorResponse object.
 */
public class GenericESException extends Exception {

	public GenericESException(Exception exc) {
		super(exc);
	}

	public GenericESException(String mess) {
		super(mess);
	}

	public GenericESException(String mess, Exception exc) {
		super(mess, exc);
	}
}
