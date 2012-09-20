<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%><%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<c:if test="${!empty invalid || !empty disabled || !empty events || pointRT.attributes.UNRELIABLE}">
  <table width="200" cellspacing="0" cellpadding="0">
    <c:choose>
      <c:when test="${!empty invalid}">
        <tr>
          <td valign="top"><tag:img png="warn" title="common.warning"/></td>
          <td colspan="3"><fmt:message key="common.pointInvalid"/></td>
        </tr>
      </c:when>
      <c:when test="${!empty disabled}">
        <tr>
          <td valign="top"><tag:img png="warn" title="common.warning"/></td>
          <td colspan="3"><fmt:message key="common.pointWarning"/></td>
        </tr>
      </c:when>
    </c:choose>
    <c:if test="${pointRT.attributes.UNRELIABLE}">
      <tr>
        <td><tag:img png="warn" title="common.valueUnreliable"/></td>
        <td style="white-space:nowrap;" colspan="3">
          <fmt:message key="common.valueUnreliable"/>
          <tag:img png="arrow_refresh" title="common.refresh" onclick="GraphicalViewDwr.forcePointRead(${point.id})" style="display:inline"/>
        </td>
      </tr>
    </c:if>
    <c:if test="${!empty events}">
      <c:forEach items="${events}" var="event">
        <tr>
          <td><tag:eventIcon event="${event}"/></td>
          <td style="white-space:nowrap;">&nbsp;<tag:alarmAck event="${event}"/></td>
          <td>${m2m2:time(event.activeTimestamp)}</td>
          <td style="white-space:nowrap;">&nbsp;<m2m2:translate message="${event.message}"/></td>
        </tr>
      </c:forEach>
    </c:if>
  </table>
</c:if>