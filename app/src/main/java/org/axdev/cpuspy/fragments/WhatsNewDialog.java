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
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.axdev.cpuspy.BuildConfig;
import org.axdev.cpuspy.R;
import org.axdev.cpuspy.utils.Utils;

import butterknife.ButterKnife;

public class WhatsNewDialog extends DialogFragment {

    private Context mContext;
    private final String githubURL = "https://github.com/existz/cpuspy/commits/staging";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.mContext = this.getActivity();
        final Resources res = getResources();
        final MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .title(res.getString(R.string.menu_changelog))
                .customView(R.layout.changelog_layout, true)
                .negativeText(res.getString(R.string.action_changelog))
                .neutralText(res.getString(R.string.action_dismiss))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        try {
                            Utils.openURL(mContext, githubURL);
                        } catch (Exception e) {
                            // Dismiss dialog if unable to open intent
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .build();

        // Get versionName from gradle
        final View view = dialog.getCustomView();
        if (view != null) {
            final TextView version = ButterKnife.findById(view, R.id.changelog_version);
            version.setText("v" + BuildConfig.VERSION_NAME);
        }

        // Override dialog enter/exit animation
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        return dialog;
    }
}