package org.iutools.morph;

public class MorphAnalGoldStandard_WordsThatFailedBefore extends MorphAnalGoldStandardAbstract {

    public MorphAnalGoldStandard_WordsThatFailedBefore() throws Exception {
    }

    protected void initCases() throws Exception {
        addCase(new AnalyzerCase("angilligiaqtunit", "{angi:angi/1v}{lli:llik/1vv}{giaq:giaq/1vv}{tu:juq/1vn}{nit:nit/tn-abl-p}"));
        addCase(new AnalyzerCase("angilligiaqtittigunnaqpat", "{angi:angi/1v}{lli:llik/1vv}{giaq:giaq/1vv}{tit:tit/1vv}{ti:si/1vv}{gunnaq:junnaq/1vv}{pat:vat/tv-int-3p}"));
        addCase(new AnalyzerCase("angilligiaqtitsigunnaqpat", "{angi:angi/1v}{lli:llik/1vv}{giaq:giaq/1vv}{tit:tit/1vv}{si:si/1vv}{gunnaq:junnaq/1vv}{pat:vat/tv-int-3p}"));
   }

}
