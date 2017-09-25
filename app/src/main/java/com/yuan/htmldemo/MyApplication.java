package com.yuan.htmldemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by shucheng.qu on 2017/9/25.
 */

public class MyApplication extends Application {

    public static Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
    }
}
