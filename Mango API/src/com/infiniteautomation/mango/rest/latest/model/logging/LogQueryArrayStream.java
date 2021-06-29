/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.logging;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.rest.latest.model.JSONStreamedArray;
import com.serotonin.m2m2.Common;

import com.serotonin.m2m2.i18n.Translations;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
public class LogQueryArrayStream implements JSONStreamedArray {

    public static final String LOGFILE_REGEX = ".*ma.log";
    private final String filename;
    private final ASTNode query;
    private final Translations translations;

    public LogQueryArrayStream(String filename, ASTNode query, Translations translations) {
        this.filename = filename;
        this.query = query;
        this.translations = translations;
    }

    /**
     * Specifically Process a Log4J file
     *
     * @param jgen
     * @throws IOException
     */
    @Override
    public void writeArrayValues(JsonGenerator jgen) throws IOException {
        if(filename == null)
            return;

        if(filename.matches(LOGFILE_REGEX)){

            MangoLogFilePatternReceiver receiver = new MangoLogFilePatternReceiver(query, jgen, translations);

            try {
                File logsDir = Common.getLogsDir();
                File logFile = new File(logsDir, filename);
                if(!logFile.exists())
                    return;

                receiver.setLogFormat("LEVEL TIMESTAMP (CLASS.METHOD:LINE) - MESSAGE"); //"%-5p %d{ISO8601} (%C.%M:%L) - %m %n"
                receiver.setFileURL(logFile.toURI().toURL().toExternalForm());
                receiver.setUseCurrentThread(true);

                //Start the parsing
                receiver.activateOptions();
            } catch (MalformedURLException e) {
                throw new IOException(e);
            } finally {
                receiver.shutdown();
            }
        }else{
            throw new IOException("Cannot query non Mango Log4J Files");
        }
    }
}
