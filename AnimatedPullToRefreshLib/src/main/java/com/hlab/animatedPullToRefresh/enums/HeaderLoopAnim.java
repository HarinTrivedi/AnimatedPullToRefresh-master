package com.hlab.animatedPullToRefresh.enums;

/**
 * Created in Android_animated_pull_to_refresh_control-master on 22/03/17.
 */

public enum HeaderLoopAnim {
    ZOOM(0), FADE(1);

    private int animType;

    HeaderLoopAnim(int type) {
        animType = type;
    }

    public int getAnimType() {
        return animType;
    }

    public static HeaderLoopAnim fromId(int id) {
        for (HeaderLoopAnim f : values()) {
            if (f.animType == id) return f;
        }
        throw new IllegalArgumentException();
    }
}
