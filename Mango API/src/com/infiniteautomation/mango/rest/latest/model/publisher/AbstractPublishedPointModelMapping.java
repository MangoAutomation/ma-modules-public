/*
 * Copyright (C) 2021 RadixIot LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.publisher;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.PublisherDao;
import com.serotonin.m2m2.module.PublisherDefinition;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;

/**
 * Base class for published point model mappings
 */
public abstract class AbstractPublishedPointModelMapping<VO extends PublishedPointVO, MODEL extends AbstractPublishedPointModel<VO>> implements RestModelJacksonMapping<VO, MODEL> {

    protected final DataPointDao dataPointDao;
    protected final PublisherDao publisherDao;
    protected final PublisherDefinition<?> definition;

    public AbstractPublishedPointModelMapping(DataPointDao dataPointDao, PublisherDao publisherDao, PublisherDefinition<?> definition) {
        this.dataPointDao = dataPointDao;
        this.publisherDao = publisherDao;
        this.definition = definition;
    }

    @Override
    public MODEL map(Object from, PermissionHolder user, RestModelMapper mapper) {
        VO vo = (VO)from;
        MODEL model = null;
        try {
            model = toClass().getDeclaredConstructor().newInstance();
            setModelProperties(model, vo, user, mapper);
            return model;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create model", e);
        }

    }

    @Override
    public VO unmap(Object from, PermissionHolder user, RestModelMapper mapper) throws ValidationException {
        MODEL model = (MODEL)from;

        Integer publisherId = publisherDao.getIdByXid(model.publisherXid);
        Integer dataPointId = dataPointDao.getIdByXid(model.dataPointXid);
        VO vo = definition.createPublishedPointVO(publisherId == null ? Common.NEW_ID : publisherId,
                dataPointId == null ? Common.NEW_ID : dataPointId);
        //Fill in the module defined properties
        setVoProperties(vo, model, user, mapper);

        return vo;
    }

    /**
     * Set the model properties from a VO
     */
    protected void setModelProperties(MODEL model, VO vo, PermissionHolder user, RestModelMapper mapper) {
        model.setId(vo.getId());
        model.setXid(vo.getXid());
        model.setName(vo.getName());
        model.setEnabled(vo.isEnabled());
        model.setPublisherXid(vo.getPublisherXid());
        model.setDataPointXid(vo.getDataPointXid());
        model.setDataPointTags(vo.getDataPointTags());
    }

    /**
     * Set the VO properties from a model
     */
    protected void setVoProperties(VO vo, MODEL model, PermissionHolder user, RestModelMapper mapper) {
        vo.setId(model.getId() == null ? Common.NEW_ID : model.id);
        vo.setXid(model.getXid());
        vo.setName(model.getName());
        vo.setEnabled(model.isEnabled());
        vo.setPublisherXid(model.publisherXid);
        vo.setDataPointXid(model.dataPointXid);
    }

}
