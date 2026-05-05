package cn.fish.cloud.serva.file.operate;


import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface ServaFile {

    /**
     * 根据id获取文件输入流
     *
     * @param fileId 文件id
     * @return 输入流
     */
    InputStream getInputStream(String fileId) throws IOException;

    /**
     * 根据id获取文件
     *
     * @param fileId 文件id
     * @return 文件
     */
    File getFile(String fileId);

    /**
     * 文件流保存
     *
     * @param inputStream 文件流
     * @return 文件id
     */
    String upload(InputStream inputStream);

    /**
     * 文件流保存
     *
     * @param inputStream 文件流
     * @param fileId      自定义文件id
     * @return 文件名字
     */
    String upload(InputStream inputStream, String fileId);

    /**
     * 文件保存
     *
     * @param file 文件
     * @return 文件id
     */
    String upload(File file);

    /**
     * 文件流覆盖
     *
     * @param inputStream 文件流
     * @param fileId      文件id
     */
    void cover(InputStream inputStream, String fileId);

    /**
     * 文件流覆盖
     *
     * @param file   文件
     * @param fileId 文件id
     */
    void cover(File file, String fileId);

    /**
     * 根据文件id删除文件
     *
     * @param fileId 文件id列表
     */
    void delete(String... fileId);

    /**
     * 拷贝文件
     *
     * @param fileId 文件id
     * @return 新文件id
     */
    String copy(String fileId);

    /**
     * 判断文件是否存在
     *
     * @param fileId 文件id
     * @return 存在true 不存在false
     */
    boolean exists(String fileId);

    /**
     * 获得文件存放路径
     *
     * @param fileId 文件id
     * @return 路径
     */
    String getFilePath(String fileId);

    /**
     * 获得文件Resource
     *
     * @param fileId 文件id
     * @return Spring Resource
     */
    Resource getFileResource(String fileId);

    String getResourcesPath();

}
