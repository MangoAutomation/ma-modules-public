/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import static com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infiniteautomation.mango.pointvaluecache.PointValueCache;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.rest.latest.streamingvalues.mapper.AggregateValueMapper;
import com.infiniteautomation.mango.rest.latest.streamingvalues.mapper.StreamMapperBuilder;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
import com.infiniteautomation.mango.statistics.StartsAndRuntime;
import com.infiniteautomation.mango.statistics.StartsAndRuntimeList;
import com.infiniteautomation.mango.statistics.ValueChangeCounter;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.MangoTestBase;
import com.serotonin.m2m2.MockMangoLifecycle;
import com.serotonin.m2m2.MockRuntimeManager;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.module.Module;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.DataPointVO.LoggingTypes;
import com.serotonin.m2m2.vo.dataPoint.DataPointWithEventDetectors;
import com.serotonin.m2m2.vo.dataPoint.MockPointLocatorVO;
import com.serotonin.m2m2.vo.dataSource.mock.MockDataSourceVO;

/**
 * @author Terry Packer
 */
public class MultiPointStatisticsStreamTest extends MangoTestBase {

    public final static String TIMESTAMP = "timestamp";
    public final static String VALUE = "value";
    public final static String INTEGRAL = "integral";

    public static final String FIRST = "first";
    public static final String LAST = "last";
    public static final String START = "start";
    public static final String COUNT = "count";

    public static final String ACCUMULATOR = "accumulator";
    public static final String DELTA = "delta";
    public static final String AVERAGE = "average";
    public static final String MAXIMUM = "maximum";
    public static final String MINIMUM = "minimum";
    public static final String SUM = "sum";

    public static final String STARTS = "starts";
    public static final String RUNTIME = "runtime";
    public static final String PROPORTION = "proportion";
    public static final String STARTS_AND_RUNTIMES = "startsAndRuntimes";

    protected static final TestRuntimeManager runtimeManager = new TestRuntimeManager();
    protected final ZoneId zoneId;
    protected ObjectMapper mapper;

    //TODO Test initial values and no initial values
    protected PointValueDao pointValueDao;
    public MultiPointStatisticsStreamTest() {
        this.zoneId = ZoneId.systemDefault();
    }

    @BeforeClass
    public static void setup() {
        // load module element definitions
        loadModules();
    }

    @Override
    public void before() {
        super.before();
        ApplicationContext context = lifecycle.getRuntimeContext();
        mapper = context.getBean(REST_OBJECT_MAPPER_NAME, ObjectMapper.class);
        pointValueDao = context.getBean(PointValueDao.class);
    }

    @Override
    public void after() {
        super.after();
        runtimeManager.points.clear();
    }

    private Map<String, StreamingPointValueTimeModel> getStatistics(Collection<? extends DataPointVO> points,
                                                                    ZonedDateTime from, ZonedDateTime to,
                                                                    String timezone,
                                                                    PointValueField[] fields) {

        // AbstractStreamMapper stores itself in a request attribute for retrieval inside HttpMessageConverter
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        var mapper = new StreamMapperBuilder()
                .withDataPoints(points)
                .withRollup(RollupEnum.ALL)
                .withFields(fields)
                .withTimezone(timezone, from, to)
                .build(AggregateValueMapper::new);

        return points.stream().collect(Collectors.toMap(DataPointVO::getXid, point ->
                pointValueDao.getAggregateDao(Duration.between(from, to))
                        .query(point, from, to, null).map(mapper)
                        .findAny()
                        .orElseThrow()
        ));
    }

    @Test
    public void testSingleAlphanumericPointNoCacheNoChangeInitialValue() {

        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 2, 1, 0, 0, 0, 0, zoneId);
        Duration adjuster = Duration.ofDays(1);

        MockDataSourceVO ds = createDataSource();
        DataPointVO dp = createDataPoint(ds.getId(), DataType.ALPHANUMERIC, 1);

        DataPointWrapper<ValueChangeCounter> point = new DataPointWrapper<>(ds, dp,
                new PointValueTime("TESTING", 0),
                Function.identity(), // no change
                (w) -> new ValueChangeCounter(from.toInstant().toEpochMilli(), to.toInstant().toEpochMilli(), w.initialValue, w.values),
                new ValueChangeCounterVerifier());

        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = from.plus(adjuster);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while (time.toInstant().isBefore(to.toInstant())) {
            point.updatePointValue(new PointValueTime(point.getNextValue(), time.toInstant().toEpochMilli()));
            time = time.plus(adjuster);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }

        //Perform the query
        String timezone = zoneId.getId();
        PointValueField[] fields = getFields();

        var result = getStatistics(List.of(dp), from, to, timezone, fields);
        test(result, point);
    }

    /**
     * Start with a value of 1 at time 0
     * Then insert a value of 1 at midnight every day during Jan 2017
     */
    @Test
    public void testSingleMultistatePointNoCacheNoChangeInitialValue() {

        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 2, 1, 0, 0, 0, 0, zoneId);
        Duration adjuster = Duration.ofDays(1);

        MockDataSourceVO ds = createDataSource();
        DataPointVO dp = createDataPoint(ds.getId(), DataType.MULTISTATE, 1);

        DataPointWrapper<StartsAndRuntimeList> point = new DataPointWrapper<>(ds, dp,
                new PointValueTime(1, 0),
                Function.identity(), // no change
                (w) -> new StartsAndRuntimeList(from.toInstant().toEpochMilli(), to.toInstant().toEpochMilli(), w.initialValue, w.values),
                new StartsAndRuntimeListVerifier());

        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = from.plus(adjuster);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while (time.toInstant().isBefore(to.toInstant())) {
            point.updatePointValue(new PointValueTime(point.getNextValue(), time.toInstant().toEpochMilli()));
            time = time.plus(adjuster);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }

        //Perform the query
        String timezone = zoneId.getId();
        PointValueField[] fields = getFields();

        var result = getStatistics(List.of(dp), from, to, timezone, fields);
        test(result, point);
    }

    @Test
    public void testSingleNumericPointNoCacheNoChangeInitialValue() {

        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 2, 1, 0, 0, 0, 0, zoneId);
        Duration adjuster = Duration.ofDays(1);

        MockDataSourceVO ds = createDataSource();
        DataPointVO dp = createDataPoint(ds.getId(), DataType.NUMERIC, 1);

        DataPointWrapper<AnalogStatistics> wrapper = new DataPointWrapper<>(ds, dp,
                new PointValueTime(1.0, 0),
                Function.identity(), // no change
                (w) -> new AnalogStatistics(from.toInstant().toEpochMilli(), to.toInstant().toEpochMilli(), w.initialValue, w.values),
                new AnalogStatisticsVerifier());

        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = from.plus(adjuster);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while (time.toInstant().isBefore(to.toInstant())) {
            wrapper.updatePointValue(new PointValueTime(wrapper.getNextValue(), time.toInstant().toEpochMilli()));
            time = time.plus(adjuster);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }

        //Perform the query
        String timezone = zoneId.getId();
        PointValueField[] fields = getFields();

        var result = getStatistics(List.of(dp), from, to, timezone, fields);
        test(result, wrapper);
    }

    @Test
    public void testSingleNumericPointNoCacheChangeInitialValue() {

        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 2, 1, 0, 0, 0, 0, zoneId);
        Duration adjuster = Duration.ofDays(1);

        MockDataSourceVO ds = createDataSource();
        DataPointVO dp = createDataPoint(ds.getId(), DataType.NUMERIC, 1);

        DataPointWrapper<AnalogStatistics> wrapper = new DataPointWrapper<>(ds, dp,
                new PointValueTime(1.0, 0),
                (value) -> new NumericValue(value.getDoubleValue() + 1.0),
                (w) -> new AnalogStatistics(from.toInstant().toEpochMilli(), to.toInstant().toEpochMilli(), w.initialValue, w.values),
                new AnalogStatisticsVerifier());

        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = from.plus(adjuster);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while (time.toInstant().isBefore(to.toInstant())) {
            wrapper.updatePointValue(new PointValueTime(wrapper.getNextValue(), time.toInstant().toEpochMilli()));
            time = time.plus(adjuster);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }

        //Perform the query
        String timezone = zoneId.getId();
        PointValueField[] fields = getFields();

        var result = getStatistics(List.of(dp), from, to, timezone, fields);
        test(result, wrapper);
    }

    @Test
    public void testMultiplePointsNoCacheChangeInitialValue() {

        //Setup the data to run once daily for 30 days
        ZonedDateTime from = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, zoneId);
        ZonedDateTime to = ZonedDateTime.of(2017, 2, 1, 0, 0, 0, 0, zoneId);
        Duration adjuster = Duration.ofDays(1);

        MockDataSourceVO ds = createDataSource();
        DataPointVO numericDp = createDataPoint(ds.getId(), DataType.NUMERIC, 1);

        DataPointWrapper<AnalogStatistics> numericWrapper = new DataPointWrapper<>(ds, numericDp,
                new PointValueTime(1.0, 0),
                (value) -> new NumericValue(value.getDoubleValue() + 1.0),
                (w) -> new AnalogStatistics(from.toInstant().toEpochMilli(), to.toInstant().toEpochMilli(), w.initialValue, w.values),
                new AnalogStatisticsVerifier());

        DataPointVO multistateDp = createDataPoint(ds.getId(), DataType.MULTISTATE, 1);

        DataPointWrapper<StartsAndRuntimeList> multistateWrapper = new DataPointWrapper<>(ds, multistateDp,
                new PointValueTime(1, 0),
                (value) -> new MultistateValue(value.getIntegerValue() + 1),
                (w) -> new StartsAndRuntimeList(from.toInstant().toEpochMilli(), to.toInstant().toEpochMilli(), w.initialValue, w.values),
                new StartsAndRuntimeListVerifier());


        //Insert the data skipping first day so we get the initial value
        ZonedDateTime time = from.plus(adjuster);
        timer.setStartTime(time.toInstant().toEpochMilli());

        while (time.toInstant().isBefore(to.toInstant())) {
            numericWrapper.updatePointValue(new PointValueTime(numericWrapper.getNextValue(), time.toInstant().toEpochMilli()));
            multistateWrapper.updatePointValue(new PointValueTime(multistateWrapper.getNextValue(), time.toInstant().toEpochMilli()));
            time = time.plus(adjuster);
            timer.fastForwardTo(time.toInstant().toEpochMilli());
        }

        //Perform the query
        String timezone = zoneId.getId();
        PointValueField[] fields = getFields();

        var result = getStatistics(List.of(numericDp, multistateDp), from, to, timezone, fields);
        test(result, numericWrapper, multistateWrapper);
    }

    protected void test(Object result, DataPointWrapper<?>... points) {
        JsonNode root = generateOutput(result);
        for (DataPointWrapper<?> wrapper : points) {
            wrapper.verify(root);
        }
    }

    protected PointValueField[] getFields() {
        return PointValueField.values();
    }

    protected PointValueTime getPointValueTime(DataType dataType, JsonNode stat) {
        if (stat.isNull()) {
            return null;
        }
        assertNotNull(stat.get(TIMESTAMP));
        assertNotNull(stat.get(VALUE));
        switch (dataType) {
            case MULTISTATE:
                return new PointValueTime(stat.get(VALUE).asInt(), stat.get(TIMESTAMP).asLong());
            case NUMERIC:
                return new PointValueTime(stat.get(VALUE).asDouble(), stat.get(TIMESTAMP).asLong());
            case ALPHANUMERIC:
                return new PointValueTime(stat.get(VALUE).asText(), stat.get(TIMESTAMP).asLong());
            default:
                throw new ShouldNeverHappenException("Unsupported data type: " + dataType);
        }
    }

    /**
     * Generate a JsonNode from the query
     */
    protected JsonNode generateOutput(Object result) {
        return mapper.valueToTree(result);
    }

    protected MockDataSourceVO createDataSource() {
        MockDataSourceVO vo = new MockDataSourceVO();
        vo.setXid(DataSourceDao.getInstance().generateUniqueXid());
        vo.setName("Test DS");
        DataSourceDao.getInstance().insert(vo);
        return vo;
    }

    protected DataPointVO createDataPoint(int dataSourceId, DataType dataType, int defaultCacheSize) {
        DataPointVO vo = new DataPointVO();
        vo.setPointLocator(new MockPointLocatorVO(dataType, true));
        vo.setXid(DataPointDao.getInstance().generateUniqueXid());
        vo.setName("Test point");
        vo.setLoggingType(LoggingTypes.ALL);
        vo.setDataSourceId(dataSourceId);
        vo.setDefaultCacheSize(defaultCacheSize);
        //TODO initial cache size
        DataPointDao.getInstance().insert(vo);
        return vo;
    }

    @Override
    protected MockMangoLifecycle getLifecycle() {
        return new TestLifecycle(modules, runtimeManager);
    }

    interface StatisticsExpectedResultCreator<T extends StatisticsGenerator> {
        T create(DataPointWrapper<T> w);
    }

    interface StatisticsVerifier<T extends StatisticsGenerator> {
        void verify(DataPointVO point, T expectedResult, JsonNode root);
    }

    static class TestLifecycle extends MockMangoLifecycle {
        public TestLifecycle(List<Module> modules, TestRuntimeManager runtimeManager) {
            super(modules);
            this.runtimeManager = runtimeManager;
        }
    }

    static class TestRuntimeManager extends MockRuntimeManager {
        final List<DataPointRT> points = new ArrayList<>();

        @Override
        public DataPointRT getDataPoint(int dataPointId) {
            for (DataPointRT rt : points) {
                if (rt.getVO().getId() == dataPointId)
                    return rt;
            }
            return null;
        }
    }

    class DataPointWrapper<T extends StatisticsGenerator> {
        final DataPointVO vo;
        final DataPointRT rt;
        final PointValueTime initialValue;
        final List<PointValueTime> values;
        final Function<DataValue, DataValue> nextValue;
        final StatisticsExpectedResultCreator<T> expectedResultCreator;
        final StatisticsVerifier<T> verifier;
        PointValueTime current;

        public DataPointWrapper(MockDataSourceVO dsVo, DataPointVO vo, PointValueTime initial,
                                Function<DataValue, DataValue> nextValue,
                                StatisticsExpectedResultCreator<T> expectedResultCreator,
                                StatisticsVerifier<T> verifier) {
            this.vo = vo;
            this.initialValue = initial;
            DataPointWithEventDetectors dp = new DataPointWithEventDetectors(vo, new ArrayList<>());
            this.rt = new DataPointRT(dp, vo.getPointLocator().createRuntime(), dsVo.createDataSourceRT(), null,
                    Common.getBean(PointValueDao.class), Common.getBean(PointValueCache.class),
                    timer);
            runtimeManager.points.add(this.rt);
            this.nextValue = nextValue;
            this.expectedResultCreator = expectedResultCreator;
            this.verifier = verifier;
            this.values = new ArrayList<>();

            if (initialValue != null) {
                this.rt.updatePointValue(initialValue, false);
                this.current = initialValue;
            }
        }

        DataValue getNextValue() {
            return nextValue.apply(current.getValue());
        }

        void updatePointValue(PointValueTime pvt) {
            rt.updatePointValue(pvt, false);
            values.add(pvt);
            current = pvt;
        }

        void verify(JsonNode root) {
            T expectedResult = expectedResultCreator.create(this);
            verifier.verify(vo, expectedResult, root);
        }
    }

    class ValueChangeCounterVerifier implements StatisticsVerifier<ValueChangeCounter> {

        @Override
        public void verify(DataPointVO point, ValueChangeCounter expectedResult, JsonNode root) {
            JsonNode stats = root.get(point.getXid());
            if (stats == null)
                fail("Missing stats for point " + point.getXid());

            long timestamp = stats.get(TIMESTAMP).asLong();
            assertEquals(expectedResult.getPeriodStartTime(), timestamp);

            JsonNode stat = stats.get(START);
            if (stat == null)
                fail("Missing " + START + " entry");
            PointValueTime value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getStartValue(), value.getValue());
            assertEquals(expectedResult.getPeriodStartTime(), value.getTime());

            stat = stats.get(FIRST);
            if (stat == null)
                fail("Missing " + FIRST + " entry");
            value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getFirstValue(), value.getValue());
            assertEquals((long) expectedResult.getFirstTime(), value.getTime());

            stat = stats.get(LAST);
            if (stat == null)
                fail("Missing " + LAST + " entry");
            value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getLastValue(), value.getValue());
            assertEquals((long) expectedResult.getLastTime(), value.getTime());

            stat = stats.get(COUNT);
            if (stat == null)
                fail("Missing " + COUNT + " entry");
            assertEquals(expectedResult.getCount(), stat.asInt());
        }
    }

    class StartsAndRuntimeListVerifier implements StatisticsVerifier<StartsAndRuntimeList> {

        @Override
        public void verify(DataPointVO point, StartsAndRuntimeList expectedResult, JsonNode root) {
            JsonNode stats = root.get(point.getXid());
            if (stats == null)
                fail("Missing stats for point " + point.getXid());

            long timestamp = stats.get(TIMESTAMP).asLong();
            assertEquals(expectedResult.getPeriodStartTime(), timestamp);

            JsonNode stat = stats.get(START);
            if (stat == null)
                fail("Missing " + START + " entry");
            PointValueTime value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getStartValue(), value.getValue());
            assertEquals(expectedResult.getPeriodStartTime(), value.getTime());

            stat = stats.get(FIRST);
            if (stat == null)
                fail("Missing " + FIRST + " entry");
            value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getFirstValue(), value.getValue());
            assertEquals((long) expectedResult.getFirstTime(), value.getTime());

            stat = stats.get(LAST);
            if (stat == null)
                fail("Missing " + LAST + " entry");
            value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getLastValue(), value.getValue());
            assertEquals((long) expectedResult.getLastTime(), value.getTime());

            stat = stats.get(COUNT);
            if (stat == null)
                fail("Missing " + COUNT + " entry");
            assertEquals(expectedResult.getCount(), stat.asInt());

            //Test data
            stat = stats.get(STARTS_AND_RUNTIMES);
            if (stat == null)
                fail("Missing data entry");

            for (int i = 0; i < expectedResult.getData().size(); i++) {
                StartsAndRuntime expected = expectedResult.getData().get(i);
                JsonNode actual = stat.get(i);
                assertEquals((int) expected.getValue(), actual.get(VALUE).intValue());
                assertEquals(expected.getStarts(), actual.get(STARTS).intValue());
                assertEquals(expected.getRuntime(), actual.get(RUNTIME).asLong());
                assertEquals(expected.getProportion(), actual.get(PROPORTION).doubleValue(), 0.000001);
            }

        }

    }

    class AnalogStatisticsVerifier implements StatisticsVerifier<AnalogStatistics> {

        @Override
        public void verify(DataPointVO point, AnalogStatistics expectedResult, JsonNode root) {
            JsonNode stats = root.get(point.getXid());
            if (stats == null)
                fail("Missing stats for point " + point.getXid());

            long timestamp = stats.get(TIMESTAMP).asLong();
            assertEquals(expectedResult.getPeriodStartTime(), timestamp);

            JsonNode stat = stats.get(START);
            if (stat == null)
                fail("Missing " + START + " entry");
            PointValueTime value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getStartValue().getDoubleValue(), value.getDoubleValue(), 0.00001);
            assertEquals(expectedResult.getPeriodStartTime(), value.getTime());

            stat = stats.get(FIRST);
            if (stat == null)
                fail("Missing " + FIRST + " entry");
            value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getFirstValue().getDoubleValue(), value.getValue().getDoubleValue(), 0.00001);
            assertEquals((long) expectedResult.getFirstTime(), value.getTime());

            stat = stats.get(LAST);
            if (stat == null)
                fail("Missing " + LAST + " entry");
            value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getLastValue().getDoubleValue(), value.getValue().getDoubleValue(), 0.00001);
            assertEquals((long) expectedResult.getLastTime(), value.getTime());

            stat = stats.get(COUNT);
            if (stat == null)
                fail("Missing " + COUNT + " entry");
            assertEquals(expectedResult.getCount(), stat.asLong());

            stat = stats.get(ACCUMULATOR);
            if (stat == null)
                fail("Missing " + ACCUMULATOR + " entry");
            value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            Double accumulatorValue;
            if (expectedResult.getLastValue() == null)
                accumulatorValue = expectedResult.getMaximumValue();
            else
                accumulatorValue = expectedResult.getLastValue().getDoubleValue();
            assertEquals(accumulatorValue, value.getDoubleValue(), 0.00001);

            stat = stats.get(AVERAGE);
            if (stat == null)
                fail("Missing " + AVERAGE + " entry");
            value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getAverage(), value.getDoubleValue(), 0.00001);

            stat = stats.get(DELTA);
            if (stat == null)
                fail("Missing " + DELTA + " entry");
            value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getDelta(), value.getDoubleValue(), 0.00001);


            stat = stats.get(MINIMUM);
            if (stat == null)
                fail("Missing " + MINIMUM + " entry");
            value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getMinimumValue(), value.getValue().getDoubleValue(), 0.00001);
            assertEquals((long) expectedResult.getMinimumTime(), value.getTime());

            stat = stats.get(MAXIMUM);
            if (stat == null)
                fail("Missing " + MAXIMUM + " entry");
            value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getMaximumValue(), value.getValue().getDoubleValue(), 0.00001);
            assertEquals((long) expectedResult.getMaximumTime(), value.getTime());

            stat = stats.get(SUM);
            if (stat == null)
                fail("Missing " + SUM + " entry");
            value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getSum(), value.getDoubleValue(), 0.00001);

            stat = stats.get(INTEGRAL);
            if (stat == null)
                fail("Missing " + INTEGRAL + " entry");
            value = getPointValueTime(point.getPointLocator().getDataType(), stat);
            assertEquals(expectedResult.getIntegral(), value.getDoubleValue(), 0.00001);
        }

    }
}
