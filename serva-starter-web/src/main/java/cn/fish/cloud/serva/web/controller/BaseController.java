package cn.fish.cloud.serva.web.controller;

import cn.fish.cloud.serva.web.response.ResponseResult;
import cn.fish.cloud.serva.web.response.StatusEnum;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;

/**
 * 封装一些常用的Controller的方法
 *
 * @author onethefish
 */
@SuppressWarnings("unchecked")
public class BaseController {

    protected <T> Page<T> getIPage(Map<String, Object> param) {
        return RequestUtil.getIPage(param);
    }

    /**
     * 成功返回
     *
     * @return {@link ResponseResult}
     */
    protected <T> ResponseResult<T> result() {
        return ResponseResult.success();
    }

    /**
     * 返回单个对象到前端
     *
     * @param obj 数据对象
     * @return {@link ResponseResult}
     */
    protected <T> ResponseResult<T> result(T obj) {
        return ResponseResult.success(obj);
    }

    /**
     * 返回IPage对象到前端
     *
     * @param iPage 分页对象
     * @return {@link ResponseResult}
     */
    protected <T> ResponseResult<T> result(IPage<?> iPage) {
        ResponseResult<T> result = ResponseResult.newInstance();
        result.setCode(StatusEnum.SUCCESS.getCode());
        if (null != iPage){
            result.setData((T) iPage.getRecords());
            result.setPageParam(ResponseResult.initPageParam(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), iPage.getPages()));
        }
        return result;
    }

    /**
     * 错误返回
     *
     * @param solutionUrl 失败解决方案界面url
     * @param code        错误国际化编码
     * @param args        国际化编码参数
     * @return {@link ResponseResult}
     */
    protected <T> ResponseResult<T> resultError(String solutionUrl, String code, Object... args) {
        return ResponseResult.error(solutionUrl, code, args);
    }

    /**
     * 错误返回
     *
     * @param solutionUrl 失败解决方案界面url
     * @param code        错误国际化编码
     * @param message     信息
     * @return {@link ResponseResult}
     */
    protected <T> ResponseResult<T> resultError(String solutionUrl, String code, String message) {
        return ResponseResult.error(solutionUrl, code, message);
    }

    /**
     * 错误返回
     *
     * @param code 错误国际化编码
     * @param args 国际化编码参数
     * @return {@link ResponseResult}
     */
    protected <T> ResponseResult<T> resultError(String code, Object... args) {
        return ResponseResult.error(code, args);
    }

    /**
     * 错误返回
     *
     * @param code    错误国际化编码
     * @param message 信息
     * @return {@link ResponseResult}
     */
    protected <T> ResponseResult<T> resultError(String code, String message) {
        return ResponseResult.error(code, message);
    }

    /**
     * 错误返回
     *
     * @return {@link ResponseResult}
     */
    protected <T> ResponseResult<T> resultError() {
        return resultError("");
    }

    /**
     * 批量删除时，反序列化前端传递的参数selectedData
     *
     * @param param map参数
     * @param clazz 实体类
     * @return List
     */
    protected <T> List<T> getSelectedDataObjectList(Map<String, Object> param, Class<T> clazz) {
        return getObjectList(param, "selectedData", clazz);
    }

    /**
     * 从前端传递过来的参数map中获取List<String>
     *
     * @param param    JSONArray的对象参数
     * @param propName 参数名
     * @return 将JSONArray转换为List
     */
    protected List<String> getStringList(Map<String, Object> param, String propName) {
        return getObjectList(param, propName, String.class);
    }

    /**
     * 从前端传递过来的参数map中获取对象List<Object>
     *
     * @param param    JSONArray的对象参数
     * @param propName 参数名
     * @return 将JSONArray转换为List
     */
    protected <T> List<T> getObjectList(Map<String, Object> param, String propName, Class<T> clazz) {
        return RequestUtil.getObjectList(param, propName, clazz);
    }

}
