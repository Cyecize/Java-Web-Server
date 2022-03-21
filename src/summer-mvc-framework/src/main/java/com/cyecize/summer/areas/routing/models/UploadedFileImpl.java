package com.cyecize.summer.areas.routing.models;

import com.cyecize.http.MultipartFile;
import com.cyecize.summer.areas.routing.interfaces.UploadedFile;
import com.cyecize.summer.utils.PathUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

        final String name = this.multipartFile.getFieldName() + new Date().getTime()
                + this.getFileExtension(this.multipartFile.getFileName());

        return this.save(relativePath, name);
    }

    @Override
    public String save(String relativePath, String fileName) throws IOException {
        return this.save(relativePath, fileName, true);
    }

    @Override
    public String save(String relativePath, String fileName, boolean overwrite) throws IOException {
        if (this.multipartFile == null) {
            throw new IOException(FILE_IS_NULL_MSG);
        }

        final String pathToFile = PathUtils.appendPath(
                this.assetsDir,
                relativePath
        );

        final File dirs = new File(pathToFile);
        if (!dirs.exists()) {
            dirs.mkdirs();
        }

        fileName = fileName.replaceAll("\\\\", "").replaceAll("/", "");

        final String fullPath = PathUtils.appendPath(
                pathToFile,
                fileName
        );

        final File file = new File(fullPath);

        if (file.exists()) {
            if (overwrite) {
                file.delete();
            } else {
                return fullPath;
            }
        }

        file.createNewFile();

        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            this.multipartFile.getInputStream().transferTo(outputStream);
        }

        return pathToFile;
    }

    @Override
    public MultipartFile getUploadedFile() {
        return this.multipartFile;
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
