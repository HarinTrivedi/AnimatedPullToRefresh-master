package com.hlabexamples.AnimatedPullToRefresh;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.hlab.animatedPullToRefresh.AnimatedPullToRefreshLayout;
import com.hlab.animatedPullToRefresh.enums.HeaderAnimSpeed;
import com.hlab.animatedPullToRefresh.enums.HeaderLoopAnim;
import com.hlab.animatedPullToRefresh.enums.HeaderTextAnim;
import com.hlab.fabrevealmenu.view.FABRevealMenu;
import com.hlabexamples.AnimatedPullToRefresh.databinding.LayoutOptionsBinding;

public class MainActivity extends AppCompatActivity implements AnimatedPullToRefreshLayout.OnRefreshListener {

    private FABRevealMenu fabMenu;
    private AnimatedPullToRefreshLayout mPullToRefreshLayout;
    private LayoutOptionsBinding menuBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ListAdapter(this));

        mPullToRefreshLayout = findViewById(R.id.pullToRefreshLayout);
        mPullToRefreshLayout.setColorAnimationArray(new int[]{Color.CYAN, Color.RED, Color.YELLOW, Color.MAGENTA});
        mPullToRefreshLayout.setOnRefreshListener(this);

        /*
        For FABRevealMenu checkout here:
        https://github.com/HarinTrivedi/FABRevealMenu-master
        */

        FloatingActionButton fab = findViewById(R.id.fab);
        fabMenu = findViewById(R.id.fabMenu);
        fabMenu.bindAncherView(fab);
        fabMenu.setCustomView(addCustomView());
    }

    private View addCustomView() {
        menuBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.layout_options, (ViewGroup) findViewById(R.id.parent), false);

        menuBinding.btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configurePullToRefreshView();
                hideKeyboard();
                fabMenu.closeMenu();
            }
        });

        return menuBinding.getRoot();
    }

    @Override
    public void onRefresh() {
        //todo: do something here when it starts to refresh
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPullToRefreshLayout.refreshComplete();
            }
        }, 6000);
    }

    private void configurePullToRefreshView() {
        mPullToRefreshLayout.setHeaderTextAnimIteration(menuBinding.spTextAnimIt.getSelectedItemPosition() + 1);
        mPullToRefreshLayout.setHeaderLoopAnimIteration(menuBinding.spLoopAnimIt.getSelectedItemPosition() + 1);

        switch (menuBinding.spSpeed.getSelectedItemPosition()) {
            case 0:
                mPullToRefreshLayout.setAnimationSpeed(HeaderAnimSpeed.FAST);
                break;
            case 1:
                mPullToRefreshLayout.setAnimationSpeed(HeaderAnimSpeed.SLOW);
                break;
        }

        switch (menuBinding.spTextAnim.getSelectedItemPosition()) {
            case 0:
                mPullToRefreshLayout.setHeaderTextAnim(HeaderTextAnim.ROTATE_CW);
                break;
            case 1:
                mPullToRefreshLayout.setHeaderTextAnim(HeaderTextAnim.ROTATE_ACW);
                break;
            case 2:
                mPullToRefreshLayout.setHeaderTextAnim(HeaderTextAnim.FADE);
                break;
            case 3:
                mPullToRefreshLayout.setHeaderTextAnim(HeaderTextAnim.ZOOM);
                break;
        }
        switch (menuBinding.spLoopAnim.getSelectedItemPosition()) {
            case 0:
                mPullToRefreshLayout.setHeaderLoopAnim(HeaderLoopAnim.ZOOM);
                break;
            case 1:
                mPullToRefreshLayout.setHeaderLoopAnim(HeaderLoopAnim.FADE);
                break;
        }

        mPullToRefreshLayout.setColorAnimEnable(menuBinding.swEnable.isChecked());

        if (menuBinding.rbLight.isChecked()) {
            mPullToRefreshLayout.setHeaderBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite));
            mPullToRefreshLayout.setHeaderTextColor(ContextCompat.getColor(this, R.color.colorLabelDark));
        } else if (menuBinding.rbDark.isChecked()) {
            mPullToRefreshLayout.setHeaderBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            mPullToRefreshLayout.setHeaderTextColor(ContextCompat.getColor(this, R.color.colorLabelLight));
        }

        mPullToRefreshLayout.setHeaderText(menuBinding.edtLabel.getText().toString());
    }

    @Override
    public void onBackPressed() {
        if (fabMenu.isShowing()) {
            fabMenu.closeMenu();
        } else {
            super.onBackPressed();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
