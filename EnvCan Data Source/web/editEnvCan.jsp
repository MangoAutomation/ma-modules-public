<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@page import="com.serotonin.m2m2.envcan.EnvCanDataSourceVO"%>
<%@page import="com.serotonin.m2m2.envcan.EnvCanPointLocatorVO"%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<script type="text/javascript">
  function saveDataSourceImpl(basic) {
	  EnvCanEditDwr.saveEnvCanDataSource(basic, $get("stationId"), 
			  saveDataSourceCB);
  }
  
  function appendPointListColumnFunctions(pointListColumnHeaders, pointListColumnFunctions) {
      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key="envcands.attr"/>";
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
      
      EnvCanEditDwr.saveEnvCanPointLocator(currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
  }
</script>

<tag:dataSourceAttrs descriptionKey="envcands.desc" helpId="envcanDS">
  <tr>
    <td class="formLabelRequired"><fmt:message key="envcands.stationId"/></td>
    <td class="formField"><input id="stationId" type="text" value="${dataSource.stationId}"/></td>
  </tr>
</tag:dataSourceAttrs>

<tag:pointList pointHelpId="envcanPP">
  <tr>
    <td class="formLabelRequired"><fmt:message key="envcands.attr"/></td>
    <td class="formField">
      <tag:exportCodesOptions id="attributeId" optionList="<%= EnvCanPointLocatorVO.ATTRIBUTE_CODES.getIdKeys() %>"/>
    </td>
  </tr>
</tag:pointList>