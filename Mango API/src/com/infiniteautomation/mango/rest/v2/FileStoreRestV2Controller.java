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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
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
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.collect.Sets;
import com.infiniteautomation.mango.rest.v2.ScriptRestController.ScriptEvalModel;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.exception.ResourceNotFoundException;
import com.infiniteautomation.mango.rest.v2.model.RoleViews;
import com.infiniteautomation.mango.rest.v2.model.filestore.FileModel;
import com.infiniteautomation.mango.rest.v2.model.filestore.FileStoreModel;
import com.infiniteautomation.mango.rest.v2.resolver.RemainingPath;
import com.infiniteautomation.mango.spring.script.PathMangoScript;
import com.infiniteautomation.mango.spring.script.ScriptService;
import com.infiniteautomation.mango.spring.service.FileStoreService;
import com.infiniteautomation.mango.spring.service.RoleService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.FileStore;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.role.Role;

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

    private final FileStoreService service;
    private final String cacheControlHeader;
    private final ScriptService scriptService;
    private final RoleService roleService;

    @Autowired
    public FileStoreRestV2Controller(FileStoreService fileStoreService, @Value("${web.cache.maxAge.rest:0}") long maxAge,
            ScriptService scriptService, RoleService roleService) {
        // use the rest max age setting but dont honor the nocache setting
        this.cacheControlHeader = CacheControl.maxAge(maxAge, TimeUnit.SECONDS).getHeaderValue();
        this.service = fileStoreService;
        this.scriptService = scriptService;
        this.roleService = roleService;
    }

    @ApiOperation(
            value = "List all file store names",
            notes = "Must have read access to see the store"
            )
    @RequestMapping(method = RequestMethod.GET)
    public List<String> list(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        return this.service.getStoreNames();
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

            @RemainingPath String pathInStore,

            MultipartHttpServletRequest multipartRequest,
            HttpServletRequest request) throws IOException {


        Path rootPath = this.service.getFileStoreRootForWrite(name);
        File root = rootPath.toFile().getCanonicalFile();

        File outputDirectory = new File(root, pathInStore).getCanonicalFile();

        if (!outputDirectory.toPath().startsWith(rootPath)) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, new TranslatableMessage("filestore.belowRoot", pathInStore));
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
                    File newFile = service.findUniqueFileName(outputDirectory, filename, overwrite);
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
            @RemainingPath String pathInStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) throws IOException, URISyntaxException {

        Path rootPath = this.service.getFileStoreRootForWrite(fileStoreName);
        File root = rootPath.toFile().getCanonicalFile();

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
            FileModel fileModel = fileToModel(service.moveFileOrFolder(fileStoreName, root, fileOrFolder, moveTo), root, request.getServletContext());
            return new ResponseEntity<>(fileModel, HttpStatus.OK);
        } catch(TranslatableException e) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, e.getTranslatableMessage());
        }
    }

    private ResponseEntity<FileModel> copyFileOrFolder(HttpServletRequest request, String fileStoreName, File root, File srcFile, String dst) throws IOException, URISyntaxException {
        if (!srcFile.exists())
            throw new NotFoundRestException();
        try {
            FileModel fileModel = fileToModel(service.copyFileOrFolder(fileStoreName, root, srcFile, dst), root, request.getServletContext());
            return new ResponseEntity<>(fileModel, HttpStatus.OK);
        } catch(TranslatableException e) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, e.getTranslatableMessage());
        }
    }

    private ResponseEntity<FileModel> createFolder(HttpServletRequest request, String fileStoreName, File root, File folder) throws IOException {
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
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("name") String name,
            @ApiParam(value = "Recurisve delete of directory", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean recursive,
            @RemainingPath String pathInStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) throws IOException, HttpMediaTypeNotAcceptableException {

        Path rootPath = this.service.getFileStoreRootForWrite(name);
        File root = rootPath.toFile().getCanonicalFile();

        File file = new File(root, pathInStore).getCanonicalFile();

        if (!file.toPath().startsWith(root.toPath())) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, new TranslatableMessage("filestore.belowRoot", pathInStore));
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
    public MappingJacksonValue getUserFileStoreModel(@ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
    @PathVariable("storeName") String storeName,
    @AuthenticationPrincipal User user,
    HttpServletRequest request,
    HttpServletResponse response) throws IOException, HttpMediaTypeNotAcceptableException {
        FileStore fs = this.service.getByName(storeName);

        //Seeing the permissions fields should require write protection
        MappingJacksonValue resultWithView = new MappingJacksonValue(new FileStoreModel(fs));
        if(service.hasEditPermission(user, fs)) {
            resultWithView.setSerializationView(RoleViews.ShowRoles.class);
        }else {
            resultWithView.setSerializationView(Object.class);
        }
        return resultWithView;
    }

    @ApiOperation(value = "Create a user file store")
    @RequestMapping(method = RequestMethod.POST, value="/user-store/{storeName}")
    public ResponseEntity<FileStoreModel> createUserFileStore(
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("storeName") String storeName,
            @ApiParam(value = "Valid File Store", required = true, allowMultiple = false)
            @RequestBody FileStoreModel fileStore,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        if(storeName == null || fileStore == null)
            throw new NotFoundRestException();
        fileStore.setStoreName(storeName);
        FileStore fs = this.service.getByName(storeName);
        if(fs != null)
            throw new GenericRestException(HttpStatus.CONFLICT, new TranslatableMessage("filestore.fileStoreExists", fileStore.getStoreName()));
        fileStore.setId(Common.NEW_ID);
        FileStore newStore = this.service.insert(fileStore.toVO());

        URI location = builder.path("/file-stores/user-store/{storeName}").buildAndExpand(newStore.getStoreName()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(new FileStoreModel(newStore), headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update a user file store")
    @RequestMapping(method = RequestMethod.PUT, value="/user-store/{id}")
    public ResponseEntity<FileStoreModel> updateUserFileStore(
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("id") Integer id,
            @ApiParam(value = "Valid File Store", required = true, allowMultiple = false)
            @RequestBody FileStoreModel fileStore,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        if(id == null || fileStore == null)
            throw new NotFoundRestException();
        FileStore existing = this.service.get(id);
        fileStore.setId(id);
        FileStore updated = this.service.update(existing, fileStore.toVO());

        URI location = builder.path("/file-stores/user-store/{storeName}").buildAndExpand(updated.getStoreName()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(new FileStoreModel(updated), headers, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a user file store")
    @RequestMapping(method = RequestMethod.DELETE, value="/user-store/{storeName}")
    public FileStoreModel deleteUserFileStore(
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("storeName") String storeName,
            @ApiParam(value = "Purge all files in file store", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean purgeFiles,
            @AuthenticationPrincipal User user) {
        FileStore toDelete = this.service.getByName(storeName);
        try {
            this.service.deleteFileStore(toDelete, purgeFiles);
        } catch(IOException e) {
            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.failedToPurgeFiles", storeName, e.getMessage()));
        }
        return new FileStoreModel(toDelete);
    }

    @ApiOperation(value = "List a directory or download a file from a store")
    @RequestMapping(method = RequestMethod.GET, value="/{name}/**")
    public ResponseEntity<?> download(
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("name") String name,
            @ApiParam(value = "Set content disposition to attachment", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean download,
            @RemainingPath String pathInStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, HttpMediaTypeNotAcceptableException {

        Path rootPath = this.service.getFileStoreRootForRead(name);
        File root = rootPath.toFile().getCanonicalFile();

        File file = new File(root, pathInStore).getCanonicalFile();

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

    /**
     * WARNING: This end point can be accessed publicly via the /file-stores/* filter
     */
    @ApiOperation(value = "Download a file from a store")
    @RequestMapping(method = RequestMethod.GET, value="/download-file/{name}/**")
    public ResponseEntity<?> downloadOnly(
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("name") String name,
            @ApiParam(value = "Set content disposition to attachment", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean download,
            @RemainingPath String pathInStore,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, HttpMediaTypeNotAcceptableException {

        Path rootPath = this.service.getFileStoreRootForRead(name);
        File root = rootPath.toFile().getCanonicalFile();

        File file = new File(root, pathInStore).getCanonicalFile();

        if (!file.toPath().startsWith(root.toPath())) {
            throw new AccessDeniedException("Path is below file store root");
        }

        if (!file.isFile()) {
            throw new ResourceNotFoundException("Can't list directory");
        }
        return getFile(file, download, request, response);
    }

    @Async
    @ApiOperation(value = "Evaluate a filestore file as a script on the backend using a scripting engine")
    @RequestMapping(method = RequestMethod.POST, value="/eval-script/{filestoreName}/**", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public void evalScript(
            @ApiParam(value = "Filestore name", required = true, allowMultiple = false)
            @PathVariable("filestoreName") String filestoreName,

            @RequestBody(required = false)
            ScriptEvalModel model,

            @RemainingPath String path,

            @AuthenticationPrincipal User user) throws IOException {

        Path rootPath = service.getFileStoreRootForRead(filestoreName);
        Path filePath = rootPath.resolve(path);

        String engineName;
        if (model != null && model.engineName != null) {
            engineName = model.engineName;
        } else {
            engineName = scriptService.findEngineForFile(filePath);
        }

        Charset charset;
        if (model != null && model.charset != null) {
            charset = Charset.forName(model.charset);
        } else {
            charset = StandardCharsets.UTF_8;
        }

        Set<Role> roles;
        if (model != null && model.roles != null) {
            roles = model.roles.stream().map(xid -> this.roleService.get(xid).getRole()).collect(Collectors.toSet());
        } else {
            roles = user.getRoles();
        }

        Map<String, Object> bindings;
        if (model != null && model.bindings != null) {
            bindings = model.bindings;
        } else {
            bindings = Collections.emptyMap();
        }

        this.scriptService.eval(new PathMangoScript(engineName, roles, filePath, charset), bindings);
    }

    protected ResponseEntity<List<FileModel>> listStoreContents(File directory, File root, HttpServletRequest request) throws IOException {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

        if (directory.equals(root) && !root.exists())
            return new ResponseEntity<>(Collections.emptyList(), responseHeaders, HttpStatus.OK);

        if (!directory.exists())
            throw new ResourceNotFoundException(service.relativePath(root, directory));

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
     * Convert a file to a model
     * @param file
     * @param root
     * @param context
     * @return
     * @throws UnsupportedEncodingException
     */
    public FileModel fileToModel(File file, File root, ServletContext context) throws UnsupportedEncodingException {
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
