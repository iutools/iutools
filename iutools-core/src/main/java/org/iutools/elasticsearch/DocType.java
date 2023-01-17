package org.iutools.elasticsearch;

import ca.nrc.dtrc.elasticsearch.Document;

import co.elastic.clients.elasticsearch._types.mapping.FieldType;
import co.elastic.clients.elasticsearch._types.mapping.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Defines a document type in an ES "database".
 *
 * All documents of that type will be stored in an ES index with name:
 *
 *    dbname__typename
 */
public class DocType {

    public Class<? extends Document> docClass = null;

    private Map<String, FieldType> fieldsMap = new HashMap<>();

    private Map<String, Property> _properties = null;
    public DocType(Class<? extends Document> _docClass) {
        this.docClass = _docClass;
    }

    public Map<String,Property> properties() throws GenericESException {
        if (_properties == null) {
            _properties = new HashMap<>();
            for (String fldName: fieldNames()) {
                Property fldProp = property4field(fldName);
                _properties.put(fldName, fldProp);
            }
        }
        return _properties;
    }

    private Property property4field(String fldName) throws GenericESException {
        FieldType type = type4field(fldName);
        Property prop = null;

        // This is ludicrous! There MUST be a way to create a Property object from the name
        // of a field and its type, but I can't figure it out. Hence this stupid long case
        // statement
        if (type == FieldType.Keyword) {
            prop = Property.of(p -> p.keyword(k -> k));
        } else if (type == FieldType.Boolean) {
            prop = Property.of(p -> p.boolean_(b -> b));
        }

        if (prop == null) {
            throw new GenericESException("Field "+fldName+" has unsupported type "+type);
        }

        return prop;
    }

    public DocType addField(String fldName, FieldType fldType) {
        fieldsMap.put(fldName, fldType);
        _properties = null;
        return this;
    }

    public Set<String> fieldNames() {
        return fieldsMap.keySet();
    }
    public FieldType type4field(String fldName) {
        return fieldsMap.get(fldName);
    }

    public String typename4field(String fldName) {
        return fieldsMap.get(fldName).name().toLowerCase();
    }

    public String indexName(String dbName) {
        return index4docClass(docClass, dbName);
    }
    public static String index4doc(Document doc, String dbName) {
		return index4typeName(doc.getClass().getSimpleName(), dbName);
	}

	public static String index4docClass(Class<? extends Document> docClass, String dbName) {
		return index4typeName(docClass.getSimpleName(), dbName);
	}

	public static String index4typeName(String typeName, String dbName) {
		String indexName = typeName;
        if (dbName != null) {
            indexName = dbName+"__"+indexName;
        }
		indexName = indexName.toLowerCase();
		return indexName;
	}
}
