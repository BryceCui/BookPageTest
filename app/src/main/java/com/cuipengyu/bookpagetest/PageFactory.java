package com.cuipengyu.bookpagetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Created by cuipengyu on 2018/3/28.
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
    private int curEndPos = 0, curBeginPos = 0, tempBeginPos, tempEndPos;
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
    //实体类
    private List<MixTocBean1.MixTocBean.ChaptersBean> chaptersList;
    //监听
    private OnReadStateChangeListener listener;
    //内容的范围
    private Rect rectF;

    PageFactory(Context context, String bookId, List<MixTocBean1.MixTocBean.ChaptersBean> chapters) {
        this(context, AppScreenUtil.getAppWidth(), AppScreenUtil.getAppHeight(), 15, bookId, chapters);
    }

    PageFactory(Context context, int Width, int Height, int FontSize, String bookId, List<MixTocBean1.MixTocBean.ChaptersBean> chapters) {
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
    public synchronized void onDraw(Canvas canvas) {
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
            canvas.drawText(chaptersList.get(currentChapter - 1).getTitle(), marginWidth, y, mTitlePaint);
            y += mLineSpace + mNumFontSize;
            // 绘制阅读页面文字
            for (String line : mLines) {
                Log.e("line---", line);
                y += mLineSpace;
                //endsWith() 方法用于测试字符串是否以指定的后缀结束。
                if (line.endsWith("@")) {
                    /**
                     *   beginIndex -- 起始索引（包括）
                     *   endIndex -- 结束索引（不包括）
                     *   public String substring(int beginIndex)
                     *   public String substring(int beginIndex, int endIndex)
                     */

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
            //保存阅读进度
            SettingManager.getInstance().saveReadProgress(bookId, currentChapter, curBeginPos, curEndPos);
        }

    }

    /**
     * 指针移到上一页页首
     */
    private void pageUp() {
        String strParagraph = "";
        Vector<String> lines = new Vector<>(); // 页面行
        int paraSpace = 0;
        mPageLineCount = mVisibleHeight / (mFontSize + mLineSpace);
        while ((lines.size() < mPageLineCount) && (curBeginPos > 0)) {
            Vector<String> paraLines = new Vector<>(); // 段落行
            byte[] parabuffer = readParagraphBack(curBeginPos); // 1.读取上一个段落

            curBeginPos -= parabuffer.length; // 2.变换起始位置指针
            try {
                strParagraph = new String(parabuffer, charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            strParagraph = strParagraph.replaceAll("\r\n", "  ");
            strParagraph = strParagraph.replaceAll("\n", " ");

            while (strParagraph.length() > 0) { // 3.逐行添加到lines
                int paintSize = mPaint.breakText(strParagraph, true, mVisibleWidth, null);
                paraLines.add(strParagraph.substring(0, paintSize));
                strParagraph = strParagraph.substring(paintSize);
            }
            lines.addAll(0, paraLines);

            while (lines.size() > mPageLineCount) { // 4.如果段落添加完，但是超出一页，则超出部分需删减
                try {
                    curBeginPos += lines.get(0).getBytes(charset).length; // 5.删减行数同时起始位置指针也要跟着偏移
                    lines.remove(0);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            curEndPos = curBeginPos; // 6.最后结束指针指向下一段的开始处
            paraSpace += mLineSpace;
            mPageLineCount = (mVisibleHeight - paraSpace) / (mFontSize + mLineSpace); // 添加段落间距，实时更新容纳行数
        }
    }

    //根据起始位置指针，读取一页内容
    private Vector<String> pageDown() {
        String strParagraph = "";
        Vector<String> lines = new Vector<>();
        int paraSpace = 0;
        //总行数=文字总可见区域/字体大小和行间距
        mPageLineCount = mVisibleHeight / (mFontSize + mLineSpace);
        while ((lines.size() < mPageLineCount) && (curEndPos < mbBufferLen)) {
            byte[] parabuffer = readParagraphForward(curEndPos);
            curEndPos += parabuffer.length;
            try {
                strParagraph = new String(parabuffer, charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            strParagraph = strParagraph.replaceAll("\r\n", "  ")
                    .replaceAll("\n", " "); // 段落中的换行符去掉，绘制的时候再换行

            while (strParagraph.length() > 0) {
                int paintSize = mPaint.breakText(strParagraph, true, mVisibleWidth, null);
                lines.add(strParagraph.substring(0, paintSize));
                strParagraph = strParagraph.substring(paintSize);
                if (lines.size() >= mPageLineCount) {
                    break;
                }
            }
            lines.set(lines.size() - 1, lines.get(lines.size() - 1) + "@");
            if (strParagraph.length() != 0) {
                try {
                    curEndPos -= (strParagraph).getBytes(charset).length;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            paraSpace += mLineSpace;
            mPageLineCount = (mVisibleHeight - paraSpace) / (mFontSize + mLineSpace);
        }
        return lines;
    }

    /**
     * 获取最后一页的内容。比较繁琐，待优化
     *
     * @return
     */
    public Vector<String> pageLast() {
        String strParagraph = "";
        Vector<String> lines = new Vector<>();
        currentPage = 0;
        while (curEndPos < mbBufferLen) {
            int paraSpace = 0;
            mPageLineCount = mVisibleHeight / (mFontSize + mLineSpace);
            curBeginPos = curEndPos;
            while ((lines.size() < mPageLineCount) && (curEndPos < mbBufferLen)) {
                byte[] parabuffer = readParagraphForward(curEndPos);
                curEndPos += parabuffer.length;
                try {
                    strParagraph = new String(parabuffer, charset);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                strParagraph = strParagraph.replaceAll("\r\n", "  ");
                strParagraph = strParagraph.replaceAll("\n", " "); // 段落中的换行符去掉，绘制的时候再换行

                while (strParagraph.length() > 0) {
                    int paintSize = mPaint.breakText(strParagraph, true, mVisibleWidth, null);
                    lines.add(strParagraph.substring(0, paintSize));
                    strParagraph = strParagraph.substring(paintSize);
                    if (lines.size() >= mPageLineCount) {
                        break;
                    }
                }
                lines.set(lines.size() - 1, lines.get(lines.size() - 1) + "@");

                if (strParagraph.length() != 0) {
                    try {
                        curEndPos -= (strParagraph).getBytes(charset).length;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                paraSpace += mLineSpace;
                mPageLineCount = (mVisibleHeight - paraSpace) / (mFontSize + mLineSpace);
            }
            if (curEndPos < mbBufferLen) {
                lines.clear();
            }
            currentPage++;
        }
        //SettingManager.getInstance().saveReadProgress(bookId, currentChapter, curBeginPos, curEndPos);
        return lines;
    }

    /**
     * 读取下一段落
     *
     * @param curEndPos 当前页结束位置指针
     * @return
     */
    private byte[] readParagraphForward(int curEndPos) {
        byte b0;
        int i = curEndPos;
        while (i < mbBufferLen) {
            b0 = mbBuff.get(i++);
            if (b0 == 0x0a) {
                break;
            }
        }
        int nParaSize = i - curEndPos;
        byte[] buf = new byte[nParaSize];
        for (i = 0; i < nParaSize; i++) {
            buf[i] = mbBuff.get(curEndPos + i);
        }
        return buf;
    }

    /**
     * 读取上一段落
     *
     * @param curBeginPos 当前页起始位置指针
     * @return
     */
    private byte[] readParagraphBack(int curBeginPos) {
        byte b0;
        int i = curBeginPos - 1;
        while (i > 0) {
            b0 = mbBuff.get(i);
            if (b0 == 0x0a && i != curBeginPos - 1) {
                i++;
                break;
            }
            i--;
        }
        int nParaSize = curBeginPos - i;
        byte[] buf = new byte[nParaSize];
        for (int j = 0; j < nParaSize; j++) {
            buf[j] = mbBuff.get(i + j);
        }
        return buf;
    }

    public boolean hasNextPage() {
        return currentChapter < chaptersList.size() || curEndPos < mbBufferLen;
    }

    public boolean hasPrePage() {
        return currentChapter > 1 || (currentChapter == 1 && curBeginPos > 0);
    }

    /**
     * 跳转下一页
     */
    public BookStatus nextPage() {
        if (!hasNextPage()) { // 最后一章的结束页
            return BookStatus.NO_NEXT_PAGE;
        } else {
            tempChapter = currentChapter;
            tempBeginPos = curBeginPos;
            tempEndPos = curEndPos;
            if (curEndPos >= mbBufferLen) { // 中间章节结束页
                currentChapter++;
                int ret = openBook(currentChapter, new int[]{0, 0}); // 打开下一章
                if (ret == 0) {
                    onLoadChapterFailure(currentChapter);
                    currentChapter--;
                    curBeginPos = tempBeginPos;
                    curEndPos = tempEndPos;
                    return BookStatus.NEXT_CHAPTER_LOAD_FAILURE;
                } else {
                    currentPage = 0;
                    onChapterChanged(currentChapter);
                }
            } else {
                curBeginPos = curEndPos; // 起始指针移到结束位置
            }
            mLines.clear();
            mLines = pageDown(); // 读取一页内容
            onPageChanged(currentChapter, ++currentPage);
        }
        return BookStatus.LOAD_SUCCESS;
    }

    /**
     * 跳转上一页
     */
    public BookStatus prePage() {
        if (!hasPrePage()) { // 第一章第一页
            return BookStatus.NO_PRE_PAGE;
        } else {
            // 保存当前页的值
            tempChapter = currentChapter;
            tempBeginPos = curBeginPos;
            tempEndPos = curEndPos;
            if (curBeginPos <= 0) {
                currentChapter--;
                int ret = openBook(currentChapter, new int[]{0, 0});
                if (ret == 0) {
                    onLoadChapterFailure(currentChapter);
                    currentChapter++;
                    return BookStatus.PRE_CHAPTER_LOAD_FAILURE;
                } else { // 跳转到上一章的最后一页
                    mLines.clear();
                    mLines = pageLast();
                    onChapterChanged(currentChapter);
                    onPageChanged(currentChapter, currentPage);
                    return BookStatus.LOAD_SUCCESS;
                }
            }
            mLines.clear();
            pageUp(); // 起始指针移到上一页开始处
            mLines = pageDown(); // 读取一页内容
            onPageChanged(currentChapter, --currentPage);
        }
        return BookStatus.LOAD_SUCCESS;
    }

    public void cancelPage() {
        currentChapter = tempChapter;
        curBeginPos = tempBeginPos;
        curEndPos = curBeginPos;

        int ret = openBook(currentChapter, new int[]{curBeginPos, curEndPos});
        if (ret == 0) {
            onLoadChapterFailure(currentChapter);
            return;
        }
        mLines.clear();
        mLines = pageDown();
    }

    /**
     * 获取当前阅读位置
     *
     * @return index 0：起始位置 1：结束位置
     */
    public int[] getPosition() {
        return new int[]{currentChapter, curBeginPos, curEndPos};
    }

    public String getHeadLineStr() {
        if (mLines != null && mLines.size() > 1) {
            return mLines.get(0);
        }
        return "";
    }

    /**
     * 设置字体大小
     *
     * @param fontsize 单位：px
     */
    public void setTextFont(int fontsize) {
//        LogUtils.i("fontSize=" + fontsize);
        mFontSize = fontsize;
        mLineSpace = mFontSize / 5 * 2;
        mPaint.setTextSize(mFontSize);
        mPageLineCount = mVisibleHeight / (mFontSize + mLineSpace);
        curEndPos = curBeginPos;
        nextPage();
    }

    /**
     * 设置字体颜色
     *
     * @param textColor
     * @param titleColor
     */
    public void setTextColor(int textColor, int titleColor) {
        mPaint.setColor(textColor);
        mTitlePaint.setColor(titleColor);
    }

    public int getTextFont() {
        return mFontSize;
    }

    /**
     * 根据百分比，跳到目标位置
     *
     * @param persent
     */
    public void setPercent(int persent) {
        float a = (float) (mbBufferLen * persent) / 100;
        curEndPos = (int) a;
        if (curEndPos == 0) {
            nextPage();
        } else {
            nextPage();
            prePage();
            nextPage();
        }
    }

    public void setBgBitmap(Bitmap BG) {
        mBookPageBg = BG;
    }
//    public void convertBetteryBitmap() {
//        batteryView = (ProgressBar) LayoutInflater.from(mContext).inflate(R.layout.layout_battery_progress, null);
//        batteryView.setProgressDrawable(ContextCompat.getDrawable(mContext,
//                SettingManager.getInstance().getReadTheme() < 4 ?
//                        R.drawable.seekbar_battery_bg : R.drawable.seekbar_battery_night_bg));
//        batteryView.setProgress(battery);
//        batteryView.setDrawingCacheEnabled(true);
//        batteryView.measure(View.MeasureSpec.makeMeasureSpec(ScreenUtils.dpToPxInt(26), View.MeasureSpec.EXACTLY),
//                View.MeasureSpec.makeMeasureSpec(ScreenUtils.dpToPxInt(14), View.MeasureSpec.EXACTLY));
//        batteryView.layout(0, 0, batteryView.getMeasuredWidth(), batteryView.getMeasuredHeight());
//        batteryView.buildDrawingCache();
//        //batteryBitmap = batteryView.getDrawingCache();
//        // tips: @link{https://github.com/JustWayward/BookReader/issues/109}
//        batteryBitmap = Bitmap.createBitmap(batteryView.getDrawingCache());
//        batteryView.setDrawingCacheEnabled(false);
//        batteryView.destroyDrawingCache();
//    }

//    public void setBattery(int battery) {
//        this.battery = battery;
//        convertBetteryBitmap();
//    }

    public void setTime(String time) {
        this.time = time;
    }

    public void recycle() {
        if (mBookPageBg != null && !mBookPageBg.isRecycled()) {
            mBookPageBg.recycle();
            mBookPageBg = null;
//            LogUtils.d("mBookPageBg recycle");
        }

//        if (batteryBitmap != null && !batteryBitmap.isRecycled()) {
//            batteryBitmap.recycle();
//            batteryBitmap = null;
//            LogUtils.d("batteryBitmap recycle");
//        }
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
