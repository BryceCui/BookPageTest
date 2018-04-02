package com.cuipengyu.bookpagetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.List;

public class NoAiminWight extends BeaseReadView {
    public NoAiminWight(Context context, String bookId, List<MixTocBean1.MixTocBean.ChaptersBean> chaptersList, OnReadStateChangeListener listener) {
        super(context, bookId, chaptersList, listener);
    }

    @Override
    protected void drawNextPageAreaAndShadow(Canvas canvas) {

    }

    @Override
    protected void drawCurrentPageShadow(Canvas canvas) {

    }

    @Override
    protected void drawCurrentBackArea(Canvas canvas) {

    }

    @Override
    protected void drawCurrentPageArea(Canvas canvas) {

    }

    @Override
    protected void calcPoints() {

    }

    @Override
    protected void calcCornerXY(float x, float y) {

    }

    @Override
    protected void startAnimation() {

    }

    @Override
    protected void abortAnimation() {

    }

    @Override
    protected void restoreAnimation() {

    }

    @Override
    protected void setBitmaps(Bitmap mCurPageBitmap, Bitmap mNextPageBitmap) {

    }

    @Override
    public void setTheme(int theme) {

    }
}
