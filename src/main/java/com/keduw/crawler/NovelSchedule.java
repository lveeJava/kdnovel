package com.keduw.crawler;

import com.keduw.model.Chapter;
import com.keduw.model.Novel;
import com.keduw.model.NovelColl;
import com.keduw.service.ChapterService;
import com.keduw.service.NovelService;
import com.keduw.util.ApplicationUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

//小说爬虫
@Component
public class NovelSchedule {

    private volatile BlockingQueue<NovelColl> novelQueue = new LinkedBlockingQueue<NovelColl>(10000 * 10);

    //每月10凌晨3点启动爬取小说
    @Scheduled(cron = "0 39 15 * * ?")
    public void novelCollect() throws Exception{
        if(isOpen){
            ReentrantLock lock = new ReentrantLock();
            NovelCrawler crawl = new NovelCrawler("crawl",true, novelQueue, lock);
            BaseInfoThread saveInfo = new BaseInfoThread(novelQueue);
            Thread thread = new Thread(saveInfo);
            thread.start();
            crawl.start(5);
        }
    }

    //每天3点检查连载中小说的章节更新情况
    @Scheduled(cron = "0 0 3 * * ?")
    public void infoCheck() throws Exception{
        if(isOpen){
            //小说总数
            NovelService novelService = (NovelService) ApplicationUtil.getBean("novelService");
            int counts = novelService.getNovelCount();
            int checkTimes = counts / 100;
            checkTimes = counts % 100 == 0 ? checkTimes : checkTimes + 1;
            int collStart = 1;
            BlockingQueue<List<Chapter>> queue = new LinkedBlockingQueue<List<Chapter>>(10000 * 10);
            // 分批次检查小说的章节
            for(int i = 0; i < checkTimes; i++) {
                while (collStart <= checkTimes) {
                    List<Novel> novelList = novelService.getNovelList(collStart, 100);
                    for(Novel novel : novelList){
                        if(novel.getStatus().equals("连载中")){
                            CheckCrawler crawler = new CheckCrawler("crawl",true, novel, queue);
                            crawler.start(1);
                        }
                    }
                    collStart ++;
                }
            }

        }
    }

    //每月1号凌晨3点爬取章节内容
    @Scheduled(cron = "0 16 23 * * ?")
    public void infoCollect() throws Exception{
        if(isOpen){
            //获取总章节数
            ChapterService chapterService = (ChapterService) ApplicationUtil.getBean("chapterService");
            int counts = chapterService.getInfoCounts();
            int times = counts / size;
            times = counts % size == 0 ? times : times + 1;
            //初始化第一批数据
            List<Chapter> chapterList = chapterService.getChapterList(init , size);
            for(Chapter chapter : chapterList){
                chapterQueue.add(chapter);
            }
            //启动爬取线程
            ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(5));
            for(int i = 0; i < 5; i++){
                NovelCrawelThread thread = new NovelCrawelThread(chapterQueue, updateQueue);
                executor.execute(thread);
            }
            //启动爬取下一页和数据保存线程
            for(int i = 0; i < 5; i++){
                NextContentThread nextPage = new NextContentThread(chapterQueue, updateQueue);
                executor.execute(nextPage);
            }
            System.out.println("爬虫线程启动总数：" + executor.getPoolSize());
            FullContentThread novelContent = new FullContentThread(updateQueue);
            Thread contentThread = new Thread(novelContent);
            contentThread.start();

            for(int i = 1; i < times; i++){
                chapterList = chapterService.getChapterList(i , size);
                for(Chapter chapter : chapterList){
                    chapterQueue.add(chapter);
                }
            }
            executor.shutdown();
        }
    }

    private boolean isOpen = false; //启动开关，日常关闭
    private int init = 0;
    private int size = 30;
    private volatile BlockingQueue<Chapter> chapterQueue = new LinkedBlockingQueue<Chapter>(10000 * 10); //存取待爬取内容的章节
    private volatile BlockingQueue<Chapter> updateQueue = new LinkedBlockingQueue<Chapter>(10000 * 10); //存取待更新到数据库的章节
}
