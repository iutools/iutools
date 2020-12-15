package org.iutools.phonology;

import ca.nrc.testing.AssertObject;

import java.util.Set;

public class AssertDialect {

    public AssertDialect assertPossibleDialectsAre(
        String word, Set<Dialect.Name> expDialects) throws Exception {
        Set<Dialect.Name> gotDialects = Dialect.possibleDialects(word);
        AssertObject.assertDeepEquals(
    "Possible dialects for word "+word+" were not as expected",
            expDialects, gotDialects);
        return this;
    }
}
