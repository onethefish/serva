package cn.fish.cloud.serva.file.operate.config;

import cn.fish.cloud.serva.file.util.FilePathUtil;
import cn.hutool.system.SystemUtil;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Data
@ToString
@Primary
@Configuration
@ConfigurationProperties(prefix = "serva.file")
public class ServaFileConfig {


    // 文件操作类
    private String type = "defaultFile";

    // 默认为系统路径可以手动配置
    private String resourcesPath = FilePathUtil.getTempPath();

    // 操作系统
    private String system = SystemUtil.OS_NAME;
}
