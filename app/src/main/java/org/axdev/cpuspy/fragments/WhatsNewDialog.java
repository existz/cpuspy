//-----------------------------------------------------------------------------
//
// (C) Rob Beane, 2015 <robbeane@gmail.com>
//
//-----------------------------------------------------------------------------

package org.axdev.cpuspy.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.axdev.cpuspy.BuildConfig;
import org.axdev.cpuspy.R;
import org.axdev.cpuspy.activity.ThemedActivity;
import org.axdev.cpuspy.utils.Utils;

import butterknife.ButterKnife;

public class WhatsNewDialog extends DialogFragment {

    private final String githubURL = "https://github.com/existz/cpuspy/commits/staging";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context mContext = this.getActivity();
        final ThemedActivity act = (ThemedActivity) mContext;
        final int primaryColor = act.primaryColor();
        final Resources res = getResources();
        final MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .title(res.getString(R.string.menu_changelog))
                .customView(R.layout.changelog_layout, true)
                .negativeText(res.getString(R.string.action_changelog))
                .neutralText(res.getString(R.string.action_dismiss))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            Utils.openChromeTab(getActivity(), githubURL, primaryColor );
                        } catch (Exception e) {
                            // Dismiss dialog if unable to open intent
                            dialog.dismiss();
                        }
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .build();

        // Get versionName from gradle
        final View view = dialog.getCustomView();
        if (view != null) {
            final TextView version = ButterKnife.findById(view, R.id.changelog_version);
            version.setText(String.format(res.getString(R.string.build_version), BuildConfig.VERSION_NAME));

            final TextView textView = ButterKnife.findById(view, R.id.changelog_text);
            final String[] changelog = res.getStringArray(R.array.changelog);
            final StringBuilder builder = new StringBuilder();
            for (final String s : changelog) {
                builder.append(s).append("\n");
                textView.setText(builder.toString());
            }
        }

        // Override dialog enter/exit animation
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        return dialog;
    }
}