package com.hlab.animatedPullToRefresh.enums;

/**
 * Created in Android_animated_pull_to_refresh_control-master on 22/03/17.
 */

public enum HeaderTextAnim {
    ROTATE_CW(0), ROTATE_ACW(1), FADE(2), ZOOM(3);

    private int animType;

    HeaderTextAnim(int type) {
        animType = type;
    }

    public int getAnimType() {
        return animType;
    }

    public static HeaderTextAnim fromId(int id) {
        for (HeaderTextAnim f : values()) {
            if (f.animType == id) return f;
        }
        throw new IllegalArgumentException();
    }
}
