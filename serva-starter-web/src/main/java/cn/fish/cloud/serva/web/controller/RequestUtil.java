package cn.fish.cloud.serva.web.controller;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * request请求工具类 todo
 *
 * @author onethefish
 */
public class RequestUtil {


    public static final String PAGE_NUM = "currentPage";
    public static final String PAGE_SIZE = "pageSize";

    private RequestUtil() {
    }

    /**
     * 获取IPage对象，mybatis plus分页使用
     *
     * @param requestParam 请求参数
     * @return {@link Page}
     */
    public static <T> Page<T> getIPage(Map<String, Object> requestParam) {
        return getIPage(requestParam, null);
    }

    /**
     * 获取IPage对象，mybatis plus分页使用
     *
     * @param requestParam    请求参数
     * @param defaultPageSize 默认每页记录数
     * @return {@link IPage}
     */
    public static <T> Page<T> getIPage(Map<String, Object> requestParam, Integer defaultPageSize) {
        Page<T> page = new Page<>();
        if (null == requestParam) {
            return page;
        }
        String pageNumberParam = MapUtil.getStr(requestParam, PAGE_NUM, String.valueOf(1));
        if (null == defaultPageSize) {
            defaultPageSize = 10;
        }
        String pageSizeNumParam = MapUtil.getStr(requestParam, PAGE_SIZE, String.valueOf(defaultPageSize));
        BigDecimal pageNumBigDecimal = new BigDecimal(pageNumberParam);
        BigDecimal pageSizeBigDecimal = new BigDecimal(pageSizeNumParam);
        //如果大于long最大值或者为负数，都设置页数和记录数为0，不查询出记录，因为mybatis分页插件是Long类型，会数据溢出，导致查询sql语法错误异常
        if (pageNumBigDecimal.compareTo(new BigDecimal(Long.MAX_VALUE)) > 0 || pageNumBigDecimal.compareTo(new BigDecimal(0)) < 0 ||
                pageSizeBigDecimal.compareTo(new BigDecimal(Long.MAX_VALUE)) > 0 || pageSizeBigDecimal.compareTo(new BigDecimal(0)) < 0) {
            page.setCurrent(0L);
            page.setSize(0L);
            return page;
        }
        return new Page<>(pageNumBigDecimal.longValue(), pageSizeBigDecimal.longValue());
    }


    /**
     * 从request的请求map参数中读取参数值
     *
     * @param param    请求参数
     * @param propName 参数名
     * @param clazz    参数类型
     * @return List集合
     */
    public static <T> List<T> getObjectList(Map<String, Object> param, String propName, Class<T> clazz) {
        Object obj = param.get(propName);
        if (null == obj) {
            return new ArrayList<>();
        }
        return null;
    }


    /**
     * 从request的请求map参数中读取参数值
     *
     * @param param    请求参数
     * @param propName 参数名
     * @param clazz    对象类型
     * @return Object
     */
    public static <T> T getObject(Map<String, Object> param, String propName, Class<T> clazz) {
        Object obj = param.get(propName);
        if (null == obj) {
            return null;
        }
        return (T) obj;
    }

    /**
     * 从request的请求map参数中读取参数值
     *
     * @param param 请求参数
     * @param clazz 对象类型
     * @return Object
     */
    public static <T> T getObject(Map<String, Object> param, Class<T> clazz) {
        return null;
    }

    /**
     * 从request的请求map参数中读取参数值，专用于PTE可编辑页面新增
     *
     * @param param 请求参数
     * @param clazz 参数类型
     * @return List集合
     */
    public static <T> List<T> getAddObjectList(Map<String, Object> param, Class<T> clazz) {
        return getObjectList(param, "addList", clazz);
    }

    /**
     * 从request的请求map参数中读取参数值，专用于PTE可编辑页面修改
     *
     * @param param 请求参数
     * @param clazz 参数类型
     * @return List集合
     */
    public static <T> List<T> getUpdateObjectList(Map<String, Object> param, Class<T> clazz) {
        return getObjectList(param, "updateList", clazz);
    }

    /**
     * 从request的请求map参数中读取参数值，专用于PTE可编辑页面删除
     *
     * @param param 请求参数
     * @param clazz 参数类型
     * @return List集合
     */
    public static <T> List<T> getDeleteObjectList(Map<String, Object> param, Class<T> clazz) {
        return getObjectList(param, "deleteList", clazz);
    }

    /**
     * 从request的请求map参数中读取参数值，专用于PTE的树
     *
     * @param param 请求参数
     * @return List集合
     */
    public static List<String> getSelectedTree(Map<String, Object> param) {
        return getObjectList(param, "selectedTree", String.class);
    }

    /**
     * 读取字符串参数，并trim
     *
     * @param param    请求参数
     * @param propName 参数名
     * @return 参数值字符串
     */
    public static String getStringTrim(Map<String, Object> param, String propName) {
        return getStringTrim(param, propName, "");
    }

    /**
     * 读取字符串参数，并trim
     *
     * @param param        请求参数
     * @param propName     参数名
     * @param defaultValue 默认值
     * @return 参数值字符串
     */
    public static String getStringTrim(Map<String, Object> param, String propName, String defaultValue) {
        String value = MapUtil.getStr(param, propName, defaultValue);
        return null == value ? "" : value.trim();
    }

    /**
     * 将request的参数转换为Map&lt;String, String&gt;格式，只能处理get请求的参数
     *
     * @param request HttpServletRequest
     * @return Map&lt;String, String&gt;格式参数
     */
    public static Map<String, String> requestParamMapToMap(HttpServletRequest request) {
        Map<String, String[]> requestParamMap = request.getParameterMap();
        if (null == requestParamMap) {
            return null;
        }
        Map<String, String> paraMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : requestParamMap.entrySet()) {
            String[] values = entry.getValue();
            String value = null;
            if (ArrayUtils.isNotEmpty(values)) {
                value = values[0];
            }
            paraMap.put(entry.getKey(), value);
        }
        return paraMap;
    }




    /**
     * 获取当前请求的HttpServletRequest
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (null == requestAttributes) {
            return null;
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        return attributes.getRequest();
    }


}
