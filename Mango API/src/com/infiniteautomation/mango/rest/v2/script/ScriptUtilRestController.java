package com.infiniteautomation.mango.rest.v2.script;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.CompiledScript;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.IDataPointValueSource;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataSource.DataSourceRT;
import com.serotonin.m2m2.rt.script.CompiledScriptExecutor;
import com.serotonin.m2m2.rt.script.OneTimePointAnnotation;
import com.serotonin.m2m2.rt.script.ResultTypeException;
import com.serotonin.m2m2.rt.script.ScriptLog;
import com.serotonin.m2m2.rt.script.ScriptPermissions;
import com.serotonin.m2m2.rt.script.ScriptPermissionsException;
import com.serotonin.m2m2.rt.script.ScriptPointValueSetter;
import com.serotonin.m2m2.rt.script.ScriptUtils;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="Script Utils", description="Run or test a Mango JavaScript script")
@RestController
@RequestMapping("/script")
public class ScriptUtilRestController {
    private static final Log LOG = LogFactory.getLog(ScriptUtilRestController.class);

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Test a script")
    @ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
        @ApiResponse(code = 500, message = "Error processing request", response=ResponseEntity.class)
    })
    @RequestMapping(method = RequestMethod.POST, value = {"/test"})
    public ResponseEntity<ScriptRestResult> testScript(@AuthenticationPrincipal User user, @RequestBody ScriptRestModel scriptModel) {
        if(LOG.isDebugEnabled()) LOG.debug("Testing script for: " + user.getName());
        Map<String, IDataPointValueSource> context = convertContextModel(scriptModel.getContext(), true);
        try {
            CompiledScript script = CompiledScriptExecutor.compile(scriptModel.getScript());

            final StringWriter scriptOut = new StringWriter();
            final PrintWriter scriptWriter = new PrintWriter(scriptOut);
            int logLevel = ScriptLog.LogLevel.FATAL;
            if(!StringUtils.isEmpty(scriptModel.getLogLevel())) {
                int levelId = ScriptLog.LOG_LEVEL_CODES.getId(scriptModel.getLogLevel());
                if(levelId == -1)
                    throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("rest.script.error.unknownLogLevel", scriptModel.getLogLevel()));
                else
                    logLevel = levelId;
            }
            if(logLevel == ScriptLog.LogLevel.NONE)
                logLevel = ScriptLog.LogLevel.FATAL;
            try(ScriptLog scriptLog = new ScriptLog("scriptTest-" + user.getUsername(), logLevel, scriptWriter);){
                final ScriptPermissions permissions = scriptModel.getPermissions().toPermissions();
                final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYY HH:mm:ss");
    
                ScriptPointValueSetter loggingSetter = new ScriptPointValueSetter(permissions) {
                    @Override
                    public void set(IDataPointValueSource point, Object value, long timestamp, String annotation) {
                        DataPointRT dprt = (DataPointRT) point;
                        if(!dprt.getVO().getPointLocator().isSettable()) {
                            scriptOut.append("Point " + dprt.getVO().getExtendedName() + " not settable.");
                            return;
                        }
    
                        if (!Permissions.hasDataPointSetPermission(permissions, dprt.getVO())) {
                            scriptOut.write(new TranslatableMessage("pointLinks.setTest.permissionDenied", dprt.getVO().getXid()).translate(Common.getTranslations()));
                            return;
                        }
    
                        scriptOut.append("Setting point " + dprt.getVO().getName() + " to " + value + " @" + sdf.format(new Date(timestamp)) + "\r\n");
                    }
    
                    @Override
                    protected void setImpl(IDataPointValueSource point, Object value, long timestamp, String annotation) {
                        // not really setting
                    }
                };
    
                try {
                    PointValueTime pvt = CompiledScriptExecutor.execute(script, context, new HashMap<String, Object>(), Common.timer.currentTimeMillis(),
                            DataTypes.ALPHANUMERIC, Common.timer.currentTimeMillis(), permissions, scriptLog, loggingSetter, null, true);
                    if(LOG.isDebugEnabled()) LOG.debug("Script output: " + scriptOut.toString());
                    return new ResponseEntity<>(new ScriptRestResult(scriptOut.toString(), new PointValueTimeModel(pvt)), HttpStatus.OK);
                } catch(ResultTypeException e) {
                    throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, e);
                }
            }

        } catch(ScriptException e) {
            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Run a script")
    @ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
        @ApiResponse(code = 500, message = "Error processing request", response=ResponseEntity.class)
    })
    @RequestMapping(method = RequestMethod.POST, value = {"/run"})
    public ResponseEntity<ScriptRestResult> runScript(@AuthenticationPrincipal User user, @RequestBody ScriptRestModel scriptModel) {
        if(LOG.isDebugEnabled()) LOG.debug("Running script for: " + user.getName());
        Map<String, IDataPointValueSource> context = convertContextModel(scriptModel.getContext(), false);
        try {
            CompiledScript script = CompiledScriptExecutor.compile(scriptModel.getScript());

            final StringWriter scriptOut = new StringWriter();
            final PrintWriter scriptWriter = new PrintWriter(scriptOut);
            int logLevel = ScriptLog.LogLevel.FATAL;
            if(!StringUtils.isEmpty(scriptModel.getLogLevel())) {
                int levelId = ScriptLog.LOG_LEVEL_CODES.getId(scriptModel.getLogLevel());
                if(levelId == -1)
                    throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("rest.script.error.unknownLogLevel", scriptModel.getLogLevel()));
                else
                    logLevel = levelId;
            }
            if(logLevel == ScriptLog.LogLevel.NONE)
                logLevel = ScriptLog.LogLevel.FATAL;
            try(ScriptLog scriptLog = new ScriptLog("runScript-" + user.getUsername(), logLevel, scriptWriter);){
                ScriptPermissions permissions = scriptModel.getPermissions().toPermissions();
                try {
                    PointValueTime pvt = CompiledScriptExecutor.execute(script, context, new HashMap<String, Object>(), Common.timer.currentTimeMillis(),
                            DataTypes.ALPHANUMERIC, Common.timer.currentTimeMillis(), permissions, scriptLog, new SetCallback(permissions, user), null, false);
                    if(LOG.isDebugEnabled()) LOG.debug("Script output: " + scriptOut.toString());
                    return new ResponseEntity<>(new ScriptRestResult(scriptOut.toString(), new PointValueTimeModel(pvt)), HttpStatus.OK);
                } catch(ResultTypeException|ScriptPermissionsException e) {
                    throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, e);
                }
            }

        } catch(ScriptException e) {
            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private Map<String, IDataPointValueSource> convertContextModel(List<ScriptContextVariableModel> contextModel, boolean testRun) {
        Map<String, IDataPointValueSource> context = new HashMap<>();
        if(contextModel != null)
            for(ScriptContextVariableModel variable : contextModel) {
                DataPointVO dpvo = DataPointDao.getInstance().getByXid(variable.getXid());
                if(dpvo == null)
                    throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("rest.error.pointNotFound", variable.getXid()));

                DataPointRT dprt = Common.runtimeManager.getDataPoint(dpvo.getId());
                if(dprt == null) {
                    if(!testRun)
                        throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("rest.error.pointNotEnabled", variable.getXid()));
                    if(dpvo.getDefaultCacheSize() == 0)
                        dpvo.setDefaultCacheSize(1);
                    dprt = new DataPointRT(dpvo, dpvo.getPointLocator().createRuntime(), DataSourceDao.getInstance().getDataSource(dpvo.getDataSourceId()), null);
                    dprt.resetValues();
                }

                context.put(variable.getVariableName(), dprt);
            }
        return context;
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
                DataValue mangoValue = ScriptUtils.coerce(value, dprt.getDataTypeId());
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
                throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            }
        }
    }
}
