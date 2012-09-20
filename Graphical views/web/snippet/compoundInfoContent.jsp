<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%--
  This snippet supports all data types.
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<b>${compoundComponent.name}</b><br/>
<c:forEach items="${childData}" var="child">
  <c:if test="${!empty child.point}">
    &nbsp;&nbsp;&nbsp;
    <c:if test="${!empty sessionUser}">
      <tag:img png="icon_comp" title="pointEdit.props.details" style="display:inline"
              onclick="window.location='data_point_details.shtm?dpid=${child.point.id}'"/>
    </c:if>
    ${child.name}: <span class="infoData">${m2m2:htmlText(child.point, child.pointValue)}</span><br/>
  </c:if>
</c:forEach>