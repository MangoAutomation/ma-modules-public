/*
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URI;
import java.nio.file.Path;

import javax.management.MBeanServer;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.spring.ConditionalOnProperty;
import com.serotonin.m2m2.Common;

/**
 * @author Jared Wiltshire
 */
@RestController
@ConditionalOnProperty(value = {"${testing.enabled:false}", "${testing.restApi.enabled:false}"})
@PreAuthorize("isAdmin()")
@RequestMapping("/testing")
public class TestingRestController {

    private final Logger log = LoggerFactory.getLogger(TestingRestController.class);

    @RequestMapping(method = {RequestMethod.GET}, value = "/location")
    public ResponseEntity<Void> testLocation(UriComponentsBuilder builder) {

        HttpHeaders headers = new HttpHeaders();
        URI location = builder.path("/{id}").buildAndExpand("over-here").toUri();
        headers.setLocation(location);

        return new ResponseEntity<>(null, headers, HttpStatus.CREATED);
    }

    @RequestMapping(method = {RequestMethod.GET}, value = "/remote-addr")
    public String testLocation(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    @RequestMapping(method = {RequestMethod.POST}, value = "/heap-dump")
    public String heapDump(@RequestParam("filename") String filename) {

        Exception hotspot;
        Exception ibm;

        try {
            Path filenamePath = Common.MA_HOME_PATH.resolve(filename + ".hprof").toAbsolutePath();
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
            Object bean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", clazz);
            clazz.getMethod("dumpHeap", String.class, boolean.class).invoke(bean, filenamePath.toString(), true);
            return filenamePath.toString();
        } catch (Exception e) {
            hotspot = e;
        }

        try {
            Path filenamePath = Common.MA_HOME_PATH.resolve(filename + ".phd").toAbsolutePath();
            File dumpFile = new File((String) Class.forName("com.ibm.jvm.Dump").getMethod("heapDumpToFile", String.class).invoke(null, filenamePath.toString()));
            return dumpFile.toString();
        } catch (Exception e) {
            ibm = e;
        }

        log.info("Hotspot heap dump error", hotspot);
        log.info("IBM heap dump error", ibm);

        return null;
    }

    @RequestMapping(method = {RequestMethod.GET}, value = "/jvm-info")
    public JVMInfo jvmInfo() {
        return new JVMInfo();
    }

    public static class JVMInfo {
        final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();

        public String getName() {
            return runtime.getName();
        }
        public long getUptime() {
            return runtime.getUptime();
        }
        public long getStartTime() {
            return runtime.getStartTime();
        }
        public String getVmName() {
            return runtime.getVmName();
        }
        public String getVmVendor() {
            return runtime.getVmVendor();
        }
        public String getVmVersion() {
            return runtime.getVmVersion();
        }
        public String getSpecName() {
            return runtime.getSpecName();
        }
        public String getSpecVendor() {
            return runtime.getSpecVendor();
        }
        public String getSpecVersion() {
            return runtime.getSpecVersion();
        }
    }
}
