<%--
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@ taglib prefix="watchlist" tagdir="/WEB-INF/tags/watchlists" %>
<div style="width:100%;">

<div style="width:100%;background-color:white;text-align:center;margin:20px 0 60px;"><img src="/images/logo.png"></div>
<watchlist:mobilePage>
  <table style="width:100%">
	<caption>Watch List</caption>
	<thead>
	<tr>
		<th scope="col">Point Name</th>
		<th scope="col">Value</th>
		<th scope="col">Timestamp</th>
	</tr>
	</thead>
	<c:set var="odd" value="1"/>
    <c:forEach items="${watchListData}" var="state">
	<c:set var="odd" value="${-odd}"/>
	  <c:choose>
		<c:when test="${odd==1}">
		<tr class="odd">
		</c:when>
		<c:otherwise>
        <tr>
		</c:otherwise>
	  </c:choose>
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
</div>