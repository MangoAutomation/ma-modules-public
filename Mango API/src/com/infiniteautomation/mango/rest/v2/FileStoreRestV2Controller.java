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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.exception.ResourceNotFoundException;
import com.infiniteautomation.mango.rest.v2.model.filestore.FileModel;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.FileStoreDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.filter.MangoShallowEtagHeaderFilter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Manage files from stores defined by FileStoreDefinition(s)
 * 
 * @author Terry Packer
 */
@Api(value="File Store", description="Allow read/write access to file storage areas")
@RestController()
@RequestMapping("/v2/file-stores")
public class FileStoreRestV2Controller extends AbstractMangoRestV2Controller{

    final String cacheControlHeader;
    
    public FileStoreRestV2Controller() {
        // use the rest max age setting but dont honor the nocache setting
        cacheControlHeader = String.format(MangoShallowEtagHeaderFilter.MAX_AGE_TEMPLATE, Common.envProps.getLong("web.cache.maxAge.rest", 0L));
    }
    
	@ApiOperation(
			value = "List all file store names",
			notes = "Must have read access to see the store"
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<List<String>> list(
    		@AuthenticationPrincipal User user,
    		HttpServletRequest request) {
		
		Map<String, FileStoreDefinition> defs = ModuleRegistry.getFileStoreDefinitions();
		List<String> accessible = new ArrayList<String>(defs.size());
		if(user.isAdmin()){
			//admin users don't need to filter the results
			for(FileStoreDefinition def : defs.values()){
				def.ensureStoreReadPermission(user);
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
	@RequestMapping(method = RequestMethod.POST, consumes=MediaType.MULTIPART_FORM_DATA_VALUE, produces=MediaType.APPLICATION_JSON_UTF8_VALUE, value="/{name}/**")
    public ResponseEntity<List<FileModel>> uploadWithPath(
       		@ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
       	 	@PathVariable("name") String name,
    		
       	 	@AuthenticationPrincipal User user,
    		
    		@RequestParam(required=false, defaultValue="false") boolean overwrite,
    		
    		MultipartHttpServletRequest multipartRequest,
    		HttpServletRequest request) throws IOException {
		
		FileStoreDefinition def = ModuleRegistry.getFileStoreDefinition(name);
		if(def == null)
			throw new NotFoundRestException();

		//Check Permissions
		def.ensureStoreWritePermission(user);

		String pathInStore = parsePath(request);
		
		File root = def.getRoot().getCanonicalFile();
		File outputDirectory = new File(root, pathInStore).getCanonicalFile();

        if (!outputDirectory.toPath().startsWith(root.toPath())) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, new TranslatableMessage("filestore.belowRoot", pathInStore));
        }
		
		if (outputDirectory.exists() && !outputDirectory.isDirectory()) {
		    throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir", removeToRoot(root, outputDirectory), name));
		}

		if(!outputDirectory.exists()){
			if(!outputDirectory.mkdirs())
				throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir", removeToRoot(root, outputDirectory), name));
		}
		
		//Put the file where it belongs
		List<FileModel> fileModels = new ArrayList<>();
		
		MultiValueMap<String, MultipartFile> filemap = multipartRequest.getMultiFileMap();
		for (String nameField : filemap.keySet()) {
		    for (MultipartFile file : filemap.get(nameField)) {
	            String filename = file.getOriginalFilename();
	            File newFile;
                if (overwrite) {
                    newFile = new File(outputDirectory, filename);
                } else {
                    newFile = findUniqueFileName(outputDirectory, filename);
                }
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
    @RequestMapping(method = RequestMethod.POST, produces=MediaType.APPLICATION_JSON_UTF8_VALUE, value="/{fileStoreName}/**")
    public ResponseEntity<FileModel> createNewFolder(
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("fileStoreName") String fileStoreName,
            @ApiParam(value = "Move file/folder to", required = false, allowMultiple = false)
            @RequestParam(required=false) String moveTo,
            @ApiParam(value = "Copy file/folder to", required = false, allowMultiple = false)
            @RequestParam(required=false) String copyTo,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) throws IOException, URISyntaxException {
        
        FileStoreDefinition def = ModuleRegistry.getFileStoreDefinition(fileStoreName);
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
        if (!fileOrFolder.exists()) {
            throw new NotFoundRestException();
        }

        Path srcPath = fileOrFolder.toPath();
        
        File dstFile = new File(fileOrFolder.getParentFile(), moveTo).getCanonicalFile();
        Path dstPath = dstFile.toPath();
        if (!dstPath.startsWith(root.toPath())) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, new TranslatableMessage("filestore.belowRoot", moveTo));
        }
        
        if (dstFile.isDirectory()) {
            dstPath = dstPath.resolve(srcPath.getFileName());
        }

        Path movedPath;
        try {
            movedPath = java.nio.file.Files.move(srcPath, dstPath);
        } catch (FileAlreadyExistsException e) {
            throw new GenericRestException(HttpStatus.CONFLICT, new TranslatableMessage("filestore.fileExists", dstPath.getFileName()));
        }
        File movedFile = new File(movedPath.toUri());
        
        FileModel fileModel = fileToModel(movedFile, root, request.getServletContext());
        return new ResponseEntity<>(fileModel, HttpStatus.OK);
    }
    
    private ResponseEntity<FileModel> copyFileOrFolder(HttpServletRequest request, String fileStoreName, File root, File srcFile, String dst) throws IOException, URISyntaxException {
        if (!srcFile.exists()) {
            throw new NotFoundRestException();
        }
        if (srcFile.isDirectory()) {
            throw new GenericRestException(HttpStatus.BAD_REQUEST, new TranslatableMessage("filestore.cantCopyDirectory"));
        }

        Path srcPath = srcFile.toPath();
        
        File dstFile = new File(srcFile.getParentFile(), dst).getCanonicalFile();
        Path dstPath = dstFile.toPath();
        if (!dstPath.startsWith(root.toPath())) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, new TranslatableMessage("filestore.belowRoot", dst));
        }
        
        if (dstFile.isDirectory()) {
            dstPath = dstPath.resolve(srcPath.getFileName());
        }

        Path copiedPath;
        try {
            copiedPath = java.nio.file.Files.copy(srcPath, dstPath);
        } catch (FileAlreadyExistsException e) {
            throw new GenericRestException(HttpStatus.CONFLICT, new TranslatableMessage("filestore.fileExists", dstPath.getFileName()));
        }
        File copiedFile = new File(copiedPath.toUri());
        
        FileModel fileModel = fileToModel(copiedFile, root, request.getServletContext());
        return new ResponseEntity<>(fileModel, HttpStatus.OK);
    }
    
    private ResponseEntity<FileModel> createFolder(HttpServletRequest request, String fileStoreName, File root, File folder) throws IOException {
        if (folder.exists()) {
            if (folder.isDirectory()) {
                throw new GenericRestException(HttpStatus.CONFLICT, new TranslatableMessage("filestore.directoryExists", removeToRoot(root, folder), fileStoreName));
            } else {
                throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir", removeToRoot(root, folder), fileStoreName));
            }
        }

        if (!folder.mkdirs())
            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir", removeToRoot(root, folder), fileStoreName));

        FileModel fileModel = fileToModel(folder, root, request.getServletContext());
        return new ResponseEntity<>(fileModel, HttpStatus.CREATED);
    }

	private File findUniqueFileName(File directory, String filename) {
	    File file = new File(directory, filename);
	    
        String originalName = Files.getNameWithoutExtension(filename);
        String extension = Files.getFileExtension(filename);
	    int i = 1;
        
	    while (file.exists()) {
	        if (extension.isEmpty()) {
	            file = new File(directory, String.format("%s_%03d", originalName, i++));
	        } else {
	            file = new File(directory, String.format("%s_%03d.%s", originalName, i++, extension));
	        }
	    }
	    
	    return file;
	}
	
    @ApiOperation(value = "Delete a file or directory")
    @RequestMapping(method = RequestMethod.DELETE, produces={}, value="/{name}/**")
    public ResponseEntity<Void> delete(
            @ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
            @PathVariable("name") String name,
            @ApiParam(value = "Recurisve delete of directory", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean recursive,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) throws IOException, HttpMediaTypeNotAcceptableException {
        
        FileStoreDefinition def = ModuleRegistry.getFileStoreDefinition(name);
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

	@ApiOperation(value = "List a directory or download a file from a store")
	@RequestMapping(method = RequestMethod.GET, produces={}, value="/{name}/**")
    public ResponseEntity<?> download(
       		@ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
       	 	@PathVariable("name") String name,
       	 	@ApiParam(value = "Set content disposition to attachment", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean download,
    		@AuthenticationPrincipal User user,
    		HttpServletRequest request,
    		HttpServletResponse response) throws IOException, HttpMediaTypeNotAcceptableException {
    	
		FileStoreDefinition def = ModuleRegistry.getFileStoreDefinition(name);
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
		if(!file.exists())
			throw new ResourceNotFoundException("filestore/" + name + "/" + path);

        // TODO Allow downloading directory as a zip
		if (file.isFile()) {
		    return getFile(file, download, request, response);
		} else {
		    return listStoreContents(file, root, request);
		}
	}

	protected ResponseEntity<List<FileModel>> listStoreContents(File directory, File root, HttpServletRequest request) throws IOException {
        Collection<File> files = Arrays.asList(directory.listFiles());
        List<FileModel> found = new ArrayList<>(files.size());
        
        for (File file : files)
            found.add(fileToModel(file, root, request.getServletContext()));

        Set<MediaType> mediaTypes = Sets.newHashSet(MediaType.APPLICATION_JSON_UTF8);
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, mediaTypes);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        
        return new ResponseEntity<>(found, responseHeaders, HttpStatus.OK);
    }
	
	protected ResponseEntity<FileSystemResource> getFile(File file, boolean download, HttpServletRequest request, HttpServletResponse response)
	        throws HttpMediaTypeNotAcceptableException {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, download ? "attachment" : "inline");

        MediaType mediaType = null;

        Set<MediaType> mediaTypes = Sets.newHashSet(MediaType.APPLICATION_OCTET_STREAM);
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, mediaTypes);
        
        // ResourceHttpMessageConverter uses ActivationMediaTypeFactory.getMediaType(resource) but this is not visible
        String mimeType = request.getServletContext().getMimeType(file.getName());
        if (StringUtils.hasText(mimeType)) {
            try {
                mediaType = MediaType.parseMediaType(mimeType);
            } catch (InvalidMediaTypeException e) {
                // Shouldn't happen - ServletContext.getMimeType() should return valid mime types
            }
        }

        // always set the content type header or AbstractHttpMessageConverter.addDefaultHeaders() will set the Content-Type
        // to whatever the Accept header was
        if (mediaType == null) {
            mediaTypes.add(MediaType.ALL);
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        } else {
            mediaTypes.add(mediaType);
            responseHeaders.setContentType(mediaType);
        }

        // this doesn't work as a header from ResponseEntity wont be set if it is already set in the response
        //responseHeaders.setCacheControl(cacheControlHeader);
        response.setHeader(HttpHeaders.CACHE_CONTROL, cacheControlHeader);
        
        return new ResponseEntity<>(new FileSystemResource(file), responseHeaders, HttpStatus.OK);
	}
	
    /**
     * Get the path within the store off the URL
     * @param request
     * @return
     * @throws UnsupportedEncodingException 
     */
    protected String parsePath(HttpServletRequest request) throws UnsupportedEncodingException{
	    String path = (String) request.getAttribute(
	            HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
	    String bestMatchPattern = (String ) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

	    AntPathMatcher apm = new AntPathMatcher();
	    return URLDecoder.decode(apm.extractPathWithinPattern(bestMatchPattern, path), StandardCharsets.UTF_8.name());
    }

	/**
	 * Remove the path up to the root folder
	 * @param root
	 * @param file
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
    public static String removeToRoot(File root, File file) throws UnsupportedEncodingException {
        Path relativePath = root.toPath().relativize(file.toPath());
        String relativePathStr = relativePath.toString().replace(File.separatorChar, '/');

	    if (file.isDirectory() && relativePathStr.endsWith("/")) {
	        relativePathStr = relativePathStr.substring(0, relativePathStr.length() - 1);
	    }
	    return relativePathStr;
	}
	
	public static FileModel fileToModel(File file, File root, ServletContext context) throws UnsupportedEncodingException {
        Path relativeFolderPath = root.toPath().relativize(file.getParentFile().toPath());
        String relativeFolderPathStr = relativeFolderPath.toString().replace(File.separatorChar, '/');
	    
	    FileModel model = new FileModel();
	    model.setFilename(file.getName());
	    model.setFolderPath(relativeFolderPathStr);
	    model.setDirectory(file.isDirectory());
        model.setLastModified(new Date(file.lastModified()));
	    model.setMimeType(context.getMimeType(file.getName()));
	    if (!file.isDirectory())
	        model.setSize(file.length());
	    return model;
	}
}
