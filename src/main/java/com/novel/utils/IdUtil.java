package com.novel.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @ProjectName: novelSpider
 * @Package: com.novel.utils
 * @ClassName: IdUtil
 * @Description: java类作用描述:生产随机数Id
 * @Author: 林浩东
 * @CreateDate: 2018/8/21/021 22:15
 * @UpdateUser: 更新者
 * @UpdateDate: 2018/8/21/021 22:15
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class IdUtil {


    public synchronized static long getId(){
    /*    long time = System.currentTimeMillis();
        Random random = new Random();
        random.setSeed(time);*/
        return Math.abs(ThreadLocalRandom.current().nextLong());

    }
}
