/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
import com.infiniteautomation.mango.rest.latest.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.latest.exception.ResourceNotFoundException;
import com.infiniteautomation.mango.rest.latest.model.filestore.FileModel;
import com.infiniteautomation.mango.rest.latest.resolver.RemainingPath;
import com.infiniteautomation.mango.spring.service.FileStoreService;
import com.infiniteautomation.mango.spring.service.FileStoreService.FileStorePath;
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
@Api(value = "File Store")
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
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/{name}/**")
    public ResponseEntity<List<FileModel>> uploadWithPath(
            @ApiParam(value = "Valid File Store name", required = true)
            @PathVariable("name") String name,

            @AuthenticationPrincipal User user,

            @RequestParam(required = false, defaultValue = "false") boolean overwrite,

            @ApiIgnore @RemainingPath String pathInStore,

            MultipartHttpServletRequest multipartRequest,
            HttpServletRequest request) throws IOException {

        FileStorePath outputDirectory = this.service.createDirectory(name, pathInStore);

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

                Path newFile = findUniqueFileName(outputDirectory.getAbsolutePath(), filename, overwrite);

                try (OutputStream output = Files.newOutputStream(newFile)) {
                    try (InputStream input = file.getInputStream()) {
                        StreamUtils.copy(input, output);
                    }
                }

                fileModels.add(fileToModel(outputDirectory.resolve(newFile), request.getServletContext()));
            }
        }

        return new ResponseEntity<>(fileModels, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Create a folder or copy/move/rename an existing file or folder",
            notes = "Must have write access to the store"
    )
    @RequestMapping(method = RequestMethod.POST, value = "/{fileStoreName}/**")
    public ResponseEntity<FileModel> createNewFolder(
            @ApiParam(value = "Valid File Store name", required = true)
            @PathVariable("fileStoreName") String fileStoreName,
            @ApiParam(value = "Move file/folder to")
            @RequestParam(required = false) String moveTo,
            @ApiParam(value = "Copy file/folder to")
            @RequestParam(required = false) String copyTo,
            @ApiIgnore @RemainingPath String pathInStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        FileModel fileModel;
        if (copyTo != null) {
            fileModel = fileToModel(service.copyFileOrFolder(fileStoreName, pathInStore, copyTo), request.getServletContext());
        } else if (moveTo != null) {
            fileModel = fileToModel(service.moveFileOrFolder(fileStoreName, pathInStore, moveTo), request.getServletContext());
        } else {
            fileModel = fileToModel(service.createDirectory(fileStoreName, pathInStore), request.getServletContext());
        }
        return new ResponseEntity<>(fileModel, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a file or directory")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{name}/**")
    public ResponseEntity<Void> delete(
            @ApiParam(value = "Valid File Store name", required = true)
            @PathVariable("name") String name,
            @ApiParam(value = "Recursive delete of directory", defaultValue = "false")
            @RequestParam(required = false, defaultValue = "false") boolean recursive,
            @ApiIgnore @RemainingPath String pathInStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        service.deleteFileOrFolder(name, pathInStore, recursive);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    // TODO Mango 4.0 remove this method, require user to specify list or download
    @ApiOperation(value = "List a directory or download a file from a store")
    @RequestMapping(method = RequestMethod.GET, value = "/{name}/**")
    public ResponseEntity<?> download(
            @ApiParam(value = "Valid File Store name", required = true)
            @PathVariable("name") String name,
            @ApiParam(value = "Set content disposition to attachment", defaultValue = "true")
            @RequestParam(required = false, defaultValue = "true") boolean download,
            @ApiIgnore @RemainingPath String pathInStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            HttpServletResponse response) {

        FileStorePath file = this.service.forRead(name, pathInStore);

        // TODO Allow downloading directory as a zip
        if (Files.isRegularFile(file.getAbsolutePath())) {
            return getFile(file.getAbsolutePath(), download, request, response);
        } else {
            return listStoreContents(file, request);
        }
    }

    /**
     * WARNING: This end point can be accessed publicly via the /file-stores/* filter
     */
    @ApiOperation(value = "Download a file from a store")
    @RequestMapping(method = RequestMethod.GET, value = "/download-file/{name}/**")
    public ResponseEntity<?> downloadOnly(
            @ApiParam(value = "Valid File Store name", required = true)
            @PathVariable("name") String name,
            @ApiParam(value = "Set content disposition to attachment", defaultValue = "true")
            @RequestParam(required = false, defaultValue = "true") boolean download,
            @ApiIgnore @RemainingPath String pathInStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            HttpServletResponse response) {

        FileStorePath file = this.service.forRead(name, pathInStore);
        return getFile(file.getAbsolutePath(), download, request, response);
    }

    protected ResponseEntity<List<FileModel>> listStoreContents(FileStorePath fileStorePath, HttpServletRequest request) {
        Path directory = fileStorePath.getAbsolutePath();
        if (!Files.isDirectory(directory)) {
            throw new NotFoundRestException();
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        List<FileModel> models;
        try {
            models = Files.list(directory)
                    .map(p -> fileToModel(fileStorePath.resolve(p), request.getServletContext()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Set<MediaType> mediaTypes = Sets.newHashSet(MediaType.APPLICATION_JSON);
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, mediaTypes);

        return new ResponseEntity<>(models, responseHeaders, HttpStatus.OK);
    }

    protected ResponseEntity<FileSystemResource> getFile(Path file, boolean download, HttpServletRequest request, HttpServletResponse response) {
        if (!Files.isRegularFile(file)) {
            throw new ResourceNotFoundException();
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, download ? "attachment" : "inline");

        Set<MediaType> mediaTypes = Sets.newHashSet(MediaType.APPLICATION_OCTET_STREAM);
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, mediaTypes);

        // dynamically set the producible media types to whatever the detected file type is
        Optional<MediaType> fileMediaType = MediaTypeFactory.getMediaType(file.getFileName().toString());
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
     *
     * @param file
     * @param root
     * @param context
     * @return
     */
    private FileModel fileToModel(FileStorePath fileStorePath, ServletContext context) {
        try {
            Path file = fileStorePath.getAbsolutePath();
            String fileName = file.getFileName().toString();
            FileModel model = new FileModel();
            model.setFilename(fileName);
            model.setFolderPath(fileStorePath.getParent().standardizedPath());
            model.setRelativePath(fileStorePath.standardizedPath());
            model.setFileStoreXid(fileStorePath.getFileStore().getXid());
            model.setDirectory(Files.isDirectory(file));
            model.setLastModified(new Date(Files.getLastModifiedTime(file).toMillis()));
            model.setMimeType(context.getMimeType(fileName));
            if (Files.isRegularFile(file))
                model.setSize(Files.size(file));
            return model;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path findUniqueFileName(Path directory, String filename, boolean overwrite) {
        Path file = directory.resolve(filename).toAbsolutePath().normalize();
        if (overwrite) {
            return file;
        }

        Path parent = file.getParent();

        int lastIndex = filename.lastIndexOf('.');
        String originalName = lastIndex < 0 ? filename : filename.substring(0, lastIndex);
        String extension = lastIndex < 0 ? "" : filename.substring(lastIndex + 1);

        int i = 1;
        while (Files.exists(file)) {
            if (extension.isEmpty()) {
                file = parent.resolve(String.format("%s_%03d", originalName, i++));
            } else {
                file = parent.resolve(String.format("%s_%03d.%s", originalName, i++, extension));
            }
        }

        return file;
    }
}
