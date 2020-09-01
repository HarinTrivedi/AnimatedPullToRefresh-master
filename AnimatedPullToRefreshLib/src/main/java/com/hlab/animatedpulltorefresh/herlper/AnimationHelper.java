package com.hlab.animatedpulltorefresh.herlper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.hlab.animatedpulltorefresh.enums.HeaderAnimSpeed;
import com.hlab.animatedpulltorefresh.enums.HeaderLoopAnim;
import com.hlab.animatedpulltorefresh.enums.HeaderTextAnim;

import java.util.List;

/**
 * Helper class to handle animations
 */
public class AnimationHelper {

    private long CHARACTER_ANIM_DURATION = 50;
    private Interpolator interpolator;
    private int originalColor = Color.BLACK;

    //colors to animate on each character
    private int[] colorAnimationArray;
    private int colorIndex = 0;
    //no of text animation iteration
    private int currentTextIteration = 0;
    //no of loop animation iteration
    private int currentLoopIteration = 0;

    private float SCALE_AMOUNT = 1.2f;
    private float FADE_MOUNT = 0.5f;
    private float ROTATION_ANGLE = 20.0f;

    // Animation attributes
    private HeaderTextAnim headerTextAnim = HeaderTextAnim.ROTATE_CW;
    private HeaderLoopAnim headerLoopAnim = HeaderLoopAnim.ZOOM;
    private HeaderAnimSpeed headerAnimSpeed = HeaderAnimSpeed.FAST;
    private int headerTextAnimIteration = 1;
    private int headerLoopAnimIteration = 1;
    private boolean isColorAnimEnable = true;

    public AnimationHelper() {
        interpolator = new LinearOutSlowInInterpolator();
        colorAnimationArray = new int[]{Color.CYAN};
    }

    /**
     * Start animations on characters
     */
    public long applyTextAnimation(final TextView target) {

        final long duration = (long) (CHARACTER_ANIM_DURATION * 2.1f);

        if (isColorAnimEnable && colorAnimationArray != null) {
            final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), originalColor, colorAnimationArray[colorIndex]);
            colorAnimation.setDuration(duration);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    target.setTextColor((Integer) animator.getAnimatedValue());
                }
            });
            colorAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    target.setTextColor(originalColor);
                }
            });
            colorAnimation.start();

            // loop though color array
            colorIndex = (colorIndex + 1) % colorAnimationArray.length;
        }

        AnimationSet set = new AnimationSet(true);
        if (headerTextAnim == HeaderTextAnim.ROTATE_CW) {
            ROTATION_ANGLE = 20.0f;
            addTextRotateAnimations(set);
        } else if (headerTextAnim == HeaderTextAnim.ROTATE_ACW) {
            ROTATION_ANGLE = -20.0f;
            addTextRotateAnimations(set);
        } else if (headerTextAnim == HeaderTextAnim.FADE) {
            addTextFadeAnimations(set);
        } else if (headerTextAnim == HeaderTextAnim.ZOOM) {
            addTextZoomAnimations(set);
        }
        target.startAnimation(set);

        // text anim iteration
        currentTextIteration = (currentTextIteration + 1) % headerTextAnimIteration;

        return duration + CHARACTER_ANIM_DURATION;
    }

    // Rotation
    private void addTextRotateAnimations(AnimationSet set) {
        RotateAnimation mRotateUpAnim = new RotateAnimation(0.0f, ROTATION_ANGLE, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(CHARACTER_ANIM_DURATION);
        set.addAnimation(mRotateUpAnim);
        RotateAnimation mRotateDownAnim = new RotateAnimation(ROTATION_ANGLE, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(CHARACTER_ANIM_DURATION);
        mRotateDownAnim.setStartOffset(CHARACTER_ANIM_DURATION + 20);
        mRotateDownAnim.setFillAfter(true);
        set.addAnimation(mRotateDownAnim);
        set.setInterpolator(interpolator);
    }

    // Fade
    private void addTextFadeAnimations(AnimationSet set) {
        AlphaAnimation mFadeInAnim = new AlphaAnimation(1, FADE_MOUNT);
        mFadeInAnim.setDuration(CHARACTER_ANIM_DURATION);
        set.addAnimation(mFadeInAnim);
        AlphaAnimation mFadeOutAnim = new AlphaAnimation(FADE_MOUNT, 1);
        mFadeOutAnim.setDuration(CHARACTER_ANIM_DURATION);
        mFadeOutAnim.setStartOffset(CHARACTER_ANIM_DURATION + 20);
        mFadeOutAnim.setFillAfter(true);
        set.addAnimation(mFadeOutAnim);
        set.setInterpolator(interpolator);
    }

    // Zoom
    private void addTextZoomAnimations(AnimationSet set) {
        ScaleAnimation mScaleAnim = new ScaleAnimation(1, SCALE_AMOUNT, 1f, SCALE_AMOUNT, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mScaleAnim.setDuration(CHARACTER_ANIM_DURATION);
        set.addAnimation(mScaleAnim);
        ScaleAnimation mScaleDownAnim = new ScaleAnimation(1, SCALE_AMOUNT, 1f, SCALE_AMOUNT, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mScaleDownAnim.setDuration(CHARACTER_ANIM_DURATION);
        mScaleDownAnim.setStartOffset(CHARACTER_ANIM_DURATION + 20);
        mScaleDownAnim.setFillAfter(true);
        set.addAnimation(mScaleDownAnim);
        set.setInterpolator(interpolator);
    }

    /**
     * Start Loop animation on given list of characters
     */
    public long applyLoopAnimation(List<TextView> targetList) {
        long duration = (long) (CHARACTER_ANIM_DURATION * 5f);
        for (final TextView target : targetList) {
            AnimationSet set = new AnimationSet(true);
            if (headerLoopAnim == HeaderLoopAnim.ZOOM) {
                addLoopScaleAnimations(duration, set);
            } else if (headerLoopAnim == HeaderLoopAnim.FADE) {
                addLoopFadeAnimations(duration, set);
            }
            target.startAnimation(set);
        }

        // loop anim iteration
        currentLoopIteration = (currentLoopIteration + 1) % headerLoopAnimIteration;
        return (long) ((duration * 2.1f) + 300);
    }

    // add scale in loop
    private void addLoopScaleAnimations(long duration, AnimationSet set) {
        ScaleAnimation mScaleAnim = new ScaleAnimation(1, SCALE_AMOUNT, 1f, SCALE_AMOUNT, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mScaleAnim.setDuration(duration);
        set.addAnimation(mScaleAnim);
        ScaleAnimation mScaleDownAnim = new ScaleAnimation(SCALE_AMOUNT, 1, SCALE_AMOUNT, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mScaleDownAnim.setDuration(duration);
        mScaleDownAnim.setStartOffset(duration + 50);
        set.addAnimation(mScaleDownAnim);
        set.setInterpolator(interpolator);
    }

    // add fade in loop
    private void addLoopFadeAnimations(long duration, AnimationSet set) {
        AlphaAnimation mFadeInAnim = new AlphaAnimation(1, FADE_MOUNT);
        mFadeInAnim.setDuration(duration);
        set.addAnimation(mFadeInAnim);
        AlphaAnimation mFadeOutAnim = new AlphaAnimation(FADE_MOUNT, 1);
        mFadeOutAnim.setDuration(duration);
        mFadeOutAnim.setStartOffset(duration + 50);
        set.addAnimation(mFadeOutAnim);
        set.setInterpolator(interpolator);
    }

    public boolean shouldContinueTextIteration() {
        return currentTextIteration != 0;
    }

    public boolean shouldContinueLoopIteration() {
        return currentLoopIteration != 0;
    }

    public void setColorAnimationArray(int[] colorAnimationArray) {
        this.colorAnimationArray = colorAnimationArray;
    }

    public void setOriginalColor(int originalColor) {
        this.originalColor = originalColor;
    }

    public HeaderTextAnim getHeaderTextAnim() {
        return headerTextAnim;
    }

    public void setHeaderTextAnim(HeaderTextAnim headerTextAnim) {
        this.headerTextAnim = headerTextAnim;
    }

    public HeaderLoopAnim getHeaderLoopAnim() {
        return headerLoopAnim;
    }

    public void setHeaderLoopAnim(HeaderLoopAnim headerLoopAnim) {
        this.headerLoopAnim = headerLoopAnim;
    }

    public int getHeaderTextAnimIteration() {
        return headerTextAnimIteration;
    }

    public void setHeaderTextAnimIteration(int headerTextAnimIteration) {
        this.headerTextAnimIteration = headerTextAnimIteration;
    }

    public int getHeaderLoopAnimIteration() {
        return headerLoopAnimIteration;
    }

    public void setHeaderLoopAnimIteration(int headerLoopAnimIteration) {
        this.headerLoopAnimIteration = headerLoopAnimIteration;
    }

    public boolean isColorAnimEnable() {
        return isColorAnimEnable;
    }

    public void setColorAnimEnable(boolean colorAnimEnable) {
        isColorAnimEnable = colorAnimEnable;
    }

    public void setAnimationSpeed(HeaderAnimSpeed animationSpeed) {
        headerAnimSpeed = animationSpeed;
        if (animationSpeed == HeaderAnimSpeed.FAST)
            CHARACTER_ANIM_DURATION = 50;
        else if (animationSpeed == HeaderAnimSpeed.SLOW)
            CHARACTER_ANIM_DURATION = 100;
    }
}
