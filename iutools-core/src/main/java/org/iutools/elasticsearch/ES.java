package org.iutools.elasticsearch;

import ca.nrc.config.ConfigException;
import ca.nrc.dtrc.elasticsearch.ESConfig;
import ca.nrc.dtrc.elasticsearch.ESFactory;
import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import ca.nrc.dtrc.elasticsearch.es5.ES5Factory;
import ca.nrc.dtrc.elasticsearch.es7.ES7Factory;
import org.iutools.config.IUConfig;

import static ca.nrc.dtrc.elasticsearch.ESFactory.ESOptions;

public class ES {
	public static ESFactory makeFactory(String indexName, ESOptions... options) throws ElasticSearchException {
		ESFactory factory = null;
		try {
			int version = IUConfig.esVersion();
			if (version == 5) {
				factory = new ES5Factory(indexName);
			} else {
				factory = new ES7Factory(indexName);
			}
			for (ESOptions anOption: options) {
				if (anOption == ESOptions.CREATE_IF_NOT_EXISTS) {
					factory.createIndexIfNotExist = true;
				}
				if (anOption == ESOptions.UPDATES_WAIT_FOR_REFRESH) {
					factory.updatesWaitForRefresh = true;
				}
			}
		} catch (ConfigException e) {
			throw new ElasticSearchException(e);
		}
		return factory;
	}
}
