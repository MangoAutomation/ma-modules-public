/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.db.query.pojo.RQLToObjectListQuery;
import com.infiniteautomation.mango.io.serial.SerialPortIdentifier;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.ICoreLicense;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.email.MangoEmailContent;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.SystemInfoDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.bean.PointHistoryCount;
import com.serotonin.m2m2.web.dwr.ModulesDwr;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryResultModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.system.TimezoneModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.system.TimezoneUtility;
import com.serotonin.m2m2.web.mvc.spring.security.MangoSessionRegistry;
import com.serotonin.provider.Providers;
import com.serotonin.web.mail.EmailSender;

import freemarker.template.TemplateException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.jazdw.rql.parser.ASTNode;

/**
 * Class to provide server information
 *
 * @author Terry Packer
 */
@Api(value = "Server Information v2", description = "Server Information")
@RestController
@RequestMapping("/server")
public class ServerRestV2Controller extends AbstractMangoRestV2Controller {

    private final Log log = LogFactory.getLog(ServerRestV2Controller.class);

    @Autowired
    MangoSessionRegistry sessionRegistry;

    private List<TimezoneModel> allTimezones;
    private TimezoneModel defaultServerTimezone;

    public ServerRestV2Controller() {
        this.allTimezones = TimezoneUtility.getTimeZoneIdsWithOffset();
        this.defaultServerTimezone = new TimezoneModel("",
                new TranslatableMessage("users.timezone.def").translate(Common.getTranslations()),
                0);
        // Always add the default to the start of the list
        this.allTimezones.add(0, this.defaultServerTimezone);
    }

    @ApiOperation(value = "Query Timezones", notes = "", response = TimezoneModel.class,
            responseContainer = "Array")
    @RequestMapping(method = RequestMethod.GET, value = "/timezones")
    public ResponseEntity<PageQueryResultModel<TimezoneModel>> queryTimezone(
            HttpServletRequest request) {
        ASTNode root = RQLUtils.parseRQLtoAST(request.getQueryString());
        List<TimezoneModel> list =
                root.accept(new RQLToObjectListQuery<TimezoneModel>(), allTimezones);
        PageQueryResultModel<TimezoneModel> model =
                new PageQueryResultModel<TimezoneModel>(list, allTimezones.size() + 1);
        return new ResponseEntity<PageQueryResultModel<TimezoneModel>>(model, HttpStatus.OK);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Send a test email", notes = "Sends email to supplied address")
    @RequestMapping(method = RequestMethod.PUT, value = "/email/test")
    public ResponseEntity<String> sendTestEmail(
            @RequestParam(value = "email", required = true, defaultValue = "") String email,
            @RequestParam(value = "username", required = true, defaultValue = "") String username,
            HttpServletRequest request) throws TemplateException, IOException {

            Translations translations = Common.getTranslations();
            Map<String, Object> model = new HashMap<>();
            model.put("message", new TranslatableMessage("ftl.userTestEmail", username));
            MangoEmailContent content = new MangoEmailContent("testEmail", model, translations,
                    translations.translate("ftl.testEmail"), Common.UTF8);
            EmailSender emailSender = new EmailSender(
                    SystemSettingsDao.instance.getValue(SystemSettingsDao.EMAIL_SMTP_HOST),
                    SystemSettingsDao.instance.getIntValue(SystemSettingsDao.EMAIL_SMTP_PORT),
                    SystemSettingsDao.instance.getBooleanValue(SystemSettingsDao.EMAIL_AUTHORIZATION),
                    SystemSettingsDao.instance.getValue(SystemSettingsDao.EMAIL_SMTP_USERNAME),
                    SystemSettingsDao.instance.getValue(SystemSettingsDao.EMAIL_SMTP_PASSWORD),
                    SystemSettingsDao.instance.getBooleanValue(SystemSettingsDao.EMAIL_TLS),
                    SystemSettingsDao.instance.getIntValue(SystemSettingsDao.EMAIL_SEND_TIMEOUT));

            String addr = SystemSettingsDao.instance.getValue(SystemSettingsDao.EMAIL_FROM_ADDRESS);
            String pretty = SystemSettingsDao.instance.getValue(SystemSettingsDao.EMAIL_FROM_NAME);
            InternetAddress fromAddress = new InternetAddress(addr, pretty);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PrintStream ps = new PrintStream(baos, true, "UTF-8")) {
                emailSender.setDebug(ps);
                try{
                    emailSender.send(fromAddress, email, content.getSubject(), content);
                }catch(Exception e) {
                    String debug = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                    log.warn("Email Session DEBUG Output\n" + debug);
                    throw e;
                }
            }
            
            return new ResponseEntity<String>(new TranslatableMessage("common.testEmailSent", email)
                    .translate(Common.getTranslations()), HttpStatus.OK);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Restart Mango",
    notes = "Returns location url in header for status updates while web interface is still active")
    @RequestMapping(method = RequestMethod.PUT, value = "/restart")
    public ResponseEntity<Void> restart(UriComponentsBuilder builder, HttpServletRequest request) {
        ProcessResult r = ModulesDwr.scheduleRestart();
        if (r.getData().get("shutdownUri") != null) {
            URI location = builder.path("/status/mango").buildAndExpand().toUri();
            return getResourceCreated(null, location);
        } else
            throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR,
                    new TranslatableMessage("modules.restartAlreadyScheduled"));

    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "List session information for all sessions", notes = "Admin only")
    @RequestMapping(method = RequestMethod.GET, value = "/http-sessions")
    public ResponseEntity<List<SessionInformation>> listSessions(@AuthenticationPrincipal User user,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<SessionInformation> sessions = new ArrayList<SessionInformation>();
        final List<Object> allPrincipals =
                sessionRegistry.getAllPrincipals();

        for (final Object principal : allPrincipals) {
            List<SessionInformation> sessionInfo =
                    sessionRegistry.getAllSessions(principal, true);
            // Expire sessions, the user was deleted
            for (SessionInformation info : sessionInfo) {
                sessions.add(info);
            }
        }
        return new ResponseEntity<>(sessions, HttpStatus.OK);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Get all available system information", notes = "")
    @ApiResponses({
        @ApiResponse(code = 500, message = "Internal error", response = ResponseEntity.class),})
    @RequestMapping(method = {RequestMethod.GET}, value = "system-info")
    public ResponseEntity<Map<String, Object>> getSystemInfo(@AuthenticationPrincipal User user) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (SystemInfoDefinition<?> def : ModuleRegistry.getSystemInfoDefinitions().values())
            map.put(def.getKey(), def.getValue());
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Get one piece of system info by key", notes = "")
    @ApiResponses({
        @ApiResponse(code = 500, message = "Internal error", response = ResponseEntity.class),
        @ApiResponse(code = 404, message = "Not Found", response = ResponseEntity.class),})
    @RequestMapping(method = {RequestMethod.GET}, value = "/system-info/{key}")
    public ResponseEntity<Object> getOne(@AuthenticationPrincipal User user,
            @ApiParam(value = "Valid System Info Key", required = true,
            allowMultiple = false) @PathVariable String key) {

        SystemInfoDefinition<?> setting = ModuleRegistry.getSystemInfoDefinition(key);
        if (setting != null)
            return new ResponseEntity<Object>(setting.getValue(), HttpStatus.OK);
        throw new NotFoundRestException();
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Get the count of values for all data points in a point by point list",
    notes = "This endpoint can be very cpu intensive if you have a lot of point data.")
    @ApiResponses({
        @ApiResponse(code = 500, message = "Internal error", response = ResponseEntity.class)})
    @RequestMapping(method = {RequestMethod.GET}, value = "/point-history-counts")
    public ResponseEntity<List<PointHistoryCount>> getPointHistoryCounts() {
        return ResponseEntity.ok(DataPointDao.getInstance().getTopPointHistoryCounts());
    }

    @ApiOperation(value = "Get general Mango installation info",
            notes = "Instance description, GUID, Core version, Normalized Core Version, Server timezone, Server locale")
    @ApiResponses({
        @ApiResponse(code = 500, message = "Internal error", response = ResponseEntity.class)})
    @RequestMapping(method = {RequestMethod.GET}, value = "/mango-info")
    public ResponseEntity<Map<String, String>> getMangoInfo(@AuthenticationPrincipal User user){
        Map<String, String> mangoInfo = new HashMap<>();

        mangoInfo.put(SystemSettingsDao.INSTANCE_DESCRIPTION, SystemSettingsDao.instance.getValue(SystemSettingsDao.INSTANCE_DESCRIPTION));
        mangoInfo.put("guid", Providers.get(ICoreLicense.class).getGuid());
        mangoInfo.put("coreVersion", Common.getVersion().toString());
        mangoInfo.put("coreVersionNormalized", Common.getVersion().getNormalVersion());
        mangoInfo.put("locale", Common.getLocale().toLanguageTag());
        mangoInfo.put("timezone", TimeZone.getDefault().toZoneId().getId());

        return ResponseEntity.ok(mangoInfo);
    }

    @ApiOperation(value = "Send a client error / stack trace to the backend for logging")
    @RequestMapping(method = {RequestMethod.POST}, value = "/client-error")
    public void postClientError(@AuthenticationPrincipal User user, @RequestBody ClientError body) {
        log.warn("Client error\n" + body.formatString(user));
    }
    
    @ApiOperation(value = "Get available serial ports, optionally refresh cached list.")
    @RequestMapping(method = {RequestMethod.GET}, value = "/serial-ports")
    @PreAuthorize("hasDataSourcePermission()")
    public Set<String> refreshFreeSerialPorts(
            @RequestParam(value = "refresh", required = false, defaultValue = "false") boolean refresh
            ) throws Exception {
        Set<String> portNames = new HashSet<String>();

        if(refresh)
            Common.serialPortManager.refreshFreeCommPorts();
        List<SerialPortIdentifier> ports = Common.serialPortManager.getAllCommPorts();
        for (SerialPortIdentifier proxy : ports)
            portNames.add(proxy.getName());

        return portNames;
    }

    @ApiOperation(value = "Get the CORS headers as set in env.properties")
    @RequestMapping(method = {RequestMethod.GET}, value = "/cors-settings")
    @PreAuthorize("isAdmin()")
    public CorsSettings getCorsHeaders() {
        CorsSettings corsSettings = new CorsSettings();
        Map<String,String> headers = new HashMap<>();
        
        String header = Common.envProps.getString("rest.cors.allowedOrigins", "");
        if(!StringUtils.isEmpty(header))
            headers.put("Access-Control-Allow-Origin", header);
        
        header = Common.envProps.getString("rest.cors.allowedMethods", "");
        if(!StringUtils.isEmpty(header))
            headers.put("Access-Control-Allow-Methods", header);
        
        header = Common.envProps.getString("rest.cors.allowedHeaders", "");
        if(!StringUtils.isEmpty(header))
            headers.put("Access-Control-Allow-Headers", header);

        header = Common.envProps.getString("rest.cors.exposedHeaders", "");
        if(!StringUtils.isEmpty(header))
            headers.put("Access-Control-Expose-Headers", header);
        
        headers.put("Access-Control-Allow-Credentials", Boolean.toString(Common.envProps.getBoolean("rest.cors.allowCredentials", false)));
        
        header = Common.envProps.getString("rest.cors.maxAge", "");
        if(!StringUtils.isEmpty(header))
            headers.put("Access-Control-Max-Age", header);

        corsSettings.setEnabled(Common.envProps.getBoolean("rest.cors.enabled", false));
        corsSettings.setHeaders(headers);
        
        return corsSettings;
    }
    
    public static class CorsSettings {
        private Map<String, String> headers;
        private boolean enabled;
        /**
         * @return the headers
         */
        public Map<String, String> getHeaders() {
            return headers;
        }
        /**
         * @param headers the headers to set
         */
        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }
        /**
         * @return the enabled
         */
        public boolean isEnabled() {
            return enabled;
        }
        /**
         * @param enabled the enabled to set
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    public static class ClientError {
        String message;
        String cause;
        List<StackFrame> stackTrace;
        String location;
        String userAgent;
        String language;
        String timezone;
        String date;

        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        public String getCause() {
            return cause;
        }
        public void setCause(String cause) {
            this.cause = cause;
        }
        public List<StackFrame> getStackTrace() {
            return stackTrace;
        }
        public void setStackTrace(List<StackFrame> stackTrace) {
            this.stackTrace = stackTrace;
        }
        public String getUserAgent() {
            return userAgent;
        }
        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }
        public String getLanguage() {
            return language;
        }
        public void setLanguage(String language) {
            this.language = language;
        }
        public String getLocation() {
            return location;
        }
        public void setLocation(String location) {
            this.location = location;
        }
        public String getTimezone() {
            return timezone;
        }
        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }
        public String getDate() {
            return date;
        }
        public void setDate(String date) {
            this.date = date;
        }

        public String formatString(User user) {
            String stackTrace = this.stackTrace.stream()
                    .map(sf -> sf.toString())
                    .collect(Collectors.joining("\n"));

            return "[user=" + user.getUsername() + ", cause=" + cause + ", location=" + location + ", userAgent=" + userAgent
                    + ", language=" + language + ", date=" + date + ", timezone=" + timezone + "]" + "\n" +
                    message + "\n" + stackTrace;
        }
    }

    public static class StackFrame {
        String functionName;
        String fileName;
        int lineNumber;
        int columnNumber;
        String source;

        public String getFunctionName() {
            return functionName;
        }
        public void setFunctionName(String functionName) {
            this.functionName = functionName;
        }
        public String getFileName() {
            return fileName;
        }
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        public int getLineNumber() {
            return lineNumber;
        }
        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }
        public int getColumnNumber() {
            return columnNumber;
        }
        public void setColumnNumber(int columnNumber) {
            this.columnNumber = columnNumber;
        }
        public String getSource() {
            return source;
        }
        public void setSource(String source) {
            this.source = source;
        }

        @Override
        public String toString() {
            return "\tat " + functionName + " (" + fileName + ":" + lineNumber + ":" + columnNumber + ")";
        }
    }
}
