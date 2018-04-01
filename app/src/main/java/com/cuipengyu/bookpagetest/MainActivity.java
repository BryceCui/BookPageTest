package com.cuipengyu.bookpagetest;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;


public class MainActivity extends AppCompatActivity {
    private FrameLayout flReadWidget;
    private MixTocBean1 mixTocBean;
    private int curTheme = -1;
    RelativeLayout mRlBookReadRoot;
    private BeaseReadView mPageWidget;
    private int currentChapter = 1;
    private boolean startRead = false;
    private String data;
    private String bookId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flReadWidget = findViewById(R.id.flReadWidget);
        mRlBookReadRoot = findViewById(R.id.mRlBookReadRoot);
        curTheme = SettingManager.getInstance().getReadTheme();
        ThemeManager.setReaderTheme(curTheme, mRlBookReadRoot);
        getDta();

    }

    public void getDta() {
        RetrofitBuilder.build().post1("/mix-atoc/57206c3539a913ad65d35c7b", new HttpEngine.CallBack<MixTocBean1>() {
            @Override
            public void onSuccess(MixTocBean1 baseBean) {
                Log.e("bbbbbbbbbbbbb", baseBean.getMixToc().getChaptersCount1() + "");
                setT(baseBean);
                data = mixTocBean.getMixToc().getChapters().get(currentChapter).getLink();
                bookId = mixTocBean.getMixToc().getBook();
                mPageWidget = new PageWidget(MainActivity.this, mixTocBean.getMixToc().getBook(), mixTocBean.getMixToc().getChapters(), new ReadListener());
                flReadWidget.addView(mPageWidget);
                mPageWidget.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.reader_menu_bg_color),
                        ContextCompat.getColor(MainActivity.this, R.color.book_read_top_text));
                mPageWidget.setFontSize(AppScreenUtil.dpToPx(12));
                readCurrentChapter();
            }

            @Override
            public void onError(String errMsg) {
                Log.e("errMsg", errMsg);
            }

            @Override
            public void onFailure() {
                Log.e("errMsg", "----");

            }
        });

    }

    public void readCurrentChapter() {
        //查看本地是否有缓存章节文件
        if (CacheManager.getInstance().getChapterFile(bookId, currentChapter) != null) {
            //存在
            showChapterRead(null, currentChapter);
        } else {
//            mPresenter.getChapterRead(mChapterList.get(currentChapter - 1).link, currentChapter);
            setdata(data,currentChapter);
        }
    }

    private void showChapterRead(ChapterBean1 data, int currentChapter) {
        if (data != null) {
            CacheManager.getInstance().saveChapterFile(bookId, currentChapter, data);
        }
        if (!startRead) {
            startRead = true;
            if (!mPageWidget.isPrepared) {
                mPageWidget.init(curTheme);
            } else {
                mPageWidget.jumpToChapter(currentChapter);
            }
        }
    }

    public void  setdata(String data, final int Chapter) {
        String s = data.replaceAll("/", "%2F");
        String s1 = s.replaceAll("\\?", "%3F");
        RetrofitBuilder.build().post2("http://chapter2.zhuishushenqi.com/chapter/" + s1, new HttpEngine.CallBack<ChapterBean1>() {

            @Override
            public void onSuccess(ChapterBean1 chapters) {
                   showChapterRead(chapters,Chapter);
//                if (chapters.getChapter().getBody() != null) {
//                    CacheManager.getInstance().saveChapterFile(bookId, Chapter, chapters);
//                }
//                if (!startRead) {
//                    startRead = true;
//                    if (!mPageWidget.isPrepared) {
//                        mPageWidget.init(curTheme);
//                    } else {
//                        mPageWidget.jumpToChapter(currentChapter);
//                    }
//                }
            }

            @Override
            public void onError(String errMsg) {
                Log.e("RetrofitRequest---", errMsg.trim());
            }

            @Override
            public void onFailure() {

            }
        });


    }

    public void setT(MixTocBean1 t) {
        this.mixTocBean = t;
    }

    private class ReadListener implements OnReadStateChangeListener {

        @Override
        public void onChapterChanged(int chapter) {
            Log.e("chapter", chapter + "");
            currentChapter = chapter;
//            mTocListAdapter.setCurrentChapter(currentChapter);
            // 加载前一节 与 后三节
            for (int i = chapter - 1; i <= chapter + 3 && i <= mixTocBean.getMixToc().getChapters().size(); i++) {
                if (i > 0 && i != chapter
                        && CacheManager.getInstance().getChapterFile(bookId, i) == null) {
                    data = mixTocBean.getMixToc().getChapters().get(i-1).getLink();
                    setdata(data,i-1);
                }
            }
        }

        @Override
        public void onPageChanged(int chapter, int page) {
            Log.e("chapter2", chapter + "--" + page);

        }

        @Override
        public void onLoadChapterFailure(int chapter) {
            startRead = false;
            setdata(data,chapter);
        }

        @Override
        public void onCenterClick() {

        }

        @Override
        public void onFlip() {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
//                if (mTocListPopupWindow != null && mTocListPopupWindow.isShowing()) {
//                    mTocListPopupWindow.dismiss();
//                    gone(mTvBookReadTocTitle);
//                    visible(mTvBookReadReading, mTvBookReadCommunity, mTvBookReadChangeSource);
//                    return true;
//                } else if (isVisible(rlReadAaSet)) {
//                    gone(rlReadAaSet);
//                    return true;
//                } else if (isVisible(mLlBookReadBottom)) {
//                    hideReadBar();
//                    return true;
//                } else if (!CollectionsManager.getInstance().isCollected(bookId)) {
//                    showJoinBookShelfDialog(recommendBooks);
//                    return true;
//                }
                //                    return true;

                break;
            case KeyEvent.KEYCODE_MENU:
//                toggleReadBar();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (SettingManager.getInstance().isVolumeFlipEnable()) {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (SettingManager.getInstance().isVolumeFlipEnable()) {
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (SettingManager.getInstance().isVolumeFlipEnable()) {
                mPageWidget.nextPage();
                return true;// 防止翻页有声音
            }
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (SettingManager.getInstance().isVolumeFlipEnable()) {
                mPageWidget.prePage();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}
