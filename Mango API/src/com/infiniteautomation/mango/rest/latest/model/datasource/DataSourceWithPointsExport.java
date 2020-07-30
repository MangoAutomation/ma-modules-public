/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.datasource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.infiniteautomation.mango.db.query.ConditionSortLimit;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.model.StreamedSeroJsonVORqlQuery;
import com.infiniteautomation.mango.spring.db.DataSourceTableDefinition;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonWriter;
import com.serotonin.json.spi.JsonPropertyOrder;
import com.serotonin.json.type.JsonStreamedArray;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

import net.jazdw.rql.parser.ASTNode;

/**
 * Container to order the data source query before the points and stream the result.
 *  Note that the
 * @author Terry Packer
 */
@JsonPropertyOrder({"dataSources", "dataPoints"})
public class DataSourceWithPointsExport {

    private final DataSourceService dataSourceService;
    private ASTNode dataSourceRql;
    private final DataPointService dataPointService;
    private final DataPointDao dataPointDao;
    private final List<Integer> dataSourceIds = new ArrayList<>();

    public DataSourceWithPointsExport(DataSourceService dataSourceService, ASTNode dataSourceRql, DataPointService dataPointService, DataPointDao dataPointDao) {
        this.dataSourceService = dataSourceService;
        this.dataSourceRql = dataSourceRql;
        this.dataPointService = dataPointService;
        this.dataPointDao = dataPointDao;
    }

    @JsonGetter("dataSources")
    public JsonStreamedArray getDataSources() {
        return new StreamedSeroJsonDataSourceRqlQuery(dataSourceService, dataSourceRql, dataSourceIds);
    }

    @JsonGetter("dataPoints")
    public JsonStreamedArray getDataPoints() {
        ConditionSortLimit csl = new ConditionSortLimit(dataPointDao.getTable().getAlias("dataSourceId").in(dataSourceIds), null, null, null);
        return new StreamedSeroJsonVORqlQuery<>(dataPointService, csl);
    }


    /**
     * Helper class to stream out data points and a data source
     *
     * @author Terry Packer
     */
    private class StreamedSeroJsonDataSourceRqlQuery extends StreamedSeroJsonVORqlQuery<DataSourceVO, DataSourceTableDefinition, DataSourceDao, DataSourceService> {
        private final List<Integer> dataSourceIds;

        public StreamedSeroJsonDataSourceRqlQuery(DataSourceService service, ASTNode rql, List<Integer> dataSourceIds) {
            super(service, service.rqlToCondition(rql, null, null));
            this.dataSourceIds = dataSourceIds;
        }

        @Override
        public void writeArrayValues(JsonWriter writer) throws IOException {
            service.customizedQuery(conditions, (DataSourceVO item, int index) -> {
                try {
                    if (count > 0)
                        writer.append(',');
                    writer.indent();
                    writer.writeObject(item);
                    dataSourceIds.add(item.getId());
                    count++;
                } catch (IOException | JsonException e) {
                    //TODO Mango 4.0 this can mangle the response, perhaps handle in exception handler to reset stream
                    //  also a nice way to cancel this query would be good as it will just keep throwing
                    // the exception if we don't cancel it.
                    throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, e);
                }
            });
        }

    }
}
