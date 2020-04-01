/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.modules;

import java.util.List;

import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 *
 * @author Terry Packer
 */
public class UpgradeUploadResult {

    private boolean restart;
    private List<InvalidModule> invalid;
    private List<String> toUpgrade;

    public boolean isRestart() {
        return restart;
    }

    public void setRestart(boolean restart) {
        this.restart = restart;
    }

    public List<InvalidModule> getInvalid() {
        return invalid;
    }

    public void setInvalid(List<InvalidModule> invalid) {
        this.invalid = invalid;
    }

    public List<String> getToUpgrade() {
        return toUpgrade;
    }

    public void setToUpgrade(List<String> toUpgrade) {
        this.toUpgrade = toUpgrade;
    }

    public static class InvalidModule {

        public InvalidModule() {

        }

        public InvalidModule(String name, TranslatableMessage cause) {
            this.name = name;
            this.cause = cause;
        }

        private String name;
        private TranslatableMessage cause;

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public TranslatableMessage getCause() {
            return cause;
        }
        public void setCause(TranslatableMessage cause) {
            this.cause = cause;
        }
    }
}
