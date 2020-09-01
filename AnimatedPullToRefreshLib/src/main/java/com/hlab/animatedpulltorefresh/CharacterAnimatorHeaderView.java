package com.hlab.animatedpulltorefresh;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hlab.animatedpulltorefresh.enums.HeaderAnimSpeed;
import com.hlab.animatedpulltorefresh.enums.HeaderLoopAnim;
import com.hlab.animatedpulltorefresh.enums.HeaderState;
import com.hlab.animatedpulltorefresh.enums.HeaderTextAnim;
import com.hlab.animatedpulltorefresh.herlper.AnimationHelper;
import com.hlab.animatedpulltorefresh.herlper.ViewHelper;

import java.util.List;

/**
 * Header view to manipulate characters views with provided customization on it
 */
class CharacterAnimatorHeaderView extends LinearLayout implements AnimatedPullToRefreshLayout.AnimatedPullToRefreshHeaderLayout {

    private List<TextView> characterViewList;
    private ViewHelper viewHelper;
    private AnimationHelper animationHelper;

    private boolean isInitialized = false;
    private int currentIndex = -1;
    private Handler mLoopHandler;
    private Runnable mLoopRunnable;

    private String headerText;
    private int headerBackgroundColor;

    public CharacterAnimatorHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CharacterAnimatorHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CharacterAnimatorHeaderView(Context context) {
        super(context);
        setWillNotDraw(false);

        viewHelper = new ViewHelper(context);
        animationHelper = new AnimationHelper();
    }

    /**
     * Initialize view
     */
    public void initView() {
        isInitialized = true;
        removeAllViews();
        setOrientation(VERTICAL);
        setGravity(Gravity.BOTTOM);
        setBackgroundColor(headerBackgroundColor);

        LinearLayout mContainer = viewHelper.generateContainerLayout();
        addView(mContainer);

        characterViewList = viewHelper.generateCharacterViewList(headerText);
        for (TextView textView : characterViewList) {
            mContainer.addView(textView);
        }

        setupAnimation();
    }

    /**
     * Initialization animations
     */
    public void setupAnimation() {
        mLoopHandler = new Handler();
        mLoopRunnable = new Runnable() {
            @Override
            public void run() {
                currentIndex++;
                if (currentIndex >= characterViewList.size()) {
                    currentIndex = -1;
                    if (!animationHelper.shouldContinueTextIteration())
                        playTextAnimation();
                    else
                        mLoopHandler.postDelayed(mLoopRunnable, 20);
                } else {
                    if (!animationHelper.shouldContinueLoopIteration())
                        playLoopAnimation();
                    else
                        mLoopHandler.postDelayed(mLoopRunnable, 20);
                }
            }
        };
    }

    /**
     * Start main animation
     */
    private void playLoopAnimation() {
        long wait = animationHelper.applyTextAnimation(characterViewList.get(currentIndex));
        mLoopHandler.postDelayed(mLoopRunnable, wait);
    }

    /**
     * Start character animation
     */
    private void playTextAnimation() {
        long wait = animationHelper.applyLoopAnimation(characterViewList);
        mLoopHandler.postDelayed(mLoopRunnable, wait);
    }

    @Override
    public void onStateChange(HeaderState headerState, HeaderState lastHeaderState) {
        if (headerState == lastHeaderState) {
            return;
        }

        switch (headerState) {
            case HEADER_STATE_NORMAL:
//                if (lastStateCode == AnimatedPullToRefreshLayout.HeaderState.HEADER_STATE_READY) {
//                }
//                if (lastStateCode == AnimatedPullToRefreshLayout.HeaderState.HEADER_STATE_REFRESHING) {
//                }
                break;
            case HEADER_STATE_READY:
                break;
            case HEADER_STATE_REFRESHING:
                mLoopHandler.post(mLoopRunnable);
                break;
            case HEADER_STATE_COMPLETE:
                mLoopHandler.removeCallbacks(mLoopRunnable);
                break;
        }
    }

    /**
     * Set color animation array
     */
    public void setColorAnimationArray(int[] colorAnimationArray) {
        animationHelper.setColorAnimationArray(colorAnimationArray);
    }

    /**
     * Set header text
     */
    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    /**
     * Set header background color
     */
    public void setHeaderBackgroundColor(int headerBackgroundColor) {
        this.headerBackgroundColor = headerBackgroundColor;
        setBackgroundColor(headerBackgroundColor);
    }

    /**
     * Set header character size
     */
    public void setHeaderTextSize(int headerTextSize) {
        viewHelper.setHeaderTextSize(headerTextSize);
        if (isInitialized)
            initView();
    }

    /**
     * Set header character color
     */
    public void setHeaderTextColor(int headerTextColor) {
        viewHelper.setHeaderTextColor(headerTextColor);
        animationHelper.setOriginalColor(headerTextColor);
        if (isInitialized)
            initView();
    }

    private void setHeaderPaddingTop(int headerPaddingTop) {
        viewHelper.setHeaderPaddingTop(headerPaddingTop);
    }

    private void setHeaderPaddingBottom(int headerPaddingBottom) {
        viewHelper.setHeaderPaddingBottom(headerPaddingBottom);
    }

    /**
     * Set character animation
     */
    public void setHeaderTextAnim(HeaderTextAnim headerTextAnim) {
        animationHelper.setHeaderTextAnim(headerTextAnim);
    }

    /**
     * Set loop animation
     */
    public void setHeaderLoopAnim(HeaderLoopAnim headerLoopAnim) {
        animationHelper.setHeaderLoopAnim(headerLoopAnim);
    }

    /**
     * Set character animation iteration count
     */
    public void setHeaderTextAnimIteration(int headerTextAnimIteration) {
        animationHelper.setHeaderTextAnimIteration(headerTextAnimIteration);
    }

    /**
     * Set loop animation iteration count
     */
    public void setHeaderLoopAnimIteration(int headerLoopAnimIteration) {
        animationHelper.setHeaderLoopAnimIteration(headerLoopAnimIteration);
    }

    /**
     * Enable / Disable color animation
     */
    public void setColorAnimEnable(boolean colorAnimEnable) {
        animationHelper.setColorAnimEnable(colorAnimEnable);
    }

    /**
     * Set animation speed
     */
    public void setAnimationSpeed(HeaderAnimSpeed animationSpeed) {
        animationHelper.setAnimationSpeed(animationSpeed);
    }

    /**
     * Set custom font typeface
     */
    public void setHeaderTextTypeface(Typeface mTitleTypeface) {
        if (mTitleTypeface != null) {
            viewHelper.setHeaderTextTypeface(mTitleTypeface);
            if (isInitialized)
                initView();
        }
    }
}