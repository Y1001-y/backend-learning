package top.mqxu.schedule.service;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class TaskService {

    /**
     * 效果一：在指定时间触发
     * cron：秒 分 时 日 月 周
     */
//    @Scheduled(cron = "0 53 8 * * ?")
//    public void triggerAtFixedTime() {
//        System.out.println("【指定时间触发】当前时间：" + LocalDateTime.now());
//    }

    /**
     * 效果二：每隔 3 秒触发
     * cron：0/3 * * * * ?  表示从第 0 秒起，每 3 秒执行一次
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void triggerEveryThreeSeconds() {
        System.out.println("【每隔3秒触发】当前时间：" + LocalDateTime.now());
    }
}
