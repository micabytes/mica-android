package com.micabytes.ui.component

import android.annotation.TargetApi
import android.content.Context
import android.database.DataSetObserver
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.Adapter
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.ListView
import androidx.databinding.BindingAdapter
import java.util.*

class LinearListView : LinearLayout {
  var adapter: Adapter? = null
    set(adp) {
      if (this.adapter != null) {
        this.adapter!!.unregisterDataSetObserver(observer)
      }
      field = adp
      adp?.registerDataSetObserver(observer)
      observer.onChanged()
    }
  private val observer = Observer(this)

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

  private class Observer internal constructor(internal val listView: LinearListView) : DataSetObserver() {

    override fun onChanged() {
      val oldViews = ArrayList<View>(listView.childCount)
      (0 until listView.childCount).mapTo(oldViews) { listView.getChildAt(it) }
      val itr = oldViews.iterator()
      listView.removeAllViews()
      for (i in 0 until listView.adapter!!.count) {
        val convertView = if (itr.hasNext()) itr.next() else null
        listView.addView(listView.adapter!!.getView(i, convertView, listView))
      }
      super.onChanged()
    }

    override fun onInvalidated() {
      listView.removeAllViews()
      super.onInvalidated()
    }
  }

  companion object {

    @JvmStatic
    @BindingAdapter("adapter")
    fun bindList(view: LinearListView, adapter: BaseAdapter) {
      view.adapter = adapter
    }

    @JvmStatic
    @BindingAdapter("adapter")
    fun bindList(view: ListView, adapter: BaseAdapter) {
      view.adapter = adapter
    }

  }

}
