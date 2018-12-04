/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.vo.event.audit.AuditEventInstanceVO;

/**
 * @author Terry Packer
 *
 */
public class AuditEventTypeModel extends AbstractEventTypeModel<AuditEventType> {

    private String changeType;
    
    public AuditEventTypeModel() {
        super(new AuditEventType());
    }
    
    public AuditEventTypeModel(AuditEventType type) {
        super(type);
        this.changeType = AuditEventInstanceVO.CHANGE_TYPE_CODES.getCode(type.getChangeType());
    }
    
    public String getChangeType() {
        return changeType;
    }

    @Override
    public AuditEventType toVO() {
        return new AuditEventType(subType, AuditEventInstanceVO.CHANGE_TYPE_CODES.getId(changeType), referenceId1);
    }
}
