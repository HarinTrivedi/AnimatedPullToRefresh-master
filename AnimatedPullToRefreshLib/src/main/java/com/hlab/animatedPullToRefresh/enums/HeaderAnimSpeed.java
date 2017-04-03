package com.hlab.animatedPullToRefresh.enums;

/**
 * Created in Android_animated_pull_to_refresh_control-master on 22/03/17.
 */

public enum HeaderAnimSpeed {
    FAST(0), SLOW(1);

    private int speed;

    HeaderAnimSpeed(int type) {
        speed = type;
    }

    public int getSpeed() {
        return speed;
    }

    public static HeaderAnimSpeed fromId(int id) {
        for (HeaderAnimSpeed f : values()) {
            if (f.speed == id) return f;
        }
        throw new IllegalArgumentException();
    }
}
