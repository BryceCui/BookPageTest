package com.cuipengyu.bookpagetest;

/**
 * 屏幕的工具类
 * Created by cuipengyu on 2018/3/14.
 */

public class AppScreenUtil {
//    private static AppScreenUtil mAppScreenUtil;
//
//    public static AppScreenUtil getmAppScreenUtil() {
//        if (mAppScreenUtil == null) {
//            mAppScreenUtil = new AppScreenUtil();
//        }
//        return mAppScreenUtil;
//    }

    //获取屏幕宽
    public static int getAppWidth() {
        return AppConnextUtil.getAppConnect().getResources().getDisplayMetrics().widthPixels;
    }

    //获取屏幕高
    public static int getAppHeight() {
        return AppConnextUtil.getAppConnect().getResources().getDisplayMetrics().heightPixels;
    }
    //dp转px
    public static int dpToPx(float dp) {
        float density = AppConnextUtil.getAppConnect().getResources().getDisplayMetrics().density;
        return (int) (density*dp+0.5f);
    }
}
