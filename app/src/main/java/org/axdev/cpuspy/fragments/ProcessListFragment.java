/*
 * Copyright (C) 2015. Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.axdev.cpuspy.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.jaredrummler.android.processes.models.AndroidAppProcess;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.adapters.ProcessListAdapter;
import org.axdev.cpuspy.utils.AndroidAppProcessLoader;

import java.util.List;

public class ProcessListFragment extends ListFragment implements AndroidAppProcessLoader.Listener {

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ListView list = getListView();
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                int action = event.getAction();
                final View mProcessTitleBar = v.getRootView().findViewById(R.id.process_title_bar);
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        if (canScrollVertically(list)) {
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        if (canScrollVertically(list)) {
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                        }
                        break;
                }

                list.setOnScrollListener(new AbsListView.OnScrollListener() {
                    private int mLastFirstVisibleItem;

                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if (mLastFirstVisibleItem < firstVisibleItem && canScrollVertically(list)) {
                            ViewCompat.setElevation(mProcessTitleBar, getResources().getDimension(R.dimen.ab_elevation));
                        } else {
                            ViewCompat.setElevation(mProcessTitleBar, 0);
                            mLastFirstVisibleItem = firstVisibleItem;
                        }
                    }
                });

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });
        list.setDivider(null);
        list.setDividerHeight(0);
        new AndroidAppProcessLoader(getActivity(), this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override public void onComplete(List<AndroidAppProcess> processes) {
        setListAdapter(new ProcessListAdapter(getActivity(), processes));
    }

    @Override public void onListItemClick(ListView l, View v, int position, long id) {
        AndroidAppProcess process = (AndroidAppProcess) getListAdapter().getItem(position);
        ProcessInfoDialog dialog = new ProcessInfoDialog();
        Bundle args = new Bundle();
        args.putParcelable("process", process);
        dialog.setArguments(args);
        dialog.show(getActivity().getSupportFragmentManager(), "ProcessInfoDialog");
    }

    private boolean canScrollVertically(AbsListView view) {
        boolean canScroll = false;
        if (view != null && view.getChildCount() > 0) {
            // First item can be partially visible, top must be 0 for the item
            canScroll = view.getFirstVisiblePosition() != 0 || view.getChildAt(0).getTop() != 0;
        }
        return canScroll;
    }
}