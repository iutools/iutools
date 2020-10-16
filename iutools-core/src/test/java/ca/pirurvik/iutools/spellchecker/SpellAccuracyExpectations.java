package ca.pirurvik.iutools.spellchecker;

public class SpellAccuracyExpectations {
    double defaultTolerance = 0.01;
    double percentFoundInTopN = 0.0;
    double percTopSuggestionOK = 0.0;
    double averageRank = 0.0;
    double avgRankTolenrance = 0.1;

    public SpellAccuracyExpectations setDefaultTolerance(double tolerance) {
        defaultTolerance = tolerance;
        return this;
    }

    public SpellAccuracyExpectations setPercentFoundInTopN(double percent) {
        percentFoundInTopN = percent;
        return this;
    }

    public SpellAccuracyExpectations setPercTopSuggestionOK(double percent) {
        percTopSuggestionOK = percent;
        return this;
    }

    public SpellAccuracyExpectations setAverageRank(double avg) {
        averageRank = avg;
        return this;
    }

    public SpellAccuracyExpectations setAvgRankTolerance(double tolerance) {
        avgRankTolenrance = tolerance;
        return this;
    }
}
