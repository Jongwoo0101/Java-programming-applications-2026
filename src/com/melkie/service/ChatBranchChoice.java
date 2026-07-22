package com.melkie.service;

/**
 * 정해진 턴 수(기본 7턴)의 대화가 끝났을 때 사용자에게 제시되는 3가지 분기 선택지.
 * "1"/"2"/"3" 같은 매직 스트링을 코드 곳곳에서 비교하지 않도록 타입으로 승격했다.
 */
public enum ChatBranchChoice {

    /** 결과 보기: 성향 분석 & 궁합도 리포트를 출력하고 다음 상대로 넘어간다. */
    SHOW_RESULT,

    /** 더 답변하기: 대화를 EXTEND_TURNS만큼 추가로 연장한다. */
    EXTEND_CHAT,

    /** 매칭하기: 최종 매칭을 확정하고 탐색을 종료한다. */
    CONFIRM_MATCH,

    /** 그 외 잘못된 입력. */
    INVALID;

    public static ChatBranchChoice fromInput(String rawInput) {
        if (rawInput == null) {
            return INVALID;
        }
        switch (rawInput.trim()) {
            case "1":
                return SHOW_RESULT;
            case "2":
                return EXTEND_CHAT;
            case "3":
                return CONFIRM_MATCH;
            default:
                return INVALID;
        }
    }
}
