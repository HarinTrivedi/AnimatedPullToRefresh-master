package com.hlab.animatedpulltorefresh.enums;

public enum HeaderState {
    HEADER_STATE_DEFAULT(-1),
    HEADER_STATE_NORMAL(0),
    HEADER_STATE_READY(1),
    HEADER_STATE_REFRESHING(2),
    HEADER_STATE_COMPLETE(3);

    int state;

    HeaderState(int i) {
        state = i;
    }

    public int getState() {
        return state;
    }
}
