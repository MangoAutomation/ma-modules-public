/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.mailingList;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.serotonin.m2m2.vo.mailingList.PhoneEntry;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 *
 * @author Terry Packer
 */
@Component
public class PhoneEntryModelMapping implements RestModelMapping<PhoneEntry, PhoneEntryModel> {

    @Override
    public Class<? extends PhoneEntry> fromClass() {
        return PhoneEntry.class;
    }

    @Override
    public Class<? extends PhoneEntryModel> toClass() {
        return PhoneEntryModel.class;
    }

    @Override
    public PhoneEntryModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        PhoneEntry vo = (PhoneEntry)from;
        return new PhoneEntryModel(vo);
    }

}
