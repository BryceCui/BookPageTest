package com.cuipengyu.bookpagetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Created by mingren on 2018/3/28.
 */

public class PageFactory {
    private Context mContext;
    //屏幕宽高
    private int mWidth, mHeight;
    //文字区域宽高
    private int mVisibleHeight, mVisibleWidth;
    //间距
    private int marginHeight, marginWidth;
    //字体大小
    private int mFontSize, mNumFontSize;
    //每页行数
    private int mPageLineCount;
    //行间距
    private int mLineSpace;
    //字节长度
    private int mbBufferLen;
    //文件内存映射
    private MappedByteBuffer mbBuff;
    //页尾  页首  临时
    private int curEndPos = 0, curBeginPos = 0, tempBeginPos;
    //当前章节  临时章节
    private int currentChapter, tempChapter;
    //它支持线程的同步 访问它比访问ArrayList慢
    private Vector<String> mLines = new Vector<>();
    /**
     * 画笔
     * 标题画笔
     * 背景
     */
    private Paint mPaint;
    private Paint mTitlePaint;
    private Bitmap mBookPageBg;
    //时间
    private DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    //时间和百分比
    private int timeLen = 0, percentLen = 0;
    private String time;
    //书籍id
    private String bookId;
    //章节大小  当前页
    private int chapterSize = 0;
    private int currentPage = 1;
    //字符编码
    private String charset = "UTF-8";
    private List<BookMixAToc.mixToc.Chapters> chaptersList;
    private OnReadStateChangeListener listener;
    private Rect rectF;

    PageFactory(Context context, String bookId, List<BookMixAToc.mixToc.Chapters> chapters) {
        this(context, AppScreenUtil.getAppWidth(), AppScreenUtil.getAppHeight(), 15, bookId, chapters);
    }

    PageFactory(Context context, int Width, int Height, int FontSize, String bookId, List<BookMixAToc.mixToc.Chapters> chapters) {
        mContext = context;
        mWidth = Width;
        mHeight = Height;
        mFontSize = FontSize;
        mLineSpace = mFontSize / 5 * 2;
        mNumFontSize = AppScreenUtil.dpToPx(16);
        marginHeight = AppScreenUtil.dpToPx(15);
        marginWidth = AppScreenUtil.dpToPx(15);
        mVisibleHeight = mHeight - marginHeight * 2 - mNumFontSize * 2 - mLineSpace * 2;
        mVisibleWidth = mWidth - marginWidth * 2;
        mPageLineCount = mVisibleHeight / (mFontSize + mLineSpace);
        rectF = new Rect(0, 0, mWidth, mHeight);
        //设置 ANTI_ALIAS_FLAG 属性可以产生平滑的边缘
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(mFontSize);
        mPaint.setTextSize(ContextCompat.getColor(context, R.color.chapter_content_day));
        mPaint.setColor(Color.BLACK);
        mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitlePaint.setTextSize(mNumFontSize);
        mTitlePaint.setColor(ContextCompat.getColor(context, R.color.chapter_title_day));
        timeLen = (int) mTitlePaint.measureText("00:00");
        percentLen = (int) mTitlePaint.measureText("00.00%");
        this.bookId = bookId;
        this.chaptersList = chapters;
        time = dateFormat.format(new Date());
    }

    public File getBookFile(int chapter) {
        File file = FileUtils.getChapterFile(bookId, chapter);
        charset = FileUtils.getCharset(file.getAbsolutePath());
        Log.e("charset=", "" + charset);
        return file;
    }

    public void openBook() {
        openBook(new int[]{0, 0});
    }

    public void openBook(int[] postion) {
        openBook(1, postion);
    }

    public int openBook(int chapter, int[] position) {
        this.currentChapter = chapter;
        this.chapterSize = chaptersList.size();
        //如果当前章节大于章节总数 那就相等
        if (currentChapter > chapterSize)
            currentChapter = chapterSize;
        String path = getBookFile(currentChapter).getPath();
        try {
            File file = new File(path);
            long length = file.length();
            if (length > 10) {
                mbBufferLen = (int) length;
                /**
                 * 只有RandomAccessFile获取的Channel才能开启任意的这三种模式
                 * FileChannel.MapMode.READ_ONLY：得到的镜像只能读不能写
                 * FileChannel.MapMode.READ_WRITE：得到的镜像可读可写（既然可写了必然可读），对其写会直接更改到存储节点
                 * FileChannel.MapMode.PRIVATE：得到一个私有的镜像，其实就是一个(position, size)区域的副本罢了，也是可读可写，只不过写不会影响到存储节点，就是一个普通的ByteBuffer了
                 * long position(); // 获取当前操作到节点文件的哪个位置
                 */
                mbBuff = new RandomAccessFile(file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, length);
                curBeginPos = position[0];
                curEndPos = position[1];
                onChapterChanged(chapter);
                mLines.clear();
                return 1;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 绘制内容界面
     *
     * @param canvas
     */
    private synchronized void onDrow(Canvas canvas) {
        if (mLines.size() == 0) {
            curEndPos = curBeginPos;
            mLines = pageDown();
        }
        if (mLines.size() > 0) {
            //左移运算符，num << 1,相当于num乘以2
            int y = marginHeight + (mLineSpace << 1);
            //如果bitmap不为空，则绘制背景
            if (mBookPageBg != null) {
                canvas.drawBitmap(mBookPageBg, null, rectF, null);
            } else {
                //设置背景为白色
                canvas.drawColor(Color.WHITE);
            }
            //绘制标题
            canvas.drawText(chaptersList.get(currentChapter - 1).title, marginWidth, y, mTitlePaint);
            y += mLineSpace + mNumFontSize;
            // 绘制阅读页面文字
            for (String line : mLines) {
                Log.e("line---", line);
                y += mLineSpace;
                if (line.endsWith("@")) {
                    canvas.drawText(line.substring(0, line.length() - 1), marginWidth, y, mPaint);
                    y += mLineSpace;
                } else {
                    canvas.drawText(line, marginWidth, y, mPaint);
                }
                y += mFontSize;
            }
            // 绘制电池
            //            if (batteryBitmap != null) {
            //                canvas.drawBitmap(batteryBitmap, marginWidth + 2,
            //                        mHeight - marginHeight - ScreenUtils.dpToPxInt(12), mTitlePaint);
            //            }
            //绘制百分比
            float percent = (float) currentChapter * 100 / chapterSize;
            canvas.drawText(decimalFormat.format(percent) + "%", (mWidth - percentLen) / 2,
                    mHeight - marginHeight, mTitlePaint);
            //绘制时间
            String mTime = dateFormat.format(new Date());
            canvas.drawText(mTime, mWidth - marginWidth - timeLen, mHeight - marginHeight, mTitlePaint);

        }

    }

    private Vector<String> pageDown() {

        return mLines;
    }

    public void setOnReadStateChangeListener(OnReadStateChangeListener listener) {
        this.listener = listener;
    }

    private void onChapterChanged(int chapter) {
        if (listener != null)
            listener.onChapterChanged(chapter);
    }

    private void onPageChanged(int chapter, int page) {
        if (listener != null)
            listener.onPageChanged(chapter, page);
    }

    private void onLoadChapterFailure(int chapter) {
        if (listener != null)
            listener.onLoadChapterFailure(chapter);
    }
}
