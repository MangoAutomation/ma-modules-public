/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.user;

import com.serotonin.m2m2.vo.LinkedAccount;
import com.serotonin.m2m2.vo.OAuth2LinkedAccount;

public class OAuth2LinkedAccountModel extends LinkedAccountModel {
    String issuer;
    String subject;

    public OAuth2LinkedAccountModel() {
        super();
    }

    public OAuth2LinkedAccountModel(OAuth2LinkedAccount linkedAccount) {
        super();
        this.issuer = linkedAccount.getIssuer();
        this.subject = linkedAccount.getSubject();
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public LinkedAccount toLinkedAccount() {
        return new OAuth2LinkedAccount(issuer, subject);
    }
}
