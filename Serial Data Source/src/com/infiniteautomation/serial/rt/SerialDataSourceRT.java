package com.infiniteautomation.serial.rt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.infiniteautomation.mango.io.serial.SerialPortException;
import com.infiniteautomation.mango.io.serial.SerialPortProxy;
import com.infiniteautomation.mango.io.serial.SerialPortProxyEvent;
import com.infiniteautomation.mango.io.serial.SerialPortProxyEventListener;
import com.infiniteautomation.mango.regex.MatchCallback;
import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.io.StreamUtils;
import com.serotonin.log.RollingIOLog;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.rt.dataSource.EventDataSource;
import com.serotonin.m2m2.util.timeout.TimeoutClient;
import com.serotonin.m2m2.util.timeout.TimeoutTask;
import com.serotonin.util.ILifecycleState;
import com.serotonin.util.queue.ByteQueue;

public class SerialDataSourceRT extends EventDataSource<SerialDataSourceVO> implements SerialPortProxyEventListener{
    private final Log LOG = LogFactory.getLog(SerialDataSourceRT.class);
    public static final int POINT_READ_EXCEPTION_EVENT = 1;
    public static final int POINT_WRITE_EXCEPTION_EVENT = 2;
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 3;
    public static final int POINT_READ_PATTERN_MISMATCH_EVENT = 4;

    private static final String HEX_REGEX = "^[0-9A-Fa-f]*$";

    private SerialPortProxy port; //Serial Communication Port
    private ByteQueue buffer; //Max size is Max Message Size
    private TimeoutTask timeoutTask; //Task to retrieve buffer contents after timeout

    private RollingIOLog ioLog;

    public SerialDataSourceRT(SerialDataSourceVO vo) {
        super(vo);
        buffer = new ByteQueue(vo.getMaxMessageSize());
    }


    /**
     * Connect to a serial port
     * @param portName
     * @throws Exception
     */
    public boolean connect() {

        if (Common.serialPortManager.portOwned(vo.getCommPortId())){
            raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.portInUse",vo.getCommPortId()));
            return false;
        }else if(getLifecycleState() != ILifecycleState.RUNNING)
            return false;
        else{
            try{
                this.port = Common.serialPortManager.open(
                        "Mango Serial Data Source",
                        vo.getCommPortId(),
                        vo.getBaudRate(),
                        vo.getFlowControlIn(),
                        vo.getFlowControlOut(),
                        vo.getDataBits(),
                        vo.getStopBits(),
                        vo.getParity());
                this.port.addEventListener(this);
                return true;

            }catch(Exception e){
                raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.portError",vo.getCommPortId(),e.getLocalizedMessage()));
                return false;
            }
        }

    }

    @Override
    public void initialize() {
        boolean connected = false;
        try{
            if (this.vo.isLogIO()) {
                //PrintWriter log = new PrintWriter(new FileWriter(file, true));
                int fileSize = (int)(vo.getIoLogFileSizeMBytes() * 1000000f);
                int maxFiles = vo.getMaxHistoricalIOLogs();

                ioLog = new RollingIOLog(getIOLogFileName(vo.getId()), Common.getLogsDir(), fileSize, maxFiles);
                ioLog.log("Data source started");
            }
            connected = this.connect();
        }catch(Exception e){
            LOG.debug("Error while initializing data source", e);
            String msg = e.getMessage();
            if(msg == null){
                msg = "Unknown";
            }
            raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.connectFailed",msg));

        }

        if(connected){
            returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis());
        }
        super.initialize();
    }
    @Override
    public void terminateImpl() {
        if(this.port != null)
            try {
                Common.serialPortManager.close(this.port);
            } catch (SerialPortException e) {
                LOG.debug("Error while closing serial port", e);
                raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.portError",this.port.getCommPortId(),e.getLocalizedMessage()));
            }

        if(this.vo.isLogIO()){
            this.ioLog.log("Data source stopped");
            this.ioLog.close();
        }
    }

    private void setPointValueImplTransport(DataPointRT dataPoint, PointValueTime valueTime) throws IOException {
        OutputStream os = this.port.getOutputStream();
        if(os == null)
            throw new IOException("Port is closed.");

        //Create Message from Message Start
        SerialPointLocatorRT pl = dataPoint.getPointLocator();

        byte[] data;

        if(this.vo.isHex()){
            //Convert to Hex
            try{
                switch(dataPoint.getDataTypeId()){
                    case DataTypes.ALPHANUMERIC:
                        data = convertToHex(valueTime.getStringValue());
                        break;
                    case DataTypes.BINARY:
                        if(valueTime.getBooleanValue())
                            data = convertToHex("00");
                        else
                            data = convertToHex("01");
                        break;
                    case DataTypes.MULTISTATE:
                        String intValue = Integer.toString(valueTime.getIntegerValue());
                        if(intValue.length()%2 != 0)
                            intValue = "0" + intValue;
                        data = convertToHex(intValue);
                        break;
                    case DataTypes.NUMERIC:
                        String numValue = Integer.toString(valueTime.getIntegerValue());
                        if(numValue.length()%2 != 0)
                            numValue = "0" + numValue;
                        data = convertToHex(numValue);
                        break;
                    default:
                        throw new ShouldNeverHappenException("Unsupported data type" + dataPoint.getDataTypeId());
                }
                if(this.vo.isLogIO())
                    this.ioLog.log(false, data);
            }catch(Exception e){
                LOG.error(e.getMessage(),e);
                raiseEvent(POINT_WRITE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.notHex"));
                return;
            }
        }else{
            //Pin the terminator on the end
            String messageTerminator = this.getVo().getMessageTerminator();

            //Do we need to or is it already on the end?
            String identifier = pl.getVo().getPointIdentifier();

            String fullMsg = identifier +  valueTime.getStringValue();
            if(!fullMsg.endsWith(messageTerminator)){
                fullMsg +=  messageTerminator;
            }

            //String output = newValue.getStringValue();
            data = fullMsg.getBytes();
            if(vo.isLogIO())
                this.ioLog.log("O: " + fullMsg);
        }

        for(byte b : data){
            os.write(b);
        }
        os.flush();
    }

    @Override
    public synchronized void setPointValueImpl(DataPointRT dataPoint, PointValueTime valueTime,
            SetPointSource source) {

        //Are we connected?
        if(this.port == null && !connect()){
            raiseEvent(POINT_WRITE_EXCEPTION_EVENT, Common.timer.currentTimeMillis(), true, new TranslatableMessage("event.serial.writeFailedPortNotSetup"));
            return;
        }

        try {
            setPointValueImplTransport(dataPoint, valueTime);
            returnToNormal(POINT_WRITE_EXCEPTION_EVENT, System.currentTimeMillis());
        } catch (IOException e) {
            if(vo.getRetries() == 0)
                LOG.error("Failed setting serial point " + dataPoint.getVO().getExtendedName() + ": " + e.getMessage(), e);
            //Try and reset the connection
            Exception ex = e;
            int retries = vo.getRetries();
            while(retries > 0) {
                try{
                    retries -= 1;
                    if(this.port != null)
                        Common.serialPortManager.close(this.port);

                    if(getLifecycleState() != ILifecycleState.RUNNING)
                        break;
                    else if(this.connect()) {
                        setPointValueImplTransport(dataPoint, valueTime);
                        returnToNormal(POINT_WRITE_EXCEPTION_EVENT, System.currentTimeMillis());
                        return;
                    }
                }catch(Exception e2){
                    ex = e2;
                }
            }

            if(ex != null) {
                raiseEvent(POINT_WRITE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.writeFailed",e.getMessage()));
                LOG.error("Error re-connecting to serial port.", ex);
            }
        }
    }

    @Override
    public void serialEvent(SerialPortProxyEvent evt){
        //Keep a lock on the buffer while we do this
        synchronized(this.buffer){
            //If our port is dead, say so
            if(this.port == null){
                raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.readFailedPortNotSetup"));
                return;
            }

            //String msg = null;
            try{
                //Read the data in from the port
                //Don't read during timeout events as there could be no data and this would block till there is
                if(!(evt instanceof TimeoutSerialEvent)){
                    InputStream in = this.port.getInputStream();
                    int data;
                    //Read in all the data we can from the InputStream
                    // this may not be the full message, or may read multiple messages
                    while (( data = in.read()) > -1 ){
                        if(buffer.size() >= this.vo.getMaxMessageSize()){
                            buffer.popAll();
                            raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.readFailed", "Max message size reached!"));
                            return; //Give up
                        }
                        buffer.push(data);
                    }
                    //Log our buffer contents
                    byte[] logMsg = buffer.peekAll();
                    if(this.vo.isLogIO()) {
                        if(this.vo.isHex())
                            this.ioLog.log(true, logMsg);
                        else
                            this.ioLog.log("I: "+ new String(logMsg, StandardCharsets.UTF_8));
                    }

                    //Setup the timeout task
                    //Serial Event so setup a Timeout Task to fire after the message timeout
                    if(this.buffer.size() > 0) {
                        if(this.timeoutTask != null)
                            this.timeoutTask.cancel();
                        this.timeoutTask = new TimeoutTask(this.vo.getReadTimeout(), new SerialTimeoutClient(this));
                    }
                }


                //We either use a terminator and timeout OR just a Timeout
                if(vo.getUseTerminator()) {

                    //If timeout then process the buffer
                    //If serial event then read input and process buffer

                    String messageRegex = vo.getMessageRegex(); //"!([A-Z0-9]{3,3})([a-zA-Z])(.*);";
                    //DS Information
                    int pointIdentifierIndex = vo.getPointIdentifierIndex();

                    //Create a String so we can use Regex and matching
                    String msg = null;
                    if(this.vo.isHex()){
                        msg = convertFromHex(buffer.peekAll());
                    }else{
                        msg = new String(buffer.peekAll(), StandardCharsets.UTF_8);
                    }

                    //Now we have a string that contains the entire contents of the buffer,
                    // split on terminator, keep it on the end of the message and process any full messages
                    // and pop them from the buffer
                    String[] messages = splitMessages(msg, this.vo.getMessageTerminator());
                    for(String message : messages) {
                        //Does our message contain the terminator?
                        //It should be impossible to have a non-terminated message
                        // that is before a message with a terminator in the buffer
                        // so it is assumed here that popping from the buffer will
                        // not cause any issues. As the only data left in the buffer will
                        // potentially be one incomplete message.
                        if(canProcessTerminatedMessage(message, this.vo.getMessageTerminator())){
                            //Pop off this message
                            this.buffer.pop(message.length());
                            if(LOG.isDebugEnabled())
                                LOG.debug("Matching will use String: " + message);
                            final AtomicBoolean matcherFailed = new AtomicBoolean(false);
                            pointListChangeLock.readLock().lock();
                            try {
                                for(final DataPointRT dp: this.dataPoints){
                                    SerialPointLocatorVO plVo = dp.getVO().getPointLocator();
                                    MatchCallback callback = new MatchCallback(){

                                        @Override
                                        public void onMatch(String pointIdentifier, PointValueTime value) {
                                            if(!updatePointValue(value, dp)){
                                                matcherFailed.set(true);
                                                raiseEvent(POINT_READ_PATTERN_MISMATCH_EVENT,System.currentTimeMillis(), true, new TranslatableMessage("event.serial.invalidValue", dp.getVO().getXid()));
                                            }
                                        }

                                        @Override
                                        public void pointPatternMismatch(String message, String messageRegex) {
                                            //Ignore as this just isn't a message we care about
                                        }

                                        @Override
                                        public void messagePatternMismatch(String message, String messageRegex) {
                                            raiseEvent(POINT_READ_PATTERN_MISMATCH_EVENT,System.currentTimeMillis(), true, new TranslatableMessage("event.serial.patternMismatch",messageRegex, message));
                                            matcherFailed.set(true);
                                        }

                                        @Override
                                        public void pointNotIdentified(String message, String messageRegex, int pointIdentifierIndex) {
                                            //Don't Care
                                        }

                                        @Override
                                        public void matchGeneralFailure(Exception e) {
                                            raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.readFailed", e.getMessage()));
                                            matcherFailed.set(true);
                                        }
                                    };

                                    try{
                                        matchPointValue(message, messageRegex, pointIdentifierIndex, plVo, vo.isHex(), LOG, callback);
                                    }catch(Exception e){
                                        callback.matchGeneralFailure(e);
                                    }
                                }
                            } finally {
                                pointListChangeLock.readLock().unlock();
                            }

                            //Did we have a failure?
                            //If no failures...
                            if(!matcherFailed.get())
                                returnToNormal(POINT_READ_PATTERN_MISMATCH_EVENT, System.currentTimeMillis());
                            returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
                        }

                        if(evt instanceof TimeoutSerialEvent){
                            //Clear the buffer
                            this.buffer.popAll();
                        }else{
                            //Check to see if we have remaining data, if not cancel timeout
                            if(this.buffer.size() == 0)
                                if(this.timeoutTask != null)
                                    this.timeoutTask.cancel();
                        }

                    }
                    return;
                }else{
                    //No Terminator case
                    //Do we have a timeout generated message?
                    if(evt instanceof TimeoutSerialEvent){
                        String msg = null;
                        //We are a timeout event so we have a timeout, pop everything into the message and assume its a message
                        if(this.vo.isHex()){
                            msg = convertFromHex(buffer.popAll());
                        }else{
                            msg = new String(buffer.popAll(), StandardCharsets.UTF_8);
                        }
                        //Just do a match on the Entire Message because we are not using Terminator
                        //String messageRegex = ".*"; //Match everything
                        //int pointIdentifierIndex = 0; //Whole message
                        String messageRegex = vo.getMessageRegex(); //"!([A-Z0-9]{3,3})([a-zA-Z])(.*);";
                        //DS Information
                        int pointIdentifierIndex = vo.getPointIdentifierIndex();

                        if(LOG.isDebugEnabled())
                            LOG.debug("Matching will use String: " + msg);
                        final AtomicBoolean matcherFailed = new AtomicBoolean(false);
                        pointListChangeLock.readLock().lock();
                        try {
                            for(final DataPointRT dp: this.dataPoints){
                                SerialPointLocatorVO plVo = dp.getVO().getPointLocator();
                                MatchCallback callback = new MatchCallback(){

                                    @Override
                                    public void onMatch(String pointIdentifier, PointValueTime pvt) {
                                        if(!updatePointValue(pvt, dp)){
                                            matcherFailed.set(true);
                                            raiseEvent(POINT_READ_PATTERN_MISMATCH_EVENT,System.currentTimeMillis(), true, new TranslatableMessage("event.serial.invalidValue", dp.getVO().getXid()));
                                        }
                                        buffer.popAll(); //Ensure we clear out the buffer...
                                    }

                                    @Override
                                    public void pointPatternMismatch(String message, String messageRegex) {
                                        //Ignore as this just isn't a message we care about
                                    }

                                    @Override
                                    public void messagePatternMismatch(String message, String messageRegex) {
                                        raiseEvent(POINT_READ_PATTERN_MISMATCH_EVENT,System.currentTimeMillis(), true, new TranslatableMessage("event.serial.patternMismatch",messageRegex, message));
                                        matcherFailed.set(true);
                                    }

                                    @Override
                                    public void pointNotIdentified(String message, String messageRegex, int pointIdentifierIndex) {
                                        //Don't Care
                                    }

                                    @Override
                                    public void matchGeneralFailure(Exception e) {
                                        raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.readFailed", e.getMessage()));
                                        matcherFailed.set(true);
                                    }

                                };
                                try{
                                    matchPointValue(msg, messageRegex, pointIdentifierIndex, plVo, vo.isHex(), LOG, callback);
                                }catch(Exception e){
                                    callback.matchGeneralFailure(e);
                                }
                            }
                        } finally {
                            pointListChangeLock.readLock().unlock();
                        }

                        //Did we have a failure?
                        //If no failures...
                        if(!matcherFailed.get())
                            returnToNormal(POINT_READ_PATTERN_MISMATCH_EVENT, System.currentTimeMillis());
                        returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
                    }
                }
            }catch(Exception e){
                LOG.error(e.getMessage(),e);
                this.buffer.popAll(); //Ensure we clear out the buffer...
                raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.readFailed",e.getMessage()));

            }
        }//End synch
    }

    /**
     * Convert to a point value time or NULL if not possible
     * @param value
     * @param dataTypeId
     * @return
     * @throws ConvertHexException
     */
    public static PointValueTime convertToPointValue(String value, int dataTypeId, boolean isHex) throws ConvertHexException{
        //Parse out the value
        DataValue dataValue = null;
        if(isHex){
            byte[] data = convertToHex(value);

            switch(dataTypeId){
                case DataTypes.ALPHANUMERIC:
                    dataValue = new AlphanumericValue(new String(data, StandardCharsets.UTF_8));
                    break;
                case DataTypes.BINARY:
                    if(data.length > 0){
                        dataValue = new BinaryValue((data[0]==1)?true:false);
                    }
                    break;
                case DataTypes.MULTISTATE:
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    if(data.length == 2)
                        dataValue = new MultistateValue(buffer.getShort());
                    else
                        dataValue = new MultistateValue(buffer.getInt());
                    break;
                case DataTypes.NUMERIC:
                    ByteBuffer nBuffer = ByteBuffer.wrap(data);
                    if(data.length == 4)
                        dataValue = new NumericValue(nBuffer.getFloat());
                    else
                        dataValue = new NumericValue(nBuffer.getDouble());
                    break;
                default:
                    throw new ShouldNeverHappenException("Un-supported data type: " + dataTypeId);
            }
        }else{
            dataValue = DataValue.stringToValue(value, dataTypeId);
        }

        if(dataValue != null)
            return new PointValueTime(dataValue,Common.timer.currentTimeMillis());
        else
            return null;
    }

    /**
     * Update a value if possible and return if we did
     * @param pvt
     * @param dp
     * @param dp
     * @return
     */
    private boolean updatePointValue(PointValueTime pvt, DataPointRT dp){
        if(pvt != null){
            dp.updatePointValue(pvt);
            if(LOG.isDebugEnabled())
                LOG.debug("Saving value: " + pvt.toString());
            return true;
        }else{
            return false;
        }
    }

    /**
     * Convert a string value to HEX
     * @param stringValue
     * @return
     */
    public static byte[] convertToHex(String stringValue) throws ConvertHexException {
        int len = stringValue.length();
        if((len&1) == 1)
            throw new ConvertHexException("Odd value lengths not permitted");
        if(!Pattern.matches(HEX_REGEX, stringValue))
            throw new ConvertHexException("Non-hex character detected.");
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(stringValue.charAt(i), 16) << 4)
                    + Character.digit(stringValue.charAt(i+1), 16));
        }

        return data;
    }

    /**
     * Convert a string value to HEX
     * @param hexValue
     * @return
     */
    public  static String convertFromHex(byte[] hexValue) {
        return StreamUtils.dumpHex(hexValue, 0, hexValue.length);
    }

    public static String getIOLogFileName(int dataSourceId) {
        return "serialIO-" +  + dataSourceId + ".log" ;
    }


    class SerialTimeoutClient extends TimeoutClient{

        private SerialDataSourceRT rt;

        public SerialTimeoutClient(SerialDataSourceRT rt){
            this.rt = rt;
        }

        @Override
        public void scheduleTimeout(long fireTime) {
            rt.serialEvent(new TimeoutSerialEvent(fireTime));
        }

        @Override
        public String getThreadName() {
            return "Serial DS Timeout Detector " + rt.getVo().getXid();
        }

    }

    /**
     * Helper for DWR and Here
     * @param message
     * @param terminator
     * @return
     */
    public static String[] splitMessages(String message, String terminator){
        return message.split("(?<=" + terminator + ")");
    }

    public static boolean canProcessTerminatedMessage(String message, String terminator){
        return message.contains(terminator);
    }

    /**
     * Match for 1 point Helper for DWR and here
     * @param msg
     * @param messageRegex
     * @param pointIdentifierIndex
     * @param plVo
     * @param callback
     * @param log
     */
    public static void matchPointValue(String msg, String messageRegex, int pointIdentifierIndex, SerialPointLocatorVO plVo, boolean isHex, Log log, MatchCallback callback) throws Exception{
        Pattern messagePattern = Pattern.compile(messageRegex);
        Matcher messageMatcher = messagePattern.matcher(msg);
        if(messageMatcher.find()){
            if(log.isDebugEnabled())
                log.debug("Message matched regex: " + messageRegex);

            //Parse out the Identifier
            String pointIdentifier = null;
            try{
                pointIdentifier = messageMatcher.group(pointIdentifierIndex);
            }catch(Exception e){
                callback.pointNotIdentified(msg, messageRegex, pointIdentifierIndex);
                return;
            }

            if(plVo.getPointIdentifier().equals(pointIdentifier)){
                if(log.isDebugEnabled())
                    log.debug("Point Identified: " + pointIdentifier);
                Pattern pointValuePattern = Pattern.compile(plVo.getValueRegex());
                Matcher pointValueMatcher = pointValuePattern.matcher(msg); //Use the index from the above message
                if(pointValueMatcher.find()){
                    String value = pointValueMatcher.group(plVo.getValueIndex());
                    if(log.isDebugEnabled()){
                        log.debug("Point Value matched regex: " + plVo.getValueRegex() + " and extracted value " + value);
                    }
                    PointValueTime pvt = convertToPointValue(value, plVo.getDataTypeId(), isHex);
                    callback.onMatch(pointIdentifier, pvt);
                } else {
                    callback.pointPatternMismatch(msg, plVo.getValueRegex());
                }
            }else{
                callback.pointNotIdentified(msg, messageRegex, pointIdentifierIndex);
            }
        }else {
            callback.messagePatternMismatch(msg, messageRegex);
        }
    }

    /**
     * Class for timeout generated events
     * @author Terry Packer
     *
     */
    public class TimeoutSerialEvent extends SerialPortProxyEvent{

        public TimeoutSerialEvent(long time) {
            super(time);
        }

    }
}
