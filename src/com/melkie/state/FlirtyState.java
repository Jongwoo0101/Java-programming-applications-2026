package com.melkie.state;

/** 능글(호감) 단계 - 텐션 30 이상 돌파 시 진입 */
public final class FlirtyState implements MeltingState {

    public static final FlirtyState INSTANCE = new FlirtyState();

    private FlirtyState() {}

    @Override
    public String tierKey() { return "FLIRTY"; }

    @Override
    public String displayName() { return "능글"; }

    @Override
    public MeltingState evaluateTransition(int tension) {
        if (tension >= 70) {
            return SteamyState.INSTANCE;
        }
        return this;
    }
}
