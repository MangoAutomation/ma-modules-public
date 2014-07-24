<%--
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
--%><%@ include file="/WEB-INF/jsp/include/tech.jsp"%>

<script type="text/javascript">

	function initImpl() {
		SerialEditDwr.getSafeTerminator(function(messageTerminator) {
			$set("messageTerminator", messageTerminator);
			if(${dataSource.useTerminator}) {
				dojo.byId("useTerminator").checked = "true";
			}
			toggleTerminator();
		});
	}

	/**
	 * Save the DS
	 */
	function saveDataSourceImpl(basic){

		  SerialEditDwr.saveSerialDataSource(basic,
	              $get("commPortId"),$get("baudRate"),$get("flowControlIn"),$get("flowControlOut"),$get("dataBits"), 
	              $get("stopBits"),$get("parity"),$get("readTimeout"),$get("useTerminator"),$get("messageTerminator"),
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
	  
	  /**
	   * Toggle Terminator row
	   */
	  function toggleTerminator() {
		  if(!dojo.byId("useTerminator").checked) {
			  $("terminatorRow").style.visibility = "collapse";
		  	  $("messageRegexRow").style.visibility = "collapse";
		  	  $("identifierIndexRow").style.visibility = "collapse";
	  	  } else { 
			  $("terminatorRow").style.visibility = "visible";
			  $("messageRegexRow").style.visibility = "visible";
			  $("identifierIndexRow").style.visibility = "visible";
		  }
	  }
	  
	  /**
	   * Runs a test string
	   */
	  function submitTestString() {
		  SerialEditDwr.testString($get("testString"), displayResult);
	  }
	  
	  function displayResult(resp) {
		  if(resp.hasMessages) {
			  var message = "";
			  for(k in resp.messages)
				  message += resp.messages[k].contextualMessage + "<br>"
			  $("testMessages").style.visibility = "visible";
			  $("testMessages").innerHTML = message;
		  }
		  else {
			  $("testMessages").innerHTML = "";
			  $("testMessages").style.visibility = "collapse";
		  }
	  }
</script>

<tag:dataSourceAttrs descriptionKey="dsEdit.serial.desc" helpId="serialDS">
<tag:serialSettings/>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.readTimeout"/></td>
 <td><input id="readTimeout" type="number" value="${dataSource.readTimeout}"></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.useTerminator"/></td>
 <td><input id="useTerminator" type="checkbox" onchange="toggleTerminator()"></input></td>
</tr>
<tr id="terminatorRow">
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.messageTerminator"/></td>
 <td><input id="messageTerminator" type="text"></input></td>
</tr>
<tr id="messageRegexRow">
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.messageRegex"/></td>
 <td><input id="messageRegex" type="text" value="${dataSource.messageRegex}"></input></td>
</tr>
<tr id="identifierIndexRow">
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.pointIdentifierIndex"/></td>
 <td><input id="pointIdentifierIndex" type="number" value="${dataSource.pointIdentifierIndex}"></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><button onclick=submitTestString();><fmt:message key="dsEdit.serial.submitTestString"/></button></td>
 <td><input id="testString" type="text"></input></td>
</tr>
<tr><td id="testMessages" style="color:red;" colspan=2></td>
</tr>
</tag:dataSourceAttrs>

<tag:pointList pointHelpId="serialPP">
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