// 声明应用的根包，Spring 会从这里向下扫描组件。
package com.liushuai.ticket;

// 导入 Spring Boot 的启动工具。
import org.springframework.boot.SpringApplication;
// 导入声明 Spring Boot 应用的注解。
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 启用自动配置、组件扫描和配置类能力。
@SpringBootApplication
// 定义票务平台的 Java 启动类。
public class TicketPlatformApplication {
    // JVM 启动时执行的程序入口。
    public static void main(String[] args) {
        // 创建 Spring 容器并启动内嵌 Web 服务器。
        SpringApplication.run(TicketPlatformApplication.class, args);
    }
}
