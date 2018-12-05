/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import com.google.common.collect.Sets;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.exception.ResourceNotFoundException;
import com.infiniteautomation.mango.rest.v2.model.filestore.FileModel;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.FileStoreDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.TranslatableException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.FileStoreDefinition;
import com.serotonin.m2m2.module.definitions.permissions.UserFileStoreCreatePermissionDefinition;
import com.serotonin.m2m2.util.FileStoreUtils;
import com.serotonin.m2m2.vo.FileStore;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.filter.MangoShallowEtagHeaderFilter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Manage files from stores defined by FileStoreDefinition(s)
 *
 * @author Terry Packer
 */
@Api(value="File Store", description="Allow read/write access to file storage areas")
@RestController()
@RequestMapping("/file-stores")
public class FileStoreRestV2Controller extends AbstractMangoRestV2Controller {

    private final FileStoreDao fileStoreDao;
    final String cacheControlHeader;

    @Autowired
    public FileStoreRestV2Controller(FileStoreDao fileStoreDao) {
        // use the rest max age setting but dont honor the nocache setting
        cacheControlHeader = String.format(MangoShallowEtagHeaderFilter.MAX_AGE_TEMPLATE, Common.envProps.getLong("web.cache.maxAge.rest", 0L));
        this.fileStoreDao = fileStoreDao;
    }

    @ApiOperation(
            value = "List all file store names",
            notes = "Must have read access to see the store"
            )
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<String>> list(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        Map<String, FileStoreDefinition> defs = this.fileStoreDao.getFileStoreMap();
        List<String> accessible = new ArrayList<String>(defs.size());
        if(user.hasAdminPermission()){
            //admin users don't need to filter the results
            for(FileStoreDefinition def : defs.values()){
                accessible.add(def.getStoreName());
            }
        }else{

            for(FileStoreDefinition def : defs.values()){
                try{
                    def.ensureStoreReadPermission(user);
                    accessible.add(def.getStoreName());
                }catch(AccessDeniedException e){ }
            }
        }
        return new ResponseEntity<>(accessible, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Upload a file to a store with a path",
            notes = "Must have write access to the store"
            )
    @RequestMapping(method = RequestMethod.POST, consumes=MediaType.MULTIPART_FORM_DATA_VALUE, value="/{name}/**")
    public ResponseEntity<List<FileModel>> uploadWithPath(
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("name") String name,

            @AuthenticationPrincipal User user,

            @RequestParam(required=false, defaultValue="false") boolean overwrite,

            MultipartHttpServletRequest multipartRequest,
            HttpServletRequest request) throws IOException {

        FileStoreDefinition def = this.fileStoreDao.getFileStoreDefinition(name);
        if(def == null)
            throw new NotFoundRestException();

        //Check Permissions
        def.ensureStoreWritePermission(user);

        String pathInStore = parsePath(request);

        File root = def.getRoot().getCanonicalFile();
        Path rootPath = root.toPath();
        File outputDirectory = new File(root, pathInStore).getCanonicalFile();

        if (!outputDirectory.toPath().startsWith(rootPath)) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, new TranslatableMessage("filestore.belowRoot", pathInStore));
        }

        if (outputDirectory.exists() && !outputDirectory.isDirectory()) {
            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir",
                    FileStoreUtils.removeToRoot(root, outputDirectory), name));
        }

        if(!outputDirectory.exists()){
            if(!outputDirectory.mkdirs())
                throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir",
                        FileStoreUtils.removeToRoot(root, outputDirectory), name));
        }

        //Put the file where it belongs
        List<FileModel> fileModels = new ArrayList<>();

        MultiValueMap<String, MultipartFile> filemap = multipartRequest.getMultiFileMap();
        for (String nameField : filemap.keySet()) {
            for (MultipartFile file : filemap.get(nameField)) {
                String filename;
                if (file instanceof CommonsMultipartFile) {
                    FileItem fileItem = ((CommonsMultipartFile) file).getFileItem();
                    filename = fileItem.getName();
                } else {
                    filename = file.getName();
                }

                try {
                    File newFile = FileStoreUtils.findUniqueFileName(outputDirectory, filename, overwrite);
                    File parent = newFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }

                    try (OutputStream output = new FileOutputStream(newFile, false)) {
                        try (InputStream input  = file.getInputStream()) {
                            StreamUtils.copy(input, output);
                        }
                    }
                    fileModels.add(fileToModel(newFile, root, request.getServletContext()));
                } catch(TranslatableException e) {
                    throw new GenericRestException(HttpStatus.FORBIDDEN, e.getTranslatableMessage());
                }
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
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("fileStoreName") String fileStoreName,
            @ApiParam(value = "Move file/folder to", required = false, allowMultiple = false)
            @RequestParam(required=false) String moveTo,
            @ApiParam(value = "Copy file/folder to", required = false, allowMultiple = false)
            @RequestParam(required=false) String copyTo,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) throws IOException, URISyntaxException {

        FileStoreDefinition def = this.fileStoreDao.getFileStoreDefinition(fileStoreName);
        if (def == null)
            throw new NotFoundRestException();

        // Check Permissions
        def.ensureStoreWritePermission(user);

        String pathInStore = parsePath(request);

        File root = def.getRoot().getCanonicalFile();
        File fileOrFolder = new File(root, pathInStore).getCanonicalFile();

        if (!fileOrFolder.toPath().startsWith(root.toPath())) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, new TranslatableMessage("filestore.belowRoot", pathInStore));
        }

        if (copyTo != null) {
            return copyFileOrFolder(request, fileStoreName, root, fileOrFolder, copyTo);
        } else if (moveTo != null) {
            return moveFileOrFolder(request, fileStoreName, root, fileOrFolder, moveTo);
        } else {
            return createFolder(request, fileStoreName, root, fileOrFolder);
        }
    }

    private ResponseEntity<FileModel> moveFileOrFolder(HttpServletRequest request, String fileStoreName, File root, File fileOrFolder, String moveTo) throws IOException, URISyntaxException {
        if (!fileOrFolder.exists())
            throw new NotFoundRestException();
        try {
            FileModel fileModel = fileToModel(FileStoreUtils.moveFileOrFolder(fileStoreName, root, fileOrFolder, moveTo), root, request.getServletContext());
            return new ResponseEntity<>(fileModel, HttpStatus.OK);
        } catch(TranslatableException e) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, e.getTranslatableMessage());
        }
    }

    private ResponseEntity<FileModel> copyFileOrFolder(HttpServletRequest request, String fileStoreName, File root, File srcFile, String dst) throws IOException, URISyntaxException {
        if (!srcFile.exists())
            throw new NotFoundRestException();
        try {
            FileModel fileModel = fileToModel(FileStoreUtils.copyFileOrFolder(fileStoreName, root, srcFile, dst), root, request.getServletContext());
            return new ResponseEntity<>(fileModel, HttpStatus.OK);
        } catch(TranslatableException e) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, e.getTranslatableMessage());
        }
    }

    private ResponseEntity<FileModel> createFolder(HttpServletRequest request, String fileStoreName, File root, File folder) throws IOException {
        if (folder.exists()) {
            if (folder.isDirectory()) {
                throw new GenericRestException(HttpStatus.CONFLICT, new TranslatableMessage("filestore.directoryExists",
                        FileStoreUtils.removeToRoot(root, folder), fileStoreName));
            } else {
                throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir",
                        FileStoreUtils.removeToRoot(root, folder), fileStoreName));
            }
        }

        if (!folder.mkdirs())
            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir",
                    FileStoreUtils.removeToRoot(root, folder), fileStoreName));

        FileModel fileModel = fileToModel(folder, root, request.getServletContext());
        return new ResponseEntity<>(fileModel, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete a file or directory")
    @RequestMapping(method = RequestMethod.DELETE, value="/{name}/**")
    public ResponseEntity<Void> delete(
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("name") String name,
            @ApiParam(value = "Recurisve delete of directory", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean recursive,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) throws IOException, HttpMediaTypeNotAcceptableException {

        FileStoreDefinition def = this.fileStoreDao.getFileStoreDefinition(name);
        if (def == null)
            throw new ResourceNotFoundException("File store: " + name);

        //Check permissions
        def.ensureStoreWritePermission(user);

        File root = def.getRoot().getCanonicalFile();
        String path = parsePath(request);
        File file = new File(root, path).getCanonicalFile();

        if (!file.toPath().startsWith(root.toPath())) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, new TranslatableMessage("filestore.belowRoot", path));
        }
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

    @ApiOperation(value = "Get a user file store model")
    @RequestMapping(method = RequestMethod.GET, value="/user-store/{storeName}")
    public ResponseEntity<FileStore> getUserFileStoreModel(@ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
    @PathVariable("storeName") String storeName,
    @AuthenticationPrincipal User user,
    HttpServletRequest request,
    HttpServletResponse response) throws IOException, HttpMediaTypeNotAcceptableException {
        FileStore fs = this.fileStoreDao.getUserFileStore(storeName);
        if (fs == null)
            throw new ResourceNotFoundException("File store: " + storeName);

        //Seeing the permissions fields should require write protection
        fs.toDefinition().ensureStoreWritePermission(user);

        return new ResponseEntity<>(fs, HttpStatus.OK);
    }

    @ApiOperation(value = "Create a user file store")
    @RequestMapping(method = RequestMethod.POST, value="/user-store/{storeName}")
    public ResponseEntity<FileStore> createUserFileStore(
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("storeName") String storeName,
            @ApiParam(value = "Valid File Store", required = true, allowMultiple = false)
            @RequestBody FileStore fileStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            HttpServletResponse response) {
        if(!Permissions.hasPermission(user, SystemSettingsDao.instance.getValue(UserFileStoreCreatePermissionDefinition.TYPE_NAME)))
            throw new PermissionException(new TranslatableMessage("filestore.user.createPermissionDenied", user.getUsername()), user);
        
        if(storeName == null || fileStore == null)
            throw new NotFoundRestException();
        fileStore.setStoreName(storeName);
        FileStoreDefinition fsd = this.fileStoreDao.getFileStoreDefinition(storeName);
        if(fsd != null)
            throw new GenericRestException(HttpStatus.CONFLICT, new TranslatableMessage("filestore.fileStoreExists", fileStore.getStoreName()));
        fileStore.setId(Common.NEW_ID);
        this.fileStoreDao.saveFileStore(fileStore);
        return new ResponseEntity<>(fileStore, HttpStatus.OK);
    }

    @ApiOperation(value = "Update a user file store")
    @RequestMapping(method = RequestMethod.PUT, value="/user-store/{id}")
    public ResponseEntity<FileStore> updateUserFileStore(
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("id") Integer id,
            @ApiParam(value = "Valid File Store", required = true, allowMultiple = false)
            @RequestBody FileStore fileStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            HttpServletResponse response) {
        if(id == null || fileStore == null)
            throw new NotFoundRestException();
        FileStore fs = this.fileStoreDao.getUserFileStoreById(id);
        if(fs == null)
            throw new NotFoundRestException();

        fs.toDefinition().ensureStoreWritePermission(user);

        fileStore.setId(id);
        this.fileStoreDao.saveFileStore(fileStore);
        return new ResponseEntity<>(fileStore, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a user file store")
    @RequestMapping(method = RequestMethod.DELETE, value="/user-store/{storeName}")
    public ResponseEntity<FileStore> deleteUserFileStore(
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("storeName") String storeName,
            @ApiParam(value = "Purge all files in file store", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean purgeFiles,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            HttpServletResponse response) {
        if(storeName == null)
            throw new NotFoundRestException();

        FileStore fs = this.fileStoreDao.getUserFileStore(storeName);
        if(fs == null) {
            //TODO check if it's a module-defined filestore and give a better error?
            throw new NotFoundRestException();
        }

        fs.toDefinition().ensureStoreWritePermission(user);
        try {
            FileStoreUtils.deleteFileStore(fs, purgeFiles);
        } catch(IOException e) {
            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.failedToPurgeFiles", storeName, e.getMessage()));
        }

        return new ResponseEntity<>(fs, HttpStatus.OK);
    }

    @ApiOperation(value = "List a directory or download a file from a store")
    @RequestMapping(method = RequestMethod.GET, value="/{name}/**")
    public ResponseEntity<?> download(
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("name") String name,
            @ApiParam(value = "Set content disposition to attachment", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean download,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, HttpMediaTypeNotAcceptableException {

        FileStoreDefinition def = this.fileStoreDao.getFileStoreDefinition(name);
        if (def == null)
            throw new ResourceNotFoundException("File store: " + name);

        //Check permissions
        def.ensureStoreReadPermission(user);

        File root = def.getRoot().getCanonicalFile();
        String path = parsePath(request);
        File file = new File(root, path).getCanonicalFile();

        if (!file.toPath().startsWith(root.toPath())) {
            throw new AccessDeniedException("Path is below file store root");
        }

        // TODO Allow downloading directory as a zip
        if (file.isFile()) {
            return getFile(file, download, request, response);
        } else {
            return listStoreContents(file, root, request);
        }
    }

    protected ResponseEntity<List<FileModel>> listStoreContents(File directory, File root, HttpServletRequest request) throws IOException {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

        if (directory.equals(root) && !root.exists())
            return new ResponseEntity<>(Collections.emptyList(), responseHeaders, HttpStatus.OK);

        if (!directory.exists())
            throw new ResourceNotFoundException(FileStoreUtils.relativePath(root, directory));

        Collection<File> files = Arrays.asList(directory.listFiles());
        List<FileModel> found = new ArrayList<>(files.size());

        for (File file : files)
            found.add(fileToModel(file, root, request.getServletContext()));

        Set<MediaType> mediaTypes = Sets.newHashSet(MediaType.APPLICATION_JSON_UTF8);
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, mediaTypes);

        return new ResponseEntity<>(found, responseHeaders, HttpStatus.OK);
    }

    protected ResponseEntity<FileSystemResource> getFile(File file, boolean download, HttpServletRequest request, HttpServletResponse response)
            throws HttpMediaTypeNotAcceptableException {

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
     * Get the path within the store off the URL
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    protected String parsePath(HttpServletRequest request) throws UnsupportedEncodingException {
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String ) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        AntPathMatcher apm = new AntPathMatcher();
        return URLDecoder.decode(apm.extractPathWithinPattern(bestMatchPattern, path), StandardCharsets.UTF_8.name());
    }

    public static FileModel fileToModel(File file, File root, ServletContext context) throws UnsupportedEncodingException {
        FileModel model = new FileModel();
        model.setFilename(file.getName());
        model.setFolderPath(FileStoreUtils.relativePath(root, file.getParentFile()));
        model.setDirectory(file.isDirectory());
        model.setLastModified(new Date(file.lastModified()));
        model.setMimeType(context.getMimeType(file.getName()));
        if (!file.isDirectory())
            model.setSize(file.length());
        return model;
    }
}
