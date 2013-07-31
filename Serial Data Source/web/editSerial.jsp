<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%><%@ include file="/WEB-INF/jsp/include/tech.jsp"%>

<script type="text/javascript">

	/**
	 * Save the DS
	 */
	function saveDataSourceImpl(basic){

		  SerialEditDwr.saveSerialDataSource(basic,
	              $get("commPortId"), $get("baudRate"), $get("flowControlIn"), $get("flowControlOut"), $get("dataBits"), 
	              $get("stopBits"), $get("parity"),$get("readTimeout"),$get("messageTerminator"),
	              $get("messageRegex"),$get("pointIdentifierIndex"),saveDataSourceCB);

	}

	/**
	 * Add a Point
	 */
	  function addPointImpl() {
		  DataSourceEditDwr.getPoint(-1, function(point) {
			  editPointCB(point);
		  });
	  }
		
	  function editPointCBImpl(locator) {
		  $set("pointIdentifier",locator.pointIdentifier);
		  $set("valueRegex",locator.valueRegex);
		  $set("valueIndex",locator.valueIndex);
		  $set("dataTypeId",locator.dataTypeId);
	  }
	  
	  /**
	   * Save a Point
	   */
	  function savePointImpl(locator) {
		  delete locator.pointIdentifier;
		  delete locator.valueRegex;
		  delete locator.valueIndex;
		  delete locator.dataTypeId;
		  
		  locator.pointIdentifier = $get("pointIdentifier");
		  locator.valueRegex = $get("valueRegex");
		  locator.valueIndex = $get("valueIndex");
		  locator.dataTypeId = $get("dataTypeId");
		  
	      SerialEditDwr.savePointLocator(currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
	  }
</script>

<tag:dataSourceAttrs descriptionKey="dsEdit.serial.desc" helpId="serial-ds">
<tag:serialSettings/>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.readTimeout"/></td>
 <td><input id="readTimeout" type="number" value="${dataSource.readTimeout}"></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.messageTerminator"/></td>
 <td><input id="messageTerminator" type="text" value="${dataSource.messageTerminator}"></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.messageRegex"/></td>
 <td><input id="messageRegex" type="text" value="${dataSource.messageRegex}"></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.pointIdentifierIndex"/></td>
 <td><input id="pointIdentifierIndex" type="number" value="${dataSource.pointIdentifierIndex}"></input></td>
</tr>

</tag:dataSourceAttrs>

<tag:pointList pointHelpId="serial-pp">
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.pointDataType"/></td>
    <td class="formField"><tag:dataTypeOptions id="dataTypeId" excludeImage="true"/></td>
  </tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.pointIdentifier"/></td>
 <td><input id="pointIdentifier" type="text" ></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.valueIndex"/></td>
 <td><input id="valueIndex" type="number" ></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.valueRegex"/></td>
 <td><input id="valueRegex" type="text" ></input></td>
</tr>

</tag:pointList>