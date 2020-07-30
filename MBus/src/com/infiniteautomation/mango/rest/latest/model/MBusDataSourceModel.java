/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import com.infiniteautomation.mango.rest.latest.model.datasource.AbstractPollingDataSourceModel;
import com.serotonin.m2m2.mbus.MBusDataSourceDefinition;
import com.serotonin.m2m2.mbus.MBusDataSourceVO;

import io.swagger.annotations.ApiModel;
import net.sf.mbus4j.Connection;
import net.sf.mbus4j.SerialPortConnection;
import net.sf.mbus4j.TcpIpConnection;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value=MBusDataSourceDefinition.DATA_SOURCE_TYPE, parent=MBusDataSourceModel.class)
public class MBusDataSourceModel extends AbstractPollingDataSourceModel<MBusDataSourceVO>{
    
    private ConnectionModel<?> connection;
    
    public MBusDataSourceModel() {

    }
    
    public MBusDataSourceModel(MBusDataSourceVO data) {
        fromVO(data);
    }

    @Override
    public String getModelType() {
        return MBusDataSourceDefinition.DATA_SOURCE_TYPE;
    }
    
    @Override
    public MBusDataSourceVO toVO() {
        MBusDataSourceVO vo = super.toVO();
        if(connection != null)
            vo.setConnection(connection.toVO());
        return vo;
    }
    
    @Override
    public void fromVO(MBusDataSourceVO vo) {
        super.fromVO(vo);
        Connection conn = vo.getConnection();
        if(conn instanceof SerialPortConnection)
            connection = new SerialPortConnectionModel((SerialPortConnection) conn);
        else if(conn instanceof TcpIpConnection)
            connection = new TcpIpConnectionModel((TcpIpConnection) conn);
            
    }
    
    /**
     * @return the connection
     */
    public ConnectionModel<?> getConnection() {
        return connection;
    }
    
    /**
     * @param connection the connection to set
     */
    public void setConnection(ConnectionModel<?> connection) {
        this.connection = connection;
    }
    
}
