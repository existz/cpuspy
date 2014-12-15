package org.axdev.cpuspy.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;

import com.afollestad.materialdialogs.MaterialDialog;

import org.axdev.cpuspy.R;

public class AboutDialog extends DialogFragment {

    private static final String VERSION_UNAVAILABLE = "N/A";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        PackageManager pm = getActivity().getPackageManager();
        String packageName = getActivity().getPackageName();
        String versionName;
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;
       } catch (PackageManager.NameNotFoundException e) {
            versionName = VERSION_UNAVAILABLE;
       }

        return new MaterialDialog.Builder(getActivity())
            .title(Html.fromHtml(getString(R.string.app_name_and_version, versionName)))
            .content(Html.fromHtml(getString(R.string.settings_message_about)))
            .positiveText(android.R.string.ok)
            .icon(R.drawable.icon)
            .show();
    }
}
