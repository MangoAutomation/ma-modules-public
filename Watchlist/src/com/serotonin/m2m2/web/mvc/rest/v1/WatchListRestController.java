/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.infiniteautomation.mango.rest.v2.bulk.VoAction;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.exception.InvalidRQLRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.ActionAndModel;
import com.infiniteautomation.mango.rest.v2.model.JSONStreamedArray;
import com.infiniteautomation.mango.rest.v2.model.StreamedArray;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.DataPointSummary;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.hierarchy.PointFolder;
import com.serotonin.m2m2.vo.hierarchy.PointHierarchy;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.watchlist.WatchListDao;
import com.serotonin.m2m2.watchlist.WatchListVO;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.FilteredPageQueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListDataPointModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListSummaryModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Watch Lists", description="")
@RestController
@RequestMapping("/watch-lists")
public class WatchListRestController extends MangoVoRestController<WatchListVO, WatchListSummaryModel, WatchListDao>{

    private static Log LOG = LogFactory.getLog(WatchListRestController.class);

    public WatchListRestController(){
        super(WatchListDao.getInstance());
    }

    @ApiOperation(
            value = "Query WatchLists",
            notes = "",
            response=WatchListSummaryModel.class,
            responseContainer="Array"
            )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response=WatchListSummaryModel.class),
            @ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
    })
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<QueryDataPageStream<WatchListVO>> queryRQL(
            HttpServletRequest request) {

        RestProcessResult<QueryDataPageStream<WatchListVO>> result = new RestProcessResult<QueryDataPageStream<WatchListVO>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            try{
                ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
                if(!user.isAdmin()){
                    //We are going to filter the results, so we need to strip out the limit(limit,offset) or limit(limit) clause.
                    WatchListStreamCallback callback = new WatchListStreamCallback(this, user);
                    FilteredPageQueryStream<WatchListVO, WatchListSummaryModel, WatchListDao> stream  =
                            new FilteredPageQueryStream<WatchListVO, WatchListSummaryModel, WatchListDao>(WatchListDao.getInstance(),
                                    this, query, callback);
                    stream.setupQuery();
                    return result.createResponseEntity(stream);
                }else
                    return result.createResponseEntity(getPageStream(query));
            }catch(InvalidRQLRestException e){
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
                return result.createResponseEntity();
            }
        }

        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Create New WatchList",
            notes = "",
            response=WatchListModel.class
            )
    @ApiResponses({
        @ApiResponse(code = 201, message = "User Created", response=WatchListModel.class),
        @ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class),
        @ApiResponse(code = 409, message = "WatchList Already Exists")
    })
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<WatchListModel> createNew(
            @ApiParam( value = "Watchlist to save", required = true )
            @RequestBody
            WatchListModel model,
            UriComponentsBuilder builder,
            HttpServletRequest request) throws RestValidationFailedException {

        RestProcessResult<WatchListModel> result = new RestProcessResult<WatchListModel>(HttpStatus.CREATED);
        User user = this.checkUser(request, result);
        if (!result.isOk()) {
            return result.createResponseEntity();
        }

        WatchListVO wl = model.getData();

        //Check XID if blank and generate one
        if(StringUtils.isBlank(wl.getXid())){
            wl.setXid(this.dao.generateUniqueXid());
        }
        //Add the user
        wl.setUserId(user.getId());

        //Setup the Points
        if(model.getPoints() != null)
            for(WatchListDataPointModel pm : model.getPoints())
                wl.getPointList().add(pm.getDataPointVO());

        //Ready to validate and then save
        if(!model.validate()){
            result.addRestMessage(this.getValidationFailedError());
            return result.createResponseEntity(model);
        }

        try {
            String initiatorId = request.getHeader("initiatorId");
            this.dao.save(wl, initiatorId);
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
        }
        return result.createResponseEntity(new WatchListModel(wl, this.dao.getPointSummaries(wl.getId())));
    }

    @ApiOperation(
            value = "Get a Watchlist",
            notes = "",
            response=WatchListModel.class
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public ResponseEntity<WatchListModel> get(
            @PathVariable String xid,
            HttpServletRequest request) throws RestValidationFailedException {
        RestProcessResult<WatchListModel> result = new RestProcessResult<WatchListModel>(HttpStatus.OK);
        try{
            User user = this.checkUser(request, result);
            if(result.isOk()){
                WatchListVO wl = this.dao.getByXid(xid);
                if(wl == null){
                    result.addRestMessage(getDoesNotExistMessage());
                    return result.createResponseEntity();
                }
                if(hasReadPermission(user, wl)){
                    List<WatchListDataPointModel> points = this.dao.getPointSummaries(wl.getId());
                    //Filter them on read permission
                    ListIterator<WatchListDataPointModel> it = points.listIterator();
                    while(it.hasNext()){
                        if(!Permissions.hasPermission(user, it.next().getReadPermission()))
                            it.remove();
                    }
                    return result.createResponseEntity(new WatchListModel(wl, points));
                }else{
                    result.addRestMessage(getUnauthorizedMessage());
                }
            }
        }catch(Exception e){
            LOG.warn(e.getMessage(), e);
            result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
        }
        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Update a WatchList",
            notes = "",
            response=WatchListModel.class
            )
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<WatchListModel> update(
            @PathVariable String xid,
            @RequestBody WatchListModel model,
            HttpServletRequest request) throws RestValidationFailedException {
        RestProcessResult<WatchListModel> result = new RestProcessResult<WatchListModel>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (!result.isOk()) {
            return result.createResponseEntity();
        }
        WatchListVO wl = this.dao.getByXid(xid);

        if(wl == null){
            result.addRestMessage(getDoesNotExistMessage());
            return result.createResponseEntity();
        }
        if(!hasEditPermission(user, wl)){
            result.addRestMessage(getUnauthorizedMessage());
            return result.createResponseEntity();
        }

        WatchListVO update = model.getData();
        //Set the id
        update.setId(wl.getId());
        //Add the user
        update.setUserId(wl.getUserId());

        //Setup the Points
        if(model.getPoints() != null)
            for(WatchListDataPointModel pm : model.getPoints())
                update.getPointList().add(pm.getDataPointVO());

        if (!model.validate()){
            result.addRestMessage(this.getValidationFailedError());
            return result.createResponseEntity(model);
        }


        try {
            String initiatorId = request.getHeader("initiatorId");
            this.dao.save(update, initiatorId);
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
        }

        return result.createResponseEntity(model);
    }

    @ApiOperation(
            value = "Delete a WatchList ",
            notes = "Only the owner or an admin can delete",
            response=WatchListModel.class
            )
    @RequestMapping(method = RequestMethod.DELETE, value = "/{xid}")
    public ResponseEntity<Void> delete(
            @PathVariable String xid,
            HttpServletRequest request) throws RestValidationFailedException {
        RestProcessResult<Void> result = new RestProcessResult<Void>(HttpStatus.OK);
        try{
            User user = this.checkUser(request, result);
            if(result.isOk()){
                WatchListVO wl = this.dao.getByXid(xid);
                if(wl == null){
                    result.addRestMessage(getDoesNotExistMessage());
                    return result.createResponseEntity();
                }
                if(isOwner(user, wl)){
                    String initiatorId = request.getHeader("initiatorId");
                    this.dao.delete(wl.getId(), initiatorId);
                    result.addRestMessage(HttpStatus.NO_CONTENT, new TranslatableMessage("common.deleted"));
                    return result.createResponseEntity();
                }else{
                    result.addRestMessage(this.getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
            }
        }catch(Exception e){
            LOG.warn(e.getMessage(), e);
            result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
        }
        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Get Data Points for a Watchlist",
            notes = "",
            response=WatchListPointsQueryDataPageStream.class
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}/data-points")
    public WatchListPointsQueryDataPageStream getDataPoints(
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {

        WatchListVO wl = this.dao.getByXid(xid);
        if (wl == null) {
            throw new NotFoundRestException();
        }

        if (!hasReadPermission(user, wl)) {
            throw new PermissionException(new TranslatableMessage("common.default", "Unauthorized access"), user);
        }

        return new WatchListPointsQueryDataPageStream(wl.getId(), user, dp -> new DataPointModel(dp));
    }

    @ApiOperation(
            value = "Get Data Points for a Watchlist for bulk import via CSV",
            notes = "Adds an additional action and originalXid column",
            response=WatchListPointsQueryDataPageStream.class
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}/data-points", produces="text/csv")
    public WatchListPointsQueryDataPageStream getDataPointsWithAction(
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {

        WatchListVO wl = this.dao.getByXid(xid);
        if (wl == null) {
            throw new NotFoundRestException();
        }

        if (!hasReadPermission(user, wl)) {
            throw new PermissionException(new TranslatableMessage("common.default", "Unauthorized access"), user);
        }

        return new WatchListPointsQueryDataPageStream(wl.getId(), user, dp -> {
            ActionAndModel<DataPointModel> actionAndModel = new ActionAndModel<>();
            actionAndModel.setAction(VoAction.UPDATE);
            actionAndModel.setOriginalXid(dp.getXid());
            actionAndModel.setModel(new DataPointModel(dp));
            return actionAndModel;
        });
    }

    @Override
    public WatchListSummaryModel createModel(WatchListVO vo) {
        return new WatchListSummaryModel(vo);
    }

    public static boolean isOwner(User user, WatchListVO vo) throws RuntimeException {
        if (user == null)
            return false;
        if (vo == null)
            return false;
        if (Permissions.hasAdminPermission(user))
            return true;
        if(vo.getUserId() == user.getId())
            return true; //Owner
        else
            return false;
    }

    public static boolean hasEditPermission(User user, WatchListVO vo) throws RuntimeException {
        if (user == null)
            return false;
        if (vo == null)
            return false;
        if (Permissions.hasAdminPermission(user))
            return true;
        if(vo.getUserId() == user.getId())
            return true; //Owner
        return Permissions.hasPermission(user, vo.getEditPermission());
    }

    public static boolean hasReadPermission(User user, WatchListVO vo) {
        if (user == null)
            return false;
        else if (vo == null)
            return false;
        else if (Permissions.hasAdminPermission(user))
            return true;
        else if(vo.getUserId() == user.getId())
            return true; //Owner
        else if(Permissions.hasPermission(user, vo.getEditPermission()))
            return true;
        else if(Permissions.hasPermission(user, vo.getReadPermission()))
            return true;
        else
            return false;
    }

    /**
     * Class to stream data points and restrict based on permissions
     * @author Terry Packer
     *
     */
    @JsonPropertyOrder({"items", "total"}) // ensures that items is serialized first so total gets correct value
    class WatchListPointsQueryDataPageStream implements StreamedArrayWithTotal {

        private final int watchlistId;
        private int pointCount = 0;
        private final User user;
        private final Function<DataPointVO, ?> mapToModel;

        public WatchListPointsQueryDataPageStream(int wlId, User user, Function<DataPointVO, ?> mapToModel) {
            this.watchlistId = wlId;
            this.user = user;
            this.mapToModel = mapToModel;
        }

        @Override
        public StreamedArray getItems() {
            WatchListVO wlvo = WatchListDao.getInstance().getWatchList(WatchListDao.getInstance().getXidById(watchlistId));
            if(WatchListVO.STATIC_TYPE.equals(wlvo.getType())) {
                return (JSONStreamedArray) (jgen) -> {
                    WatchListDao.getInstance().getPoints(watchlistId, new MappedRowCallback<DataPointVO>(){
    
                        @Override
                        public void row(DataPointVO dp, int index) {
                            if(Permissions.hasDataPointReadPermission(user, dp)){
                                DataPointDao.getInstance().loadPartialRelationalData(dp);
    
                                Object model = mapToModel.apply(dp);
    
                                try {
                                    jgen.writeObject(model);
                                    pointCount++;
                                } catch (IOException e) {
                                    LOG.error(e.getMessage(), e);
                                }
                            }
                        }
                    });
                };
            } else if(WatchListVO.QUERY_TYPE.equals(wlvo.getType())) {
                if(wlvo.getParams().size() > 0)
                    throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("common.default", "parameters in query not supported at endpoint yet"));
                return (JSONStreamedArray) (jgen) -> {
                    ASTNode node = RQLUtils.parseRQLtoAST(wlvo.getQuery());
                    DataPointDao.getInstance().rqlQuery(node, new MappedRowCallback<DataPointVO>(){
    
                        @Override
                        public void row(DataPointVO dp, int index) {
                            if(Permissions.hasDataPointReadPermission(user, dp)){
                                DataPointDao.getInstance().loadPartialRelationalData(dp);    
                                Object model = mapToModel.apply(dp);
    
                                try {
                                    jgen.writeObject(model);
                                    pointCount++;
                                } catch (IOException e) {
                                    LOG.error(e.getMessage(), e);
                                }
                            }
                        }
                    });
                };
            } else if(WatchListVO.HIERARCHY_TYPE.equals(wlvo.getType())) {
                PointHierarchy ph = DataPointDao.getInstance().getPointHierarchy(true);
                List<PointFolder> folders = getFolders(ph, wlvo.getFolderIds());
                List<DataPointVO> result = new ArrayList<>();
                for(PointFolder pf : folders) {
                    for(DataPointSummary dps : pf.getPoints()) {
                        DataPointVO dp = DataPointDao.getInstance().get(dps.getId());
                        if(Permissions.hasDataPointReadPermission(user, dp))
                            result.add(dp);
                    }
                }
                
                return (JSONStreamedArray)(jgen) -> {
                    for(DataPointVO dp : result) {
                        DataPointDao.getInstance().loadPartialRelationalData(dp);    
                        Object model = mapToModel.apply(dp);

                        try {
                            jgen.writeObject(model);
                            pointCount++;
                        } catch (IOException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                };
            } else if(WatchListVO.TAGS_TYPE.equals(wlvo.getType())) {
                throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("common.default", "parameters in query not supported at endpoint yet"));
            } else {
                throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("common.default", "unknown watchlist type: " + wlvo.getType()));
            }
        }

        @Override
        public int getTotal() {
            return pointCount;
        }

    }
    
    private List<PointFolder> getFolders(PointHierarchy ph, List<Integer> folderIds) {
        List<PointFolder> result = new ArrayList<>();
        getFoldersRecursive(ph.getRoot(), folderIds, result);
        return result;
    }
    
    private void getFoldersRecursive(PointFolder root, List<Integer> folderIds, List<PointFolder> result) {
        if(folderIds.contains(root.getId())) {
            addAllFoldersRecursive(root, result);
        } else
            for(PointFolder pf : root.getSubfolders())
                getFoldersRecursive(pf, folderIds, result);
    }
    
    private void addAllFoldersRecursive(PointFolder root, List<PointFolder> result) {
        for(PointFolder pf : root.getSubfolders())
            addAllFoldersRecursive(pf, result);
        result.add(root);
    }
}
