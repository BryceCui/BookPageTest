package com.cuipengyu.bookpagetest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FrameLayout flReadWidget;
    private MixTocBean1.MixTocBean mixTocBean=new MixTocBean1.MixTocBean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flReadWidget = findViewById(R.id.flReadWidget);
        RetrofitBuilder.build().post1("mix-atoc/57206c3539a913ad65d35c7b", new HttpEngine.CallBack<MixTocBean1>() {
            @Override
            public void onSuccess(MixTocBean1 baseBean) {
                Log.e("b",baseBean.getMixToc().getChaptersCount1()+"");
                setdata(baseBean.getMixToc().getChapters().get(1).getLink());
            }

            @Override
            public void onError(String errMsg) {

            }

            @Override
            public void onFailure() {

            }
        });

    }

    public void setdata(String data) {
        RetrofitBuilder.build().post("http://chapter2.zhuishushenqi.com/chapter/" + data, new HttpEngine.BaseCallBack() {
            @Override
            public void onSuccess(String result) {
            }

            @Override
            public void onFailure() {

            }

            @Override
            public void onError() {

            }
        });
    }
}
