/**
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.regex.MatchCallback;
import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.rest.latest.model.SerialTestResultModel;
import com.infiniteautomation.mango.rest.latest.model.SerialValidationModel;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.infiniteautomation.serial.rt.SerialDataSourceRT;
import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value = "Serial data sources utilities")
@RestController()
@RequestMapping("/serial-data-source")
public class SerialDataSourceRestController {

    private final Log LOG = LogFactory.getLog(SerialDataSourceRestController.class);
    private final DataSourceService service;

    @Autowired
    public SerialDataSourceRestController(DataSourceService service) {
        this.service = service;
    }

    @ApiOperation(
            value = "Get logfile name",
            notes = "Must have permission to edit the data source"
            )
    @RequestMapping(method = RequestMethod.GET, value="/log-file-path/{xid}")
    public String getLogFilePath(
            @ApiParam(value = "XID of Data Source", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {

        DataSourceVO vo = service.get(xid);
        return SerialDataSourceRT.getIOLogFileName(vo.getId());
    }

    @ApiOperation(value = "Validate Serial Data", notes = "")
    @RequestMapping(method = RequestMethod.POST, value = "/validate-ascii/{xid}")
    public List<SerialTestResultModel> validateSerialData(
            @RequestBody SerialValidationModel model,
            @ApiParam(value = "Valid Serial data source XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {

        List<SerialTestResultModel> results = new ArrayList<>();

        DataSourceVO ds = service.get(xid);
        if(!(ds instanceof SerialDataSourceVO))
            throw new BadRequestException(new TranslatableMessage("validate.incompatibleDataSourceType"));

        //Are we a hex string
        if(model.isHex()){
            if(!model.getMessage().matches("[0-9A-Fa-f]+")){
                throw new BadRequestException(new TranslatableMessage("serial.validate.notHex"));
            }
        }
        List<DataPointVO> points = DataPointDao.getInstance().getDataPoints(ds.getId());

        if(model.isUseTerminator()) {
            //Convert the message
            String[] messages = SerialDataSourceRT.splitMessages(model.getMessage(), model.getMessageTerminator());

            for(String message : messages) {
                if(SerialDataSourceRT.canProcessTerminatedMessage(message, model.getMessageTerminator())){
                    //Check all the points
                    for(final DataPointVO vo : points){
                        MatchCallback callback = new MatchCallback(){

                            @Override
                            public void onMatch(String pointIdentifier, PointValueTime value) {
                                SerialTestResultModel result = new SerialTestResultModel();
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
                            public void pointPatternMismatch(String message, String messageRegex) {
                                SerialTestResultModel result = new SerialTestResultModel();
                                results.add(result);
                                result.setSuccess(false);
                                result.setPointName(vo.getName());
                                result.setPointXid(vo.getXid());
                                result.setError(new TranslatableMessage("serial.test.noPointRegexMatch"));
                            }

                            @Override
                            public void messagePatternMismatch(String message, String messageRegex) {
                                SerialTestResultModel result = new SerialTestResultModel();
                                results.add(result);
                                result.setSuccess(false);
                                result.setPointName(vo.getName());
                                result.setPointXid(vo.getXid());
                                result.setError(new TranslatableMessage("serial.test.noMessageMatch"));
                            }

                            @Override
                            public void pointNotIdentified(String message, String messageRegex, int pointIdentifierIndex) {
                                SerialTestResultModel result = new SerialTestResultModel();
                                results.add(result);
                                result.setSuccess(false);
                                result.setPointName(vo.getName());
                                result.setPointXid(vo.getXid());
                                result.setError( new TranslatableMessage("serial.test.noIdentifierFound"));
                            }

                            @Override
                            public void matchGeneralFailure(Exception e) {
                                SerialTestResultModel result = new SerialTestResultModel();
                                results.add(result);
                                result.setSuccess(false);
                                result.setPointName(vo.getName());
                                result.setPointXid(vo.getXid());
                                result.setError(new TranslatableMessage("common.default", e.getMessage()));                            }
                        };

                        try{
                            SerialDataSourceRT.matchPointValue(message,
                                    model.getMessageRegex(),
                                    model.getPointIdentifierIndex(),
                                    (SerialPointLocatorVO)vo.getPointLocator(),
                                    model.isHex(), LOG, callback);
                        }catch(Exception e){
                            callback.matchGeneralFailure(e);
                        }
                    }
                }else{
                    SerialTestResultModel result = new SerialTestResultModel();
                    results.add(result);
                    result.setSuccess(false);
                    result.setError( new TranslatableMessage("serial.test.noTerminator"));
                }
            }
        }
        else {
            //Check all the points
            for(final DataPointVO vo : points){
                MatchCallback callback = new MatchCallback(){

                    @Override
                    public void onMatch(String pointIdentifier, PointValueTime value) {
                        SerialTestResultModel result = new SerialTestResultModel();
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
                    public void pointPatternMismatch(String message, String messageRegex) {
                        SerialTestResultModel result = new SerialTestResultModel();
                        results.add(result);
                        result.setSuccess(false);
                        result.setPointName(vo.getName());
                        result.setPointXid(vo.getXid());
                        result.setError(new TranslatableMessage("serial.test.noPointRegexMatch"));
                    }

                    @Override
                    public void messagePatternMismatch(String message, String messageRegex) {
                        SerialTestResultModel result = new SerialTestResultModel();
                        results.add(result);
                        result.setSuccess(false);
                        result.setPointName(vo.getName());
                        result.setPointXid(vo.getXid());
                        result.setError(new TranslatableMessage("serial.test.noMessageMatch"));
                    }

                    @Override
                    public void pointNotIdentified(String message, String messageRegex, int pointIdentifierIndex) {
                        SerialTestResultModel result = new SerialTestResultModel();
                        results.add(result);
                        result.setSuccess(false);
                        result.setPointName(vo.getName());
                        result.setPointXid(vo.getXid());
                        result.setError( new TranslatableMessage("serial.test.noIdentifierFound"));
                    }

                    @Override
                    public void matchGeneralFailure(Exception e) {
                        SerialTestResultModel result = new SerialTestResultModel();
                        results.add(result);
                        result.setSuccess(false);
                        result.setPointName(vo.getName());
                        result.setPointXid(vo.getXid());
                        result.setError(new TranslatableMessage("common.default", e.getMessage()));                            }
                };

                try{
                    SerialDataSourceRT.matchPointValue(model.getMessage(),
                            model.getMessageRegex(),
                            model.getPointIdentifierIndex(),
                            (SerialPointLocatorVO)vo.getPointLocator(),
                            model.isHex(), LOG, callback);
                }catch(Exception e){
                    callback.matchGeneralFailure(e);
                }
            }
        }


        return results;
    }


}
