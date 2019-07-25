/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonInputMessage;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.infiniteautomation.mango.rest.v2.genericcsv.GenericCSVMessageConverter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.emport.PointValueTimeExportStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.emport.PointValueTimeImportStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.emport.PointValueTimeImportStream.XidPointValueTime;
import com.serotonin.m2m2.web.MediaTypes;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeCSVEmportMessageConverter extends GenericCSVMessageConverter {

    public PointValueTimeCSVEmportMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper, MediaTypes.CSV_V2);
    }
    
    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        boolean canSuperRead = super.canRead(type, contextClass, mediaType);
        return canSuperRead && type instanceof Class && PointValueTimeImportStream.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    protected Object readJavaType(JavaType javaType, HttpInputMessage inputMessage) {
        
        PointValueTimeImportStream stream = new PointValueTimeImportStream((consumer, error) -> {
            try {
                
                Class<?> deserializationView = null;
                if (inputMessage instanceof MappingJacksonInputMessage) {
                    deserializationView = ((MappingJacksonInputMessage) inputMessage).getDeserializationView();
                }
                
                ObjectReader reader;
                if (deserializationView != null) {
                    reader = this.objectMapper.readerWithView(deserializationView);
                } else {
                    reader = this.objectMapper.reader();
                }
                
                reader = reader.forType(XidPointValueTime.class);
                
                MediaType contentType = inputMessage.getHeaders().getContentType();
                Charset charset = this.charsetForContentType(contentType);

                try (BOMInputStream is = new BOMInputStream(inputMessage.getBody(), false, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE,
                        ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE)) {

                    String charsetName = is.getBOMCharsetName();
                    if (charsetName != null) {
                        charset = Charset.forName(charsetName);
                    }

                    try (Reader in = new InputStreamReader(is, charset)) {
                        
                        try(CSVReader csvReader = new CSVReader(in)) {
        
                            String[] header = csvReader.readNext();
                            Map<Integer, String> columnPositions = new HashMap<>();
                            int position = 0;
                            for (String propertyName : header) {
                                columnPositions.put(position++, propertyName);
                            }
        
                            String[] row;
                            while((row = csvReader.readNext()) != null) {
                                JsonNode node = readCSVRow(columnPositions, row);
                                consumer.accept(reader.readValue(node));
                            }
                        }
                    }
                }
            }catch(Exception e) {
                error.accept(e);
            }
        });
        return stream;
    }
    
    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        boolean canSuperWrite = super.canWrite(type, clazz, mediaType);
        return canSuperWrite && type instanceof Class && PointValueTimeExportStream.class.isAssignableFrom((Class<?>) type);
    }
    
}
