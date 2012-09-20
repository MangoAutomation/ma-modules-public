<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@ taglib prefix="views" tagdir="/WEB-INF/tags/graphicalViews" %>

<tag:page dwr="GraphicalViewDwr" js="/resources/view.js,${modulePath}/web/graphicalViews.js,${modulePath}/web/wz_jsgraphics.js">
  <script type="text/javascript">
    <c:if test="${!empty currentView}">
      mango.view.initNormalView();
    </c:if>
        
    function unshare() {
        GraphicalViewDwr.deleteViewShare(function() { window.location = '/views.shtm'; });
    }
  </script>
  
  <table class="borderDiv">
    <tr>
      <td class="smallTitle"><fmt:message key="views.title"/> <tag:help id="graphicalViews"/></td>
      <td width="50"></td>
      <td align="right">
        <sst:select value="${currentView.id}" onchange="window.location='?viewId='+ this.value;">
          <c:forEach items="${views}" var="aView">
            <sst:option value="${aView.key}">${sst:escapeLessThan(aView.value)}</sst:option>
          </c:forEach>
        </sst:select>
        <c:if test="${!empty currentView}">
          <c:choose>
            <c:when test="${owner}">
              <a href="view_edit.shtm?viewId=${currentView.id}"><tag:img png="pencil" title="viewEdit.editView"/></a>
            </c:when>
            <c:otherwise>
              <tag:img png="delete" title="viewEdit.deleteView" onclick="unshare()"/>
            </c:otherwise>
          </c:choose>
          <a href="view_edit.shtm?viewId=${currentView.id}&copy=true"><tag:img png="copy" title="viewEdit.copyView"/></a>
        </c:if>
        <a href="view_edit.shtm"><tag:img png="add" title="views.newView"/></a>
      </td>
    </tr>
  </table>
  
  <views:displayView view="${currentView}" emptyMessageKey="views.noViews"/>
</tag:page>