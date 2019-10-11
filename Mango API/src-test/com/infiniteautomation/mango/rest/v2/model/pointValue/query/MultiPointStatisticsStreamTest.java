/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeJsonWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream.StreamContentType;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
import com.infiniteautomation.mango.statistics.StartsAndRuntime;
import com.infiniteautomation.mango.statistics.StartsAndRuntimeList;
import com.infiniteautomation.mango.statistics.ValueChangeCounter;
import com.infiniteautomation.mango.util.datetime.NextTimePeriodAdjuster;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.Common.TimePeriods;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.MangoTestBase;
import com.serotonin.m2m2.MockMangoLifecycle;
import com.serotonin.m2m2.MockRuntimeManager;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.Module;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.DataPointRT.FireEvents;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.ImageValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.DataPointVO.LoggingTypes;
import com.serotonin.m2m2.vo.dataPoint.MockPointLocatorVO;
import com.serotonin.m2m2.vo.dataSource.mock.MockDataSourceVO;
import com.serotonin.m2m2.web.servlet.ImageValueServlet;

/**
 *
 * @author Terry Packer
 */
public class MultiPointStatisticsStreamTest extends MangoTestBase {

    protected ZoneId zoneId;
    protected static final TestRuntimeManager runtimeManager = new TestRuntimeManager();
    protected final ImageValueServlet imageServlet = new ImageValueServlet();
    
    public MultiPointStatisticsStreamTest() {
        this.zoneId = ZoneId.systemDefault();
    }

    //TODO Test initial values and no initial values
 
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.MangoTestBase#after()
     */
    @Override
    public void after() {
        super.after();
        runtimeManager.points.clear();
    }
    
    @Test
    public void testSingleImagePointNoCacheNoChangeInitialValue() throws IOException {
        
        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 01, 01, 00, 00, 00, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 02, 01, 00, 00, 00, 0, zoneId);
        NextTimePeriodAdjuster adjuster = new NextTimePeriodAdjuster(TimePeriods.DAYS, 1);

        MockDataSourceVO ds = createDataSource();
        DataPointVO dp = createDataPoint(ds.getId(), DataTypes.IMAGE, 1);
        
        ImageValue v = new ImageValue(createImageBytes(10), ImageValue.TYPE_JPG);
        PointValueTime initialValue = new PointValueTime(v, 0);
        
        DataPointWrapper<ValueChangeCounter> point = new DataPointWrapper<ValueChangeCounter>(ds, dp, 
                initialValue, 
                (value) -> {
                    //No change
                    return value;
                },
                (info, w) ->{
                    return new ValueChangeCounter(info.getFromMillis(), info.getToMillis(), w.initialValue, w.values);
                },
                (w, gen, root) -> {
                    JsonNode stats = root.get(w.vo.getXid());
                    if(stats == null)
                        fail("Missing stats for point " + w.vo.getXid());
                    
                    JsonNode stat = stats.get(PointValueTimeWriter.START);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.START + " entry");
                    
                    assertNotNull(stat.get(PointValueTimeWriter.TIMESTAMP));
                    PointValueTime value = w.rt.getPointValueAfter(0);
                    assertEquals(gen.getStartValue(), value.getValue());
                    assertEquals(0l, value.getTime());

                    stat = stats.get(PointValueTimeWriter.FIRST);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.FIRST + " entry");
                    //Test the access via the servlet
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    //TODO Cannot compare the values as the gen doesn't have the 'data' loaded
                    assertEquals((long)gen.getFirstTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.LAST);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.LAST + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    //TODO Cannot compare the values as the gen doesn't have the 'data' loaded
                    assertEquals((long)gen.getLastTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.COUNT);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.COUNT + " entry");
                    assertEquals(gen.getCount(), stat.asInt());
                });

        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = (ZonedDateTime) adjuster.adjustInto(from);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while(time.toInstant().isBefore(to.toInstant())) {
            point.updatePointValue(new IdPointValueTime(point.vo.getId(), point.getNextValue(), time.toInstant().toEpochMilli()));
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }
        
        //Perform the query
        String dateTimeFormat = null;
        String timezone = zoneId.getId();
        PointValueTimeCacheControl cache = PointValueTimeCacheControl.NONE;
        PointValueField[] fields = getFields();
        ZonedDateTimeStatisticsQueryInfo info = new ZonedDateTimeStatisticsQueryInfo(from, to, dateTimeFormat, timezone, cache, fields);

        test(info, point);
    }
    
    @Test
    public void testSingleImagePointOnlyCacheChange() throws IOException {
        
        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 01, 01, 00, 00, 00, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 02, 01, 00, 00, 00, 0, zoneId);
        NextTimePeriodAdjuster adjuster = new NextTimePeriodAdjuster(TimePeriods.DAYS, 1);

        int cacheSize = 10;
        MockDataSourceVO ds = createDataSource();
        DataPointVO dp = createDataPoint(ds.getId(), DataTypes.IMAGE, cacheSize);
        
        AtomicInteger imageSize = new AtomicInteger(1);
        ImageValue v = new ImageValue(createImageBytes(imageSize.getAndIncrement()), ImageValue.TYPE_JPG);
        PointValueTime initialValue = new PointValueTime(v, 0);
        
        DataPointWrapper<ValueChangeCounter> point = new DataPointWrapper<ValueChangeCounter>(ds, dp, 
                initialValue, 
                (value) -> {
                    return new ImageValue(createImageBytes(imageSize.getAndIncrement()), ImageValue.TYPE_JPG);
                },
                (info, w) ->{
                    return new ValueChangeCounter(info.getFromMillis(), info.getToMillis(), null, w.values);
                },
                (w, gen, root) -> {
                    JsonNode stats = root.get(w.vo.getXid());
                    if(stats == null)
                        fail("Missing stats for point " + w.vo.getXid());
                    
                    JsonNode stat = stats.get(PointValueTimeWriter.START);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.START + " entry");
                    assertNull(gen.getStartValue());
                    assertTrue(stat.isNull());

                    stat = stats.get(PointValueTimeWriter.FIRST);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.FIRST + " entry");
                    PointValueTime value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getFirstValue(), value.getValue());
                    assertEquals((long)gen.getFirstTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.LAST);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.LAST + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getLastValue(), value.getValue() );
                    assertEquals((long)gen.getLastTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.COUNT);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.COUNT + " entry");
                    assertEquals(gen.getCount(), stat.asInt());                 
                });

        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = (ZonedDateTime) adjuster.adjustInto(from);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while(time.toInstant().isBefore(to.toInstant())) {
            point.updatePointValue(new IdPointValueTime(point.vo.getId(), point.getNextValue(), time.toInstant().toEpochMilli()));
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }
        
        //Insert some values directly into the cache
        point.values.clear();
        for(int i=0; i<cacheSize; i++) {
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
            point.saveOnlyToCache(new PointValueTime(point.getNextValue(), timer.currentTimeMillis()));
        }
        
        //Ensure we get all the data
        Instant now = Instant.ofEpochMilli(timer.currentTimeMillis() + 1);
        to = ZonedDateTime.ofInstant(now, zoneId);
        //Perform the query
        String dateTimeFormat = null;
        String timezone = zoneId.getId();
        PointValueTimeCacheControl cache = PointValueTimeCacheControl.CACHE_ONLY;
        PointValueField[] fields =  getFields();
        ZonedDateTimeStatisticsQueryInfo info = new ZonedDateTimeStatisticsQueryInfo(from, to, dateTimeFormat, timezone, cache, fields);

        test(info, point);
    }
    
    @Test
    public void testSingleAlphanumericPointNoCacheNoChangeInitialValue() throws IOException {
        
        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 01, 01, 00, 00, 00, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 02, 01, 00, 00, 00, 0, zoneId);
        NextTimePeriodAdjuster adjuster = new NextTimePeriodAdjuster(TimePeriods.DAYS, 1);

        MockDataSourceVO ds = createDataSource();
        DataPointVO dp = createDataPoint(ds.getId(), DataTypes.ALPHANUMERIC, 1);
        
        DataPointWrapper<ValueChangeCounter> point = new DataPointWrapper<ValueChangeCounter>(ds, dp, 
                new PointValueTime("TESTING", 0), 
                (value) -> {
                    //No change
                    return value;
                },
                (info, w) ->{
                    return new ValueChangeCounter(info.getFromMillis(), info.getToMillis(), w.initialValue, w.values);
                },
                new ValueChangeCounterVerifier());

        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = (ZonedDateTime) adjuster.adjustInto(from);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while(time.toInstant().isBefore(to.toInstant())) {
            point.updatePointValue(new IdPointValueTime(point.vo.getId(), point.getNextValue(), time.toInstant().toEpochMilli()));
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }
        
        //Perform the query
        String dateTimeFormat = null;
        String timezone = zoneId.getId();
        PointValueTimeCacheControl cache = PointValueTimeCacheControl.NONE;
        PointValueField[] fields = getFields();
        ZonedDateTimeStatisticsQueryInfo info = new ZonedDateTimeStatisticsQueryInfo(from, to, dateTimeFormat, timezone, cache, fields);

        test(info, point);
    }
    
    /**
     * Start with a value of 1 at time 0
     * Then insert a value of 1 at midnight every day during Jan 2017
     * @throws IOException
     */
    @Test
    public void testSingleMultistatePointNoCacheNoChangeInitialValue() throws IOException {
        
        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 01, 01, 00, 00, 00, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 02, 01, 00, 00, 00, 0, zoneId);
        NextTimePeriodAdjuster adjuster = new NextTimePeriodAdjuster(TimePeriods.DAYS, 1);

        MockDataSourceVO ds = createDataSource();
        DataPointVO dp = createDataPoint(ds.getId(), DataTypes.MULTISTATE, 1);
        
        DataPointWrapper<StartsAndRuntimeList> point = new DataPointWrapper<StartsAndRuntimeList>(ds, dp, 
                new PointValueTime(1, 0), 
                (value) -> {
                    //No change
                    return value;
                },
                (info, w) ->{
                    return new StartsAndRuntimeList(info.getFromMillis(), info.getToMillis(), w.initialValue, w.values);
                },
                new StartsAndRuntimeListVerifier());

        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = (ZonedDateTime) adjuster.adjustInto(from);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while(time.toInstant().isBefore(to.toInstant())) {
            point.updatePointValue(new IdPointValueTime(point.vo.getId(), point.getNextValue(), time.toInstant().toEpochMilli()));
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }
        
        //Perform the query
        String dateTimeFormat = null;
        String timezone = zoneId.getId();
        PointValueTimeCacheControl cache = PointValueTimeCacheControl.NONE;
        PointValueField[] fields = getFields();
        ZonedDateTimeStatisticsQueryInfo info = new ZonedDateTimeStatisticsQueryInfo(from, to, dateTimeFormat, timezone, cache, fields);

        test(info, point);
    }
    
    
    @Test
    public void testSingleNumericPointNoCacheNoChangeInitialValue() throws IOException {
        
        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 01, 01, 00, 00, 00, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 02, 01, 00, 00, 00, 0, zoneId);
        NextTimePeriodAdjuster adjuster = new NextTimePeriodAdjuster(TimePeriods.DAYS, 1);

        MockDataSourceVO ds = createDataSource();
        DataPointVO dp = createDataPoint(ds.getId(), DataTypes.NUMERIC, 1);
        
        DataPointWrapper<AnalogStatistics> point = new DataPointWrapper<AnalogStatistics>(ds, dp, 
                new PointValueTime(1.0, 0), 
                (value) -> {
                    //No change
                    return value;
                },
                (info, w) ->{
                    return new AnalogStatistics(info.getFromMillis(), info.getToMillis(), w.initialValue, w.values);
                },
                new AnalogStatisticsVerifier());

        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = (ZonedDateTime) adjuster.adjustInto(from);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while(time.toInstant().isBefore(to.toInstant())) {
            point.updatePointValue(new IdPointValueTime(point.vo.getId(), point.getNextValue(), time.toInstant().toEpochMilli()));
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }
        
        //Perform the query
        String dateTimeFormat = null;
        String timezone = zoneId.getId();
        PointValueTimeCacheControl cache = PointValueTimeCacheControl.NONE;
        PointValueField[] fields = getFields();
        ZonedDateTimeStatisticsQueryInfo info = new ZonedDateTimeStatisticsQueryInfo(from, to, dateTimeFormat, timezone, cache, fields);

        test(info, point);
    }
    
    @Test
    public void testSingleNumericPointNoCacheChangeInitialValue() throws IOException {
        
        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 01, 01, 00, 00, 00, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 02, 01, 00, 00, 00, 0, zoneId);
        NextTimePeriodAdjuster adjuster = new NextTimePeriodAdjuster(TimePeriods.DAYS, 1);

        MockDataSourceVO ds = createDataSource();
        DataPointVO dp = createDataPoint(ds.getId(), DataTypes.NUMERIC, 1);
        
        DataPointWrapper<AnalogStatistics> point = new DataPointWrapper<AnalogStatistics>(ds, dp, 
                new PointValueTime(1.0, 0), 
                (value) -> {
                    return new NumericValue(value.getDoubleValue() + 1.0);
                },
                (info, w) ->{
                    return new AnalogStatistics(info.getFromMillis(), info.getToMillis(), w.initialValue, w.values);
                },
                new AnalogStatisticsVerifier());

        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = (ZonedDateTime) adjuster.adjustInto(from);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while(time.toInstant().isBefore(to.toInstant())) {
            point.updatePointValue(new IdPointValueTime(point.vo.getId(), point.getNextValue(), time.toInstant().toEpochMilli()));
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }
        
        //Perform the query
        String dateTimeFormat = null;
        String timezone = zoneId.getId();
        PointValueTimeCacheControl cache = PointValueTimeCacheControl.NONE;
        PointValueField[] fields = getFields();
        ZonedDateTimeStatisticsQueryInfo info = new ZonedDateTimeStatisticsQueryInfo(from, to, dateTimeFormat, timezone, cache, fields);

        test(info, point);
    }
    
    @Test
    public void testSingleNumericPointOnlyCacheChange() throws IOException {
        
        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 01, 01, 00, 00, 00, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 02, 01, 00, 00, 00, 0, zoneId);
        NextTimePeriodAdjuster adjuster = new NextTimePeriodAdjuster(TimePeriods.DAYS, 1);

        int cacheSize = 10;
        MockDataSourceVO ds = createDataSource();
        DataPointVO dp = createDataPoint(ds.getId(), DataTypes.NUMERIC, cacheSize);
        
        DataPointWrapper<AnalogStatistics> point = new DataPointWrapper<AnalogStatistics>(ds, dp, 
                new PointValueTime(1.0, 0), 
                (value) -> {
                    return new NumericValue(value.getDoubleValue() + 1.0);
                },
                (info, w) ->{
                    return new AnalogStatistics(info.getFromMillis(), info.getToMillis(), null, w.values);
                },
                (w, gen, root) -> {
                    JsonNode stats = root.get(w.vo.getXid());
                    if(stats == null)
                        fail("Missing stats for point " + w.vo.getXid());
                    
                    JsonNode stat = stats.get(PointValueTimeWriter.START);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.START + " entry");
                    
                    assertNull(gen.getStartValue());
                    assertTrue(stat.isNull());

                    stat = stats.get(PointValueTimeWriter.FIRST);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.FIRST + " entry");
                    PointValueTime value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getFirstValue(), value.getValue().getDoubleValue(), 0.00001);
                    assertEquals((long)gen.getFirstTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.LAST);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.LAST + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getLastValue(), value.getValue().getDoubleValue(), 0.00001);
                    assertEquals((long)gen.getLastTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.COUNT);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.COUNT + " entry");
                    assertEquals(gen.getCount(), stat.asInt());
                    
                    stat = stats.get(PointValueTimeWriter.ACCUMULATOR);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.ACCUMULATOR + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    Double accumulatorValue = gen.getLastValue();
                    if(accumulatorValue == null)
                        accumulatorValue = gen.getMaximumValue();
                    assertEquals(accumulatorValue, value.getDoubleValue(), 0.00001);
                    
                    stat = stats.get(PointValueTimeWriter.AVERAGE);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.AVERAGE + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getAverage(), value.getDoubleValue(), 0.00001);

                    stat = stats.get(PointValueTimeWriter.DELTA);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.DELTA + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getDelta(), value.getDoubleValue(), 0.00001);
                    
                    stat = stats.get(PointValueTimeWriter.MINIMUM);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.MINIMUM + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getMinimumValue(), value.getValue().getDoubleValue(), 0.00001);
                    assertEquals((long)gen.getMinimumTime(), value.getTime());

                    stat = stats.get(PointValueTimeWriter.MAXIMUM);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.MAXIMUM + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getMaximumValue(), value.getValue().getDoubleValue(), 0.00001);
                    assertEquals((long)gen.getMaximumTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.SUM);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.SUM + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getSum(), value.getDoubleValue(), 0.00001);
                    
                    stat = stats.get(PointValueTimeWriter.INTEGRAL);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.INTEGRAL + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getIntegral(), value.getDoubleValue(), 0.00001);
                });

        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = (ZonedDateTime) adjuster.adjustInto(from);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while(time.toInstant().isBefore(to.toInstant())) {
            point.updatePointValue(new IdPointValueTime(point.vo.getId(), point.getNextValue(), time.toInstant().toEpochMilli()));
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }
        
        //Insert some values directly into the cache
        point.values.clear();
        for(int i=0; i<cacheSize; i++) {
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
            point.saveOnlyToCache(new PointValueTime(point.getNextValue(), timer.currentTimeMillis()));
        }
        
        //Ensure we get all the data
        Instant now = Instant.ofEpochMilli(timer.currentTimeMillis() + 1);
        to = ZonedDateTime.ofInstant(now, zoneId);
        //Perform the query
        String dateTimeFormat = null;
        String timezone = zoneId.getId();
        PointValueTimeCacheControl cache = PointValueTimeCacheControl.CACHE_ONLY;
        PointValueField[] fields =  getFields();
        ZonedDateTimeStatisticsQueryInfo info = new ZonedDateTimeStatisticsQueryInfo(from, to, dateTimeFormat, timezone, cache, fields);

        test(info, point);
    }
    
    
    @Test
    public void testSingleNumericPointOnlyCacheChangeCachedValueAtFrom() throws IOException {
        
        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 01, 01, 00, 00, 00, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 02, 01, 00, 00, 00, 0, zoneId);
        NextTimePeriodAdjuster adjuster = new NextTimePeriodAdjuster(TimePeriods.DAYS, 1);

        int cacheSize = 10;
        MockDataSourceVO ds = createDataSource();
        DataPointVO dp = createDataPoint(ds.getId(), DataTypes.NUMERIC, cacheSize);
        
        DataPointWrapper<AnalogStatistics> point = new DataPointWrapper<AnalogStatistics>(ds, dp, 
                new PointValueTime(1.0, 0), 
                (value) -> {
                    return new NumericValue(value.getDoubleValue() + 1.0);
                },
                (info, w) ->{
                    return new AnalogStatistics(info.getFromMillis(), info.getToMillis(), w.values.get(0), w.values);
                },
                new AnalogStatisticsVerifier());

        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = (ZonedDateTime) adjuster.adjustInto(from);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while(time.toInstant().isBefore(to.toInstant())) {
            point.updatePointValue(new IdPointValueTime(point.vo.getId(), point.getNextValue(), time.toInstant().toEpochMilli()));
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }
        
        //Insert some values directly into the cache
        point.values.clear();
        for(int i=0; i<cacheSize; i++) {
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
            point.saveOnlyToCache(new AnnotatedPointValueTime(point.getNextValue(), timer.currentTimeMillis(), new TranslatableMessage("common.default", "testing")));
        }
        
        //Ensure we get all the data
        from = ZonedDateTime.ofInstant(Instant.ofEpochMilli(point.values.get(0).getTime()), zoneId);
        Instant now = Instant.ofEpochMilli(timer.currentTimeMillis() + 1);
        to = ZonedDateTime.ofInstant(now, zoneId);
        //Perform the query
        String dateTimeFormat = null;
        String timezone = zoneId.getId();
        PointValueTimeCacheControl cache = PointValueTimeCacheControl.CACHE_ONLY;
        PointValueField[] fields = getFields();
        ZonedDateTimeStatisticsQueryInfo info = new ZonedDateTimeStatisticsQueryInfo(from, to, dateTimeFormat, timezone, cache, fields);

        test(info, point);
    }
    
    @Test
    public void testSingleNumericPointBothChange() throws IOException {
        
        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 01, 01, 00, 00, 00, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 02, 01, 00, 00, 00, 0, zoneId);
        NextTimePeriodAdjuster adjuster = new NextTimePeriodAdjuster(TimePeriods.DAYS, 1);

        int cacheSize = 10;
        MockDataSourceVO ds = createDataSource();
        DataPointVO dp = createDataPoint(ds.getId(), DataTypes.NUMERIC, cacheSize);
        
        DataPointWrapper<AnalogStatistics> point = new DataPointWrapper<AnalogStatistics>(ds, dp, 
                new PointValueTime(1.0, 0), 
                (value) -> {
                    return new NumericValue(value.getDoubleValue() + 1.0);
                },
                (info, w) ->{
                    return new AnalogStatistics(info.getFromMillis(), info.getToMillis(), new PointValueTime(1.0, 0), w.values);
                },
                (w, gen, root) -> {
                    JsonNode stats = root.get(w.vo.getXid());
                    if(stats == null)
                        fail("Missing stats for point " + w.vo.getXid());
                    
                    JsonNode stat = stats.get(PointValueTimeWriter.START);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.START + " entry");
  
                    PointValueTime value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getStartValue(), value.getDoubleValue(), 0.00001);
                    assertEquals((long)gen.getPeriodStartTime(), value.getTime());

                    stat = stats.get(PointValueTimeWriter.FIRST);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.FIRST + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getFirstValue(), value.getValue().getDoubleValue(), 0.00001);
                    assertEquals((long)gen.getFirstTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.LAST);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.LAST + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getLastValue(), value.getValue().getDoubleValue(), 0.00001);
                    assertEquals((long)gen.getLastTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.COUNT);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.COUNT + " entry");
                    assertEquals(gen.getCount(), stat.asInt());
                    
                    stat = stats.get(PointValueTimeWriter.ACCUMULATOR);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.ACCUMULATOR + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    Double accumulatorValue = gen.getLastValue();
                    if(accumulatorValue == null)
                        accumulatorValue = gen.getMaximumValue();
                    assertEquals(accumulatorValue, value.getDoubleValue(), 0.00001);
                    
                    stat = stats.get(PointValueTimeWriter.AVERAGE);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.AVERAGE + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getAverage(), value.getDoubleValue(), 0.00001);

                    stat = stats.get(PointValueTimeWriter.DELTA);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.DELTA + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getDelta(), value.getDoubleValue(), 0.00001);
                    
                    stat = stats.get(PointValueTimeWriter.MINIMUM);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.MINIMUM + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getMinimumValue(), value.getValue().getDoubleValue(), 0.00001);
                    assertEquals((long)gen.getMinimumTime(), value.getTime());

                    stat = stats.get(PointValueTimeWriter.MAXIMUM);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.MAXIMUM + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getMaximumValue(), value.getValue().getDoubleValue(), 0.00001);
                    assertEquals((long)gen.getMaximumTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.SUM);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.SUM + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getSum(), value.getDoubleValue(), 0.00001);
                    
                    stat = stats.get(PointValueTimeWriter.INTEGRAL);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.INTEGRAL + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getIntegral(), value.getDoubleValue(), 0.00001);
                });

        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = (ZonedDateTime) adjuster.adjustInto(from);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while(time.toInstant().isBefore(to.toInstant())) {
            point.updatePointValue(new IdPointValueTime(point.vo.getId(), point.getNextValue(), time.toInstant().toEpochMilli()));
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }
        
        //Insert some values directly into the cache
        for(int i=0; i<cacheSize; i++) {
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
            point.saveOnlyToCache(new PointValueTime(point.getNextValue(), timer.currentTimeMillis()));
        }
        
        //Ensure we get all the data
        Instant now = Instant.ofEpochMilli(timer.currentTimeMillis() + 1);
        to = ZonedDateTime.ofInstant(now, zoneId);
        //Perform the query
        String dateTimeFormat = null;
        String timezone = zoneId.getId();
        PointValueTimeCacheControl cache = PointValueTimeCacheControl.BOTH;
        PointValueField[] fields =  getFields();
        ZonedDateTimeStatisticsQueryInfo info = new ZonedDateTimeStatisticsQueryInfo(from, to, dateTimeFormat, timezone, cache, fields);

        test(info, point);
    }
    
    @Test
    public void testMultiplePointsNoCacheChangeInitialValue() throws IOException {
        
        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 01, 01, 00, 00, 00, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 02, 01, 00, 00, 00, 0, zoneId);
        NextTimePeriodAdjuster adjuster = new NextTimePeriodAdjuster(TimePeriods.DAYS, 1);

        MockDataSourceVO ds = createDataSource();
        DataPointVO numericDp = createDataPoint(ds.getId(), DataTypes.NUMERIC, 1);
        
        DataPointWrapper<AnalogStatistics> numericPoint = new DataPointWrapper<AnalogStatistics>(ds, numericDp, 
                new PointValueTime(1.0, 0), 
                (value) -> {
                    return new NumericValue(value.getDoubleValue() + 1.0);
                },
                (info, w) ->{
                    return new AnalogStatistics(info.getFromMillis(), info.getToMillis(), w.initialValue, w.values);
                },
                new AnalogStatisticsVerifier());

        DataPointVO multistateDp = createDataPoint(ds.getId(), DataTypes.MULTISTATE, 1);
        
        DataPointWrapper<StartsAndRuntimeList> multistatePoint = new DataPointWrapper<StartsAndRuntimeList>(ds, multistateDp, 
                new PointValueTime(1, 0), 
                (value) -> {
                    //No change
                    return new MultistateValue(value.getIntegerValue() + 1);
                },
                (info, w) ->{
                    return new StartsAndRuntimeList(info.getFromMillis(), info.getToMillis(), w.initialValue, w.values);
                },
                new StartsAndRuntimeListVerifier());

        
        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = (ZonedDateTime) adjuster.adjustInto(from);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while(time.toInstant().isBefore(to.toInstant())) {
            numericPoint.updatePointValue(new IdPointValueTime(numericPoint.vo.getId(), numericPoint.getNextValue(), time.toInstant().toEpochMilli()));
            multistatePoint.updatePointValue(new IdPointValueTime(multistatePoint.vo.getId(), multistatePoint.getNextValue(), time.toInstant().toEpochMilli()));
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }
        
        //Perform the query
        String dateTimeFormat = null;
        String timezone = zoneId.getId();
        PointValueTimeCacheControl cache = PointValueTimeCacheControl.NONE;
        PointValueField[] fields = getFields();
        ZonedDateTimeStatisticsQueryInfo info = new ZonedDateTimeStatisticsQueryInfo(from, to, dateTimeFormat, timezone, cache, fields);

        test(info, numericPoint, multistatePoint);
    }
    
    @Test
    public void testMultiplePointsOnlyCacheChange() throws IOException {
        
        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 01, 01, 00, 00, 00, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 02, 01, 00, 00, 00, 0, zoneId);
        NextTimePeriodAdjuster adjuster = new NextTimePeriodAdjuster(TimePeriods.DAYS, 1);

        int cacheSize = 10;
        MockDataSourceVO ds = createDataSource();
        DataPointVO numericDp = createDataPoint(ds.getId(), DataTypes.NUMERIC, cacheSize);
        
        DataPointWrapper<AnalogStatistics> numericPoint = new DataPointWrapper<AnalogStatistics>(ds, numericDp, 
                new PointValueTime(1.0, 0), 
                (value) -> {
                    return new NumericValue(value.getDoubleValue() + 1.0);
                },
                (info, w) ->{
                    return new AnalogStatistics(info.getFromMillis(), info.getToMillis(), null, w.values);
                },
                (w, gen, root) -> {
                    JsonNode stats = root.get(w.vo.getXid());
                    if(stats == null)
                        fail("Missing stats for point " + w.vo.getXid());
                    
                    JsonNode stat = stats.get(PointValueTimeWriter.START);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.START + " entry");
                    
                    assertNull(gen.getStartValue());
                    assertTrue(stat.isNull());

                    stat = stats.get(PointValueTimeWriter.FIRST);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.FIRST + " entry");
                    PointValueTime value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getFirstValue(), value.getValue().getDoubleValue(), 0.00001);
                    assertEquals((long)gen.getFirstTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.LAST);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.LAST + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getLastValue(), value.getValue().getDoubleValue(), 0.00001);
                    assertEquals((long)gen.getLastTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.COUNT);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.COUNT + " entry");
                    assertEquals(gen.getCount(), stat.asInt());
                    
                    stat = stats.get(PointValueTimeWriter.ACCUMULATOR);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.ACCUMULATOR + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    Double accumulatorValue = gen.getLastValue();
                    if(accumulatorValue == null)
                        accumulatorValue = gen.getMaximumValue();
                    assertEquals(accumulatorValue, value.getDoubleValue(), 0.00001);
                    
                    stat = stats.get(PointValueTimeWriter.AVERAGE);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.AVERAGE + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getAverage(), value.getDoubleValue(), 0.00001);

                    stat = stats.get(PointValueTimeWriter.DELTA);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.DELTA + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getDelta(), value.getDoubleValue(), 0.00001);
                    
                    stat = stats.get(PointValueTimeWriter.MINIMUM);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.MINIMUM + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getMinimumValue(), value.getValue().getDoubleValue(), 0.00001);
                    assertEquals((long)gen.getMinimumTime(), value.getTime());

                    stat = stats.get(PointValueTimeWriter.MAXIMUM);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.MAXIMUM + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getMaximumValue(), value.getValue().getDoubleValue(), 0.00001);
                    assertEquals((long)gen.getMaximumTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.SUM);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.SUM + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getSum(), value.getDoubleValue(), 0.00001);
                    
                    stat = stats.get(PointValueTimeWriter.INTEGRAL);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.INTEGRAL + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getIntegral(), value.getDoubleValue(), 0.00001);
                });

        DataPointVO multistateDp = createDataPoint(ds.getId(), DataTypes.MULTISTATE, cacheSize);
        DataPointWrapper<StartsAndRuntimeList> multistatePoint = new DataPointWrapper<StartsAndRuntimeList>(ds, multistateDp, 
                new PointValueTime(1, 0), 
                (value) -> {
                    return new MultistateValue(value.getIntegerValue() + 1);
                },
                (info, w) ->{
                    return new StartsAndRuntimeList(info.getFromMillis(), info.getToMillis(), null, w.values);
                },
                (w, gen, root) -> {
                    JsonNode stats = root.get(w.vo.getXid());
                    if(stats == null)
                        fail("Missing stats for point " + w.vo.getXid());
                    
                    JsonNode stat = stats.get(PointValueTimeWriter.START);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.START + " entry");
                    
                    assertNull(gen.getStartValue());
                    assertTrue(stat.isNull());

                    stat = stats.get(PointValueTimeWriter.FIRST);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.FIRST + " entry");
                    PointValueTime value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getFirstValue(), value.getValue());
                    assertEquals((long)gen.getFirstTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.LAST);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.LAST + " entry");
                    value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
                    assertEquals(gen.getLastValue(), value.getValue());
                    assertEquals((long)gen.getLastTime(), value.getTime());
                    
                    stat = stats.get(PointValueTimeWriter.COUNT);
                    if(stat == null)
                        fail("Missing " + PointValueTimeWriter.COUNT + " entry");
                    assertEquals(gen.getCount(), stat.asInt());
                    
                    //Test data
                    stat = stats.get(PointValueTimeWriter.STARTS_AND_RUNTIMES);
                    if(stat == null)
                        fail("Missing data entry");
                    
                    for(int i=0; i<gen.getData().size(); i++) {
                        StartsAndRuntime expected = gen.getData().get(i);
                        JsonNode actual = stat.get(i);
                        assertEquals((int)expected.getValue(), actual.get(PointValueTimeWriter.VALUE).intValue());
                        assertEquals(expected.getStarts(), actual.get(PointValueTimeWriter.STARTS).intValue());
                        assertEquals(expected.getRuntime(), actual.get(PointValueTimeWriter.RUNTIME).asLong());
                        assertEquals(expected.getProportion(), actual.get(PointValueTimeWriter.PROPORTION).doubleValue(), 0.000001);
                    }
                });
        
        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = (ZonedDateTime) adjuster.adjustInto(from);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while(time.toInstant().isBefore(to.toInstant())) {
            numericPoint.updatePointValue(new IdPointValueTime(numericPoint.vo.getId(), numericPoint.getNextValue(), time.toInstant().toEpochMilli()));
            multistatePoint.updatePointValue(new IdPointValueTime(multistatePoint.vo.getId(), multistatePoint.getNextValue(), time.toInstant().toEpochMilli()));
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }
        
        //Insert some values directly into the cache
        numericPoint.values.clear();
        multistatePoint.values.clear();
        for(int i=0; i<cacheSize; i++) {
            time = (ZonedDateTime) adjuster.adjustInto(time);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
            numericPoint.saveOnlyToCache(new PointValueTime(numericPoint.getNextValue(), timer.currentTimeMillis()));
            multistatePoint.saveOnlyToCache(new PointValueTime(multistatePoint.getNextValue(), timer.currentTimeMillis()));
        }
        
        //Ensure we get all the data
        Instant now = Instant.ofEpochMilli(timer.currentTimeMillis() + 1);
        to = ZonedDateTime.ofInstant(now, zoneId);
        //Perform the query
        String dateTimeFormat = null;
        String timezone = zoneId.getId();
        PointValueTimeCacheControl cache = PointValueTimeCacheControl.CACHE_ONLY;
        PointValueField[] fields =  getFields();
        ZonedDateTimeStatisticsQueryInfo info = new ZonedDateTimeStatisticsQueryInfo(from, to, dateTimeFormat, timezone, cache, fields);

        test(info, numericPoint, multistatePoint);
    }
    
    /**
     * @param info
     * @param voMap
     * @throws IOException 
     */
    private void test(ZonedDateTimeStatisticsQueryInfo info, DataPointWrapper<?>...points) throws IOException {
        Map<Integer, DataPointVO> voMap = new HashMap<>();
        for(DataPointWrapper<?> wrapper : points)
            voMap.put(wrapper.vo.getId(), wrapper.vo);
        JsonNode root = generateOutput(info, voMap);
        for(DataPointWrapper<?> wrapper : points)
            wrapper.verify(info, root);
    }

    protected PointValueField[] getFields() {
        return PointValueField.values();
    }
    
    protected PointValueTime getPointValueTime(int dataTypeId, JsonNode stat) {
        if(stat.isNull()) {
            return null;
        }
        assertNotNull(stat.get(PointValueTimeWriter.TIMESTAMP));
        assertNotNull(stat.get(PointValueTimeWriter.VALUE));
        switch(dataTypeId) {
            case DataTypes.MULTISTATE:
                return new PointValueTime(stat.get(PointValueTimeWriter.VALUE).asInt(), stat.get(PointValueTimeWriter.TIMESTAMP).asLong());
            case DataTypes.NUMERIC:
                return new PointValueTime(stat.get(PointValueTimeWriter.VALUE).asDouble(), stat.get(PointValueTimeWriter.TIMESTAMP).asLong());
            case DataTypes.ALPHANUMERIC:
                return new PointValueTime(stat.get(PointValueTimeWriter.VALUE).asText(), stat.get(PointValueTimeWriter.TIMESTAMP).asLong());
            case DataTypes.IMAGE:
                try {
                    //Use image value servlet to get the image out, first create a mock request
                    // to simulate the normal request
                    String url = stat.get(PointValueTimeWriter.VALUE).asText();
                    String[] paths = url.split("/");
                    MockHttpServletRequest request = new MockHttpServletRequest();
                    request.setMethod("GET");
                    request.setRequestURI(url);
                    request.setPathInfo("/" + paths[2]);
                    MockHttpServletResponse response = new MockHttpServletResponse();
                    imageServlet.service(request, response);
                    assertEquals(200, response.getStatus());
                    byte[] imgData = response.getContentAsByteArray();
                    return new PointValueTime(new ImageValue(imgData, ImageValue.TYPE_JPG), stat.get(PointValueTimeWriter.TIMESTAMP).asLong());
                } catch (Exception e) {
                    throw new ShouldNeverHappenException(e);
                }
            default:
                throw new ShouldNeverHappenException("Unsupported data type: " + dataTypeId);
        }
    }
    
    /**
     * Generate a JsonNode from the query
     * @param info
     * @param voMap
     * @return
     * @throws IOException
     */
    protected JsonNode generateOutput(ZonedDateTimeStatisticsQueryInfo info, Map<Integer, DataPointVO> voMap) throws IOException {
        MultiPointStatisticsStream stream = new MultiPointStatisticsStream(info, voMap, Common.databaseProxy.newPointValueDao());
        
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        JsonGenerator jgen = factory.createGenerator(output);
        PointValueTimeWriter writer = new PointValueTimeJsonWriter(stream.getQueryInfo(), jgen);
        stream.setContentType(StreamContentType.JSON);
        stream.start(writer);
        stream.streamData(writer);
        stream.finish(writer);
        jgen.flush();
        
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(output.toString(Common.UTF8));
    }
    
    protected MockDataSourceVO createDataSource() {
        MockDataSourceVO vo = new MockDataSourceVO();
        vo.setXid(DataSourceDao.getInstance().generateUniqueXid());
        vo.setName("Test DS");
        DataSourceDao.getInstance().save(vo);
        return vo;
    }

    protected DataPointVO createDataPoint(int dataSourceId, int dataType, int defaultCacheSize) {
        DataPointVO vo = new DataPointVO();
        vo.setPointLocator(new MockPointLocatorVO(dataType, true));
        vo.setXid(DataPointDao.getInstance().generateUniqueXid());
        vo.setName("Test point");
        vo.setLoggingType(LoggingTypes.ALL);
        vo.setDataSourceId(dataSourceId);
        vo.setDefaultCacheSize(defaultCacheSize);
        //TODO initial cache size
        DataPointDao.getInstance().save(vo);
        return vo;
    }
    
    class DataPointWrapper<T extends StatisticsGenerator> {
        MockDataSourceVO dsVo;
        DataPointVO vo;
        DataPointRT rt;
        PointValueTime initialValue;
        PointValueTime current;
        List<PointValueTime> values;
        Function<DataValue, DataValue> nextValue;
        StatisticsCreator<T> creator;
        StatisticsVerifier<T> verifyResults;
        
        public DataPointWrapper(MockDataSourceVO dsVo, DataPointVO vo, PointValueTime initial,
                Function<DataValue, DataValue> nextValue,
                StatisticsCreator<T> creator,
                StatisticsVerifier<T> verify) {
            this.dsVo = dsVo;
            this.vo = vo;
            this.initialValue = initial;
            this.rt = new DataPointRT(vo, vo.getPointLocator().createRuntime(), dsVo, null, timer);
            runtimeManager.points.add(this.rt);
            this.nextValue = nextValue;
            this.creator = creator;
            this.verifyResults = verify;
            this.values = new ArrayList<>();
            
            if(initialValue != null) {
                this.rt.updatePointValue(initialValue, false);
                this.current = initialValue;
            }
        }
        
        void saveOnlyToCache(PointValueTime pvt) {
            rt.savePointValueDirectToCache(pvt, null, false, false, FireEvents.NEVER);
            values.add(pvt);
            current = pvt;
        }
        
        DataValue getNextValue() {
            return nextValue.apply(current.getValue());
        }
        
        void updatePointValue(PointValueTime pvt) {
            rt.updatePointValue(pvt, false);
            values.add(pvt);
            current = pvt;
        }

        void verify(ZonedDateTimeStatisticsQueryInfo info, JsonNode root) {
            T stats = creator.create(info, this);
            verifyResults.verify(this, stats, root);
        }
    }

    interface StatisticsCreator<T extends StatisticsGenerator> {
        T create(ZonedDateTimeStatisticsQueryInfo info, DataPointWrapper<T> w);
    }
    interface StatisticsVerifier<T extends StatisticsGenerator> {
        void verify(DataPointWrapper<T> w, T generator, JsonNode root);
    }
    
    class ValueChangeCounterVerifier implements StatisticsVerifier<ValueChangeCounter> {

        /* (non-Javadoc)
         * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointStatisticsStreamTest.StatisticsVerifier#verify(com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointStatisticsStreamTest.DataPointWrapper, com.serotonin.m2m2.view.stats.StatisticsGenerator, com.fasterxml.jackson.databind.JsonNode)
         */
        @Override
        public void verify(DataPointWrapper<ValueChangeCounter> w, ValueChangeCounter gen,
                JsonNode root) {
            JsonNode stats = root.get(w.vo.getXid());
            if(stats == null)
                fail("Missing stats for point " + w.vo.getXid());
            
            JsonNode stat = stats.get(PointValueTimeWriter.START);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.START + " entry");
            PointValueTime value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getStartValue(), value.getValue());
            assertEquals((long)gen.getPeriodStartTime(), value.getTime());

            stat = stats.get(PointValueTimeWriter.FIRST);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.FIRST + " entry");
            value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getFirstValue(), value.getValue());
            assertEquals((long)gen.getFirstTime(), value.getTime());
            
            stat = stats.get(PointValueTimeWriter.LAST);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.LAST + " entry");
            value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getLastValue(), value.getValue() );
            assertEquals((long)gen.getLastTime(), value.getTime());
            
            stat = stats.get(PointValueTimeWriter.COUNT);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.COUNT + " entry");
            assertEquals(gen.getCount(), stat.asInt());
        }
    }
    
    class StartsAndRuntimeListVerifier implements StatisticsVerifier<StartsAndRuntimeList> {

        /* (non-Javadoc)
         * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointStatisticsStreamTest.StatisticsVerifier#verify(com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointStatisticsStreamTest.DataPointWrapper, com.serotonin.m2m2.view.stats.StatisticsGenerator, com.fasterxml.jackson.databind.JsonNode)
         */
        @Override
        public void verify(DataPointWrapper<StartsAndRuntimeList> w, StartsAndRuntimeList gen,
                JsonNode root) {
            JsonNode stats = root.get(w.vo.getXid());
            if(stats == null)
                fail("Missing stats for point " + w.vo.getXid());
            
            JsonNode stat = stats.get(PointValueTimeWriter.START);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.START + " entry");
            PointValueTime value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getStartValue(), value.getValue());
            assertEquals((long)gen.getPeriodStartTime(), value.getTime());

            stat = stats.get(PointValueTimeWriter.FIRST);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.FIRST + " entry");
            value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getFirstValue(), value.getValue());
            assertEquals((long)gen.getFirstTime(), value.getTime());
            
            stat = stats.get(PointValueTimeWriter.LAST);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.LAST + " entry");
            value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getLastValue(), value.getValue() );
            assertEquals((long)gen.getLastTime(), value.getTime());
            
            stat = stats.get(PointValueTimeWriter.COUNT);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.COUNT + " entry");
            assertEquals(gen.getCount(), stat.asInt());
            
            //Test data
            stat = stats.get(PointValueTimeWriter.STARTS_AND_RUNTIMES);
            if(stat == null)
                fail("Missing data entry");
            
            for(int i=0; i<gen.getData().size(); i++) {
                StartsAndRuntime expected = gen.getData().get(i);
                JsonNode actual = stat.get(i);
                assertEquals((int)expected.getValue(), actual.get(PointValueTimeWriter.VALUE).intValue());
                assertEquals(expected.getStarts(), actual.get(PointValueTimeWriter.STARTS).intValue());
                assertEquals(expected.getRuntime(), actual.get(PointValueTimeWriter.RUNTIME).asLong());
                assertEquals(expected.getProportion(), actual.get(PointValueTimeWriter.PROPORTION).doubleValue(), 0.000001);
            }
            
        }
        
    }
    class AnalogStatisticsVerifier implements StatisticsVerifier<AnalogStatistics> {

        /* (non-Javadoc)
         * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointStatisticsStreamTest.StatisticsVerifier#verify(com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointStatisticsStreamTest.DataPointWrapper, com.serotonin.m2m2.view.stats.StatisticsGenerator, com.fasterxml.jackson.databind.JsonNode)
         */
        @Override
        public void verify(DataPointWrapper<AnalogStatistics> w, AnalogStatistics gen,
                JsonNode root) {
            JsonNode stats = root.get(w.vo.getXid());
            if(stats == null)
                fail("Missing stats for point " + w.vo.getXid());
            
            JsonNode stat = stats.get(PointValueTimeWriter.START);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.START + " entry");
            PointValueTime value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getStartValue(), value.getDoubleValue(), 0.00001);
            assertEquals((long)gen.getPeriodStartTime(), value.getTime());

            stat = stats.get(PointValueTimeWriter.FIRST);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.FIRST + " entry");
            value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getFirstValue(), value.getValue().getDoubleValue(), 0.00001);
            assertEquals((long)gen.getFirstTime(), value.getTime());
            
            stat = stats.get(PointValueTimeWriter.LAST);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.LAST + " entry");
            value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getLastValue(), value.getValue().getDoubleValue(), 0.00001);
            assertEquals((long)gen.getLastTime(), value.getTime());
            
            stat = stats.get(PointValueTimeWriter.COUNT);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.COUNT + " entry");
            assertEquals(gen.getCount(), stat.asInt());
            
            stat = stats.get(PointValueTimeWriter.ACCUMULATOR);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.ACCUMULATOR + " entry");
            value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            Double accumulatorValue = gen.getLastValue();
            if(accumulatorValue == null)
                accumulatorValue = gen.getMaximumValue();
            assertEquals(accumulatorValue, value.getDoubleValue(), 0.00001);
            
            stat = stats.get(PointValueTimeWriter.AVERAGE);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.AVERAGE + " entry");
            value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getAverage(), value.getDoubleValue(), 0.00001);

            stat = stats.get(PointValueTimeWriter.DELTA);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.DELTA + " entry");
            value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getDelta(), value.getDoubleValue(), 0.00001);
            
            
            stat = stats.get(PointValueTimeWriter.MINIMUM);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.MINIMUM + " entry");
            value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getMinimumValue(), value.getValue().getDoubleValue(), 0.00001);
            assertEquals((long)gen.getMinimumTime(), value.getTime());

            stat = stats.get(PointValueTimeWriter.MAXIMUM);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.MAXIMUM + " entry");
            value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getMaximumValue(), value.getValue().getDoubleValue(), 0.00001);
            assertEquals((long)gen.getMaximumTime(), value.getTime());
            
            stat = stats.get(PointValueTimeWriter.SUM);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.SUM + " entry");
            value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getSum(), value.getDoubleValue(), 0.00001);
            
            stat = stats.get(PointValueTimeWriter.INTEGRAL);
            if(stat == null)
                fail("Missing " + PointValueTimeWriter.INTEGRAL + " entry");
            value = getPointValueTime(w.vo.getPointLocator().getDataTypeId(), stat);
            assertEquals(gen.getIntegral(), value.getDoubleValue(), 0.00001);
        }
        
    }
    
    /**
     * Create a random square image with w=h=size
     * @param size
     * @return
     */
    private byte[] createImageBytes(int size) {
        int width = size;
        int height = size;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // create random image pixel by pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int a = (int) (Math.random() * 256); // alpha
                int r = (int) (Math.random() * 256); // red
                int g = (int) (Math.random() * 256); // green
                int b = (int) (Math.random() * 256); // blue

                int p = (a << 24) | (r << 16) | (g << 8) | b; // pixel

                image.setRGB(x, y, p);
            }
        }
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            ImageIO.write(image, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.MangoTestBase#getLifecycle()
     */
    @Override
    protected MockMangoLifecycle getLifecycle() {
        return new TestLifecycle(modules, enableH2Web, h2WebPort, runtimeManager);
    }
    
    class TestLifecycle extends MockMangoLifecycle {

        /**
         * @param modules
         * @param enableWebConsole
         * @param webPort
         */
        public TestLifecycle(List<Module> modules, boolean enableWebConsole, int webPort, TestRuntimeManager runtimeManager) {
            super(modules, enableWebConsole, webPort);
            this.runtimeManager = runtimeManager;
        }
 
    }
    
    static class TestRuntimeManager extends MockRuntimeManager {
        
        List<DataPointRT> points = new ArrayList<>();
        
        public DataPointRT getDataPoint(int dataPointId) {
            for(DataPointRT rt : points) {
                if(rt.getVO().getId() == dataPointId)
                    return rt;
            }
            return null;
        };
        
        
    }
}
