<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@page import="com.serotonin.m2m2.vmstat.VMStatDataSourceVO"%>
<%@page import="com.serotonin.m2m2.vmstat.VMStatPointLocatorVO"%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<script type="text/javascript">
  function saveDataSourceImpl(basic) {
      VMStatEditDwr.saveVMStatDataSource(basic, $get("pollSeconds"),
              $get("outputScale"), saveDataSourceCB);
  }
  
  function appendPointListColumnFunctions(pointListColumnHeaders, pointListColumnFunctions) {
      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key="dsEdit.vmstat.attribute"/>";
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
      
      VMStatEditDwr.saveVMStatPointLocator(currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
  }
</script>

<tag:dataSourceAttrs descriptionKey="dsEdit.vmstat.desc" helpId="vmstatDS">
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.vmstat.pollSeconds"/></td>
    <td class="formField"><input id="pollSeconds" type="text" value="${dataSource.pollSeconds}"/></td>
  </tr>
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.vmstat.outputScale"/></td>
    <td class="formField">
      <tag:exportCodesOptions id="outputScale" value="${dataSource.outputScale}" optionList="<%= VMStatDataSourceVO.OUTPUT_SCALE_CODES.getIdKeys() %>"/>
    </td>
  </tr>
</tag:dataSourceAttrs>

<tag:pointList pointHelpId="vmstatPP">
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.vmstat.attribute"/></td>
    <td class="formField">
      <tag:exportCodesOptions id="attributeId" optionList="<%= VMStatPointLocatorVO.ATTRIBUTE_CODES.getIdKeys() %>"/>
    </td>
  </tr>
</tag:pointList>