package org.axdev.cpuspy.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.View;
import android.widget.ImageView;

import org.axdev.cpuspy.R;

public class WelcomeActivity extends ActionBarActivity {

    private static final String WELCOME_SCREEN = "welcomeScreenShown";

    /** Initialize the Activity */
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inflate the view, stash the app context, and get all UI elements
        setContentView(R.layout.welcome_layout);

        // set Toolbar as ActionBar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_welcome);
        setSupportActionBar(mToolbar);

        ImageView imageView = (ImageView)findViewById(R.id.toolbar_welcome_image);
        // Animate states view sliding in from the right
        Animation slideIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_in_right);

        imageView.startAnimation(slideIn);
    }

    /** Finish activity when clicked */
    public void onClick(View view) {
        finish();
    }

    @Override protected void onDestroy() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(WELCOME_SCREEN, true);
        editor.commit();
        super.onDestroy();
    }
}