package com.cyecize.summer.areas.routing.models;

import com.cyecize.http.MultipartFile;
import com.cyecize.summer.areas.routing.interfaces.UploadedFile;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class UploadedFileImpl implements UploadedFile {

    private static final String FILE_IS_NULL_MSG = "File is null";

    private final String assetsDir;

    private final MultipartFile multipartFile;

    public UploadedFileImpl(String assetsFir, MultipartFile memoryFile) {
        this.assetsDir = assetsFir;
        this.multipartFile = memoryFile;
    }

    @Override
    public String save(String relativePath) throws IOException {
        if (this.multipartFile == null) {
            throw new IOException(FILE_IS_NULL_MSG);
        }

        String name = this.multipartFile.getFieldName() + new Date().getTime();
        name = DigestUtils.md5Hex(name);
        name += this.getFileExtension(this.multipartFile.getFileName());

        return this.save(relativePath, name);
    }

    @Override
    public String save(String relativePath, String fileName) throws IOException {
        if (this.multipartFile == null) {
            throw new IOException(FILE_IS_NULL_MSG);
        }

        relativePath = this.stripSlashes(relativePath);
        fileName = this.stripSlashes(fileName);
        fileName = fileName.replaceAll("\\\\", "").replaceAll("/", "");

        final File dirs = new File(this.assetsDir + File.separator + relativePath);
        if (!dirs.exists()) {
            dirs.mkdirs();
        }

        final String pathToFile = File.separator + relativePath + File.separator + fileName;
        final Path path = Files.createFile(Paths.get(this.assetsDir + pathToFile));

        try (final FileOutputStream outputStream = new FileOutputStream(path.toFile())) {
            this.multipartFile.getInputStream().transferTo(outputStream);
        }

        return pathToFile;
    }

    @Override
    public MultipartFile getUploadedFile() {
        return this.multipartFile;
    }

    private String stripSlashes(String str) {
        str = str.replaceAll("\\.\\.\\/", "");
        str = this.trimStaringSlash(str);
        str = this.trimEndingSlash(str);

        return str.trim();
    }

    private String trimStaringSlash(String path) {
        if (path.startsWith("/") || path.startsWith("\\")) {
            path = path.substring(1);
        }

        return path;
    }

    private String trimEndingSlash(String path) {
        if (path.endsWith("/") || path.endsWith("\\")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
