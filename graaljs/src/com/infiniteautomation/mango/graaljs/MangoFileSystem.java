/*
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.graaljs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.Set;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.script.permissions.LoadFileStorePermission;
import org.graalvm.polyglot.io.FileSystem;

import com.infiniteautomation.mango.spring.service.FileStoreService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Jared Wiltshire
 */
public class MangoFileSystem implements FileSystem {

    private final FileSystem delegate;
    private final FileStoreService fileStoreService;
    private final PermissionService permissionService;
    private final LoadFileStorePermission loadFileStorePermission;
    // superadmin only
    private final MangoPermission accessAllPaths = new MangoPermission();

    public MangoFileSystem(FileSystem delegate, FileStoreService fileStoreService, PermissionService permissionService,
                           LoadFileStorePermission loadFileStorePermission) {
        super();
        this.delegate = delegate;
        this.fileStoreService = fileStoreService;
        this.permissionService = permissionService;
        this.loadFileStorePermission = loadFileStorePermission;
    }

    @Override
    public Path parsePath(URI uri) {
        if ("filestore".equals(uri.getScheme())) {
            return fileStoreService.getPathForRead(uri.getHost(), uri.getPath().substring(1));
        }
        return delegate.parsePath(uri);
    }

    @Override
    public Path parsePath(String path) {
        return delegate.parsePath(path);
    }

    @Override
    public void checkAccess(Path path, Set<? extends AccessMode> modes, LinkOption... linkOptions) throws IOException {
        delegate.checkAccess(path, modes, linkOptions);

        PermissionHolder user = Common.getUser();
        try {
            if (permissionService.hasPermission(user, loadFileStorePermission.getPermission())) {
                try {
                    Path fileStorePath = fileStoreService.relativize(path);
                    String fileStoreName = fileStorePath.getName(0).toString();
                    if (modes.contains(AccessMode.WRITE)) {
                        fileStoreService.getPathForWrite(fileStoreName, "");
                    } else {
                        fileStoreService.getPathForRead(fileStoreName, "");
                    }
                    return;
                } catch (IllegalArgumentException | NotFoundException e) {
                    // not a file store path
                }
            }

            permissionService.ensurePermission(user, accessAllPaths);
        } catch (PermissionException e) {
            // file store denied access
            throw new SecurityException(e);
        }
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        delegate.createDirectory(dir, attrs);
    }

    @Override
    public void delete(Path path) throws IOException {
        delegate.delete(path);
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        return delegate.newByteChannel(path, options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
        return delegate.newDirectoryStream(dir, filter);
    }

    @Override
    public Path toAbsolutePath(Path path) {
        return delegate.toAbsolutePath(path);
    }

    @Override
    public Path toRealPath(Path path, LinkOption... linkOptions) throws IOException {
        return delegate.toRealPath(path, linkOptions);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return delegate.readAttributes(path, attributes, options);
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        delegate.setAttribute(path, attribute, value, options);
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        delegate.copy(source, target, options);
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        delegate.move(source, target, options);
    }

    @Override
    public void createLink(Path link, Path existing) throws IOException {
        delegate.createLink(link, existing);
    }

    @Override
    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException {
        delegate.createSymbolicLink(link, target, attrs);
    }

    @Override
    public Path readSymbolicLink(Path link) throws IOException {
        return delegate.readSymbolicLink(link);
    }

    @Override
    public void setCurrentWorkingDirectory(Path currentWorkingDirectory) {
        delegate.setCurrentWorkingDirectory(currentWorkingDirectory);
    }

    @Override
    public String getSeparator() {
        return delegate.getSeparator();
    }

    @Override
    public String getPathSeparator() {
        return delegate.getPathSeparator();
    }

    @Override
    public String getMimeType(Path path) {
        return delegate.getMimeType(path);
    }

    @Override
    public Charset getEncoding(Path path) {
        return delegate.getEncoding(path);
    }

    @Override
    public Path getTempDirectory() {
        return delegate.getTempDirectory();
    }

}
