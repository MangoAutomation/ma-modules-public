<%--
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@page import="com.serotonin.m2m2.internal.InternalDataSourceVO"%>
<%@page import="com.serotonin.m2m2.internal.InternalPointLocatorVO"%>
<%@page import="com.serotonin.m2m2.Common" %>
<%@page import="com.infiniteautomation.mango.monitor.MonitoredValues" %>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<script type="text/javascript">

  function saveDataSourceImpl(basic) {
      InternalEditDwr.saveInternalDataSource(basic, $get("updatePeriods"),
              $get("updatePeriodType"), $get("createPointsPattern"), saveDataSourceCB);
      
  }
  
  function appendPointListColumnFunctions(pointListColumnHeaders, pointListColumnFunctions) {
      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key="dsEdit.internal.attribute"/>";
      pointListColumnFunctions[pointListColumnFunctions.length] =
              function(p) { return p.pointLocator.configurationDescription; };
  }
  
  function editPointCBImpl(locator) {
      $set("monitorId", locator.monitorId);
  }
  
  function savePointImpl(locator) {
      delete locator.settable;
      delete locator.dataTypeId;
      delete locator.relinquishable;
      
      locator.monitorId = $get("monitorId");
      
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
  <tr>
    <td class="formLabel"><fmt:message key="dsEdit.internal.createPointsPattern"/></td>
    <td class="formField">
      <input type="text" id="createPointsPattern" value="${dataSource.createPointsPattern}" class="formLong"/>
    </td>
  </tr>
  <tr>
    <td class="formLabel"/>
    <td class="formField">
      <select id="preformedPatterns" class="formLong">
        <option value=""><fmt:message key="common.none"/></option>
        <option value=".+"><fmt:message key="common.all"/></option>
        <option value="com.serotonin.m2m2.rt.dataSource.PollingDataSource_.+"><fmt:message key="dsEdit.internal.autoCreate.pollingDataSource"></fmt:message>
        <option value="com.serotonin.m2m2.rt.dataSource.PollingDataSource_.+_SUCCESS"><fmt:message key="dsEdit.internal.autoCreate.pollingDataSource.sequential"></fmt:message>
        <option value="com.serotonin.m2m2.rt.dataSource.PollingDataSource_.+_DURATION"><fmt:message key="dsEdit.internal.autoCreate.pollingDataSource.duration"></fmt:message>
        <option value="com.serotonin.m2m2.rt.dataSource.PollingDataSource_.+_PERCENTAGE"><fmt:message key="dsEdit.internal.autoCreate.pollingDataSource.percentage"></fmt:message>
        <option value="com[.]serotonin[.]m2m2[.]persistent.+"><fmt:message key="dsEdit.internal.autoCreate.persistent"/></option>
        <option value="com[.]serotonin[.]m2m2[.]persistent[.]RECIEVING_RATE_MONITOR_.+"><fmt:message key="dsEdit.internal.autoCreate.persistent.writeSpeed"/></option>
        <option value="com[.]serotonin[.]m2m2[.]persistent[.]TOTAL_CONNECTION_TIME_MONITOR_.+"><fmt:message key="dsEdit.internal.autoCreate.persistent.connectionTime"/></option>
      </select>
	  <tag:img png="arrow-turn-090-left" onclick="$set('createPointsPattern', $get('preformedPatterns'))" title="common.set"></tag:img>
    </td>
  </tr>
</tag:dataSourceAttrs>

<tag:pointList pointHelpId="internalPP">
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.internal.attribute"/></td>
    <td class="formField">
    	<select id="monitorId">
    	<c:set var="monitorItems" value="<%= Common.MONITORED_VALUES.getMonitors() %>" />
        <c:forEach items="${monitorItems}" var="monit">
            <option value='${sst:escapeLessThan(sst:quotEncode(monit.id))}'.replace(/&lt;/g, "<"><m2m2:translate message="${monit.name}"/></option>
        </c:forEach>
    	</select>
    </td>
  </tr>
</tag:pointList>