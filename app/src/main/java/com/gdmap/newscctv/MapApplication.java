package com.gdmap.newscctv;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;

import com.amap.api.navi.AMapNavi;

/**
 * Created by jinjiarui on 2017/9/29.
 */

public class MapApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        AMapNavi.setApiKey(this,"key");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

}
