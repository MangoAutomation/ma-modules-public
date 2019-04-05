/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import com.serotonin.m2m2.vo.Validatable;

import net.sf.mbus4j.Connection;

/**
 * @author Terry Packer
 *
 */
public abstract class MBusScanRequest implements Validatable {

    private String dataSourceXid;
    
    public Connection createConnection() {
        // TODO Auto-generated method stub
        return null;
    }


    public String getDataSourceXid() {
        return dataSourceXid;
    }
    
    public void setDataSourceXid(String dataSourceXid) {
        this.dataSourceXid = dataSourceXid;
    }

}
