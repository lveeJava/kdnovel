package com.keduw.service;


import com.keduw.model.Chapter;

import java.util.List;

/**
 * @ProjectName: novelSpider
 * @Package: com.novel.service
 * @ClassName: ChapterService
 * @Description: java类作用描述
 * @Author: 林浩东
 */
public interface ChapterService {

     //插入章节
     void insertChapter(Chapter chapter);

     //更新章节列表
     void updateChapter(Chapter chapter);

     //通过小说id返回章节列表
     List<Chapter> getChapterList(int NovelId);
}
