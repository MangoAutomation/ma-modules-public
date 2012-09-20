<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/snippet/common.jsp" %>
<%@page import="com.serotonin.m2m2.web.servlet.ImageValueServlet"%>
<c:if test="${!empty point.pointLocator.webcamLiveFeedCode}"><a href="webcam_live_feed.htm?pointId=${point.id}" target="webcamLiveFeed"></c:if>
<c:choose>
  <c:when test="${empty error}"><img src="<%= ImageValueServlet.servletPath %>${pointValue.time}_${point.id}.${imageType}<c:if test="${!empty scalePercent}">?p=${scalePercent}</c:if>" alt=""/></c:when>
  <c:otherwise><span class="simpleRenderer"/><fmt:message key="${error}"/></span></c:otherwise>
</c:choose>
<c:if test="${!empty point.pointLocator.webcamLiveFeedCode}"><a href=""></a></c:if>