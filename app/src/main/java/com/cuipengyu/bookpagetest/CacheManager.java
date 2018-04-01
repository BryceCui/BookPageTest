/**
 * Copyright 2016 JustWayward Team
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cuipengyu.bookpagetest;



import java.io.File;

/**
 * @author yuyh.
 * @date 2016/9/28.
 */
public class CacheManager {

    private static CacheManager manager;

    public static CacheManager getInstance() {
        return manager == null ? (manager = new CacheManager()) : manager;
    }



    private String getSearchHistoryKey() {
        return "searchHistory";
    }

    /**
     * 获取我收藏的书单列表
     *
     * @return
     */

    private String getCollectionKey() {
        return "my_book_lists";
    }



    private String getTocListKey(String bookId) {
        return bookId + "-bookToc";
    }

    public File getChapterFile(String bookId, int chapter) {
        File file = FileUtils.getChapterFile(bookId, chapter);
        if (file != null && file.length() > 50)
            return file;
        return null;
    }

    public void saveChapterFile(String bookId, int chapter, ChapterBean1 data) {
        File file = FileUtils.getChapterFile(bookId, chapter);
        String body=data.getChapter().getBody();
        FileUtils.writeFile(file.getAbsolutePath(), StringUtils.formatContent(body), false);
    }

    /**
     * 获取缓存大小
     *
     * @return
     */
    /**
     * 清除缓存
     *
     * @param clearReadPos 是否删除阅读记录
     */

}
