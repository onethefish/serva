package cn.fish.cloud.serva.web.utils;

import cn.hutool.core.io.NioUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * 文件下载工具类
 *
 * @author onethefish
 */
@Slf4j
public class ResponseDownloadUtil {
    private ResponseDownloadUtil() {

    }

    /**
     * Spring MVC 附件下载：{@code Content-Disposition} 使用 {@code filename} + RFC 5987 {@code filename*}，
     * 与 {@link MediaType} 一并写入 {@link ResponseEntity}，便于统一异常处理与流式响应。
     *
     * @param inputStream 响应体字节流（勿为 null）
     * @param filename    下载展示文件名（勿为空）
     * @param mediaType     内容类型；null 时视为 {@link MediaType#APPLICATION_OCTET_STREAM}
     */
    public static ResponseEntity<Resource> download(InputStream inputStream, String filename, MediaType mediaType) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Argument inputStream can not be null");
        }
        if (StrUtil.isEmpty(filename)) {
            throw new IllegalArgumentException("Argument filename can not be null");
        }
        MediaType type = mediaType != null ? mediaType : MediaType.APPLICATION_OCTET_STREAM;
        InputStreamResource resource = new InputStreamResource(inputStream);
        return ResponseEntity.ok()
                             .header(HttpHeaders.CONTENT_DISPOSITION, attachmentContentDisposition(filename))
                             .contentType(type)
                             .body(resource);
    }

    /**
     * {@code attachment; filename="..."; filename*=UTF-8''...}
     */
    public static String attachmentContentDisposition(String filename) {
        String asciiName = filename.replace("\"", "'");
        return "attachment; filename=\"" + asciiName + "\"; filename*=UTF-8''"
                + URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * @param response {@link HttpServletResponse}
     * @param path     文件路径
     */
    public static void download(HttpServletResponse response, String path) {
        File file = new File(path);
        download(response, file);
    }

    /**
     * 下载
     *
     * @param response    response
     * @param inputStream 文件输入流
     * @param fileName    文件名称
     */
    public static void download(HttpServletResponse response, InputStream inputStream, String fileName) {
        if (StrUtil.isEmpty(fileName)) {
            throw new IllegalArgumentException("Argument fileName can not be null");
        }
        download(response, inputStream, fileName, false);
    }

    /**
     * 下载
     *
     * @param response         response
     * @param resourceFilePath resource下的文件路径，相对路径
     * @param fileName         文件名称
     */
    public static void download(HttpServletResponse response, String resourceFilePath, String fileName) {
        if (StrUtil.isEmpty(fileName)) {
            throw new IllegalArgumentException("Argument fileName can not be null");
        }
        try {
            InputStream inputStream = new ClassPathResource(resourceFilePath).getInputStream();
            download(response, inputStream, fileName, false);
        } catch (IOException e) {
            logError(e);
        }
    }

    /**
     * @param response {@link HttpServletResponse}
     * @param file     文件
     */
    public static void download(HttpServletResponse response, File file) {
        download(response, file, file.getName(), false);
    }

    /**
     * @param response  {@link HttpServletResponse}
     * @param file      文件
     * @param fileName  文件名
     * @param isFireFox 是否火狐浏览器
     */
    public static void download(HttpServletResponse response, File file, String fileName, boolean isFireFox) {
        if (null == file) {
            throw new IllegalArgumentException("Argument file can not be null");
        }
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            download(response, fileInputStream, fileName, isFireFox);
        } catch (IOException e) {
            logError(e);
        }
    }

    /**
     * @param response    {@link HttpServletResponse}
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @param isFireFox   是否火狐浏览器
     */
    public static void download(HttpServletResponse response, InputStream inputStream, String fileName, boolean isFireFox) {
        if (null == inputStream) {
            throw new IllegalArgumentException("Argument inputStream can not be null");
        }
        try (// 开启输入流渠道
             ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
             // 开启输出流渠道(响应)
             WritableByteChannel writableByteChannel = Channels.newChannel(response.getOutputStream())) {
            setResponse(response, fileName, isFireFox);
            NioUtil.copy(readableByteChannel, writableByteChannel);
        } catch (IOException ex) {
            logError(ex);
        }
    }

    /**
     * 设置相应参数
     *
     * @param response  相应对象
     * @param fileName  文件名
     * @param isFireFox 是否火狐浏览器
     */
    private static void setResponse(HttpServletResponse response, String fileName, boolean isFireFox) throws UnsupportedEncodingException {
        // 清空首部空白行
        response.reset();
        String fileTrueName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        fileTrueName = fileTrueName.replace("+", "%20");
        // 设置内容类型
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        String filename;
        // 火狐浏览器需要跟换编码
        if (isFireFox) {
            filename = new String(fileTrueName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        }
        else {
            filename = URLEncoder.encode(fileTrueName, StandardCharsets.UTF_8);
        }
        // 设置请求头
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);
    }

    /**
     * @param response {@link HttpServletResponse}
     * @param file     文件
     */
    public static void showImg(HttpServletResponse response, File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("file " + file.getPath() + " not exists");
        }
        try (BufferedInputStream br = new BufferedInputStream(new FileInputStream(file))) {
            try (OutputStream out = response.getOutputStream()) {
                byte[] buf = new byte[1024];
                int len = 0;
                response.reset();
                String fileTrueName = URLDecoder.decode(file.getName(), StandardCharsets.UTF_8);
                fileTrueName = fileTrueName.replace("+", "%20");
                response.setContentType("image/*");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(fileTrueName, StandardCharsets.UTF_8));
                while ((len = br.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        } catch (IOException ex) {
            logError(ex);
        }
    }

    private static void logError(IOException e) {
        log.error("download error :" + e.getCause(), e);
    }
}
