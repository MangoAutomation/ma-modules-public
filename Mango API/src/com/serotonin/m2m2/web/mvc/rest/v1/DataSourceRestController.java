/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.exception.InvalidRQLRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.DataPointModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RestModelMapper;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.DataSourceStreamCallback;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Data Sources", description="Data Sources")
@RestController
@RequestMapping("/data-sources")
public class DataSourceRestController extends MangoVoRestController<DataSourceVO<?>, AbstractDataSourceModel<?>, DataSourceDao<DataSourceVO<?>>>{

    private final DataSourceService service;
    private final BiFunction<DataSourceVO<?>, User, AbstractDataSourceModel<?>> map;
    
    @Autowired
    public DataSourceRestController(final DataSourceService service, final RestModelMapper modelMapper){
        super(DataSourceDao.getInstance());
        this.service = service;
        this.map = (vo, user) -> {
            return modelMapper.map(vo, AbstractDataSourceModel.class, user);
        };
    }
    private static Log LOG = LogFactory.getLog(DataSourceRestController.class);

    @ApiOperation(
            value = "Query Data Sources",
            notes = "Use RQL formatted query",
            response=AbstractDataSourceModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<QueryDataPageStream<DataSourceVO<?>>> queryRQL(

            HttpServletRequest request) {

        RestProcessResult<QueryDataPageStream<DataSourceVO<?>>> result = new RestProcessResult<QueryDataPageStream<DataSourceVO<?>>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            try{
                ASTNode node = RQLUtils.parseRQLtoAST(request.getQueryString());
                DataSourceStreamCallback callback = new DataSourceStreamCallback(this, user);
                return result.createResponseEntity(getPageStream(node, callback));
            }catch(InvalidRQLRestException e){
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
                return result.createResponseEntity();
            }
        }
        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Get all data sources",
            notes = "Only returns data sources available to logged in user"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/list")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<AbstractDataSourceModel<?>>> getAllDataSources(HttpServletRequest request) {

        RestProcessResult<List<AbstractDataSourceModel<?>>> result = new RestProcessResult<List<AbstractDataSourceModel<?>>>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if(result.isOk()){
            List<DataSourceVO<?>> dataSources = DataSourceDao.getInstance().getAll();
            List<AbstractDataSourceModel<?>> models = new ArrayList<AbstractDataSourceModel<?>>();
            for(DataSourceVO<?> ds : dataSources){
                try{
                    if(Permissions.hasDataSourcePermission(user, ds))
                        models.add(map.apply(ds, user));
                }catch(PermissionException e){
                    //Munch Munch
                }

            }
            return result.createResponseEntity(models);
        }
        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Get data source by xid",
            notes = "Only returns data sources available to logged in user"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public ResponseEntity<AbstractDataSourceModel<?>> getDataSource(HttpServletRequest request, @PathVariable String xid) {

        RestProcessResult<AbstractDataSourceModel<?>> result = new RestProcessResult<AbstractDataSourceModel<?>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            DataSourceVO<?> vo = DataSourceDao.getInstance().getByXid(xid);

            if (vo == null) {
                return new ResponseEntity<AbstractDataSourceModel<?>>(HttpStatus.NOT_FOUND);
            }else{
                try{
                    if(Permissions.hasDataSourcePermission(user, vo))
                        return result.createResponseEntity(map.apply(vo, user));
                    else{
                        result.addRestMessage(getUnauthorizedMessage());
                        return result.createResponseEntity();
                    }
                }catch(PermissionException e){
                    LOG.warn(e.getMessage(), e);
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
            }
        }
        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Get data source by ID",
            notes = "Only returns data sources available to logged in user"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/by-id/{id}")
    public ResponseEntity<AbstractDataSourceModel<?>> getDataSourceById(
            @ApiParam(value = "Valid Data Source ID", required = true, allowMultiple = false)
            @PathVariable int id, HttpServletRequest request) {

        RestProcessResult<AbstractDataSourceModel<?>> result = new RestProcessResult<AbstractDataSourceModel<?>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            DataSourceVO<?> vo = DataSourceDao.getInstance().get(id);

            if (vo == null) {
                return new ResponseEntity<AbstractDataSourceModel<?>>(HttpStatus.NOT_FOUND);
            }else{
                try{
                    if(Permissions.hasDataSourcePermission(user, vo))
                        return result.createResponseEntity(map.apply(vo, user));
                    else{
                        result.addRestMessage(getUnauthorizedMessage());
                        return result.createResponseEntity();
                    }
                }catch(PermissionException e){
                    LOG.warn(e.getMessage(), e);
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
            }
        }
        return result.createResponseEntity();
    }

    /**
     * Put a data source into the system
     * @param xid
     * @param model
     * @param builder
     * @param request
     * @return
     */
    @ApiOperation(value = "Update data source")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<AbstractDataSourceModel<?>> updateDataSource(
            @PathVariable String xid,
            @RequestBody(required=true) AbstractDataSourceModel<?> model,
            UriComponentsBuilder builder,
            HttpServletRequest request) {

        RestProcessResult<AbstractDataSourceModel<?>> result = new RestProcessResult<AbstractDataSourceModel<?>>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if(result.isOk()){
            DataSourceVO<?> vo = model.getData();

            DataSourceVO<?> existing = DataSourceDao.getInstance().getByXid(xid);
            if (existing == null) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            //Check permissions
            try{
                if(!Permissions.hasDataSourcePermission(user, existing)){
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
            }catch(PermissionException e){
                LOG.warn(e.getMessage(), e);
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }

            vo.setId(existing.getId());

            ProcessResult validation = new ProcessResult();
            vo.validate(validation);

            if(model.validate() && Permissions.hasDataSourcePermission(user, vo)){
                Common.runtimeManager.saveDataSource(vo);
            }else{
                result.addRestMessage(this.getValidationFailedError());
                return result.createResponseEntity(model);
            }

            //Put a link to the updated data in the header?
            URI location = builder.path("/data-sources/{xid}").buildAndExpand(vo.getXid()).toUri();
            result.addRestMessage(getResourceUpdatedMessage(location));
            return result.createResponseEntity(model);
        }
        //Not logged in
        return result.createResponseEntity();
    }

    @ApiOperation(value = "Save data source")
    @RequestMapping(
            method = {RequestMethod.POST}
            )
    public ResponseEntity<AbstractDataSourceModel<?>> saveDataSource(
            @RequestBody(required=true) AbstractDataSourceModel<?> model,
            UriComponentsBuilder builder,
            HttpServletRequest request) {
        RestProcessResult<AbstractDataSourceModel<?>> result = new RestProcessResult<AbstractDataSourceModel<?>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()) {

            try {
                if(!Permissions.hasDataSourcePermission(user)) {
                    result.addRestMessage(this.getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
            } catch (PermissionException pe) {
                LOG.warn(pe.getMessage(), pe);
                result.addRestMessage(this.getUnauthorizedMessage());
                return result.createResponseEntity();
            }

            DataSourceVO<?> vo = model.getData();
            //Check to see if the data source already exists
            if(!StringUtils.isEmpty(vo.getXid())){
                DataSourceVO<?> existing = DataSourceDao.getInstance().getByXid(model.getXid());
                if(existing != null){
                    result.addRestMessage(HttpStatus.CONFLICT, new TranslatableMessage("rest.exception.alreadyExists", model.getXid()));
                    return result.createResponseEntity();
                }
            }

            if (StringUtils.isEmpty(vo.getXid()))
                vo.setXid(DataSourceDao.getInstance().generateUniqueXid());

            if(!model.validate() || !Permissions.hasDataSourcePermission(user, vo)) {
                result.addRestMessage(this.getValidationFailedError());
                return result.createResponseEntity(model);
            }
            else {
                Common.runtimeManager.saveDataSource(vo);
                DataSourceVO<?> created = DataSourceDao.getInstance().getByXid(model.getXid());
                URI location = builder.path("/data-sources/{xid}").buildAndExpand(new Object[]{created.getXid()}).toUri();
                result.addRestMessage(this.getResourceCreatedMessage(location));
                return result.createResponseEntity(map.apply(created, user));
            }
        } else {
            return result.createResponseEntity();
        }
    }


    @ApiOperation(value = "Delete data source")
    @RequestMapping(
            method = {RequestMethod.DELETE},
            value = {"/{xid}"}
            )
    public ResponseEntity<AbstractDataSourceModel<?>> deleteDataSource(@PathVariable String xid, UriComponentsBuilder builder, HttpServletRequest request) {
        RestProcessResult<AbstractDataSourceModel<?>> result = new RestProcessResult<AbstractDataSourceModel<?>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()) {
            DataSourceVO<?> existing = DataSourceDao.getInstance().getByXid(xid);
            if(existing == null) {
                result.addRestMessage(this.getDoesNotExistMessage());
                return result.createResponseEntity();
            } else {
                try {
                    if(!Permissions.hasDataSourcePermission(user, existing.getId())) {
                        result.addRestMessage(this.getUnauthorizedMessage());
                        return result.createResponseEntity();
                    }
                } catch (PermissionException pe) {
                    LOG.warn(pe.getMessage(), pe);
                    result.addRestMessage(this.getUnauthorizedMessage());
                    return result.createResponseEntity();
                }

                Common.runtimeManager.deleteDataSource(existing.getId());
                return result.createResponseEntity(map.apply(existing, user));
            }
        }
        return result.createResponseEntity();
    }

    @ApiOperation(value = "Copy data source", notes="Copy the data source with optional new XID and Name and enable/disable state (default disabled)")
    @RequestMapping(method = RequestMethod.PUT, value = "/copy/{xid}")
    public ResponseEntity<AbstractDataSourceModel<?>> copy(
            @PathVariable String xid,
            @ApiParam(value = "Copy's new XID", required = false, defaultValue="null", allowMultiple = false)
            @RequestParam(required=false, defaultValue="null") String copyXid,
            @ApiParam(value = "Copy's name", required = false, defaultValue="null", allowMultiple = false)
            @RequestParam(required=false, defaultValue="null") String copyName,
            @ApiParam(value = "Device name for copied points name", required = false, allowMultiple = false)
            @RequestParam(required=false) String copyDeviceName,
            @ApiParam(value = "Enable/disabled state", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean enabled,
            UriComponentsBuilder builder,
            HttpServletRequest request) {

        RestProcessResult<AbstractDataSourceModel<?>> result = new RestProcessResult<AbstractDataSourceModel<?>>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if(result.isOk()){
            DataSourceVO<?> existing = DataSourceDao.getInstance().getByXid(xid);
            if (existing == null) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            //Check permissions
            try{
                if(!Permissions.hasDataSourcePermission(user, existing)){
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
            }catch(PermissionException e){
                LOG.warn(e.getMessage(), e);
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }

            //Determine the new name
            String name;
            if(StringUtils.isEmpty(copyName))
                name = StringUtils.abbreviate(
                        TranslatableMessage.translate(Common.getTranslations(), "common.copyPrefix", existing.getName()), 40);
            else
                name = copyName;

            //Determine the new xid
            String newXid;
            if(StringUtils.isEmpty(copyXid))
                newXid = dao.generateUniqueXid();
            else
                newXid = copyXid;


            //Setup the Copy
            DataSourceVO<?> copy = existing.copy();
            copy.setId(Common.NEW_ID);
            copy.setName(name);
            copy.setXid(newXid);
            copy.setEnabled(enabled);

            ProcessResult validation = new ProcessResult();
            copy.validate(validation);

            AbstractDataSourceModel<?> model = map.apply(copy, user);

            if(model.validate()){
                Common.runtimeManager.saveDataSource(copy);
                this.dao.copyDataSourcePoints(existing.getId(), copy.getId(), copyDeviceName);
            }else{
                result.addRestMessage(this.getValidationFailedError());
                return result.createResponseEntity(model);
            }

            //Put a link to the updated data in the header?
            URI location = builder.path("/data-sources/{xid}").buildAndExpand(copy.getXid()).toUri();
            result.addRestMessage(getResourceUpdatedMessage(location));
            return result.createResponseEntity(model);
        }
        //Not logged in
        return result.createResponseEntity();
    }

    @ApiOperation(value = "Enable/disable/restart a data source")
    @RequestMapping(method = RequestMethod.PUT, value = "/enable-disable/{xid}")
    public ResponseEntity<DataPointModel> enableDisable(
            @AuthenticationPrincipal User user,

            @PathVariable String xid,

            @ApiParam(value = "Enable or disable the data source", required = true, allowMultiple = false)
            @RequestParam(required=true) boolean enabled,

            @ApiParam(value = "Restart the data source, enabled must equal true", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean restart) {
        DataSourceVO<?> dsvo = DataSourceDao.getInstance().getByXid(xid);
        if(dsvo == null)
            throw new NotFoundRestException();

        try {
            Permissions.ensureDataSourcePermission(user, dsvo);
        } catch(PermissionException e) {
            throw new AccessDeniedException("User does not have permission to edit the data source", e);
        }

        if (enabled && restart) {
            dsvo.setEnabled(true);
            Common.runtimeManager.saveDataSource(dsvo); //saving will restart it
        } else if(dsvo.isEnabled() != enabled) {
            dsvo.setEnabled(enabled);
            Common.runtimeManager.saveDataSource(dsvo);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public AbstractDataSourceModel<?> createModel(DataSourceVO<?> vo) {
        throw new UnsupportedOperationException();
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(java.lang.Object)
     */
    @Override
    public AbstractDataSourceModel<?> createModel(DataSourceVO<?> vo, User user) {
        if(vo != null)
            return map.apply(vo, user);
        else
            return null;
    }

}
