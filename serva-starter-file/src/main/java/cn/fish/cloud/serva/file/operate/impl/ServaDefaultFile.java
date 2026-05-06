package cn.fish.cloud.serva.file.operate.impl;

import cn.fish.cloud.serva.file.operate.ServaFile;
import cn.fish.cloud.serva.file.operate.config.ServaFileConfig;
import cn.hutool.core.lang.id.NanoId;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;

@Slf4j
@Component("servaDefaultFile")
@ConditionalOnProperty(name = "serva.file.type", havingValue = "defaultFile", matchIfMissing = true)
public class ServaDefaultFile implements ServaFile {

    private final ServaFileConfig servaFileConfig;

    public ServaDefaultFile(ServaFileConfig servaFileConfig) {
        this.servaFileConfig = servaFileConfig;
        log.info(servaFileConfig.toString());
    }


    @Override
    public InputStream getInputStream(@NonNull String fileId) throws IOException {
        return new FileInputStream(getFilePath(fileId));
    }

    @Override
    public File getFile(@NonNull String fileId) {
        return new File(getFilePath(fileId));
    }

    @Override
    public String upload(InputStream inputStream) {
        String fileId = getFileId();
        upload(inputStream, fileId);
        return fileId;
    }

    @Override
    public String upload(File file) {
        try {
            return upload(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String upload(InputStream inputStream, String fileId) {
        String filePath = getFilePath(fileId);
        cn.hutool.core.io.FileUtil.writeFromStream(inputStream, new File(filePath));
        return fileId;
    }

    @Override
    public void cover(InputStream inputStream, @NonNull String fileId) {
        delete(fileId);
        upload(inputStream);
    }

    @Override
    public void cover(File file, String fileId) {
        delete(fileId);
        try {
            upload(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void delete(@NonNull String... fileIds) {
        for (String fileId : fileIds) {
            delete(fileId);
        }
    }

    @Override
    public String copy(String fileId) {
        String fileIdNew = getFileId();
        cn.hutool.core.io.FileUtil.copy(getFilePath(fileId), getFilePath(fileIdNew), false);
        return fileIdNew;
    }

    @Override
    public boolean exists(String fileId) {
        return cn.hutool.core.io.FileUtil.exist(getFilePath(fileId));
    }

    private void delete(@NonNull String fileId) {
        File file = new File(getFilePath(fileId));
        if (file.exists()) {
            boolean delete = file.delete();
            if (!delete) {
                log.debug("delete file fail");
            }
        }
    }

    @Override
    public String getFilePath(String fileId) {
        return getResourcesPath().concat(File.separator).concat(fileId);
    }

    @Override
    public Resource getFileResource(String fileId) {
        return new FileSystemResource(getFile(fileId));
    }

    @Override
    public String getResourcesPath() {
        return servaFileConfig.getResourcesPath();
    }

    private String getFileId() {
        return NanoId.randomNanoId();
    }
}