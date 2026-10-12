package top.mqxu.mybatis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyBatisApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MyBatisApplication.class);
        // 打印所有被注册的自动配置类名（看 MyBatisPlus 的类在不在）
        app.addInitializers(ctx -> {
            System.out.println("=== 已注册自动配置 ===");
            String[] names = ctx.getBeanFactory().getBeanNamesForType(
                    org.springframework.beans.factory.config.BeanFactoryPostProcessor.class, false, false);
            System.out.println("容器初始化了，包含 Bean 数: " + ctx.getBeanDefinitionCount());
        });
        app.run(args);
    }
}