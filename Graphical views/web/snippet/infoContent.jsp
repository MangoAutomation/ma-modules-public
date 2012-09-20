<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%--
  This snippet supports all data types.
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<c:if test="${!empty sessionUser && !empty point}">
  <tag:img png="icon_comp" title="pointEdit.props.details" style="display:inline"
          onclick="window.location='data_point_details.shtm?dpid=${point.id}'"/>
</c:if>
<b>${pointComponent.name}</b><br/>
<c:if test="${!empty point}">
  &nbsp;&nbsp;&nbsp;<fmt:message key="common.value"/>: 
  <c:choose>
    <c:when test="${point.pointLocator.dataTypeId == applicationScope['constants.DataTypes.IMAGE']}">
      <jsp:include page="/WEB-INF/snippet/imageValueThumbnail.jsp"/>
    </c:when>
    <c:otherwise><span class="infoData">${m2m2:htmlText(point, pointValue)}</span><br/></c:otherwise>
  </c:choose>
  <c:if test="${!empty pointValue}">&nbsp;&nbsp;&nbsp;<fmt:message key="common.time"/>: <span class="infoData">${m2m2:pointValueTime(pointValue)}</span><br/></c:if>
</c:if>
