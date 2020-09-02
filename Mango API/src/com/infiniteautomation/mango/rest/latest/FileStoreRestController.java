/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import com.google.common.collect.Sets;
import com.infiniteautomation.mango.rest.latest.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.latest.exception.GenericRestException;
import com.infiniteautomation.mango.rest.latest.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.latest.exception.ResourceNotFoundException;
import com.infiniteautomation.mango.rest.latest.model.filestore.FileModel;
import com.infiniteautomation.mango.rest.latest.resolver.RemainingPath;
import com.infiniteautomation.mango.spring.service.FileStoreService;
import com.infiniteautomation.mango.util.exception.TranslatableIllegalArgumentException;
import com.serotonin.m2m2.i18n.TranslatableException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Manage files from stores defined by FileStoreDefinition(s)
 *
 * @author Terry Packer
 */
@Api(value="File Store")
@RestController()
@RequestMapping("/file-stores")
public class FileStoreRestController extends AbstractMangoRestController {

    private final FileStoreService service;
    private final String cacheControlHeader;

    @Autowired
    public FileStoreRestController(FileStoreService fileStoreService, @Value("${web.cache.maxAge.rest:0}") long maxAge) {
        // use the rest max age setting but dont honor the nocache setting
        this.cacheControlHeader = CacheControl.maxAge(maxAge, TimeUnit.SECONDS).getHeaderValue();
        this.service = fileStoreService;
    }

    @ApiOperation(
            value = "Upload a file to a store with a path",
            notes = "Must have write access to the store"
            )
    @RequestMapping(method = RequestMethod.POST, consumes=MediaType.MULTIPART_FORM_DATA_VALUE, value="/{name}/**")
    public ResponseEntity<List<FileModel>> uploadWithPath(
            @ApiParam(value = "Valid File Store name", required = true)
            @PathVariable("name") String name,

            @AuthenticationPrincipal User user,

            @RequestParam(required=false, defaultValue="false") boolean overwrite,

            @ApiIgnore @RemainingPath String pathInStore,

            MultipartHttpServletRequest multipartRequest,
            HttpServletRequest request) throws IOException {


        File outputDirectory;
        try{
            outputDirectory = this.service.getPathForWrite(name, pathInStore).toFile();
        }catch(TranslatableIllegalArgumentException e) {
            throw new AccessDeniedException(e.getTranslatableMessage());
        }

        File root;
        try {
            root = this.service.getPathForWrite(name, "").toFile();
        }catch(TranslatableIllegalArgumentException e) {
            throw new AccessDeniedException(e.getTranslatableMessage());
        }

        if (outputDirectory.exists() && !outputDirectory.isDirectory()) {
            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir",
                    service.removeToRoot(root, outputDirectory), name));
        }

        if(!outputDirectory.exists()){
            if(!outputDirectory.mkdirs())
                throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir",
                        service.removeToRoot(root, outputDirectory), name));
        }

        //Put the file where it belongs
        List<FileModel> fileModels = new ArrayList<>();

        MultiValueMap<String, MultipartFile> filesMap = multipartRequest.getMultiFileMap();
        for (String nameField : filesMap.keySet()) {
            for (MultipartFile file : filesMap.get(nameField)) {
                String filename;
                if (file instanceof CommonsMultipartFile) {
                    FileItem fileItem = ((CommonsMultipartFile) file).getFileItem();
                    filename = fileItem.getName();
                } else {
                    filename = file.getName();
                }

                File newFile = service.findUniqueFileName(outputDirectory, filename, overwrite);
                Files.createDirectories(newFile.toPath().getParent());

                try (OutputStream output = new FileOutputStream(newFile, false)) {
                    try (InputStream input  = file.getInputStream()) {
                        StreamUtils.copy(input, output);
                    }
                }
                fileModels.add(fileToModel(newFile, root, request.getServletContext()));
            }
        }

        return new ResponseEntity<>(fileModels, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Create a folder or copy/move/rename an existing file or folder",
            notes = "Must have write access to the store"
            )
    @RequestMapping(method = RequestMethod.POST, value="/{fileStoreName}/**")
    public ResponseEntity<FileModel> createNewFolder(
            @ApiParam(value = "Valid File Store name", required = true)
            @PathVariable("fileStoreName") String fileStoreName,
            @ApiParam(value = "Move file/folder to")
            @RequestParam(required=false) String moveTo,
            @ApiParam(value = "Copy file/folder to")
            @RequestParam(required=false) String copyTo,
            @ApiIgnore @RemainingPath String pathInStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) throws IOException {

        File fileOrFolder;
        try{
            fileOrFolder = this.service.getPathForWrite(fileStoreName, pathInStore).toFile();
        }catch(TranslatableIllegalArgumentException e) {
            throw new AccessDeniedException(e.getTranslatableMessage());
        }

        File root;
        try{
            root = this.service.getPathForWrite(fileStoreName, "").toFile();
        }catch(TranslatableIllegalArgumentException e) {
            throw new AccessDeniedException(e.getTranslatableMessage());
        }

        if (copyTo != null) {
            return copyFileOrFolder(request, fileStoreName, root, fileOrFolder, copyTo);
        } else if (moveTo != null) {
            return moveFileOrFolder(request, fileStoreName, root, fileOrFolder, moveTo);
        } else {
            return createFolder(request, fileStoreName, root, fileOrFolder);
        }
    }

    private ResponseEntity<FileModel> moveFileOrFolder(HttpServletRequest request, String fileStoreName, File root, File fileOrFolder, String moveTo) throws IOException {
        if (!fileOrFolder.exists())
            throw new NotFoundRestException();
        try {
            FileModel fileModel = fileToModel(service.moveFileOrFolder(fileStoreName, root, fileOrFolder, moveTo), root, request.getServletContext());
            return new ResponseEntity<>(fileModel, HttpStatus.OK);
        } catch(TranslatableException | TranslatableIllegalArgumentException e) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, e.getTranslatableMessage());
        }
    }

    private ResponseEntity<FileModel> copyFileOrFolder(HttpServletRequest request, String fileStoreName, File root, File srcFile, String dst) throws IOException {
        if (!srcFile.exists())
            throw new NotFoundRestException();
        try {
            FileModel fileModel = fileToModel(service.copyFileOrFolder(fileStoreName, root, srcFile, dst), root, request.getServletContext());
            return new ResponseEntity<>(fileModel, HttpStatus.OK);
        } catch(TranslatableException | TranslatableIllegalArgumentException e) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, e.getTranslatableMessage());
        }
    }

    private ResponseEntity<FileModel> createFolder(HttpServletRequest request, String fileStoreName, File root, File folder) {
        if (folder.exists()) {
            if (folder.isDirectory()) {
                throw new GenericRestException(HttpStatus.CONFLICT, new TranslatableMessage("filestore.directoryExists",
                        service.removeToRoot(root, folder), fileStoreName));
            } else {
                throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir",
                        service.removeToRoot(root, folder), fileStoreName));
            }
        }

        if (!folder.mkdirs())
            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir",
                    service.removeToRoot(root, folder), fileStoreName));

        FileModel fileModel = fileToModel(folder, root, request.getServletContext());
        return new ResponseEntity<>(fileModel, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete a file or directory")
    @RequestMapping(method = RequestMethod.DELETE, value="/{name}/**")
    public ResponseEntity<Void> delete(
            @ApiParam(value = "Valid File Store name", required = true)
            @PathVariable("name") String name,
            @ApiParam(value = "Recursive delete of directory", defaultValue="false")
            @RequestParam(required=false, defaultValue="false") boolean recursive,
            @ApiIgnore @RemainingPath String pathInStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) throws IOException {

        File file = this.service.getPathForWrite(name, pathInStore).toFile();

        if(!file.exists())
            throw new NotFoundRestException();

        if (file.isDirectory() && recursive) {
            FileUtils.deleteDirectory(file);
        } else {
            if (!file.delete()) {
                throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.errorDeletingFile"));
            }
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    // TODO Mango 4.0 remove this method, require user to specify list or download
    @ApiOperation(value = "List a directory or download a file from a store")
    @RequestMapping(method = RequestMethod.GET, value="/{name}/**")
    public ResponseEntity<?> download(
            @ApiParam(value = "Valid File Store name", required = true)
            @PathVariable("name") String name,
            @ApiParam(value = "Set content disposition to attachment", defaultValue="true")
            @RequestParam(required=false, defaultValue="true") boolean download,
            @ApiIgnore @RemainingPath String pathInStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        File file;
        try{
            file = this.service.getPathForRead(name, pathInStore).toFile();
        }catch(TranslatableIllegalArgumentException e) {
            throw new AccessDeniedException(e.getTranslatableMessage());
        }

        File root;
        try{
            root = this.service.getPathForRead(name, "").toFile();
        }catch(TranslatableIllegalArgumentException e) {
            throw new AccessDeniedException(e.getTranslatableMessage());
        }

        // TODO Allow downloading directory as a zip
        if (file.isFile()) {
            return getFile(file, download, request, response);
        } else {
            return listStoreContents(file, root, request);
        }
    }

    /**
     * WARNING: This end point can be accessed publicly via the /file-stores/* filter
     */
    @ApiOperation(value = "Download a file from a store")
    @RequestMapping(method = RequestMethod.GET, value="/download-file/{name}/**")
    public ResponseEntity<?> downloadOnly(
            @ApiParam(value = "Valid File Store name", required = true)
            @PathVariable("name") String name,
            @ApiParam(value = "Set content disposition to attachment", defaultValue="true")
            @RequestParam(required=false, defaultValue="true") boolean download,
            @ApiIgnore @RemainingPath String pathInStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            HttpServletResponse response) {

        File file = this.service.getPathForRead(name, pathInStore).toFile();
        return getFile(file, download, request, response);
    }

    protected ResponseEntity<List<FileModel>> listStoreContents(File directory, File root, HttpServletRequest request) throws IOException {
        if (!directory.exists() || !directory.isDirectory()) {
            throw new ResourceNotFoundException();
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        if (directory.equals(root) && !root.exists())
            return new ResponseEntity<>(Collections.emptyList(), responseHeaders, HttpStatus.OK);

        if (!directory.exists())
            throw new ResourceNotFoundException(service.relativePath(root, directory));

        List<FileModel> models = Files.list(directory.toPath())
                .map(p -> fileToModel(p.toFile(), root, request.getServletContext()))
                .collect(Collectors.toList());

        Set<MediaType> mediaTypes = Sets.newHashSet(MediaType.APPLICATION_JSON);
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, mediaTypes);

        return new ResponseEntity<>(models, responseHeaders, HttpStatus.OK);
    }

    protected ResponseEntity<FileSystemResource> getFile(File file, boolean download, HttpServletRequest request, HttpServletResponse response) {
        if (!file.exists() || !file.isFile()) {
            throw new ResourceNotFoundException();
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, download ? "attachment" : "inline");

        Set<MediaType> mediaTypes = Sets.newHashSet(MediaType.APPLICATION_OCTET_STREAM);
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, mediaTypes);

        // dynamically set the producible media types to whatever the detected file type is
        Optional<MediaType> fileMediaType = MediaTypeFactory.getMediaType(file.getName());
        if (fileMediaType.isPresent()) {
            mediaTypes.add(fileMediaType.get());
        } else {
            mediaTypes.add(MediaType.ALL);

            // force the content type to application/octet for unknown file types
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        // this doesn't work as a header from ResponseEntity won't be set if it is already set in the response
        //responseHeaders.setCacheControl(cacheControlHeader);
        // set header directly on response instead
        response.setHeader(HttpHeaders.CACHE_CONTROL, cacheControlHeader);

        return new ResponseEntity<>(new FileSystemResource(file), responseHeaders, HttpStatus.OK);
    }

    /**
     * Convert a file to a model
     * @param file
     * @param root
     * @param context
     * @return
     * @throws UnsupportedEncodingException
     */
    public FileModel fileToModel(File file, File root, ServletContext context) {
        FileModel model = new FileModel();
        model.setFilename(file.getName());
        model.setFolderPath(service.relativePath(root, file.getParentFile()));
        model.setDirectory(file.isDirectory());
        model.setLastModified(new Date(file.lastModified()));
        model.setMimeType(context.getMimeType(file.getName()));
        if (!file.isDirectory())
            model.setSize(file.length());
        return model;
    }
}
