package cn.fish.cloud.serva.mybatis.interceptor;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.ParameterUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.OracleDialect;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * oceanBase 数据库oracle模式方言
 *
 * @author onethefish
 */
public class FishPaginationInnerInterceptor extends PaginationInnerInterceptor {

    private static final String OCEANBASE = ":oceanbase:";
    private static final String OCEANBASE_MODE = "mode=oracle";

    private static final Cache<CacheKey, Long> TOTAL_CACHE = Caffeine.newBuilder()
                                                                     .expireAfterAccess(30, TimeUnit.SECONDS)
                                                                     .build();

    public FishPaginationInnerInterceptor() {

    }

    public FishPaginationInnerInterceptor(DbType dbType) {
        super(dbType);
    }

    @Override
    public boolean willDoQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                               BoundSql boundSql) throws SQLException {
        IPage<?> page = ParameterUtils.findPage(parameter).orElse(null);
        if (page == null || page.getSize() < 0 || !page.searchCount() || resultHandler != Executor.NO_RESULT_HANDLER) {
            return true;
        }

        BoundSql countSql;
        MappedStatement countMs = buildCountMappedStatement(ms, page.countId());
        if (countMs != null) {
            countSql = countMs.getBoundSql(parameter);
        }
        else {
            countMs = buildAutoCountMappedStatement(ms);
            String countSqlStr = autoCountSql(page, boundSql.getSql());
            PluginUtils.MPBoundSql mpBoundSql = PluginUtils.mpBoundSql(boundSql);
            countSql = new BoundSql(countMs.getConfiguration(), countSqlStr, mpBoundSql.parameterMappings(), parameter);
            PluginUtils.setAdditionalParameter(countSql, mpBoundSql.additionalParameters());
        }
        CacheKey cacheKey = executor.createCacheKey(countMs, parameter, rowBounds, countSql);
        Long total = TOTAL_CACHE.getIfPresent(cacheKey);
        if (null == total) {
            long begin = System.currentTimeMillis();
            List<Object> result = executor.query(countMs, parameter, rowBounds, resultHandler, cacheKey, countSql);
            total = 0L;
            if (CollUtil.isNotEmpty(result)) {
                // 个别数据库 count 没数据不会返回 0
                Object o = result.get(0);
                if (o != null) {
                    total = Long.parseLong(o.toString());
                }
                long end = System.currentTimeMillis();
                if (end - begin > 100) {
                    TOTAL_CACHE.put(cacheKey, total);
                }
            }
        }
        page.setTotal(total);
        return continuePage(page);
    }


    /**
     * 阿里巴巴自己内部还在打架，都3.5.4.1 还认不出自己的oceanBase oracle模式，不建议使用
     *
     * @param executor Executor
     * @return IDialect
     */
    @Override
    protected IDialect findIDialect(Executor executor) {
        IDialect dialect = getDialect();
        if (null == dialect) {
            // 处理OceanBase oracle模式下方言改为oracle方言,默认是mysql方言,jdbcUrl配置mode=oracle,如果为mysql模式则不配置采用默认的
            try {
                Connection conn = executor.getTransaction().getConnection();
                String jdbcUrl = conn.getMetaData().getURL();
                if (jdbcUrl.contains(OCEANBASE) && jdbcUrl.contains(OCEANBASE_MODE)) {
                    setDialect(new OracleDialect());
                }
            } catch (SQLException e) {
                throw ExceptionUtils.mpe(e);
            }
            dialect = super.findIDialect(executor);
            setDialect(dialect);
        }
        return dialect;
    }
}
