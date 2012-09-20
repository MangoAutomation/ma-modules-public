<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/snippet/common.jsp" %>
<c:choose>
  <c:when test="${empty pointComponent.image}"><tag:img png="icon_comp" title="common.noImage"/></c:when>
  <c:otherwise>
    <img src="${pointComponent.image}" width="${pointComponent.width}" height="${pointComponent.height}" alt=""
            <c:if test="${!empty pointComponent.bkgdColorOverride}">style="background-color:${pointComponent.bkgdColorOverride};"</c:if>/>
  </c:otherwise>
</c:choose>
<input type="hidden" id="dyn${pointComponent.id}" value="{graphic:'${pointComponent.dynamicImageId}',value:'${proportion}'}"/>
<c:if test="${pointComponent.displayText}">
  <div style="position:absolute;left:${pointComponent.textX}px;top:${pointComponent.textY}px;">
    <%@ include file="basicContent.jsp" %>
  </div>
</c:if>