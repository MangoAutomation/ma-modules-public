package com.infiniteautomation.mango.rest.v2.model.filestore;

import java.util.Date;

public class FileModel {

    String folderPath;
    String filename;
    String mimeType;
    Date lastModified;
    Long size;

    boolean directory;

    /**
     * @param folderPath
     * @param filename
     * @param mimeType
     * @param lastModified
     * @param size
     * @param directory
     */
    public FileModel(String folderPath, String filename, String mimeType, Date lastModified,
            Long size, boolean directory) {
        super();
        this.folderPath = folderPath;
        this.filename = filename;
        this.mimeType = mimeType;
        this.lastModified = lastModified;
        this.size = size;
        this.directory = directory;
    }

    public FileModel() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
}
