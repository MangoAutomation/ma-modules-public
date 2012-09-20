package com.serotonin.m2m2.squwk.pub;

import java.util.List;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.web.dwr.PublisherEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

public class SquwkPublisherDwr extends PublisherEditDwr {
    @DwrPermission(admin = true)
    public ProcessResult saveSquwkSender(String name, String xid, boolean enabled, List<SquwkPointVO> points,
            String accessKey, String secretKey, int cacheWarningSize, int cacheDiscardSize, boolean changesOnly,
            boolean sendSnapshot, int snapshotSendPeriods, int snapshotSendPeriodType) {
        SquwkSenderVO p = (SquwkSenderVO) Common.getUser().getEditPublisher();

        p.setName(name);
        p.setXid(xid);
        p.setEnabled(enabled);
        p.setPoints(points);
        p.setAccessKey(accessKey);
        p.setSecretKey(secretKey);
        p.setCacheWarningSize(cacheWarningSize);
        p.setCacheDiscardSize(cacheDiscardSize);
        p.setChangesOnly(changesOnly);
        p.setSendSnapshot(sendSnapshot);
        p.setSnapshotSendPeriods(snapshotSendPeriods);
        p.setSnapshotSendPeriodType(snapshotSendPeriodType);

        return trySave(p);
    }
}
