/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.db.query.pojo.RQLToObjectListQuery;
import com.infiniteautomation.mango.rest.v2.exception.InvalidRQLRestException;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.EventDao;
import com.serotonin.m2m2.email.MangoEmailContent;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.maint.work.EmailWorkItem;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.dwr.ModulesDwr;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryResultModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.system.DiskInfoModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.system.SystemInfoModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.system.TimezoneModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.system.TimezoneUtility;
import com.serotonin.m2m2.web.mvc.spring.security.MangoSessionRegistry;
import com.serotonin.util.DirectoryInfo;
import com.serotonin.util.DirectoryUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Server Information", description="Server Information")
@RestController
@RequestMapping("/server")
public class ServerRestController extends MangoRestController{

    private static Log LOG = LogFactory.getLog(ServerRestController.class);

    @Autowired
    MangoSessionRegistry sessionRegistry;

    private List<TimezoneModel> allTimezones;
    private TimezoneModel defaultServerTimezone;

    public ServerRestController(){
        this.allTimezones  = TimezoneUtility.getTimeZoneIdsWithOffset();
        this.defaultServerTimezone = new TimezoneModel("", new TranslatableMessage("users.timezone.def").translate(Common.getTranslations()), 0);
        //Always add the default to the start of the list
        this.allTimezones.add(0, this.defaultServerTimezone);
    }

    @ApiOperation(
            value = "Query Timezones",
            notes = "",
            response=TimezoneModel.class,
            responseContainer="Array"
            )
    @RequestMapping(method = RequestMethod.GET, value="/timezones")
    public ResponseEntity<PageQueryResultModel<TimezoneModel>> queryTimezone(HttpServletRequest request) {

        RestProcessResult<PageQueryResultModel<TimezoneModel>> result = new RestProcessResult<PageQueryResultModel<TimezoneModel>>(HttpStatus.OK);

        this.checkUser(request, result);
        if(result.isOk()){
            try{
                //Parse the RQL Query
                ASTNode root = RQLUtils.parseRQLtoAST(request.getQueryString());

                List<TimezoneModel> list = root.accept(new RQLToObjectListQuery<TimezoneModel>(), allTimezones);

                PageQueryResultModel<TimezoneModel> model = new PageQueryResultModel<TimezoneModel>(list, allTimezones.size() + 1);
                return result.createResponseEntity(model);
            }catch(InvalidRQLRestException e){
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
                return result.createResponseEntity();
            }
        }

        return result.createResponseEntity();
    }

    @ApiOperation(value = "Send a test email", notes="Sends email to supplied address")
    @RequestMapping(method = RequestMethod.PUT, value = "/email/test")
    public ResponseEntity<String> sendTestEmail(
            @RequestParam(value = "email", required = true, defaultValue = "") String email,
            @RequestParam(value = "username", required = true, defaultValue = "") String username,
            HttpServletRequest request) throws RestValidationFailedException {

        RestProcessResult<String> result = new RestProcessResult<String>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            if(Permissions.hasAdminPermission(user)){
                try{
                    Translations translations = Common.getTranslations();
                    Map<String, Object> model = new HashMap<>();
                    model.put("message", new TranslatableMessage("ftl.userTestEmail", username));
                    MangoEmailContent cnt = new MangoEmailContent("testEmail", model, translations,
                            translations.translate("ftl.testEmail"), Common.UTF8);
                    EmailWorkItem.queueEmail(email, cnt);
                    return result.createResponseEntity(new TranslatableMessage("common.testEmailSent", email).translate(Common.getTranslations()));
                }catch(Exception e){
                    result.addRestMessage(this.getInternalServerErrorMessage(e.getMessage()));
                }
            }else{
                LOG.warn("Non admin user: " + user.getUsername() + " attempted to send a test email.");
                result.addRestMessage(this.getUnauthorizedMessage());
            }
        }

        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "System Info",
            notes = "Provides disk use, db sizes and point, event counts",
            response=Map.class
            )
    @RequestMapping(method = RequestMethod.GET, value="/system-info")
    public ResponseEntity<SystemInfoModel> getSystemInfo(HttpServletRequest request) {

        RestProcessResult<SystemInfoModel> result = new RestProcessResult<SystemInfoModel>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if(result.isOk()){
            if(user.isAdmin()){
                SystemInfoModel model = new SystemInfoModel();
                // Database size
                model.setSqlDbSizeBytes(Common.databaseProxy.getDatabaseSizeInBytes());

                //Do we have any NoSQL Data
                if (Common.databaseProxy.getNoSQLProxy() != null){
                    String pointValueStoreName = Common.envProps.getString("db.nosql.pointValueStoreName", "mangoTSDB");
                    model.setNoSqlDbSizeBytes(Common.databaseProxy.getNoSQLProxy().getDatabaseSizeInBytes(pointValueStoreName));
                }
                // Filedata data
                DirectoryInfo fileDatainfo = DirectoryUtils.getSize(new File(Common.getFiledataPath()));
                model.setFileDataSizeBytes(fileDatainfo.getSize());


                // Point history counts.
                model.setTopPoints(DataPointDao.getInstance().getTopPointHistoryCounts());
                model.setEventCount(EventDao.getInstance().getEventCount());

                //Disk Info
                FileSystem fs = FileSystems.getDefault();
                List<DiskInfoModel> disks = new ArrayList<DiskInfoModel>();
                model.setDisks(disks);
                for(Path root : fs.getRootDirectories()){
                    try {
                        FileStore store = Files.getFileStore(root);
                        DiskInfoModel disk = new DiskInfoModel();
                        disk.setName(root.getRoot().toString());
                        disk.setTotalSpaceBytes(store.getTotalSpace());
                        disk.setUsableSpaceBytes(store.getUsableSpace());
                        disks.add(disk);
                    } catch (IOException e) { }
                }

                //CPU Info
                OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
                model.setLoadAverage(osBean.getSystemLoadAverage());

                //OS Info
                model.setArchitecture(osBean.getArch());
                model.setOperatingSystem(osBean.getName());
                model.setOsVersion(osBean.getVersion());


                return result.createResponseEntity(model);
            }else{
                result.addRestMessage(HttpStatus.UNAUTHORIZED, new TranslatableMessage("common.default", "User not admin"));
            }
        }

        return result.createResponseEntity();
    }

    @ApiOperation(value = "Restart Mango", notes="Returns URL for status updates while web interface is still active")
    @RequestMapping(method = RequestMethod.PUT, value = "/restart")
    public ResponseEntity<String> restart(UriComponentsBuilder builder, HttpServletRequest request) throws RestValidationFailedException {

        RestProcessResult<String> result = new RestProcessResult<String>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            if(Permissions.hasAdminPermission(user)){
                ProcessResult r = ModulesDwr.scheduleRestart();
                if(r.getData().get("shutdownUri") != null){
                    //TODO Make SystemStatus web socket and push out message around shutdown
                    URI location = builder.path("/status/mango").buildAndExpand().toUri();
                    result.addRestMessage(getResourceUpdatedMessage(location));
                }
                else {
                    result.addRestMessage(HttpStatus.NOT_MODIFIED, new TranslatableMessage("modules.restartAlreadyScheduled"));
                }
            }else{
                LOG.warn("Non admin user: " + user.getUsername() + " attempted to restart Mango.");
                result.addRestMessage(this.getUnauthorizedMessage());
            }
        }

        return result.createResponseEntity();
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "List session information for all sessions", notes = "Admin only")
    @RequestMapping(method = RequestMethod.GET,  value="/http-sessions")
    public ResponseEntity<List<SessionInformation>> listSessions(
            @AuthenticationPrincipal User user,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<SessionInformation> sessions = new ArrayList<SessionInformation>();
        final List<Object> allPrincipals = sessionRegistry.getAllPrincipals();

        for (final Object principal : allPrincipals) {
            List<SessionInformation> sessionInfo = sessionRegistry.getAllSessions(principal, true);
            //Expire sessions, the user was deleted
            for(SessionInformation info : sessionInfo){
                sessions.add(info);
            }
        }
        return new ResponseEntity<>(sessions, HttpStatus.OK);
    }


}
