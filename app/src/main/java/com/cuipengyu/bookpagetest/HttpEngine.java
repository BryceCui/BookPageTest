package com.cuipengyu.bookpagetest;


import java.util.Map;

/**
 * Created by peirato on 2017/3/1.
 */

public interface HttpEngine {


    <B extends BaseBean> void post(String url, CallBack<B > callBack);
    <B > void post1(String url, CallBack<B > callBack);

    void post(String url, BaseCallBack callBack);

    HttpEngine param(String key, String value);

    HttpEngine param(Map<String, String> map);

    interface CallBack<B  > {

        void onSuccess(B b);
        void onSuccess(MixTocBean1  result);

        void onError(String errMsg);

        void onFailure();

    }

    interface BaseCallBack {

        void onSuccess(String result);

        void onFailure();

        void onError();

    }


}
