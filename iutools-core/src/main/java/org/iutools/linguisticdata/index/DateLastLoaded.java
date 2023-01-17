package org.iutools.linguisticdata.index;

import ca.nrc.dtrc.elasticsearch.Document;

/*
 * Date on which a particular "resource" was loaded into ElasticSearch
 */
public class DateLastLoaded extends Document {
	public Long timestamp;
	public String resource;

	public DateLastLoaded() {}

	public DateLastLoaded(String _resource, long _date) {
		this.resource = _resource;
		this.timestamp = _date;
	}

	@Override
	public DateLastLoaded setId(String _id) {
		super.setId(_id);
		return this;
	}

	@Override
	public String getId() {
		return resource;
	}
}
