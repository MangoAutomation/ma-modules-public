/*
 * Copyright (C) 2020 Infinite Automation Systems Inc. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.db.query.pojo.RQLFilterJavaBean;
import com.infiniteautomation.mango.rest.latest.exception.GenericRestException;
import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.RoleViews;
import com.infiniteautomation.mango.rest.latest.model.StreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.filestore.FileStoreModel;
import com.infiniteautomation.mango.spring.service.FileStoreService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.vo.FileStore;
import com.serotonin.m2m2.vo.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * Used to create/update/delete user file stores. Also queries and gets file stores defined by a {@link com.serotonin.m2m2.module.FileStoreDefinition}
 * @author Jared Wiltshire
 */
@Api(value="User file stores")
@RestController
@RequestMapping("/user-file-stores")
public class UserFileStoresRestController {

    private final FileStoreService fileStoreService;

    @Autowired
    public UserFileStoresRestController(FileStoreService fileStoreService) {
        this.fileStoreService = fileStoreService;
    }

    @ApiOperation(value = "Query all file stores")
    @RequestMapping(method = RequestMethod.GET)
    public StreamWithTotal<FileStoreModel> queryAllStores(
            Translations translations,
            ASTNode query) {

        List<FileStoreModel> models = this.fileStoreService.getStores().stream()
                .map(FileStoreModel::new)
                .collect(Collectors.toList());

        return new FilteredStreamWithTotal<>(models, new RQLFilterJavaBean<>(query, translations));
    }

    @ApiOperation(value = "Get a user file store model")
    @RequestMapping(method = RequestMethod.GET, value="/{storeName}")
    public MappingJacksonValue getUserFileStoreModel(
            @PathVariable("storeName") String storeName,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            HttpServletResponse response) {
        FileStore fs = this.fileStoreService.getByName(storeName);

        //Seeing the permissions fields should require write protection
        MappingJacksonValue resultWithView = new MappingJacksonValue(new FileStoreModel(fs));
        if(fileStoreService.hasEditPermission(user, fs)) {
            resultWithView.setSerializationView(RoleViews.ShowRoles.class);
        }else {
            resultWithView.setSerializationView(Object.class);
        }
        return resultWithView;
    }

    @ApiOperation(value = "Create a user file store")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<FileStoreModel> createUserFileStore(
            @ApiParam(value = "File store to create", required = true)
            @RequestBody FileStoreModel fileStore,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        FileStore fs = this.fileStoreService.getByName(fileStore.getStoreName());
        if(fs != null)
            throw new GenericRestException(HttpStatus.CONFLICT, new TranslatableMessage("filestore.fileStoreExists", fileStore.getStoreName()));
        fileStore.setId(Common.NEW_ID);
        FileStore newStore = this.fileStoreService.insert(fileStore.toVO());

        URI location = builder.path("/user-file-stores/{storeName}").buildAndExpand(newStore.getStoreName()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new FileStoreModel(newStore), headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update a user file store")
    @RequestMapping(method = RequestMethod.PUT, value="/{id}")
    public ResponseEntity<FileStoreModel> updateUserFileStore(
            @ApiParam(value = "Valid File Store name", required = true)
            @PathVariable("id") Integer id,
            @ApiParam(value = "Valid File Store", required = true)
            @RequestBody FileStoreModel fileStore,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        FileStore existing = this.fileStoreService.get(id);
        fileStore.setId(id);
        FileStore updated = this.fileStoreService.update(existing, fileStore.toVO());

        URI location = builder.path("/user-file-stores/{storeName}").buildAndExpand(updated.getStoreName()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new FileStoreModel(updated), headers, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a user file store")
    @RequestMapping(method = RequestMethod.DELETE, value="/{storeName}")
    public void deleteUserFileStore(
            @ApiParam(value = "File Store name", required = true)
            @PathVariable("storeName") String storeName,
            @ApiParam(value = "Purge all files in file store", defaultValue="false")
            @RequestParam(required=false, defaultValue="false") boolean purgeFiles,
            @AuthenticationPrincipal User user) {

        FileStore toDelete = this.fileStoreService.getByName(storeName);
        try {
            this.fileStoreService.deleteFileStore(toDelete, purgeFiles);
        } catch(IOException e) {
            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("filestore.failedToPurgeFiles", storeName, e.getMessage()));
        }
    }
}
