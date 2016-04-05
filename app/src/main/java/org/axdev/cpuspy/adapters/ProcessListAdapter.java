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

package org.axdev.cpuspy.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.squareup.picasso.Picasso;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.utils.TypefaceHelper;
import org.axdev.cpuspy.utils.Utils;

import java.util.List;

import static org.axdev.cpuspy.picasso.AppIconRequestHandler.SCHEME_PNAME;

public class ProcessListAdapter extends BaseAdapter {

    private final List<AndroidAppProcess> processes;
    private final LayoutInflater inflater;
    private final Context context;
    private final Picasso picasso;
    private final int iconSize;

    public ProcessListAdapter(Context context, List<AndroidAppProcess> processes) {
        this.context = context.getApplicationContext();
        this.inflater = LayoutInflater.from(context);
        this.iconSize = Utils.toPx(context, 46);
        this.picasso = Picasso.with(context);
        this.processes = processes;
    }

    @Override public int getCount() {
        return processes.size();
    }

    @Override public AndroidAppProcess getItem(int position) {
        return processes.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_process, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AndroidAppProcess process = getItem(position);

        ImageView imageView = holder.find(R.id.imageView);
        TextView textView = holder.find(R.id.textView);
        TextView textViewSummary = holder.find(R.id.textView_summary);

        picasso.load(Uri.parse(SCHEME_PNAME + ":" + process.getPackageName()))
                .placeholder(android.R.drawable.sym_def_app_icon)
                .resize(iconSize, iconSize)
                .centerInside()
                .into(imageView);

        final Typeface robotoMedium = TypefaceHelper.mediumTypeface(context);
        final String packageName = Utils.getPackageName(context, process);
        textView.setTypeface(robotoMedium);
        textView.setText(packageName);
        textViewSummary.setText(process.name);

        return convertView;
    }

    private class ViewHolder {

        private final SparseArray<View> views = new SparseArray<>();

        private final View view;

        private ViewHolder(View view) {
            this.view = view;
            view.setTag(this);
        }

        private <T extends View> T find(int id) {
            View v = views.get(id);
            if (v == null) {
                v = view.findViewById(id);
                views.put(id, v);
            }
            //noinspection unchecked
            return (T) v;
        }

    }

}