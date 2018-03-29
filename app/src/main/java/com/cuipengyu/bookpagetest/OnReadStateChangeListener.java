package com.cuipengyu.bookpagetest;

/**
 * Created by cuipengyu on 2018/3/28.
 */

public interface OnReadStateChangeListener {
    void onChapterChanged(int chapter);

    void onPageChanged(int chapter, int page);

    void onLoadChapterFailure(int chapter);

    void onCenterClick();

    void onFlip();
}
