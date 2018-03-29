package com.cuipengyu.bookpagetest;

import android.util.Log;


import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by peirato on 2017/2/13.
 */

public class RetrofitBuilder implements HttpEngine {
    private Retrofit retrofit;
    private static RetrofitBuilder retrofitBuilder;
    private OkHttpClient mOkHttpClient;
    private Map<String, String> params;
    private static String url;

    private RetrofitBuilder() {
        mOkHttpClient = new OkHttpClient.Builder()
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Response response = chain.proceed(request);
                        Log.e("RetrofitRequest------", response + "");
                        return response;
                    }
                }).build();
        retrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl("http://api.zhuishushenqi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        params = new HashMap<>();

    }

    public static RetrofitBuilder build() {

        retrofitBuilder = new RetrofitBuilder();
        return retrofitBuilder;

    }

    @Override
    public <B extends BaseBean> void post(String url, final CallBack<B> callBack) {
        RetrofitService server = retrofit.create(RetrofitService.class);

        final Type[] types = callBack.getClass().getGenericInterfaces();
        final Type finalNeedType = MethodHandler(types).get(0);

        server.post(url, params) //请求完成后在io线程中执行 //请求在新的线程中执行
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("RetrofitBuilder", e.getMessage());
                        callBack.onFailure();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String s = responseBody.string();
                            B data = new Gson().fromJson(s, finalNeedType);
                            if (data.isOk()) {
                                callBack.onSuccess(data);
                                Log.e("data", data.toString());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                });

    }

    @Override
    public <B> void post1(String url, final CallBack<B> callBack) {
        RetrofitService server = retrofit.create(RetrofitService.class);
        server.post1(url, params) //请求完成后在io线程中执行 //请求在新的线程中执行
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MixTocBean1>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(MixTocBean1 mixTocBean1) {
                        if (mixTocBean1.getMixToc().isOk())
                            callBack.onFailure();
                        callBack.onSuccess(mixTocBean1);

                    }
                });
    }


    @Override
    public void post(String url, final BaseCallBack callBack) {
        RetrofitService server = retrofit.create(RetrofitService.class);
        server.post(url, params) //请求完成后在io线程中执行 //请求在新的线程中执行
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseBody>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
//                        Log.e("RetrofitBuilder ---", e.getMessage() + "");

                        callBack.onFailure();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                        try {
                            callBack.onSuccess(responseBody.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                            callBack.onError();
                        }

                    }


                });

    }


    @Override
    public RetrofitBuilder param(String key, String value) {
        params.put(key, value);
        return retrofitBuilder;
    }

    public RetrofitBuilder param(Map<String, String> map) {
        params.putAll(map);
        return retrofitBuilder;
    }

    public interface RetrofitService {

        @GET()
        Observable<ResponseBody> post(@Url String url,
                                      @QueryMap Map<String, String> map);

        @GET()
        Observable<MixTocBean1> post1(@Url String url,
                                      @QueryMap Map<String, String> map);
//        /**
//         * 获取书籍的章节总列表
//         * @param bookId
//         * @param view 默认参数为:chapters
//         * @return
//         */
//        @GET("/mix-atoc/{bookId}")
//        Single<BookChapterPackage> getBookChapterPackage(@Path("bookId") String bookId, @Query("view") String view);
//
//        /**
//         * 章节的内容
//         * 这里采用的是同步请求。
//         * @param url
//         * @return
//         */
//        @GET("http://chapter2.zhuishushenqi.com/chapter/{url}")
//        Single<ChapterInfoPackage> getChapterInfoPackage(@Path("url") String url);

    }

    private List<Type> MethodHandler(Type[] types) {
        List<Type> needtypes = new ArrayList<>();

        for (Type paramType : types) {
            if (paramType instanceof ParameterizedType) {
                Type[] parentypes = ((ParameterizedType) paramType).getActualTypeArguments();
                for (Type childtype : parentypes) {
                    needtypes.add(childtype);
                    if (childtype instanceof ParameterizedType) {
                        Type[] childtypes = ((ParameterizedType) childtype).getActualTypeArguments();
                        for (Type type : childtypes) {
                            needtypes.add(type);
                        }
                    }
                }
            }
        }
        return needtypes;
    }

    static void setBase(String url1) {
        url = url1;
    }
}
