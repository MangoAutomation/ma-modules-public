/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.v2.model.JSONStreamedArray;
import com.infiniteautomation.mango.rest.v2.model.StreamWithTotal;
import com.infiniteautomation.mango.rest.v2.model.filestore.FileModel;
import com.infiniteautomation.mango.rest.v2.model.logging.LogMessageModel;
import com.infiniteautomation.mango.rest.v2.model.logging.LogQueryArrayStream;
import com.infiniteautomation.mango.spring.service.FileStoreService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Logging")
@RestController
@RequestMapping("/logging")
public class LoggingRestController {

    private final FileStoreService service;

    @Autowired
    public LoggingRestController(FileStoreService service) {
        this.service = service;
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "List Log Files", notes = "Returns a list of logfile metadata")
    @RequestMapping(method = RequestMethod.GET, value = "/files")
    public List<FileModel> list(
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpServletRequest request) throws IOException {

        File logsDir = Common.getLogsDir();
        Stream<FileModel> models = Files.list(logsDir.toPath()).filter(p -> {
            return Files.isRegularFile(p) && Files.isReadable(p) && !p.startsWith(".") &&
                    (p.endsWith(".log") || p.endsWith(".txt"));
        }).map(p -> {
            File file = p.toFile();
            return new FileModel(
                    "",
                    file.getName(),
                    "text/plain",
                    new Date(file.lastModified()),
                    file.length(),
                    false);
        });

        if (limit != null) {
            models = models.limit(limit);
        }

        return models.collect(Collectors.toList());
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Query log files", notes = "Returns a list of log files")
    @RequestMapping(method = RequestMethod.GET, value = "/log-files")
    public StreamWithTotal<FileModel> queryFiles(HttpServletRequest request) throws IOException {

        ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());

        File logsDir = Common.getLogsDir();
        List<FileModel> models = Files.list(logsDir.toPath()).filter(p -> {
            return Files.isRegularFile(p) && Files.isReadable(p) && !p.startsWith(".") &&
                    (p.endsWith(".log") || p.endsWith(".txt"));
        }).map(p -> {
            File file = p.toFile();
            return new FileModel(
                    "",
                    file.getName(),
                    "text/plain",
                    new Date(file.lastModified()),
                    file.length(),
                    false);
        }).collect(Collectors.toList());

        return new FilteredStreamWithTotal<>(models, query);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Query inside a ma.log logfile", response = LogMessageModel.class, responseContainer = "List")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "level", paramType="query", allowableValues = "ALL,TRACE,DEBUG,INFO,WARN,ERROR,FATAL,OFF"),
        @ApiImplicitParam(name = "classname", paramType="query"),
        @ApiImplicitParam(name = "method", paramType="query"),
        @ApiImplicitParam(name = "lineNumber", paramType="query", dataType = "int"),
        @ApiImplicitParam(name = "time", paramType="query", dataType = "date"),
        @ApiImplicitParam(name = "message", paramType="query")
    })
    @RequestMapping(method = RequestMethod.GET, value="/by-filename/{filename}")
    public JSONStreamedArray query(
            @PathVariable String filename,
            HttpServletRequest request) {

        ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());

        File file = new File(Common.getLogsDir(), filename);
        if(file.exists()){
            //Pattern pattern = new
            if(filename.matches(LogQueryArrayStream.LOGFILE_REGEX)){
                return new LogQueryArrayStream(filename, query);
            }else {
                throw new AccessDeniedException();
            }
        }else{
            throw new NotFoundRestException();
        }
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "View log", notes = "Optionally download file as attachment", response = String.class)
    @RequestMapping(method = RequestMethod.GET, produces={"text/plain"}, value = "/view/{filename}")
    public ResponseEntity<FileSystemResource> download(
            @ApiParam(value = "Set content disposition to attachment", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean download,
            @AuthenticationPrincipal User user,
            @PathVariable String filename, HttpServletRequest request) {
        File file = new File(Common.getLogsDir(), filename);
        if (file.exists()) {
            //TODO There is a known bug here where if the file rolls over during download/transmission the request will fail
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, download ? "attachment" : "inline");
            return new ResponseEntity<>(new FileSystemResource(file), responseHeaders, HttpStatus.OK);
        }else {
            throw new NotFoundRestException();
        }
    }

}
