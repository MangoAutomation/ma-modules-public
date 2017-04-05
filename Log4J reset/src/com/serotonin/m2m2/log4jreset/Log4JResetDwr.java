package com.serotonin.m2m2.log4jreset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import com.serotonin.m2m2.web.dwr.ModuleDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

public class Log4JResetDwr extends ModuleDwr {
    private static final Log LOG = LogFactory.getLog(Log4JResetDwr.class);

    @DwrPermission(admin = true)
    public void resetLog4J() {
        LOG.info("Reloading Log4J configuration");
    	((LoggerContext)LogManager.getContext(false)).reconfigure();;
        LOG.info("Finished reloading Log4J configuration");
    }

    @DwrPermission(admin = true)
    public void testDebug() {
        LOG.debug("Log4JReset module test debug message");
    }

    @DwrPermission(admin = true)
    public void testInfo() {
        LOG.info("Log4JReset module test info message");
    }

    @DwrPermission(admin = true)
    public void testWarn() {
        LOG.warn("Log4JReset module test warn message");
    }

    @DwrPermission(admin = true)
    public void testError() {
        LOG.error("Log4JReset module test error message");
    }

    @DwrPermission(admin = true)
    public void testFatal() {
        LOG.fatal("Log4JReset module test fatal message");
    }
}
