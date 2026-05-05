package cn.fish.cloud.serva.mybatis.configuration;

import cn.fish.cloud.serva.mybatis.config.ServaMybatisConfig;
import cn.fish.cloud.serva.mybatis.interceptor.ServaPaginationInnerInterceptor;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;


/**
 * Mybatis与Mybatis plus 分页配置
 */
@Configuration
public class ServaMybatisConfiguration {

    private final ServaMybatisConfig servaMybatisConfig;

    public ServaMybatisConfiguration(ServaMybatisConfig servaMybatisConfig) {
        this.servaMybatisConfig = servaMybatisConfig;
    }

    /**
     * order必须比DataPermissionInterceptor配置的@Order(2)小，否则在拦截器执行时，会先执行分页拦截器
     * 这里的主要目的是注意oceanBase 的分页拦截器 以及 后续权限拦截器的加载顺序
     */
    @Bean
    @Order(1)
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = null;
        String dbType = servaMybatisConfig.getDbType();
        if (StrUtil.isNotBlank(dbType)) {
            paginationInnerInterceptor = new ServaPaginationInnerInterceptor(DbType.getDbType(dbType));
        }
        else {
            paginationInnerInterceptor = new ServaPaginationInnerInterceptor();
        }
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }

}
