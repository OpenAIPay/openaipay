package cn.openaipay.bootstrap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AiPay应用类
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@SpringBootApplication(scanBasePackages = "cn.openaipay")
@MapperScan("cn.openaipay.infrastructure")
public class AiPayApplication {

    /**
     * 应用启动入口。
     */
    public static void main(String[] args) {
        SpringApplication.run(AiPayApplication.class, args);
    }
}
