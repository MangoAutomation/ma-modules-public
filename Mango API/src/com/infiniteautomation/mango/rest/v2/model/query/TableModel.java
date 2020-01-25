/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.query;

import java.util.List;

import com.infiniteautomation.mango.db.query.QueryAttribute;

/**
 * Class that describes what is available to Query
 * for a Dao.
 *
 * @author Terry Packer
 *
 */
public class TableModel {

    private String tableName;
    //List of what is available
    private List<QueryAttribute> attributes;

    public TableModel(){ }

    /**
     * @param tableName
     * @param attributes
     */
    public TableModel(String tableName, List<QueryAttribute> attributes) {
        super();
        this.tableName = tableName;
        this.attributes = attributes;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public List<QueryAttribute> getAttributes() {
        return attributes;
    }
    public void setAttributes(List<QueryAttribute> attributes) {
        this.attributes = attributes;
    }

}
