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


import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.jaredrummler.android.processes.models.AndroidAppProcess;

import org.axdev.cpuspy.adapters.ProcessListAdapter;
import org.axdev.cpuspy.utils.AndroidAppProcessLoader;

import java.util.List;

public class ProcessListFragment extends ListFragment implements AndroidAppProcessLoader.Listener {

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ListView list = getListView();
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //list.setDivider(null);
        //list.setDividerHeight(0);
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
        dialog.show(getActivity().getFragmentManager(), "ProcessInfoDialog");
    }
}