<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@ taglib prefix="watchlist" tagdir="/WEB-INF/tags/watchlists" %>

<watchlist:mobilePage>
  <table border="1">
    <c:forEach items="${watchListData}" var="state">
      <tr>
        <td>${state.name}</td>
        <c:choose>
          <c:when test="${state.disabled}">
            <td colspan="2"><fmt:message key="common.pointWarning"/></td>
          </c:when>
          <c:otherwise>
            <td align="center">${state.value}</td>
            <td>${state.time}</td>
          </c:otherwise>
        </c:choose>
      </tr>
    </c:forEach>
  </table>
  
  <fmt:message key="header.watchlist"/>:
  <sst:select id="watchListSelect" value="${selectedWatchList}"
          onchange="window.location='/mobile/watch_list.shtm?watchListId='+ this.value">
    <c:forEach items="${watchLists}" var="wl">
      <sst:option value="${wl.key}">${wl.value}</sst:option>
    </c:forEach>
  </sst:select>

  <a href="/mobile/watch_list.shtm"><fmt:message key="header.reload"/></a>
  
  <a href="/mobile/logout.htm"><fmt:message key="header.logout"/></a>
</watchlist:mobilePage>