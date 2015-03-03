package com.serotonin.m2m2.squwk;

import com.serotonin.m2m2.module.PublisherDefinition;
import com.serotonin.m2m2.squwk.pub.SquwkPublisherDwr;
import com.serotonin.m2m2.squwk.pub.SquwkSenderVO;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;
import com.serotonin.m2m2.vo.publish.PublisherVO;

public class SquwkPublisherDefinition extends PublisherDefinition {
    @Override
    public String getPublisherTypeName() {
        return "SQUWK";
    }

    @Override
    public String getDescriptionKey() {
        return "publisherEdit.squwk";
    }

    @Override
    protected PublisherVO<? extends PublishedPointVO> createPublisherVO() {
        return new SquwkSenderVO();
    }

    @Override
    public String getEditPagePath() {
        return "web/editpp.jsp";
    }

    @Override
    public Class<?> getDwrClass() {
        return SquwkPublisherDwr.class;
    }
}
