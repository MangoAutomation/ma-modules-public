/*
 *   Mango - Open Source M2M - http://mango.serotoninsoftware.com
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.m2m2.mbus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonEntity;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.mbus.rest.MBusDataSourceModel;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.util.SerializationHelper;

import net.sf.mbus4j.Connection;
import net.sf.mbus4j.SerialPortConnection;
import net.sf.mbus4j.TcpIpConnection;
import net.sf.mbus4j.dataframes.MBusMedium;
import net.sf.mbus4j.dataframes.datablocks.vif.SiPrefix;

@JsonEntity
public class MBusDataSourceVO extends DataSourceVO<MBusDataSourceVO> {

//    private final static Log LOG = LogFactory.getLog(MBusDataSourceVO.class);

    public static MBusDataSourceVO createNewDataSource() {
        MBusDataSourceVO result = new MBusDataSourceVO();
        result.setConnection(new TcpIpConnection("192.168.1.210", 10001, Connection.DEFAULT_BAUDRATE, TcpIpConnection.DEFAULT_RESPONSE_TIMEOUT_OFFSET));
        return result;
    }
    private static final ExportCodes EVENT_CODES = new ExportCodes();

    static {
        EVENT_CODES.addElement(MBusDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, "DATA_SOURCE_EXCEPTION");
        EVENT_CODES.addElement(MBusDataSourceRT.POINT_READ_EXCEPTION_EVENT, "POINT_READ_EXCEPTION");
        EVENT_CODES.addElement(MBusDataSourceRT.POINT_WRITE_EXCEPTION_EVENT, "POINT_WRITE_EXCEPTION");
    }
    private int updatePeriodType = Common.TimePeriods.DAYS;
    @JsonProperty
    private int updatePeriods = 1;
    @JsonProperty
    private boolean quantize;
    private Connection connection;

    @Override
    protected void addEventTypes(List<EventTypeVO> eventTypes) {
        eventTypes.add(createEventType(MBusDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.dataSource")));
        eventTypes.add(createEventType(MBusDataSourceRT.POINT_READ_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.pointRead"), EventType.DuplicateHandling.IGNORE_SAME_MESSAGE, AlarmLevels.URGENT));
        eventTypes.add(createEventType(MBusDataSourceRT.POINT_WRITE_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.pointWrite")));
    }

    @Override
    public TranslatableMessage getConnectionDescription() {
        if (connection instanceof TcpIpConnection) {
            return new TranslatableMessage("common.default", ((TcpIpConnection) connection).getHost() + ":" + ((TcpIpConnection) connection).getPort());
        } else if (connection instanceof SerialPortConnection) {
            return new TranslatableMessage("common.default", ((SerialPortConnection) connection).getPortName());
        } else {
            return new TranslatableMessage("common.default", "null");
        }
    }

    @Override
    public MBusPointLocatorVO createPointLocator() {
        return new MBusPointLocatorVO();
    }

    @Override
    public MBusDataSourceRT createDataSourceRT() {
        return new MBusDataSourceRT(this);
    }

    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    public int getUpdatePeriodType() {
        return updatePeriodType;
    }

    public void setUpdatePeriodType(int updatePeriodType) {
        this.updatePeriodType = updatePeriodType;
    }

    public int getUpdatePeriods() {
        return updatePeriods;
    }

    public void setUpdatePeriods(int updatePeriods) {
        this.updatePeriods = updatePeriods;
    }

    @Override
    public void validate(ProcessResult response) {
        super.validate(response);

        if (connection == null) {
            response.addContextualMessage("connection", "validate.required");
        }else{
        	//Validate the connections pieces
        	 if (connection instanceof TcpIpConnection) {
        		 TcpIpConnection cnxn =  ((TcpIpConnection) connection);
        		 if(StringUtils.isEmpty(cnxn.getHost()))
        			 response.addContextualMessage("ipAddressOrHostname", "validate.required");
        		 if(cnxn.getPort() < 1)
        			 response.addContextualMessage("tcpPort", "validate.greaterThanZero");
             } else if (connection instanceof SerialPortConnection) {
            	 SerialPortConnection cnxn = ((SerialPortConnection) connection);
                 if(StringUtils.isEmpty(cnxn.getPortName())){
                	 response.addContextualMessage("commPortId", "validate.required");
                 }
             }
        }
        if (!Common.TIME_PERIOD_CODES.isValidId(updatePeriodType)) {
            response.addContextualMessage("updatePeriodType", "validate.invalidValue");
        }
        if (updatePeriods <= 0) {
            response.addContextualMessage("updatePeriods", "validate.greaterThanZero");
        }
    }
    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int SERIAL_VERSION = 4;

    // Serialization for saveDataSource
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(SERIAL_VERSION);
        out.writeObject(connection);
        out.writeInt(updatePeriodType);
        out.writeInt(updatePeriods);
        out.writeBoolean(quantize);
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if ((ver == 2) || (ver == 1)) {
            String connectionType = in.readUTF();
            switch (connectionType) {
                case "SERIAL_DIRECT":
                    connection = new SerialPortConnection(SerializationHelper.readSafeUTF(in));
                    break;
                default:
                case "SERIAL_AT_MODEM":
                    // TODO modem stuff goes here
                    throw new RuntimeException("AT Modem is not supported");
            }
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();

            connection.setBitPerSecond(in.readInt());
            //flowControlIn = 
            in.readInt();
            //flowControlOut = 
            in.readInt();
            //dataBits = 
            in.readInt();
            //stopBits = 
            in.readInt();
            //parity = 
            in.readInt();
        } else if (ver == 3) {
            String connectionType = in.readUTF();
            switch (connectionType) {
                case "SERIAL_DIRECT":
                    connection = new SerialPortConnection(SerializationHelper.readSafeUTF(in));
                    break;
                default:
                case "SERIAL_AT_MODEM":
                    // TODO modem stuff goes here
                    throw new RuntimeException("AT Modem is not supported");
            }
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();

            connection.setBitPerSecond(in.readInt());
            //flowControlIn = 
            in.readInt();
            //flowControlOut = 
            in.readInt();
            //dataBits = 
            in.readInt();
            //stopBits = 
            in.readInt();
            //parity = 
            in.readInt();
            connection.setResponseTimeOutOffset(in.readInt());
        } else if (ver == 4) {
            readObjectVer4(in);
        }

    }

    /**
     * Helper for JSP
     *
     * @return
     */
    public boolean isSerialDirect() {
        return connection == null ? false : SerialPortConnection.class.equals(connection.getClass());
    }

    /**
     * Helper for JSP
     *
     * @return
     */
    public boolean isTcpIp() {
        return connection == null ? false : TcpIpConnection.class.equals(connection.getClass());
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    private void readObjectVer4(ObjectInputStream in) throws IOException, ClassNotFoundException {
        connection = (Connection) in.readObject();
        updatePeriodType = in.readInt();
        updatePeriods = in.readInt();
        quantize = in.readBoolean();
    }

    /**
     * @return the quantize
     */
    public boolean isQuantize() {
        return quantize;
    }

    /**
     * @param quantize the quantize to set
     */
    public void setQuantize(boolean quantize) {
        this.quantize = quantize;
    }

    public Set<MBusMedium> getRegularMedia() {
        return MBusMedium.getRegularValues();
    }

    public SiPrefix[] getSiPrefix() {
        return SiPrefix.values();
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.vo.dataSource.DataSourceVO#jsonRead(com.serotonin.json.JsonReader, com.serotonin.json.type.JsonObject)
     */
    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
    	super.jsonRead(reader, jsonObject);
    	
    	 Integer value = readUpdatePeriodType(jsonObject);
         if (value != null)
             updatePeriodType = value;
         
         String s = jsonObject.getString("connectionType");
         if(s == null){
        	 List<String> codes = new ArrayList<String>();
        	 codes.add("mbusSerial");
        	 codes.add("mbusTcpIp");
        	 throw new TranslatableJsonException("emport.error.missing", "connectionType", codes);
         }else{
    		 int bitPerSecond = jsonObject.getInt("bitPerSecond");
    		 int responseTimeoutOffset = jsonObject.getInt("responseTimeoutOffset");
        	 switch(s){
        	 case "mbusSerial":
        		 String portName = jsonObject.getString("portName");
        		 connection = new SerialPortConnection(portName, bitPerSecond, responseTimeoutOffset);
        		 break;
        	 case "mbusTcpIp":
        		 String host = jsonObject.getString("host");
        		 int port = jsonObject.getInt("port");
        		 connection = new TcpIpConnection(host, port, bitPerSecond, responseTimeoutOffset);
        		 break;
        	 }
         }
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.vo.dataSource.DataSourceVO#jsonWrite(com.serotonin.json.ObjectWriter)
     */
    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
    	super.jsonWrite(writer);
    	writeUpdatePeriodType(writer, this.updatePeriodType);
    	if(connection instanceof SerialPortConnection){
    		writer.writeEntry("connectionType", "mbusSerial");
    		SerialPortConnection conn = (SerialPortConnection)connection;
    		writer.writeEntry("bitPerSecond", conn.getBitPerSecond());
    		writer.writeEntry("responseTimeoutOffset", conn.getResponseTimeOutOffset());
    		writer.writeEntry("portName", conn.getPortName());
    	}else if(connection instanceof TcpIpConnection){
    		writer.writeEntry("connectionType", "mbusTcpIp");
    		TcpIpConnection conn = (TcpIpConnection)connection;
    		writer.writeEntry("bitPerSecond", conn.getBitPerSecond());
    		writer.writeEntry("responseTimeoutOffset", conn.getResponseTimeOutOffset());
    		writer.writeEntry("host", conn.getHost());
    		writer.writeEntry("port", conn.getPort());
    	}
    }
    
    /* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.DataSourceVO#getModel()
     */
    @Override
    public MBusDataSourceModel asModel() {
        return new MBusDataSourceModel(this);
    }
}
