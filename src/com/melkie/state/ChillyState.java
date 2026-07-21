package com.melkie.state;

/** 밀당(철벽) 단계 - 초기 상태 */
public final class ChillyState implements MeltingState {

    public static final ChillyState INSTANCE = new ChillyState();

    private ChillyState() {}

    @Override
    public String tierKey() { return "CHILLY"; }

    @Override
    public String displayName() { return "밀당"; }

    @Override
    public MeltingState evaluateTransition(int tension) {
        if (tension >= 70) {
            return SteamyState.INSTANCE;
        } else if (tension >= 30) {
            return FlirtyState.INSTANCE;
        }
        return this;
    }
}
