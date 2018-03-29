package com.cuipengyu.bookpagetest;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Set;

/**
 * Created by cuipengyu on 2018/3/29.
 */

public class SharedPreferencesUtil {
    private static SharedPreferencesUtil mUtil;
    private Context mContext;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    public synchronized static SharedPreferencesUtil getInstance() {
        return mUtil;
    }

    public static void init(Context context, String saveName, int mode) {
        mUtil = new SharedPreferencesUtil();
        mUtil.mContext = context;
        mUtil.mPreferences = mUtil.mContext.getSharedPreferences(saveName, mode);
        mUtil.mEditor = mUtil.mPreferences.edit();
    }

    public boolean getBoolean(String key, boolean defaultVal) {
        return this.mPreferences.getBoolean(key, defaultVal);
    }

    public boolean getBoolean(String key) {
        return this.mPreferences.getBoolean(key, false);
    }


    public String getString(String key, String defaultVal) {
        return this.mPreferences.getString(key, defaultVal);
    }

    public String getString(String key) {
        return this.mPreferences.getString(key, null);
    }

    public int getInt(String key, int defaultVal) {
        return this.mPreferences.getInt(key, defaultVal);
    }

    public int getInt(String key) {
        return this.mPreferences.getInt(key, 0);
    }


    public float getFloat(String key, float defaultVal) {
        return this.mPreferences.getFloat(key, defaultVal);
    }

    public float getFloat(String key) {
        return this.mPreferences.getFloat(key, 0f);
    }

    public long getLong(String key, long defaultVal) {
        return this.mPreferences.getLong(key, defaultVal);
    }

    public long getLong(String key) {
        return this.mPreferences.getLong(key, 0l);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Set<String> getStringSet(String key, Set<String> defaultVal) {
        return this.mPreferences.getStringSet(key, defaultVal);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Set<String> getStringSet(String key) {
        return this.mPreferences.getStringSet(key, null);
    }

    public boolean exists(String key) {
        return mPreferences.contains(key);
    }


    public SharedPreferencesUtil putString(String key, String value) {
        mEditor.putString(key, value);
        mEditor.commit();
        return this;
    }

    public SharedPreferencesUtil putInt(String key, int value) {
        mEditor.putInt(key, value);
        mEditor.commit();
        return this;
    }

    public SharedPreferencesUtil putFloat(String key, float value) {
        mEditor.putFloat(key, value);
        mEditor.commit();
        return this;
    }

    public SharedPreferencesUtil putLong(String key, long value) {
        mEditor.putLong(key, value);
        mEditor.commit();
        return this;
    }

    public SharedPreferencesUtil putBoolean(String key, boolean value) {
        mEditor.putBoolean(key, value);
        mEditor.commit();
        return this;
    }

    public void commit() {
        mEditor.commit();
    }

    public SharedPreferencesUtil remove(String key) {
        mEditor.remove(key);
        mEditor.commit();
        return this;
    }

    public SharedPreferencesUtil removeAll() {
        mEditor.clear();
        mEditor.commit();
        return this;
    }
}
