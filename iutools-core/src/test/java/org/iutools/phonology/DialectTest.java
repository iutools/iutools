package org.iutools.phonology;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class DialectTest {

    @Test
    public void test__possibleDialects__SyllabicNunavut() throws Exception {
        String word = "ᐃᓄᒃᑎᑐᑦ";
        Set<Dialect.Name> gotDialects = Dialect.possibleDialects(word);
        Set<Dialect.Name> expDialects = new HashSet<Dialect.Name>();
        expDialects.add(Dialect.Name.NUNAVUT);

        new AssertDialect()
            .assertPossibleDialectsAre(word, expDialects);
    }

    @Test
    public void test__possibleDialects__SyllabicNunavik() throws Exception {
        String word = "ᓄᓪᓚᖓᑫᓐᓇᕆᑦ";
        Set<Dialect.Name> gotDialects = Dialect.possibleDialects(word);
        Set<Dialect.Name> expDialects = new HashSet<Dialect.Name>();
        expDialects.add(Dialect.Name.NUNAVIK);

        new AssertDialect()
            .assertPossibleDialectsAre(word, expDialects);
    }


}
