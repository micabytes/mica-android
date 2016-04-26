package com.micabytes.gui;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// android:orientation
@SuppressWarnings("ClassUnconnectedToPackage")
public class LinearListView extends LinearLayout
{
    private Adapter adapter;
    @SuppressWarnings("ThisEscapedInObjectConstruction") private final Observer observer = new Observer(this);

    public LinearListView(Context context)
    {
        super(context);
    }

    public LinearListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public LinearListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setAdapter(Adapter adp)
    {
        if (adapter != null) {
          adapter.unregisterDataSetObserver(observer);
        }

        adapter = adp;
      adp.registerDataSetObserver(observer);
      observer.onChanged();
    }

  Adapter getAdapter() {
    return adapter;
  }

  private static final class Observer extends DataSetObserver
    {
        final LinearListView listView;

        private Observer(LinearListView lw)
        {
            listView = lw;
        }

        @SuppressWarnings("MethodWithMultipleLoops")
        @Override
        public void onChanged()
        {
            List<View> oldViews = new ArrayList<>(listView.getChildCount());

            for (int i = 0; i < listView.getChildCount(); i++)
                oldViews.add(listView.getChildAt(i));

            Iterator<View> itr = oldViews.iterator();

            listView.removeAllViews();

            for (int i = 0; i < listView.getAdapter().getCount(); i++)
            {
                View convertView = itr.hasNext() ? itr.next() : null;
                listView.addView(listView.getAdapter().getView(i, convertView, listView));
            }
            super.onChanged();
        }

        @Override
        public void onInvalidated()
        {
            listView.removeAllViews();
            super.onInvalidated();
        }
    }
}
