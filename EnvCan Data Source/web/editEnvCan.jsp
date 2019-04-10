<%--
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@page import="com.serotonin.m2m2.envcan.EnvCanDataSourceVO"%>
<%@page import="com.serotonin.m2m2.envcan.EnvCanPointLocatorVO"%>
<%@page import="com.serotonin.m2m2.DataTypes"%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<script type="text/javascript">
  var envCanAttributeId;
  function saveDataSourceImpl(basic) {
	  EnvCanEditDwr.saveEnvCanDataSource(basic, $get("stationId"), document.getElementById("dataStartTime").valueAsDate,
			  saveDataSourceCB);
  }
  
  function appendPointListColumnFunctions(pointListColumnHeaders, pointListColumnFunctions) {
      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key="envcands.attr"/>";
      pointListColumnFunctions[pointListColumnFunctions.length] =
              function(p) { return p.pointLocator.configurationDescription; };
  }
  
  function editPointCBImpl(locator) {
      $set("attributeId", locator.attributeId);
      envCanAttributeId = locator.attributeId;
  }
  
  function savePointImpl(locator) {
      delete locator.settable;
      delete locator.dataTypeId;
      delete locator.relinquishable;
      
      locator.attributeId = $get("attributeId");
      
      EnvCanEditDwr.saveEnvCanPointLocator(currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
  }
  
  function envCanDataTypeChange(){
      var attrId = $get("attributeId");
      switch(attrId){
	  	case "10": //Weather
	  	  	dataPointDataTypeChanged(<%= DataTypes.ALPHANUMERIC %>);
	  		envCanAttributeId = attrId;
	  	break;
	  	default:
	  	    //Only change data type if we were weather
	  	    if(envCanAttributeId === "10"){
				dataPointDataTypeChanged(<%= DataTypes.NUMERIC %>);
	  	    }
	  		envCanAttributeId = attrId;
	  } 
  }
</script>

<tag:dataSourceAttrs descriptionKey="envcands.desc" helpId="envCanDS">
  <tr>
    <td class="formLabelRequired"><fmt:message key="envcands.stationId"/></td>
    <td class="formField"><input id="stationId" type="text" value="${dataSource.stationId}"/></td>
  </tr>
  <tr>
  	<td class="formLabelRequired"><fmt:message key="envcands.dataStartTime"/></td>
  	<td class="formField"><input id="dataStartTime" type="date"/></td>
  </tr>
  <script>document.getElementById("dataStartTime").valueAsDate = new Date(${dataSource.dataStartTime});</script>
</tag:dataSourceAttrs>

<tag:pointList pointHelpId="envCanPP">
  <tr>
    <td class="formLabelRequired"><fmt:message key="envcands.attr"/></td>
    <td class="formField">
      <tag:exportCodesOptions id="attributeId" optionList="<%= EnvCanPointLocatorVO.ATTRIBUTE_CODES.getIdKeys() %>"  onchange="envCanDataTypeChange()"/>
    </td>
  </tr>
</tag:pointList>