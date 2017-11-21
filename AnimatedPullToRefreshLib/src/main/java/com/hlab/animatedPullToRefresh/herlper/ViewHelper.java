package com.hlab.animatedPullToRefresh.herlper;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlab.animatedPullToRefresh.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to manage views inside Header
 */

public class ViewHelper {

    private Context context;
    private LinearLayout.LayoutParams tvParams, containerParams;

    // View Attributes
    private int headerTextSize;
    private int headerTextColor;
    private int headerPaddingTop;
    private int headerPaddingBottom;
    private Typeface mTitleTypeface;

    public ViewHelper(Context context) {
        this.context = context;

        containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(context.getResources().getDimensionPixelSize(R.dimen.headerPaddingLeft),
                context.getResources().getDimensionPixelSize(R.dimen.headerPaddingTop),
                context.getResources().getDimensionPixelSize(R.dimen.headerPaddingRight),
                0);
    }

    /**
     * Generates parent container layout
     */
    public LinearLayout generateContainerLayout() {
        LinearLayout container = new LinearLayout(context);
        container.setLayoutParams(containerParams);
        container.setGravity(Gravity.CENTER);
        container.setOrientation(LinearLayout.HORIZONTAL);
        return container;
    }

    /**
     * Generates character view
     */
    private TextView generateCharacterTextView(char c) {
        TextView textView = new TextView(context);
        textView.setText(String.valueOf(c));
        textView.setTextColor(headerTextColor);
        textView.setLayoutParams(tvParams);
        textView.setTextSize(headerTextSize);
        if (mTitleTypeface != null)
            textView.setTypeface(mTitleTypeface);
        return textView;
    }

    /**
     * Start adding views in parent
     */
    public List<TextView> generateCharacterViewList(String text) {
        if (text == null)
            text = "";
        List<TextView> characterViewList = new ArrayList<>();

        for (char c : text.toCharArray()) {
            characterViewList.add(generateCharacterTextView(c));
        }
        return characterViewList;
    }

    public int getHeaderTextSize() {
        return headerTextSize;
    }

    public void setHeaderTextSize(int headerTextSize) {
        this.headerTextSize = headerTextSize;
    }

    public int getHeaderTextColor() {
        return headerTextColor;
    }

    public void setHeaderTextColor(int headerTextColor) {
        this.headerTextColor = headerTextColor;
    }

    public int getHeaderPaddingTop() {
        return headerPaddingTop;
    }

    public void setHeaderPaddingTop(int headerPaddingTop) {
        tvParams.topMargin = headerPaddingTop;
        this.headerPaddingTop = headerPaddingTop;
    }

    public int getHeaderPaddingBottom() {
        return headerPaddingBottom;
    }

    public void setHeaderPaddingBottom(int headerPaddingBottom) {
//        tvParams.bottomMargin = headerPaddingBottom;
        this.headerPaddingBottom = headerPaddingBottom;
    }

    public void setHeaderTextTypeface(Typeface mTitleTypeface) {
        this.mTitleTypeface = mTitleTypeface;
    }
}
