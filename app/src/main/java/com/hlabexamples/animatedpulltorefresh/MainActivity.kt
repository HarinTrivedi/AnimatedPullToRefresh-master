package com.hlabexamples.animatedpulltorefresh

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hlab.animatedpulltorefresh.AnimatedPullToRefreshLayout
import com.hlab.animatedpulltorefresh.AnimatedPullToRefreshLayout.OnRefreshListener
import com.hlab.animatedpulltorefresh.enums.HeaderAnimSpeed
import com.hlab.animatedpulltorefresh.enums.HeaderLoopAnim
import com.hlab.animatedpulltorefresh.enums.HeaderTextAnim
import com.hlab.fabrevealmenu.view.FABRevealMenu
import com.hlabexamples.animatedpulltorefresh.databinding.LayoutOptionsBinding

class MainActivity : AppCompatActivity(), OnRefreshListener {
  private lateinit var fabMenu: FABRevealMenu
  private lateinit var mPullToRefreshLayout: AnimatedPullToRefreshLayout
  private lateinit var menuBinding: LayoutOptionsBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val recyclerView = findViewById<RecyclerView?>(R.id.recyclerView)
    recyclerView.layoutManager = LinearLayoutManager(this)
    recyclerView.adapter = ItemListAdapter(this)
    mPullToRefreshLayout = findViewById(R.id.pullToRefreshLayout)
    mPullToRefreshLayout.setColorAnimationArray(intArrayOf(Color.CYAN, Color.RED, Color.YELLOW, Color.MAGENTA))
    mPullToRefreshLayout.setOnRefreshListener(this)

    /*
        For FABRevealMenu checkout here:
        https://github.com/HarinTrivedi/FABRevealMenu-master
        */
    val fab = findViewById<FloatingActionButton?>(R.id.fab)
    fabMenu = findViewById(R.id.fabMenu)
    fabMenu.bindAnchorView(fab)
    fabMenu.customView = addCustomView()
  }

  private fun addCustomView(): View {
    menuBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.layout_options, findViewById(R.id.parent), false)
    menuBinding.btnApply.setOnClickListener {
      configurePullToRefreshView()
      hideKeyboard()
      fabMenu.closeMenu()
    }
    return menuBinding.root
  }

  override fun onRefresh() {
    //todo: do something here when it starts to refresh
    Handler().postDelayed({ mPullToRefreshLayout.refreshComplete() }, 6000)
  }

  private fun configurePullToRefreshView() {
    mPullToRefreshLayout.setHeaderTextAnimIteration(menuBinding.spTextAnimIt.selectedItemPosition + 1)
    mPullToRefreshLayout.setHeaderLoopAnimIteration(menuBinding.spLoopAnimIt.selectedItemPosition + 1)
    when (menuBinding.spSpeed.selectedItemPosition) {
      0 -> mPullToRefreshLayout.setAnimationSpeed(HeaderAnimSpeed.FAST)
      1 -> mPullToRefreshLayout.setAnimationSpeed(HeaderAnimSpeed.SLOW)
    }
    when (menuBinding.spTextAnim.selectedItemPosition) {
      0 -> mPullToRefreshLayout.setHeaderTextAnim(HeaderTextAnim.ROTATE_CW)
      1 -> mPullToRefreshLayout.setHeaderTextAnim(HeaderTextAnim.ROTATE_ACW)
      2 -> mPullToRefreshLayout.setHeaderTextAnim(HeaderTextAnim.FADE)
      3 -> mPullToRefreshLayout.setHeaderTextAnim(HeaderTextAnim.ZOOM)
    }
    when (menuBinding.spLoopAnim.selectedItemPosition) {
      0 -> mPullToRefreshLayout.setHeaderLoopAnim(HeaderLoopAnim.ZOOM)
      1 -> mPullToRefreshLayout.setHeaderLoopAnim(HeaderLoopAnim.FADE)
    }
    mPullToRefreshLayout.setColorAnimEnable(menuBinding.swEnable.isChecked)
    if (menuBinding.rbLight.isChecked) {
      mPullToRefreshLayout.setHeaderBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
      mPullToRefreshLayout.setHeaderTextColor(ContextCompat.getColor(this, R.color.colorLabelDark))
    } else if (menuBinding.rbDark.isChecked) {
      mPullToRefreshLayout.setHeaderBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
      mPullToRefreshLayout.setHeaderTextColor(ContextCompat.getColor(this, R.color.colorLabelLight))
    }
    mPullToRefreshLayout.setHeaderText(menuBinding.edtLabel.text.toString())
  }

  override fun onBackPressed() {
    if (fabMenu.isShowing) {
      fabMenu.closeMenu()
    } else {
      super.onBackPressed()
    }
  }

  private fun hideKeyboard() {
    val view = this.currentFocus
    if (view != null) {
      val imm = (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
      imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
  }
}