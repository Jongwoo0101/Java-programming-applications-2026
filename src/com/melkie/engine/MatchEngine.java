package com.melkie.engine;

/**
 * 무거운 머신러닝 연산 없이, 순수 자바 반복문과 Math.pow / Math.sqrt 만으로
 * 코사인 유사도(Cosine Similarity)를 구현한 수학적 매칭 엔진.
 *
 *              sum(Ai * Bi)
 * similarity = ------------------------------
 *              sqrt(sum(Ai^2)) * sqrt(sum(Bi^2))
 */
public final class MatchEngine {

    private MatchEngine() {}

    /** 두 벡터의 코사인 유사도를 0.0 ~ 1.0 사이 값으로 반환한다. */
    public static double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("벡터 차원이 일치하지 않습니다.");
        }

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            magnitudeA += Math.pow(a[i], 2);
            magnitudeB += Math.pow(b[i], 2);
        }

        double denominator = Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB);
        if (denominator == 0.0) {
            return 0.0;
        }
        return dotProduct / denominator;
    }

    /** 싱크로율(%) 형태로 변환. 사용자가 대화 중 아무 신호도 주지 않았다면 기본 벡터로 보정한다. */
    public static double similarityPercent(double[] userVector, double[] targetVector) {
        double[] safeUserVector = isZeroVector(userVector)
                ? new double[]{10, 10, 10}
                : userVector;
        double raw = cosineSimilarity(safeUserVector, targetVector);
        // 음수 유사도는 0%로, 그 외에는 백분율로 스케일링
        return Math.max(0.0, raw) * 100.0;
    }

    private static boolean isZeroVector(double[] v) {
        for (double x : v) {
            if (x != 0.0) return false;
        }
        return true;
    }
}
