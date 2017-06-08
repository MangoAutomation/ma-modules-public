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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

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
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.exception.ResourceNotFoundException;
import com.infiniteautomation.mango.rest.v2.model.filestore.FileModel;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.FileStoreDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.User;
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
			notes = "Must have write access to the store, will overwrite existing files"
			)
	@RequestMapping(method = RequestMethod.POST, consumes=MediaType.MULTIPART_FORM_DATA_VALUE, produces=MediaType.APPLICATION_JSON_UTF8_VALUE, value="/{name}/**")
    public ResponseEntity<List<FileModel>> uploadWithPath(
       		@ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
       	 	@PathVariable("name") String name,
    		@AuthenticationPrincipal User user,
    		MultipartHttpServletRequest multipartRequest,
    		HttpServletRequest request) throws IOException {
		
		FileStoreDefinition def = ModuleRegistry.getFileStoreDefinition(name);
		if(def == null)
			throw new NotFoundRestException();

		//Check Permissions
		def.ensureStoreWritePermission(user);

		String pathInStore = parsePath(request);
		
		File root = def.getRoot().getCanonicalFile();
		File toSave = new File(root, pathInStore).getCanonicalFile();

        if (!toSave.toPath().startsWith(root.toPath())) {
            throw new GenericRestException(HttpStatus.FORBIDDEN, new TranslatableMessage("filestore.belowRoot", pathInStore));
        }
		
		if (toSave.exists() && !toSave.isDirectory()) {
		    throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir", removeToRoot(root, toSave), name));
		}

		if(!toSave.exists()){
			if(!toSave.mkdirs())
				throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.cannotCreateDir", removeToRoot(root, toSave), name));
		}
		
		//Put the file where it belongs
		List<FileModel> fileModels = new ArrayList<>();
		
		MultiValueMap<String, MultipartFile> filemap = multipartRequest.getMultiFileMap();
		for (String nameField : filemap.keySet()) {
		    for (MultipartFile file : filemap.get(nameField)) {
	            String filename = file.getOriginalFilename();
	            File newFile = findUniqueFileName(toSave, filename);
	            try (OutputStream output = new FileOutputStream(newFile, false)) {
	                try (InputStream input  = file.getInputStream()) {
	                    ByteStreams.copy(input, output);
	                }
	            }
                fileModels.add(fileToModel(newFile, toSave, request.getServletContext()));
		    }
		}

		return new ResponseEntity<>(fileModels, HttpStatus.OK);
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

	@ApiOperation(value = "List a directory or download a file from a store")
	@RequestMapping(method = RequestMethod.GET, produces={}, value="/{name}/**")
    public ResponseEntity<?> download(
       		@ApiParam(value = "Valid File Store name", required = true, allowMultiple = false)
       	 	@PathVariable("name") String name,
       	 	@ApiParam(value = "Set content disposition to attachment", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean download,
    		@AuthenticationPrincipal User user,
    		HttpServletRequest request) throws IOException, HttpMediaTypeNotAcceptableException {
    	
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
		    return getFile(file, download, request);
		} else {
		    return listStoreContents(file, request);
		}
	}

	protected ResponseEntity<List<FileModel>> listStoreContents(File directory, HttpServletRequest request) throws IOException {
        Collection<File> files = Arrays.asList(directory.listFiles());
        List<FileModel> found = new ArrayList<>(files.size());
        
        for (File file : files)
            found.add(fileToModel(file, directory, request.getServletContext()));

        Set<MediaType> mediaTypes = Sets.newHashSet(MediaType.APPLICATION_JSON_UTF8);
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, mediaTypes);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        
        return new ResponseEntity<>(found, responseHeaders, HttpStatus.OK);
    }
	
	protected ResponseEntity<FileSystemResource> getFile(File file, boolean download, HttpServletRequest request) throws HttpMediaTypeNotAcceptableException {
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
	    String name = URLDecoder.decode(root.toURI().relativize(file.toURI()).toString(), StandardCharsets.UTF_8.name());
	    if (file.isDirectory() && name.endsWith("/")) {
	        name = name.substring(0, name.length() - 1);
	    }
	    return name;
	}
	
	public static FileModel fileToModel(File file, File relativeTo, ServletContext context) throws UnsupportedEncodingException {
	    FileModel model = new FileModel();
	    model.setFilename(removeToRoot(relativeTo, file));
	    model.setDirectory(file.isDirectory());
        model.setLastModified(new Date(file.lastModified()));
	    model.setMimeType(context.getMimeType(file.getName()));
	    if (!file.isDirectory())
	        model.setSize(file.length());
	    return model;
	}
}
