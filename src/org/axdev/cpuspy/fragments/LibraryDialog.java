package org.axdev.cpuspy.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;

import com.afollestad.materialdialogs.Alignment;
import com.afollestad.materialdialogs.MaterialDialog;

import org.axdev.cpuspy.R;

public class LibraryDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
            .title(Html.fromHtml(getString(R.string.settings_title_library)))
            .titleAlignment(Alignment.CENTER)
            .content(Html.fromHtml(getString(R.string.settings_message_library)))
            .positiveText(android.R.string.ok)
            .show();
    }
}
