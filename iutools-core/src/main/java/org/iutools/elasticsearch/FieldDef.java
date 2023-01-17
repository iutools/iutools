package org.iutools.elasticsearch;

import co.elastic.clients.elasticsearch._types.mapping.FieldType;

/**
 * Definition of a field in an ES DocType.
 */
public class FieldDef {
    private String name = null;
    private FieldType type = null;

//    public static enum FieldType {KEYWORD};

    public FieldDef(String fldName, FieldType fldType) {
        this.name = fldName;
        this.type = fldType;
    }
}
