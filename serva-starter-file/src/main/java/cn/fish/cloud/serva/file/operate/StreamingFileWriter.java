package cn.fish.cloud.serva.file.operate;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 向最终存储位置顺序写入字节（用于 {@link ServaFile#uploadDirect(StreamingFileWriter)} 等）。
 */
@FunctionalInterface
public interface StreamingFileWriter {

    void writeTo(OutputStream out) throws IOException;
}
