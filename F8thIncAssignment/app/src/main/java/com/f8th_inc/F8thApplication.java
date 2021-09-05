package com.f8th_inc;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class F8thApplication extends Application {

    private static F8thApplication mF8thApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mF8thApplication = this;
        startService(new Intent(this, VolumeService.class));
    }

    public static F8thApplication getF8thApplication() {
        return mF8thApplication;
    }

    public boolean isApplicationInBackground() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(getPackageName())) {
                return true;
            }
        }
        return false;
    }

}
