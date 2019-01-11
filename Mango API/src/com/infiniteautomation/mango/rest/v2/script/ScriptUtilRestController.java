package com.infiniteautomation.mango.rest.v2.script;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.v2.model.javascript.MangoJavaScriptModel;
import com.infiniteautomation.mango.rest.v2.model.javascript.MangoJavaScriptResultModel;
import com.infiniteautomation.mango.spring.service.MangoJavaScriptService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.IDataPointValueSource;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataSource.DataSourceRT;
import com.serotonin.m2m2.rt.script.OneTimePointAnnotation;
import com.serotonin.m2m2.rt.script.ResultTypeException;
import com.serotonin.m2m2.rt.script.ScriptPermissions;
import com.serotonin.m2m2.rt.script.ScriptPointValueSetter;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="Script Utils to run or test a Mango JavaScript script")
@RestController
@RequestMapping("/script")
public class ScriptUtilRestController {

    private static final Log LOG = LogFactory.getLog(ScriptUtilRestController.class);

    private final MangoJavaScriptService service;
    
    @Autowired
    ScriptUtilRestController(MangoJavaScriptService service) {
        this.service = service;
    }
    
    @ApiOperation(value = "Validate a script")
    @RequestMapping(method = RequestMethod.POST, value = {"/validate"})
    public MangoJavaScriptResultModel validate(
            @AuthenticationPrincipal User user, 
            @RequestBody MangoJavaScriptModel model) {
        if(LOG.isDebugEnabled()) LOG.debug("Testing script for: " + user.getName());
        return new MangoJavaScriptResultModel(service.testScript(model.toVO(), user));
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Run a script, always runs with permissions of submitting user. Admin only")
    @RequestMapping(method = RequestMethod.POST, value = {"/run"})
    public MangoJavaScriptResultModel runScript(
            @AuthenticationPrincipal User user, 
            @RequestBody MangoJavaScriptModel scriptModel) {
        if(LOG.isDebugEnabled()) LOG.debug("Running script for: " + user.getName());
        scriptModel.setPermissions(user.getPermissionsSet());
        return new MangoJavaScriptResultModel(service.executeScript(scriptModel.toVO(), new SetCallback(new ScriptPermissions(user.getPermissionsSet()), user), user));
    }

    class SetCallback extends ScriptPointValueSetter {

        private final User user;

        public SetCallback(ScriptPermissions permissions, User user) {
            super(permissions);
            this.user = user;
        }
        @Override
        public void setImpl(IDataPointValueSource point, Object value, long timestamp, String annotation) {
            DataPointRT dprt = (DataPointRT) point;

            try {
                DataValue mangoValue = service.coerce(value, dprt.getDataTypeId());
                SetPointSource source;
                PointValueTime newValue = new PointValueTime(mangoValue, timestamp);
                if(StringUtils.isBlank(annotation))
                    source = user;
                else
                    source = new OneTimePointAnnotation(user, annotation);

                DataSourceRT<? extends DataSourceVO<?>> dsrt = Common.runtimeManager.getRunningDataSource(dprt.getDataSourceId());
                dsrt.setPointValue(dprt, newValue, source);
            }
            catch (ResultTypeException e) {
                throw new ServerErrorException(e);
            }
        }
    }
}
