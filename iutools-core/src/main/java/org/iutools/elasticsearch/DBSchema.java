package org.iutools.elasticsearch;

import ca.nrc.dtrc.elasticsearch.Document;

import java.util.*;

/**
 * Define the schema for an ElasticSearch "database"
 */
public class DBSchema {

    public static enum Type {KEYWORD};

    private Set<DocType> _docTypes = new HashSet<>();

    public DBSchema addDocType(DocType docType) {
        _docTypes.add(docType);
        return this;
    }

    public Collection<DocType> docTypes() {
        return _docTypes;
    }

}
