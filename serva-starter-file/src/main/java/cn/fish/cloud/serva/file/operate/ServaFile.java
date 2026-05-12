package cn.fish.cloud.serva.file.operate;


import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
     * 分配新的存储对象 id（尚未落盘，直至通过 {@link #openWriteStream} 写入并关闭流）。
     * <p>方案 1 的第一步：与 {@link #openWriteStream(String)} 组合，直接向最终位置写入，避免先写临时文件再 copy。</p>
     */
    String allocateFileId();

    /**
     * 打开指向该 id 最终存储位置的输出流；调用方顺序写入后须关闭流。
     * <p>若写入失败或放弃上传，应调用 {@link #delete(String...)} 清理可能存在的半截文件。</p>
     */
    OutputStream openWriteStream(String fileId) throws IOException;

    /**
     * 在最终存储位置上完成一次流式写入并返回 fileId（内部等价于分配 id、打开流、回调写入、关闭）。
     * <p>方案 2：若回调抛出异常，实现应尽力删除未完成的文件。</p>
     */
    String uploadDirect(StreamingFileWriter writer) throws IOException;

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
     * 获得文件Resource
     *
     * @param fileId 文件id
     * @return Spring Resource
     */
    Resource getFileResource(String fileId);

}
