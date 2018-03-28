package com.cuipengyu.bookpagetest;

import android.app.Application;
import android.content.Context;

/**
 * App全局对象 ,manifest中声明name
 * Created by cuipengyu on 2018/3/14.
 */

public class AppConnextUtil extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        AppConnextUtil.context = getApplicationContext();
    }

    public static Context getAppConnect() {
        return AppConnextUtil.context;
    }

}
