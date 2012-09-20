<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@page import="com.serotonin.m2m2.internal.InternalDataSourceVO"%>
<%@page import="com.serotonin.m2m2.internal.InternalPointLocatorVO"%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<script type="text/javascript">
  function saveDataSourceImpl(basic) {
      InternalEditDwr.saveInternalDataSource(basic, $get("updatePeriods"),
              $get("updatePeriodType"), saveDataSourceCB);
  }
  
  function appendPointListColumnFunctions(pointListColumnHeaders, pointListColumnFunctions) {
      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key="dsEdit.internal.attribute"/>";
      pointListColumnFunctions[pointListColumnFunctions.length] =
              function(p) { return p.pointLocator.configurationDescription; };
  }
  
  function editPointCBImpl(locator) {
      $set("attributeId", locator.attributeId);
  }
  
  function savePointImpl(locator) {
      delete locator.settable;
      delete locator.dataTypeId;
      delete locator.relinquishable;
      
      locator.attributeId = $get("attributeId");
      
      InternalEditDwr.saveInternalPointLocator(currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
  }
</script>

<tag:dataSourceAttrs descriptionKey="dsEdit.internal.desc" helpId="internalDS">
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.updatePeriod"/></td>
    <td class="formField">
      <input type="text" id="updatePeriods" value="${dataSource.updatePeriods}" class="formShort"/>
      <tag:timePeriods id="updatePeriodType" value="${dataSource.updatePeriodType}" ms="true" s="true" min="true" h="true"/>
    </td>
  </tr>
</tag:dataSourceAttrs>

<tag:pointList pointHelpId="internalPP">
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.internal.attribute"/></td>
    <td class="formField">
      <tag:exportCodesOptions id="attributeId" optionList="<%= InternalPointLocatorVO.ATTRIBUTE_CODES.getIdKeys() %>"/>
    </td>
  </tr>
</tag:pointList>