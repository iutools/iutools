package org.iutools.morph;

public class MorphAnalCurrentExpectations_WordsThatFailedBefore extends MorphAnalCurrentExpectationsAbstract {


    public MorphAnalCurrentExpectations_WordsThatFailedBefore() throws MorphologicalAnalyzerException {
        initMorphAnalCurrentExpectations();
    }


    protected void initMorphAnalCurrentExpectations() throws MorphologicalAnalyzerException {

        //
        // Words that produce the correct analysis, but not as the top
        // alternative
        //
        expectFailure("angilligiaqtunit", OutcomeType.CORRECT_NOT_FIRST);
        expectFailure("angilligiaqtitsigunnaqpat", OutcomeType.CORRECT_NOT_FIRST);
        expectFailure("angilligiaqtittigunnaqpat", OutcomeType.CORRECT_NOT_FIRST);

        //
        // Words that produce some analyses, but none of them is the correct
        // one.
        //
        //
        //expectFailure("taaksumunga", OutcomeType.CORRECT_NOT_PRESENT);

        //
        // Words that produce no decomposition at all
        //
    }
}
