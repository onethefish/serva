package cn.fish.cloud.serva.file.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 文件夹路径工具类
 *
 * @author onethefish
 */
@Slf4j
public class FilePathUtil {

    private FilePathUtil() {

    }

    /**
     * 系统路径
     *
     * @return 系统路径
     */
    public static String getBasedir() {
        //在启动脚本中配置了system.basedir，为当前系统路径
        return System.getProperty("system.basedir");
    }

    /**
     * 临时文件路径，存放上传的文件
     *
     * @return 临时文件夹路径
     */
    public static String getTempPath() {
        return getResourcesPath("temp");
    }

    /**
     * 获取resource/static路径
     *
     * @return 路径
     */
    public static String getStaticResourcesPath() {
        return getResourcesPath("static").concat(File.separator);
    }

    private static String getResourcesPath(String tempPath) {
        tempPath = StrUtil.replace(tempPath, StrUtil.DOT, "");
        tempPath = StrUtil.replace(tempPath, "&", "");

        String basedir = getBasedir();
        if (StrUtil.isEmpty(basedir)) {
            try {
                if (StrUtil.isBlank(tempPath)) {
                    return ResourceUtils.getURL("classpath:").getPath();
                } else {
                    return ResourceUtils.getURL("classpath:").getPath().concat(tempPath).concat(File.separator);
                }
            } catch (FileNotFoundException e) {
                log.error("", e);
            }
        } else {
            if (StrUtil.isBlank(tempPath)) {
                return basedir.concat(File.separator);
            } else {
                return basedir.concat(File.separator).concat(tempPath).concat(File.separator);
            }
        }
        return "";
    }

    /**
     * 获取resource路径
     *
     * @return 路径
     */
    public static String getResourcesPath() {
        return getResourcesPath(null);
    }
}
