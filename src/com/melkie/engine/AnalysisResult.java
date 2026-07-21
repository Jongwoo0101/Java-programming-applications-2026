package com.melkie.engine;

import java.util.Collections;
import java.util.List;

/**
 * TextAnalysisEngine 한 번의 분석 결과를 담는 불변(immutable) 값 객체.
 */
public final class AnalysisResult {

    private final int tensionDelta;
    private final double dominanceDelta;
    private final double submissionDelta;
    private final double assertivenessDelta;
    private final List<String> matchedKeywords;

    public AnalysisResult(int tensionDelta, double dominanceDelta, double submissionDelta,
                           double assertivenessDelta, List<String> matchedKeywords) {
        this.tensionDelta = tensionDelta;
        this.dominanceDelta = dominanceDelta;
        this.submissionDelta = submissionDelta;
        this.assertivenessDelta = assertivenessDelta;
        this.matchedKeywords = Collections.unmodifiableList(matchedKeywords);
    }

    public int getTensionDelta() { return tensionDelta; }
    public double getDominanceDelta() { return dominanceDelta; }
    public double getSubmissionDelta() { return submissionDelta; }
    public double getAssertivenessDelta() { return assertivenessDelta; }
    public List<String> getMatchedKeywords() { return matchedKeywords; }
}
