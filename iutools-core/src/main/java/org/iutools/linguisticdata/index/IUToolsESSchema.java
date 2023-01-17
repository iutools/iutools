package org.iutools.linguisticdata.index;

import org.iutools.elasticsearch.DBSchema;
import org.iutools.elasticsearch.DocType;

import co.elastic.clients.elasticsearch._types.mapping.FieldType;

/**
 * Schema for the IUTools ES "database".
 */
public class IUToolsESSchema extends DBSchema {

    public IUToolsESSchema() {

        DocType docType = new DocType(MorphemeEntry.class)
            .addField("descr.id", FieldType.Keyword)
            .addField("descr.canonicalForm", FieldType.Keyword);
        addDocType(docType);
    }
}
