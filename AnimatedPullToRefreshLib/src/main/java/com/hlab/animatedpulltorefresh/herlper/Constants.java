package com.hlab.animatedpulltorefresh.herlper;

public class Constants {

    // time out for no movements during swipe action
    public static final int RETURN_DURATION = 200;

    // time out for showing refresh complete
    public static final int REFRESH_COMPLETE_DURATION = 200;

    // Duration of the animation from the top of the content view to parent top
    public static final int RETURN_TO_TOP_DURATION = 200;

    // Duration of the animation from the top of the content view to the height of header
    public static final int RETURN_TO_HEADER_DURATION = 200;

    // maximum swipe distance( percent of parent container)
    public static final float MAX_SWIPE_DISTANCE_FACTOR = .3f;

    // swipe distance to trigger refreshing
    public static final int SWIPE_REFRESH_TRIGGER_DISTANCE = 100;

    // swipe resistance factor
    public static final float RESISTANCE_FACTOR = .4f;
}
