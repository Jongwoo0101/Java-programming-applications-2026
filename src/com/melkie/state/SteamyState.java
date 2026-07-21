package com.melkie.state;

/** 치명(무장해제) 단계 - 텐션 70 이상 돌파 시 진입하는 최종 상태 */
public final class SteamyState implements MeltingState {

    public static final SteamyState INSTANCE = new SteamyState();

    private SteamyState() {}

    @Override
    public String tierKey() { return "STEAMY"; }

    @Override
    public String displayName() { return "치명"; }

    @Override
    public MeltingState evaluateTransition(int tension) {
        // 최종 상태이므로 더 이상 전이하지 않는다.
        return this;
    }
}
