/*
 * Copyright (C) 2020 Infinite Automation Systems Inc. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.db.query.pojo.RQLFilterJavaBean;
import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.RoleViews;
import com.infiniteautomation.mango.rest.latest.model.StreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.filestore.FileStoreModel;
import com.infiniteautomation.mango.spring.service.FileStoreService;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.vo.FileStore;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

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
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public MappingJacksonValue getUserFileStoreModel(
            @PathVariable("xid") String xid,
            @AuthenticationPrincipal PermissionHolder user) {

        FileStore fs = this.fileStoreService.get(xid);

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
            UriComponentsBuilder builder) {

        FileStore newStore = this.fileStoreService.insert(fileStore.toVO());
        URI location = builder.path("/user-file-stores/{xid}").buildAndExpand(newStore.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new FileStoreModel(newStore), headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update a user file store")
    @RequestMapping(method = RequestMethod.PUT, value="/{xid}")
    public ResponseEntity<FileStoreModel> updateUserFileStore(
            @ApiParam(value = "File store XID", required = true)
            @PathVariable("xid") String xid,
            @ApiParam(value = "Updated file store", required = true)
            @RequestBody FileStoreModel fileStore,
            UriComponentsBuilder builder) {

        FileStore updated = this.fileStoreService.update(xid, fileStore.toVO());

        URI location = builder.path("/user-file-stores/{xid}").buildAndExpand(updated.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new FileStoreModel(updated), headers, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a user file store")
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public void deleteUserFileStore(
            @ApiParam(value = "File store XID", required = true)
            @PathVariable("xid") String xid) {

        this.fileStoreService.delete(xid);
    }
}
