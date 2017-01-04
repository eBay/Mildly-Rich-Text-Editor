package com.ebay.mildlyrichtexteditor;

import android.content.Intent;
import android.support.v7.app.AppCompatDelegate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class DemoAppActivityTest {
    private ActivityController<DemoAppActivity> controller;
    private DemoAppActivity activity;

    @Before
    public void setup() {
        activity = Robolectric.setupActivity(DemoAppActivity.class);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Test
    public void verifyLifeCycleCallbacks() {
        controller = Robolectric.buildActivity(DemoAppActivity.class);
        Intent intent = new Intent(RuntimeEnvironment.application, DemoAppActivity.class);
        intent.putExtra("activity_extra", "mExtra");
        activity = controller.withIntent(intent).create().start().resume().visible().get();
        controller.pause().stop().destroy();
    }
}