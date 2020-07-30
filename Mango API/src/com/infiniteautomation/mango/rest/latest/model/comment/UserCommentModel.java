/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 *
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.comment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.vo.comment.UserCommentVO;

import io.swagger.annotations.ApiModelProperty;

/**
 * This class should really JSON ignore name properties
 *
 * @author Terry Packer
 *
 */
public class UserCommentModel extends AbstractVoModel<UserCommentVO> {

    private int userId;
    private String username;
    private String comment;
    private long timestamp;
    private String commentType;
    private int referenceId;

    public UserCommentModel() {

    }

    public UserCommentModel(UserCommentVO vo) {
        fromVO(vo);
    }

    @Override
    protected UserCommentVO newVO() {
        return new UserCommentVO();
    }

    @Override
    public void fromVO(UserCommentVO vo) {
        super.fromVO(vo);
        this.userId = vo.getUserId();
        this.username = vo.getUsername();
        this.timestamp = vo.getTs();
        this.comment = vo.getComment();
        this.commentType = UserCommentVO.COMMENT_TYPE_CODES.getCode(vo.getCommentType());
        this.referenceId = vo.getReferenceId();
    }

    @Override
    public UserCommentVO toVO() throws ValidationException {
        UserCommentVO vo = super.toVO();
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setTs(timestamp);
        vo.setComment(comment);
        vo.setCommentType(UserCommentVO.COMMENT_TYPE_CODES.getId(commentType));
        vo.setReferenceId(referenceId);
        return vo;
    }

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    @Override
    public String getName() {
        return super.getName();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @ApiModelProperty(allowableValues = "POINT,EVENT,JSON_DATA")
    public String getCommentType() {
        return commentType;
    }

    public void setCommentType(String commentType) {
        this.commentType = commentType;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }
}
