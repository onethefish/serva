package cn.fish.cloud.serva.mybatis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mybatis-plus.global-config.db-config")
public class ServaMybatisConfig {

    String dbType;
}
