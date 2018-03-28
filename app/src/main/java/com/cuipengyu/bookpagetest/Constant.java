package com.cuipengyu.bookpagetest;

/**
 * Created by mingren on 2018/3/28.
 */

public class Constant {
    //判断sd卡是否存在并且返回包名下文件路径
    public static String PATH_DATA = FileUtils.createRootPath(AppConnextUtil.getAppConnect()) + "/cache";
    public static String PATH_TXT = PATH_DATA + "/book/";
}
