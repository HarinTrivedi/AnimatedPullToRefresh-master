package com.hlab.animatedPullToRefresh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.hlab.animatedPullToRefresh.enums.HeaderAnimSpeed;
import com.hlab.animatedPullToRefresh.enums.HeaderLoopAnim;
import com.hlab.animatedPullToRefresh.enums.HeaderState;
import com.hlab.animatedPullToRefresh.enums.HeaderTextAnim;

import static com.hlab.animatedPullToRefresh.herlper.Constants.MAX_SWIPE_DISTANCE_FACTOR;
import static com.hlab.animatedPullToRefresh.herlper.Constants.REFRESH_COMPLETE_DURATION;
import static com.hlab.animatedPullToRefresh.herlper.Constants.RESISTANCE_FACTOR;
import static com.hlab.animatedPullToRefresh.herlper.Constants.RETURN_DURATION;
import static com.hlab.animatedPullToRefresh.herlper.Constants.RETURN_TO_HEADER_DURATION;
import static com.hlab.animatedPullToRefresh.herlper.Constants.RETURN_TO_TOP_DURATION;
import static com.hlab.animatedPullToRefresh.herlper.Constants.SWIPE_REFRESH_TRIGGER_DISTANCE;

/**
 * AnimatedPullToRefreshLayout
 * View to add pull to refresh functionality on any child view
 * Also adds character header animator view with multiple customizations
 */
public class AnimatedPullToRefreshLayout extends ViewGroup {

    private final Animation mAnimateStayComplete = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
        }
    };
    private HeaderState currentHeaderState = HeaderState.HEADER_STATE_NORMAL;
    private HeaderState lastHeaderState = HeaderState.HEADER_STATE_DEFAULT;
    private int mTargetOriginalTop;
    private int mFrom;
    private int mTouchSlop;
    private int mDistanceToTriggerSync = -1;
    private int mCurrentTargetOffsetTop = 0;
    private float mPrevY;
    private int mTriggerOffset = 0;
    private boolean mInReturningAnimation;
    private final AnimationListener mReturningAnimationListener = new AnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            // Once the target content has returned to its start position, reset
            // the target offset to 0
            // mCurrentTargetOffsetTop = 0;
            mInReturningAnimation = false;
        }
    };
    private boolean mRefreshing = false;
    private boolean isHorizontalScroll;
    private boolean mCheckValidMotionFlag = true;
    private OnRefreshListener mListener;
    private MotionEvent mDownEvent;
    private RefreshCheckHandler mRefreshCheckHandler;
    private ScrollUpHandler mScrollUpHandler;
    private View mHeadview;
    private View mTarget = null;
    private final Runnable mStayRefreshCompletePosition = new Runnable() {
        @Override
        public void run() {
            animateStayComplete(mStayCompleteListener);
        }
    };
    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = mTargetOriginalTop;
            if (mFrom != mTargetOriginalTop) {
                targetTop = (mFrom + (int) ((mTargetOriginalTop - mFrom) * interpolatedTime));
            }
            int offset = targetTop - mTarget.getTop();
            final int currentTop = mTarget.getTop();
            if (offset + currentTop < 0) {
                offset = 0 - currentTop;
            }
            setTargetOffsetTop(offset, true);
        }
    };
    private final Runnable mReturnToStartPosition = new Runnable() {
        @Override
        public void run() {
            mInReturningAnimation = true;
            animateOffsetToStartPosition(mTarget.getTop(), mReturningAnimationListener);
        }
    };
    private final AnimationListener mStayCompleteListener = new AnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            mReturnToStartPosition.run();
            mRefreshing = false;
        }
    };
    // Cancel the refresh gesture and animate everything back to its original state.
    private final Runnable mCancel = new Runnable() {
        @Override
        public void run() {
            mInReturningAnimation = true;
            // Timeout fired since the user last moved their finger; animate the
            // trigger to 0 and put the target back at its original position
            animateOffsetToStartPosition(mTarget.getTop(), mReturningAnimationListener);
        }
    };
    private final Animation mAnimateToTriggerPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = mDistanceToTriggerSync;
            if (mFrom > mDistanceToTriggerSync) {
                targetTop = (mFrom + (int) ((mDistanceToTriggerSync - mFrom) * interpolatedTime));
            }
            int offset = targetTop - mTarget.getTop();
            final int currentTop = mTarget.getTop();
            if (offset + currentTop < 0) {
                offset = 0 - currentTop;
            }
            setTargetOffsetTop(offset, true);
        }
    };
    private final Runnable mReturnToTrigerPosition = new Runnable() {
        @Override
        public void run() {
            mInReturningAnimation = true;
            animateOffsetToTrigerPosition(mTarget.getTop(), mReturningAnimationListener);
        }
    };
    private CharacterAnimatorHeaderView headerView;

    public AnimatedPullToRefreshLayout(Context context) {
        this(context, null);
    }

    public AnimatedPullToRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatedPullToRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setWillNotDraw(false);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimatedPullToRefreshLayout);
        if (a != null) {
            String headerText = a.getString(R.styleable.AnimatedPullToRefreshLayout_headerText);
            int headerTextSize = a.getDimensionPixelSize(R.styleable.AnimatedPullToRefreshLayout_headerTextSize, (int) getResources().getDimension(R.dimen.headerTextSize));
            int headerTextColor = a.getColor(R.styleable.AnimatedPullToRefreshLayout_headerTextColor, getColor(android.R.color.darker_gray));
            int headerBackgroundColor = a.getColor(R.styleable.AnimatedPullToRefreshLayout_headerBackgroundColor, getColor(android.R.color.transparent));
            HeaderAnimSpeed headerAnimSpeed = HeaderAnimSpeed.fromId(a.getInt(R.styleable.AnimatedPullToRefreshLayout_animationSpeed, HeaderAnimSpeed.FAST.getSpeed()));
            HeaderTextAnim headerTextAnim = HeaderTextAnim.fromId(a.getInt(R.styleable.AnimatedPullToRefreshLayout_headerTextAnimation, HeaderTextAnim.ROTATE_CW.getAnimType()));
            HeaderLoopAnim headerLoopAnim = HeaderLoopAnim.fromId(a.getInt(R.styleable.AnimatedPullToRefreshLayout_headerLoopAnimation, HeaderLoopAnim.ZOOM.getAnimType()));
            int headerTextAnimIteration = a.getInt(R.styleable.AnimatedPullToRefreshLayout_headerTextAnimIteration, HeaderTextAnim.ROTATE_CW.getAnimType());
            int headerLoopAnimIteration = a.getInt(R.styleable.AnimatedPullToRefreshLayout_headerLoopAnimIteration, HeaderLoopAnim.ZOOM.getAnimType());
            boolean isColorAnimEnabled = a.getBoolean(R.styleable.AnimatedPullToRefreshLayout_headerTextColorAnimationEnabled, true);
            Typeface mTitleTypeface = null;
            //Font
            if (a.hasValue(R.styleable.AnimatedPullToRefreshLayout_headerTextFontFamily)) {
                int fontId = a.getResourceId(R.styleable.AnimatedPullToRefreshLayout_headerTextFontFamily, -1);
                if (fontId != -1)
                    mTitleTypeface = ResourcesCompat.getFont(context, fontId);
            }

            a.recycle();

            if (isInEditMode())
                return;

            // adding header layout : important
            headerView = new CharacterAnimatorHeaderView(getContext());
            headerView.setHeaderText(headerText);
            headerView.setHeaderTextSize(headerTextSize);
            headerView.setHeaderTextColor(headerTextColor);
            headerView.setHeaderBackgroundColor(headerBackgroundColor);
            headerView.setHeaderTextAnim(headerTextAnim);
            headerView.setHeaderLoopAnim(headerLoopAnim);
            headerView.setHeaderTextAnimIteration(headerTextAnimIteration);
            headerView.setHeaderLoopAnimIteration(headerLoopAnimIteration);
            headerView.setColorAnimEnable(isColorAnimEnabled);
            headerView.setAnimationSpeed(headerAnimSpeed);
            headerView.setHeaderTextTypeface(mTitleTypeface);

            setHeaderView(headerView);
        }

    }

    private void animateStayComplete(AnimationListener listener) {
        mAnimateStayComplete.reset();
        mAnimateStayComplete.setDuration(REFRESH_COMPLETE_DURATION);
        mAnimateStayComplete.setAnimationListener(listener);
        mTarget.startAnimation(mAnimateStayComplete);
    }

    private void animateOffsetToTrigerPosition(int from, AnimationListener listener) {
        mFrom = from;
        mAnimateToTriggerPosition.reset();
        mAnimateToTriggerPosition.setDuration(RETURN_TO_HEADER_DURATION);
        mAnimateToTriggerPosition.setAnimationListener(listener);
        mTarget.startAnimation(mAnimateToTriggerPosition);
    }

    private void animateOffsetToStartPosition(int from, AnimationListener listener) {
        mFrom = from;
        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(RETURN_TO_TOP_DURATION);
        mAnimateToStartPosition.setAnimationListener(listener);
        mTarget.startAnimation(mAnimateToStartPosition);
    }

    /**
     * Whether it is possible for the view of this layout to scroll up.
     */
    private boolean canViewScrollUp(View view, MotionEvent event) {
        boolean ret;

        event.offsetLocation(view.getScrollX() - view.getLeft(), view.getScrollY() - view.getTop());
        if (mScrollUpHandler != null) {
            boolean canViewScrollUp = mScrollUpHandler.canScrollUp(view);
            if (canViewScrollUp)
                return true;
        }

        ret = ViewCompat.canScrollVertically(view, -1) || canChildrenScrollUp(view, event);
        return ret;
    }

    /**
     * Whether it is possible for the child view of this layout to scroll up.
     */
    private boolean canChildrenScrollUp(View view, MotionEvent event) {
        if (view instanceof ViewGroup) {
            final ViewGroup viewgroup = (ViewGroup) view;
            int count = viewgroup.getChildCount();
            for (int i = 0; i < count; ++i) {
                View child = viewgroup.getChildAt(i);
                Rect bounds = new Rect();
                child.getHitRect(bounds);
                if (bounds.contains((int) event.getX(), (int) event.getY())) {
                    return canViewScrollUp(child, event);
                }
            }
        }

        return false;
    }

    private void setHeaderView(View headerView) {
        if (mHeadview != null) {
            if (mHeadview == headerView)
                return;
            removeView(mHeadview);
        }
        mHeadview = headerView;

        addView(mHeadview, new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        this.headerView.initView();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks(mCancel);
        removeCallbacks(mReturnToStartPosition);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mReturnToStartPosition);
        removeCallbacks(mCancel);
    }

    /**
     * Set the listener to be notified when a refresh is executed via the swipe gesture.
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    private void setRefreshState(HeaderState state) {
        currentHeaderState = state;
        ((AnimatedPullToRefreshHeaderLayout) mHeadview).onStateChange(currentHeaderState, lastHeaderState);
        lastHeaderState = state;
    }

    private void updateHeadViewState(boolean changeHeightOnly) {
        if (changeHeightOnly) {
            setRefreshState(currentHeaderState);
        } else {
            if (mTarget.getTop() > mDistanceToTriggerSync) {
                setRefreshState(HeaderState.HEADER_STATE_READY);
            } else {
                setRefreshState(HeaderState.HEADER_STATE_NORMAL);
            }
        }
    }

    public void refreshComplete() {
        setRefreshing(false);
    }

    /**
     * Whether the RefreshView is actively showing refresh progress.
     */
    public boolean isRefreshing() {
        return mRefreshing;
    }

    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is executed by a swipe gesture.
     */
    protected void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
//                if (refreshMode == REFRESH_MODE_PULL) {
                mReturnToTrigerPosition.run();
//                }

            } else {
                // keep refreshing state for refresh complete
//                if (refreshMode == REFRESH_MODE_PULL) {
                mRefreshing = true;
                removeCallbacks(mReturnToStartPosition);
                removeCallbacks(mCancel);
                mStayRefreshCompletePosition.run();
//                }
                setRefreshState(HeaderState.HEADER_STATE_COMPLETE);
            }
        }
    }

    private View getContentView() {
        return getChildAt(0) == mHeadview ? getChildAt(1) : getChildAt(0);
    }

    private void ensureTarget() {
        if (mTarget == null) {
            if (getChildCount() > 2 && !isInEditMode()) {
                throw new IllegalStateException(
                        "AnimatedPullToRefresh can host ONLY one direct child");
            }
            mTarget = getContentView();
            mTargetOriginalTop = mTarget.getTop();

        }
        if (mDistanceToTriggerSync == -1) {
            if (getParent() != null && ((View) getParent()).getHeight() > 0) {
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                mTriggerOffset = (int) (SWIPE_REFRESH_TRIGGER_DISTANCE * metrics.density);
                mDistanceToTriggerSync = (int) Math.min(
                        ((View) getParent()).getHeight() * MAX_SWIPE_DISTANCE_FACTOR,
                        mTriggerOffset + mTargetOriginalTop);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() == 0) {
            return;
        }
        MarginLayoutParams lp = (MarginLayoutParams) mHeadview.getLayoutParams();
        final int headViewLeft = getPaddingLeft() + lp.leftMargin;
        final int headViewTop = mCurrentTargetOffsetTop - mHeadview.getMeasuredHeight() + getPaddingTop() + lp.topMargin;
        final int headViewRight = headViewLeft + mHeadview.getMeasuredWidth();
        final int headViewBottom = headViewTop + mHeadview.getMeasuredHeight();
        mHeadview.layout(headViewLeft, headViewTop, headViewRight, headViewBottom);
        final View content = getContentView();
        lp = (MarginLayoutParams) content.getLayoutParams();
        final int childLeft = getPaddingLeft() + lp.leftMargin;
        final int childTop = mCurrentTargetOffsetTop + getPaddingTop() + lp.topMargin;
        final int childRight = childLeft + content.getMeasuredWidth();
        final int childBottom = childTop + content.getMeasuredHeight();
        content.layout(childLeft, childTop, childRight, childBottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() > 2 && !isInEditMode()) {
            throw new IllegalStateException("AnimatedPullToRefresh can host one child content view.");
        }

        measureChildWithMargins(mHeadview, widthMeasureSpec, 0, heightMeasureSpec, 0);

        final View content = getContentView();
        if (getChildCount() > 0) {
            MarginLayoutParams lp = (MarginLayoutParams) content.getLayoutParams();
            content.measure(
                    MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - lp.leftMargin - lp.rightMargin, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - lp.topMargin - lp.bottomMargin, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        if (getChildCount() > 1 && !isInEditMode()) {
            throw new IllegalStateException("AnimatedPullToRefresh can host ONLY one child content view");
        }
        super.addView(child, index, params);
    }

    private boolean checkCanDoRefresh() {
        return mRefreshCheckHandler == null || mRefreshCheckHandler.canRefresh();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            ret = true;
        return ret;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();
        boolean handled = false;
        float curY = ev.getY();

        if (!isEnabled()) {
            return false;
        }

        // record the first event:
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mDownEvent = MotionEvent.obtain(ev);
            mPrevY = mDownEvent.getY();
            mCheckValidMotionFlag = true;
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (mDownEvent != null) {
                float yDiff = Math.abs(curY - mDownEvent.getY());

                float xDiff = Math.abs(ev.getX() - mDownEvent.getX());
                if (isHorizontalScroll) {
                    mPrevY = curY;
                    return false;
                } else if (xDiff <= mTouchSlop) {
                    return false;
                }

                if (yDiff < mTouchSlop) {
                    mPrevY = curY;
                    return false;
                }
            }
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (mDownEvent != null) {

                float yDiff = Math.abs(curY - mDownEvent.getY());
                if (isHorizontalScroll) {
                    isHorizontalScroll = false;
                    mPrevY = ev.getY();
                    return false;
                } else if (yDiff < mTouchSlop) {
                    mPrevY = curY;
                    return false;
                }
            }
        }

        MotionEvent event = MotionEvent.obtain(ev);
        if (!mInReturningAnimation && !canViewScrollUp(mTarget, event)) {
            handled = onTouchEvent(ev);
        } else {
            mPrevY = ev.getY();
        }

        return handled || super.onInterceptTouchEvent(ev);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        final int action = event.getAction();
        boolean handled = false;
        int curTargetTop = mTarget.getTop();
        mCurrentTargetOffsetTop = curTargetTop - mTargetOriginalTop;
        switch (action) {

            case MotionEvent.ACTION_MOVE:
                if (mDownEvent != null && !mInReturningAnimation) {
                    final float eventY = event.getY();
                    float yDiff = eventY - mDownEvent.getY();
                    boolean isScrollUp = eventY - mPrevY > 0;

                    // if yDiff is large enough to be counted as one move event
                    if (mCheckValidMotionFlag && (yDiff > mTouchSlop || yDiff < -mTouchSlop)) {
                        mCheckValidMotionFlag = false;
                    } else {
                        if (isRefreshing()) {
                            mPrevY = event.getY();
                            handled = false;
                            break;
                        }
                    }

                    // curTargetTop is bigger than trigger
                    if (curTargetTop >= mDistanceToTriggerSync) {
                        // User movement passed distance; trigger a refresh
                        removeCallbacks(mCancel);
                    }
                    // curTargetTop is not bigger than trigger
                    else {
                        // Just track the user's movement
                        if (!isScrollUp && (curTargetTop < mTargetOriginalTop + 1)) {
                            removeCallbacks(mCancel);
                            mPrevY = event.getY();
                            handled = false;
                            // clear the progressBar
                            break;
                        } else {
                            updatePositionTimeout(true);
                        }
                    }

                    handled = true;
                    if (curTargetTop >= mTargetOriginalTop && !isRefreshing())
                        setTargetOffsetTop((int) ((eventY - mPrevY) * RESISTANCE_FACTOR), false);
                    else
                        setTargetOffsetTop((int) ((eventY - mPrevY)), true);
                    mPrevY = event.getY();
                }

                break;
            case MotionEvent.ACTION_UP:
                if (mRefreshing)
                    break;
                if (mCurrentTargetOffsetTop >= mTriggerOffset/* && refreshMode == REFRESH_MODE_PULL*/) {
                    startRefresh();
                    handled = true;
                } else {
                    updatePositionTimeout(false);
                    handled = true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mDownEvent != null) {
                    mDownEvent.recycle();
                    mDownEvent = null;
                }
                break;
        }
        return handled;
    }

    private void startRefresh() {
        if (!checkCanDoRefresh()) {
            updatePositionTimeout(false);
            return;
        }
        removeCallbacks(mCancel);
        setRefreshState(HeaderState.HEADER_STATE_REFRESHING);
        setRefreshing(true);
        if (mListener != null)
            mListener.onRefresh();
    }

    private void updateContentOffsetTop(int targetTop, boolean changeHeightOnly) {
        final int currentTop = mTarget.getTop();
        if (targetTop < mTargetOriginalTop) {
            targetTop = mTargetOriginalTop;
        }
        setTargetOffsetTop(targetTop - currentTop, changeHeightOnly);
    }

    private void setTargetOffsetTop(int offset, boolean changeHeightOnly) {
        if (offset == 0)
            return;
        // check whether the mTarget total top offset is going to be smaller than 0
        if (mCurrentTargetOffsetTop + offset >= 0) {
            mTarget.offsetTopAndBottom(offset);
            mHeadview.offsetTopAndBottom(offset);
            mCurrentTargetOffsetTop += offset;

            mHeadview.getLayoutParams().height = (int) (mHeadview.getMeasuredHeight() + offset * 0.8f);
            mHeadview.requestLayout();

            invalidate();
        } else {
            updateContentOffsetTop(mTargetOriginalTop, changeHeightOnly);
        }
        updateHeadViewState(changeHeightOnly);
    }

    private void updatePositionTimeout(boolean isDelayed) {
        removeCallbacks(mCancel);
        postDelayed(mCancel, isDelayed ? RETURN_DURATION : 0);
    }

    @SuppressWarnings("deprecation")
    private int getColor(int colorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getResources().getColor(colorResId, getContext().getTheme());
        else
            getResources().getColor(colorResId);
        return colorResId;
    }

    public void setColorAnimationArray(int[] colorAnimationArray) {
        headerView.setColorAnimationArray(colorAnimationArray);
    }

    // setters
    public void setHeaderText(String headerText) {
        if (headerView != null) {
            headerView.setHeaderText(headerText);
            headerView.initView();
        }
    }

    public void setHeaderTextSize(int headerTextSize) {
        if (headerView != null) {
            headerView.setHeaderTextSize(headerTextSize);
            headerView.invalidate();
        }
    }

    public void setHeaderTextColor(int headerTextColor) {
        if (headerView != null) {
            headerView.setHeaderTextColor(headerTextColor);
            headerView.invalidate();
        }
    }

    public void setHeaderBackgroundColor(int headerBackgroundColor) {
        if (headerView != null) {
            headerView.setHeaderBackgroundColor(headerBackgroundColor);
            headerView.invalidate();
        }
    }

    public void setHeaderTextAnim(HeaderTextAnim headerTextAnim) {
        if (headerView != null) {
            headerView.setHeaderTextAnim(headerTextAnim);
            headerView.invalidate();
        }
    }

    public void setHeaderLoopAnim(HeaderLoopAnim headerLoopAnim) {
        if (headerView != null) {
            headerView.setHeaderLoopAnim(headerLoopAnim);
            headerView.invalidate();
        }
    }

    public void setHeaderTextAnimIteration(int headerTextAnimIteration) {
        if (headerView != null) {
            headerView.setHeaderTextAnimIteration(headerTextAnimIteration);
            headerView.invalidate();
        }
    }

    public void setHeaderLoopAnimIteration(int headerLoopAnimIteration) {
        if (headerView != null) {
            headerView.setHeaderLoopAnimIteration(headerLoopAnimIteration);
            headerView.invalidate();
        }
    }

//    public void setHeaderPaddingTop(int headerPaddingTop) {
//        if (headerView != null) {
//            headerView.setHeaderPaddingTop(headerPaddingTop);
//            headerView.invalidate();
//        }
//    }
//
//    public void setHeaderPaddingBottom(int headerPaddingBottom) {
//        if (headerView != null) {
//            headerView.setHeaderPaddingBottom(headerPaddingBottom);
//            headerView.invalidate();
//        }
//    }

    public void setColorAnimEnable(boolean colorAnimEnable) {
        if (headerView != null) {
            headerView.setColorAnimEnable(colorAnimEnable);
            headerView.invalidate();
        }
    }

    public void setAnimationSpeed(HeaderAnimSpeed animationSpeed) {
        if (headerView != null) {
            headerView.setAnimationSpeed(animationSpeed);
            headerView.invalidate();
        }
    }

    public void setHeaderTextTypeface(Typeface mTitleTypeface) {
        if (headerView != null) {
            headerView.setHeaderTextTypeface(mTitleTypeface);
            headerView.invalidate();
        }
    }

    /**
     * Classes that wish to be notified when the swipe gesture correctly
     * triggers a refresh should implement this interface.
     */
    public interface OnRefreshListener {
        void onRefresh();
    }

    /**
     * Classes that checking whether refresh can be triggered
     */
    interface RefreshCheckHandler {
        boolean canRefresh();
    }

    interface ScrollUpHandler {
        boolean canScrollUp(View view);
    }

    /**
     * Classes that must be implemented by for custom header view
     */
    interface AnimatedPullToRefreshHeaderLayout {
        void onStateChange(HeaderState currentHeaderState, HeaderState lastHeaderState);
    }
}
