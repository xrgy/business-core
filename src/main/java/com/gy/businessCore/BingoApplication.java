package com.gy.businessCore;

import com.gy.businessCore.base.BaseRepositoryFactoryBean;
import com.gy.businessCore.schedule.WeaveScheduleTask;
import com.gy.businessCore.utils.ProperUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by gy on 2018/3/31.
 */
@SpringBootApplication
//@EnableAutoConfiguration
@EnableJpaRepositories(repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class)
public class BingoApplication {
    public static void main(String[] args){
        ProperUtil.SetConfInfo();
        SpringApplication.run(BingoApplication.class,args);
        WeaveScheduleTask task = new WeaveScheduleTask();
//        task.scheduleCalBusScore();
    }
}
