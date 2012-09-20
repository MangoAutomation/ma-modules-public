<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/snippet/common.jsp" %>
<c:set var="content"><%--
  --%><c:choose><%--
    --%><c:when test="${displayPointName}">${pointComponent.name}:&nbsp;<b>${m2m2:htmlText(point, pointValue)}</b></c:when><%--
    --%><c:otherwise>${m2m2:htmlText(point, pointValue)}</c:otherwise><%--
  --%></c:choose><%--
--%></c:set>
<c:if test="${!empty styleAttribute}"><div style="${styleAttribute}"></c:if>
<c:choose>
  <c:when test='${!empty viewComponent}'>
    <c:choose>
      <c:when test='${empty viewComponent.bkgdColorOverride}'>
        <span class="simpleRenderer"/>${content}</span>
      </c:when>
      <c:when test='${viewComponent.bkgdColorOverride == "transparent"}'>
        <span class="simpleRenderer" style="background:transparent;border:0;"/>${content}</span>
      </c:when>
      <c:otherwise>
        <span class="simpleRenderer" style="background-color:${viewComponent.bkgdColorOverride};"/>${content}</span>
      </c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise>
    <c:choose>
      <c:when test='${empty pointComponent.bkgdColorOverride}'>
        <span class="simpleRenderer"/>${content}</span>
      </c:when>
      <c:when test='${pointComponent.bkgdColorOverride == "transparent"}'>
        <span class="simpleRenderer" style="background:transparent;border:0;"/>${content}</span>
      </c:when>
      <c:otherwise>
        <span class="simpleRenderer" style="background-color:${pointComponent.bkgdColorOverride};"/>${content}</span>
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
<c:if test="${!empty styleAttribute}"></div></c:if>