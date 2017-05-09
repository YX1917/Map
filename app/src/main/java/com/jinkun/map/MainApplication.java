package com.jinkun.map;

import android.app.Application;

import com.fengmap.android.FMMapSDK;

/**
 * Created by Xxyou on 2017/4/17.
 */

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        //初始化SDK
        FMMapSDK.init(this);
        super.onCreate();
    }
}
