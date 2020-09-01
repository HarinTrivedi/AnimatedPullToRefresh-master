package com.hlabexamples.animatedpulltorefresh

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * Created in Android_animated_pull_to_refresh_control-master on 23/03/17.
 */
class ItemListAdapter(private val context: Context) : RecyclerView.Adapter<ItemListAdapter.ItemViewHolder?>() {
  private var items: MutableList<String> = ArrayList()
  private var inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

  init {
    for (i in 0..19) {
      items.add("List Item " + (i + 1))
    }
  }

  override fun getItemCount(): Int {
    return items.size
  }

  class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tv: TextView? = itemView.findViewById<View?>(android.R.id.text1) as TextView
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
    return ItemViewHolder(inflater.inflate(android.R.layout.simple_list_item_1, parent, false))
  }

  override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
    holder.tv?.setTextColor(Color.BLACK)
    holder.tv?.text = items.get(position)
  }
}