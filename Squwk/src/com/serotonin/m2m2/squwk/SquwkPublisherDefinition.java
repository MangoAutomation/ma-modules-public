package com.serotonin.m2m2.squwk;

import com.serotonin.m2m2.module.PublisherDefinition;
import com.serotonin.m2m2.squwk.pub.SquwkPublisherDwr;
import com.serotonin.m2m2.squwk.pub.SquwkSenderVO;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;
import com.serotonin.m2m2.vo.publish.PublisherVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.publisher.AbstractPublishedPointModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.publisher.AbstractPublisherModel;

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

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.PublisherDefinition#getPublisherModelClass()
	 */
	@Override
	public Class<? extends AbstractPublisherModel<?, ?>> getPublisherModelClass() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.PublisherDefinition#getPublishedPointModelClass()
	 */
	@Override
	public Class<? extends AbstractPublishedPointModel<?>> getPublishedPointModelClass() {
		// TODO Auto-generated method stub
		return null;
	}
}
