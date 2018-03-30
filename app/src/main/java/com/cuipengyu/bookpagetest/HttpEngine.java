package com.cuipengyu.bookpagetest;


import java.util.Map;

/**
 * Created by peirato on 2017/3/1.
 */

public interface HttpEngine {


    <B > void post1(String url, CallBack<B > callBack);


    HttpEngine param(String key, String value);

    HttpEngine param(Map<String, String> map);

    interface CallBack<B  > {
        void onSuccess(B b);
        void onError(String errMsg);
        void onFailure();
    }

    interface BaseCallBack {

        void onSuccess(String result);

        void onFailure();

        void onError();

    }


}
