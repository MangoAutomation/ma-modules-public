/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.asciifile.AsciiFileSystemSettingsDefinition;
import com.infiniteautomation.asciifile.rt.AsciiFileDataSourceRT;
import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceVO;
import com.infiniteautomation.asciifile.vo.AsciiFilePointLocatorVO;
import com.infiniteautomation.mango.regex.MatchCallback;
import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.rest.latest.model.AsciiFileTestResultModel;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value = "ASCII File data sources utilities")
@RestController()
@RequestMapping("/ascii-file-data-source")
public class AsciiFileDataSourceRestController {

    private final DataSourceService service;

    @Autowired
    public AsciiFileDataSourceRestController(DataSourceService service) {
        this.service = service;
    }

    @PreAuthorize("isGrantedPermission('permissionDatasource')")
    @ApiOperation(value = "Validate ASCII File is readable on Server", notes = "")
    @RequestMapping(method = RequestMethod.POST, value = "/validate-ascii-file-exists", consumes= {"text/plain;charset=UTF-8"})
    public void validateFileExists(
            @RequestBody
            String path,
            @AuthenticationPrincipal PermissionHolder user) {
        File verify = new File(path);
        try {
            verify.getCanonicalPath();
        }catch(Exception e) {
            throw new BadRequestException(new TranslatableMessage("dsEdit.file.ioexceptionCanonical", path));
        }

        String restrictedPaths = SystemSettingsDao.instance.getValue(AsciiFileSystemSettingsDefinition.RESTRICTED_PATH);
        if(!StringUtils.isEmpty(restrictedPaths))
            for(String p : restrictedPaths.split(";"))
                if(path.startsWith(p)) {
                    throw new PermissionException(new TranslatableMessage("dsEdit.file.pathRestrictedBy", path), user);
                }

        if (!verify.exists() || !verify.canRead())
            throw new PermissionException(new TranslatableMessage("dsEdit.file.cannotRead"), user);
    }

    @ApiOperation(value = "Validate ASCII", notes = "")
    @RequestMapping(method = RequestMethod.POST, value = "/validate-ascii/{xid}", consumes= {"text/plain;charset=UTF-8"})
    public List<AsciiFileTestResultModel> validateASCIIString(
            @RequestBody
            String ascii,
            @ApiParam(value = "Valid ASCII data source XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user) {

        DataSourceVO ds = service.get(xid);
        if(!(ds instanceof AsciiFileDataSourceVO))
            throw new BadRequestException(new TranslatableMessage("validate.incompatibleDataSourceType"));

        //Message we will work with
        String msg = StringEscapeUtils.unescapeJava(ascii);

        //Map to store the values vs the points they are for
        List<AsciiFileTestResultModel> results = new ArrayList<>();

        DataPointDao dpd = DataPointDao.getInstance();
        List<DataPointVO> points = dpd.getDataPoints(ds.getId());

        for(final DataPointVO vo : points){
            MatchCallback callback = new MatchCallback(){

                @Override
                public void onMatch(String pointIdentifier, PointValueTime value) {
                    AsciiFileTestResultModel result = new AsciiFileTestResultModel();
                    results.add(result);
                    result.setSuccess(true);
                    result.setPointName(vo.getName());
                    result.setPointXid(vo.getXid());
                    result.setIdentifier(pointIdentifier);
                    if(value != null) {
                        result.setValue(value.getValue().getObjectValue());
                        result.setTimestamp(new Date(value.getTime()));
                    }
                }

                @Override
                public void pointPatternMismatch(String message, String pointValueRegex) {
                    AsciiFileTestResultModel result = new AsciiFileTestResultModel();
                    results.add(result);
                    result.setSuccess(false);
                    result.setPointName(vo.getName());
                    result.setPointXid(vo.getXid());
                    result.setError(new TranslatableMessage("dsEdit.file.test.noPointRegexMatch"));
                }

                @Override
                public void messagePatternMismatch(String message, String messageRegex) { }

                @Override
                public void pointNotIdentified(String message, String messageRegex, int pointIdentifierIndex) {
                    AsciiFileTestResultModel result = new AsciiFileTestResultModel();
                    results.add(result);
                    result.setSuccess(false);
                    result.setPointName(vo.getName());
                    result.setPointXid(vo.getXid());
                    result.setError( new TranslatableMessage("dsEdit.file.test.noIdentifierFound"));
                }

                @Override
                public void matchGeneralFailure(Exception e) {

                    AsciiFileTestResultModel result = new AsciiFileTestResultModel();
                    results.add(result);
                    result.setSuccess(false);
                    result.setPointName(vo.getName());
                    result.setPointXid(vo.getXid());
                    result.setError(new TranslatableMessage("common.default", e.getMessage()));
                }
            };
            AsciiFilePointLocatorVO locator = vo.getPointLocator();
            AsciiFileDataSourceRT.matchPointValueTime(msg,
                    Pattern.compile(locator.getValueRegex()),
                    locator.getPointIdentifier(),
                    locator.getPointIdentifierIndex(),
                    locator.getDataTypeId(),
                    locator.getValueIndex(),
                    locator.getHasTimestamp(),
                    locator.getTimestampIndex(),
                    locator.getTimestampFormat(), callback);
        }
        return results;
    }

}
