/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.datasource;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infiniteautomation.mango.rest.latest.exception.GenericRestException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

/**
 * Class for to subset the settings for a data source for read only access
 *
 * @author Terry Packer
 *
 */
public class ReadOnlyDataSourceModel extends AbstractDataSourceModel<DataSourceVO> {

    private String modelType;

    public ReadOnlyDataSourceModel() {

    }

    public ReadOnlyDataSourceModel(DataSourceVO vo) {
        fromVO(vo);
    }

    /**
     * Return the TYPE_NAME from the Data Source definition
     * @return
     */
    @Override
    public String getModelType() {
        return modelType;
    }

    @Override
    protected DataSourceVO newVO() {
        DataSourceDefinition<?> def = getDefinition();
        DataSourceVO vo = def.baseCreateDataSourceVO();
        vo.setDefinition(def);
        return vo;
    }

    @Override
    @JsonIgnore
    public DataSourceDefinition<DataSourceVO> getDefinition() {
        DataSourceDefinition<DataSourceVO> definition = ModuleRegistry.getDataSourceDefinition(getModelType());
        if(definition == null)
            throw new GenericRestException(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("rest.exception.modelNotFound", getModelType()));
        return definition;
    }

    @Override
    public void fromVO(DataSourceVO vo) {
        super.fromVO(vo);
        this.modelType = vo.getDefinition().getDataSourceTypeName();
    }

}
