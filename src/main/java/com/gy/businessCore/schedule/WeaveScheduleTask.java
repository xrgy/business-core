package com.gy.businessCore.schedule;

import com.gy.businessCore.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by gy on 2018/12/18.
 */
@Component
public class WeaveScheduleTask {
    //@Component泛指各种组件，就是说当我们的类不属于各种归类的时候（不属于@Controller、@Services等的时候），我们就可以使用@Component来标注这个类。
    //被@PostConstruct修饰的方法会在服务器加载Servle的时候运行，并且只会被服务器执行一次。PostConstruct在构造函数之后执行,init()方法之前执行。
    ScheduledExecutorService serviceSchedule = Executors.newScheduledThreadPool(15);

    @Autowired
    BusinessService businessService;


//    @PostConstruct
    public void scheduleCalBusScore() {
        serviceSchedule.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                businessService.calculateAllScore();
                System.out.println("start cal score");
            }
        }, 1, 3, TimeUnit.MINUTES);

    }

}
