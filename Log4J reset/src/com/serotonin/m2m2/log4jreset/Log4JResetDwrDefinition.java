package com.serotonin.m2m2.log4jreset;

import com.serotonin.m2m2.module.DwrDefinition;
import com.serotonin.m2m2.web.dwr.ModuleDwr;

public class Log4JResetDwrDefinition extends DwrDefinition {
    @Override
    public Class<? extends ModuleDwr> getDwrClass() {
        return Log4JResetDwr.class;
    }
}
