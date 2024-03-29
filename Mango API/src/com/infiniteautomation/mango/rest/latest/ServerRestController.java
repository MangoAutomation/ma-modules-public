/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
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

import com.infiniteautomation.mango.io.messaging.SentMessage;
import com.infiniteautomation.mango.io.messaging.email.EmailMessage;
import com.infiniteautomation.mango.io.serial.SerialPortIdentifier;
import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.rest.latest.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.latest.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.email.EmailContentModel;
import com.infiniteautomation.mango.rest.latest.model.server.NetworkInterfaceModel;
import com.infiniteautomation.mango.rest.latest.model.server.ServerCommandModel;
import com.infiniteautomation.mango.rest.latest.model.system.TimezoneModel;
import com.infiniteautomation.mango.rest.latest.model.system.TimezoneUtility;
import com.infiniteautomation.mango.spring.service.MailingListService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.spring.service.UsersService;
import com.serotonin.db.pair.StringStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.ICoreLicense;
import com.serotonin.m2m2.IMangoLifecycle;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.email.MangoEmailContent;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.SystemInfoDefinition;
import com.serotonin.m2m2.module.definitions.permissions.SendToMailingListPermission;
import com.serotonin.m2m2.rt.maint.work.ProcessWorkItem;
import com.serotonin.m2m2.util.HostUtils;
import com.serotonin.m2m2.util.HostUtils.NICInfo;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.bean.PointHistoryCount;
import com.serotonin.m2m2.vo.mailingList.MailingList;
import com.serotonin.m2m2.vo.mailingList.RecipientListEntryType;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.web.mvc.spring.security.MangoSessionRegistry;
import com.serotonin.provider.Providers;
import com.serotonin.web.mail.EmailContent;

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
public class ServerRestController extends AbstractMangoRestController {

    private final Logger log = LoggerFactory.getLogger(ServerRestController.class);

    private final MangoSessionRegistry sessionRegistry;
    private final MailingListService mailingListService;

    private final UsersService userService;
    private final PermissionService permissionService;

    private List<TimezoneModel> allTimezones;
    private TimezoneModel defaultServerTimezone;
    private final PointValueDao pointValueDao;
    private final SystemSettingsDao systemSettingsDao;

    @Autowired
    public ServerRestController(UsersService userService, MailingListService mailingListService,
                                PermissionService permissionService, MangoSessionRegistry sessionRegistry,
                                PointValueDao pointValueDao, SystemSettingsDao systemSettingsDao) {
        this.userService = userService;
        this.mailingListService = mailingListService;
        this.sessionRegistry = sessionRegistry;
        this.permissionService = permissionService;
        this.pointValueDao = pointValueDao;
        this.systemSettingsDao = systemSettingsDao;

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
    public StreamWithTotal<TimezoneModel> queryTimezone(
            ASTNode query,
            Translations translations) {

        return new FilteredStreamWithTotal<>(allTimezones, query, translations);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Send a test email", notes = "Sends email to supplied address")
    @RequestMapping(method = RequestMethod.PUT, value = "/email/test")
    @Async
    public CompletableFuture<TranslatableMessage> sendTestEmail(
            @RequestParam(value = "email", required = true, defaultValue = "") String email,
            @RequestParam(value = "username", required = true, defaultValue = "") String username,
            HttpServletRequest request) throws TemplateException, IOException, AddressException {

        Translations translations = Common.getTranslations();
        Map<String, Object> model = new HashMap<>();
        model.put("message", new TranslatableMessage("ftl.userTestEmail", username));
        MangoEmailContent content = new MangoEmailContent("testEmail", model, translations,
                translations.translate("ftl.testEmail"), StandardCharsets.UTF_8);
        //Use null address to show that Mango sent it
        return sendTestEmail(Collections.singleton(email), null, content.getSubject(), content, (sent) -> {
            return new TranslatableMessage("common.testEmailSent", email);
        });
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Send an email", notes = "Sends email to supplied user")
    @RequestMapping(method = RequestMethod.POST, value = "/email")
    @Async
    public CompletableFuture<TranslatableMessage> sendEmail(
            @RequestBody EmailContentModel contentModel,
            @RequestParam(value = "username", required = true) String username,
            @AuthenticationPrincipal PermissionHolder user) throws TemplateException, IOException, AddressException {

        contentModel.ensureValid();

        //Ensure permissions and existence
        User sendTo = userService.get(username);

        //TODO confirm this user has a valid email address
        return sendTestEmail(Collections.singleton(sendTo.getEmail()), null, contentModel.getSubject(), contentModel.toEmailContent(), (sent) -> {
            return new TranslatableMessage("common.emailSent", sendTo.getEmail());
        });
    }

    @PreAuthorize("isGrantedPermission('" + SendToMailingListPermission.PERMISSION + "')")
    @ApiOperation(value = "Send an email to a mailing list", notes = "Requires mailing list send permission")
    @RequestMapping(method = RequestMethod.POST, value = "/email/mailing-list/{xid}")
    @Async
    public CompletableFuture<TranslatableMessage> sendEmailToMailingList(
            @PathVariable String xid,
            @RequestBody EmailContentModel contentModel,
            @AuthenticationPrincipal PermissionHolder user) throws TemplateException, IOException, AddressException {

        contentModel.ensureValid();

        //Ensure permissions and existence
        MailingList sendTo = mailingListService.get(xid);
        Set<String> emailUsers = mailingListService.getActiveRecipients(sendTo.getEntries(),
                Common.timer.currentTimeMillis(),
                RecipientListEntryType.MAILING_LIST,
                RecipientListEntryType.ADDRESS,
                RecipientListEntryType.USER);


        return sendTestEmail(emailUsers, null, contentModel.getSubject(), contentModel.toEmailContent(), (sent) -> {
            return new TranslatableMessage("common.emailSentToMailingList", xid);
        });
    }

    /**
     * Send email helper method
     */
    private CompletableFuture<TranslatableMessage> sendTestEmail(Set<String> to, String from, String subject, EmailContent content, Function<SentMessage, TranslatableMessage> done) throws AddressException{
        Set<InternetAddress> toAddresses = new HashSet<>();
        for(String toAddress : to) {
            toAddresses.add(new InternetAddress(toAddress));
        }

        EmailMessage message = new EmailMessage(null, toAddresses, subject, content);
        return Common.messageManager.sendMessageUsingFirstAvailableTransport(message)
                .thenApply(done).toCompletableFuture();
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Restart Mango",
    notes = "Returns location url in header for status updates while web interface is still active")
    @RequestMapping(method = RequestMethod.PUT, value = "/restart")
    public ResponseEntity<Void> restart(
            @RequestParam(value = "delay", required = false) Long delay,

            @AuthenticationPrincipal PermissionHolder user,

            UriComponentsBuilder builder,
            HttpServletRequest request) {

        IMangoLifecycle lifecycle = Providers.get(IMangoLifecycle.class);
        lifecycle.scheduleShutdown(delay, true, user);

        URI location = builder.path("/status/mango").buildAndExpand().toUri();
        return getResourceCreated(null, location);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Run OS command",
    notes = "Returns the output of the command, admin only")
    @RequestMapping(method = RequestMethod.POST, value = "/execute-command")
    public String executeCommand(
            @RequestBody
            ServerCommandModel command,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder,
            HttpServletRequest request) throws IOException {

        if (StringUtils.isBlank(command.getCommand()))
            return null;

        //Key -> Successful output
        //Value --> error output
        StringStringPair result = ProcessWorkItem.executeProcessCommand(command.getCommand(), command.getTimeout());
        if(result.getValue() != null)
            throw new ServerErrorException(new TranslatableMessage("common.default", result.getValue()));
        else
            return result.getKey();

    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "List session information for all sessions", notes = "Admin only")
    @RequestMapping(method = RequestMethod.GET, value = "/http-sessions")
    public ResponseEntity<List<SessionInformation>> listSessions(@AuthenticationPrincipal PermissionHolder user,
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
    public ResponseEntity<Map<String, Object>> getSystemInfo(@AuthenticationPrincipal PermissionHolder user) {
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
    public ResponseEntity<Object> getOne(@AuthenticationPrincipal PermissionHolder user,
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
    public ResponseEntity<List<PointHistoryCount>> getPointHistoryCounts(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(pointValueDao.topPointHistoryCounts(limit));
    }

    @ApiOperation(value = "Get general Mango installation info",
            notes = "Instance description, GUID, Core version, Normalized Core Version, Server timezone, Server locale")
    @ApiResponses({
        @ApiResponse(code = 500, message = "Internal error", response = ResponseEntity.class)})
    @RequestMapping(method = {RequestMethod.GET}, value = "/mango-info")
    public ResponseEntity<Map<String, String>> getMangoInfo(@AuthenticationPrincipal PermissionHolder user){
        Map<String, String> mangoInfo = new HashMap<>();

        mangoInfo.put(SystemSettingsDao.INSTANCE_DESCRIPTION, systemSettingsDao.getValue(SystemSettingsDao.INSTANCE_DESCRIPTION));
        mangoInfo.put("guid", Providers.get(ICoreLicense.class).getGuid());
        mangoInfo.put("coreVersion", Common.getVersion().toString());
        mangoInfo.put("coreVersionNormalized", Common.getVersion().getNormalVersion());
        mangoInfo.put("locale", Common.getLocale().toLanguageTag());
        mangoInfo.put("timezone", TimeZone.getDefault().toZoneId().getId());

        return ResponseEntity.ok(mangoInfo);
    }

    @ApiOperation(
            value = "Accept the current license agreement.",
            notes = "Only valid if the current license agreement has not been accepted.  If you do not accept, Mango will restart in 15 seconds, giving you a 2nd chance in case you change your mind.")
    @ApiResponses({
        @ApiResponse(code = 400, message = "License already accepted.")}
            )
    @RequestMapping(method = {RequestMethod.POST}, value = "/accept-license-agreement")
    public void acceptLicenseAgreement(
            @ApiParam(value = "Agree or not", required = true, allowMultiple = false)
            @RequestParam Boolean agree,
            @AuthenticationPrincipal PermissionHolder user) {

        //Check to see if the versions match, if so this request is invalid as it has already been confirmed

        if (agree) {
            systemSettingsDao.setIntValue(SystemSettingsDao.LICENSE_AGREEMENT_VERSION, Common.getLicenseAgreementVersion());
        } else {
            if (Common.getLicenseAgreementVersion() == systemSettingsDao.getIntValue(SystemSettingsDao.LICENSE_AGREEMENT_VERSION))
                throw new BadRequestException(new TranslatableMessage("systemSettings.licenseAlreadyAgreed"));

            //Start shutdown timer
            log.error("Mango will restart in 15 seconds.");
            Providers.get(IMangoLifecycle.class).scheduleShutdown(15000L, true, user);
        }
    }

    @ApiOperation(value = "Get the current license agreement version.")
    @RequestMapping(method = {RequestMethod.GET}, value = "/license-agreement-version")
    public Integer getLicenseAgreement(
            @AuthenticationPrincipal PermissionHolder user){
        return systemSettingsDao.getIntValue(SystemSettingsDao.LICENSE_AGREEMENT_VERSION);
    }

    @ApiOperation(value = "Send a client error / stack trace to the backend for logging")
    @RequestMapping(method = {RequestMethod.POST}, value = "/client-error")
    public void postClientError(@AuthenticationPrincipal PermissionHolder user, @RequestBody ClientError body) {
        log.warn("Client error\n" + body.formatString(user));
    }

    @ApiOperation(value = "Get available serial ports, optionally refresh cached list.")
    @RequestMapping(method = {RequestMethod.GET}, value = "/serial-ports")
    @PreAuthorize("isGrantedPermission('permissionDatasource')")
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

    @ApiOperation(value = "Get the CORS headers as set in configuration file")
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


    @ApiOperation(value = "List network interfaces", notes="Requires global data source permission")
    @RequestMapping(method = {RequestMethod.GET}, value = "/network-interfaces")
    @PreAuthorize("isGrantedPermission('permissionDatasource')")
    public List<NetworkInterfaceModel> getNetworkInterfaces(
            @RequestParam(value = "includeLoopback", required = false, defaultValue = "false") boolean includeLoopback,
            @RequestParam(value = "includeDefault", required = false, defaultValue = "false") boolean includeDefault,
            @AuthenticationPrincipal PermissionHolder user) {

        List<NetworkInterfaceModel> models = new ArrayList<>();
        if(includeDefault) {
            NetworkInterfaceModel model = new NetworkInterfaceModel();
            model.setHostAddress("0.0.0.0");
            model.setInterfaceName("");
            models.add(model);
        }

        try {
            for (NICInfo ni : HostUtils.getLocalInet4Addresses(includeLoopback)) {
                NetworkInterfaceModel model = new NetworkInterfaceModel();
                model.setHostAddress(ni.getInetAddress().getHostAddress());
                model.setInterfaceName(ni.getInterfaceName());
                models.add(model);
            }
        }
        catch (SocketException e) {
            throw new ServerErrorException(new TranslatableMessage("common.default", e.getMessage()), e);
        }

        return models;
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

        public String formatString(PermissionHolder user) {
            String stackTrace = this.stackTrace.stream()
                    .map(sf -> sf.toString())
                    .collect(Collectors.joining("\n"));

            return "[user=" + user.getPermissionHolderName() + ", cause=" + cause + ", location=" + location + ", userAgent=" + userAgent
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
