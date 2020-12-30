/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 *
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.latest.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.filestore.FileModel;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.Translations;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value = "Logging")
@RestController
@RequestMapping("/logging")
public class LoggingRestController {

    private final Set<String> logFileExtensions = new HashSet<>(Arrays.asList(
            ".log",
            ".txt",
            ".txt.gz",
            ".log.gz"));

    private FileModel toModel(Path p) {
        File file = p.toFile();
        return new FileModel(
                "",
                file.getName(),
                "text/plain",
                new Date(file.lastModified()),
                file.length(),
                false);
    }

    private boolean filterFiles(Path p) {
        if (!Files.isRegularFile(p) || !Files.isReadable(p)) {
            return false;
        }
        String filename = p.getFileName().toString();
        return !filename.startsWith(".") && logFileExtensions.stream().anyMatch(filename::endsWith);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Query log files", notes = "Returns a list of log files")
    @RequestMapping(method = RequestMethod.GET, value = "/log-files")
    public StreamWithTotal<FileModel> queryFiles(ASTNode query,
                                                 Translations translations) throws IOException {

        List<FileModel> models = Files.list(Common.getLogsPath())
                .filter(this::filterFiles)
                .map(this::toModel)
                .collect(Collectors.toList());
        return new FilteredStreamWithTotal<>(models, query, translations);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "View log", notes = "Optionally download file as attachment", response = String.class)
    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE}, value = "/view/{filename}")
    public ResponseEntity<FileSystemResource> download(
            @ApiParam(value = "Set content disposition to attachment", defaultValue = "false")
            @RequestParam(required = false, defaultValue = "false") boolean download,
            @PathVariable String filename) {

        Path file = Common.getLogsPath().resolve(filename);
        if (!filterFiles(file)) {
            throw new NotFoundRestException();
        }

        //TODO There is a known bug here where if the file rolls over during download/transmission the request will fail
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, download ? "attachment" : "inline");
        if (file.getFileName().toString().endsWith(".gz")) {
            responseHeaders.set(HttpHeaders.CONTENT_ENCODING, "gzip");
        }
        return new ResponseEntity<>(new FileSystemResource(file), responseHeaders, HttpStatus.OK);
    }

}
